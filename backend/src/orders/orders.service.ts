import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { BrandType, OrderKind, OrderStatus, Prisma, UserRole } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { NotificationsService } from '../notifications/notifications.service';
import { FinanceService } from '../finance/finance.service';
import { ReturnsService } from '../returns/returns.service';
import { InvoicesService } from '../invoices/invoices.service';
import { PaymentsService } from '../payments/payments.service';
import { PrismaService } from '../prisma/prisma.service';
import { UsersService } from '../users/users.service';
import {
  CreateOrderDto,
  CreateOrderWithPaymentDto,
  OrderPaymentMode,
} from './dto/create-order.dto';
import {
  OrderReturnRejectDto,
  OrderReturnRequestDto,
} from './dto/order-return.dto';
import { ReorderPreviewDto } from './dto/order-reorder.dto';

const orderInclude = {
  items: { include: { product: true } },
  shopkeeper: {
    select: {
      id: true,
      name: true,
      email: true,
      phone: true,
      shopName: true,
      address: true,
      latitude: true,
      longitude: true,
    },
  },
  dealer: {
    select: {
      id: true,
      name: true,
      email: true,
      phone: true,
      shopName: true,
      address: true,
      latitude: true,
      longitude: true,
    },
  },
  invoice: true,
} as const;

const LOW_STOCK_THRESHOLD = 10;
const REORDER_UNAVAILABLE_MSG =
  'This product is currently unavailable and has been removed from the reorder cart.';

@Injectable()
export class OrdersService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly invoicesService: InvoicesService,
    private readonly notifications: NotificationsService,
    private readonly paymentsService: PaymentsService,
    private readonly usersService: UsersService,
    private readonly config: ConfigService,
    private readonly financeService: FinanceService,
    private readonly returnsService: ReturnsService,
  ) {}

  private maxOrderQuantity(): number {
    return this.config.get<number>('ordering.maxOrderQuantity', 10000);
  }

  private assertValidLineQuantities(items: { quantity: number }[]) {
    const max = this.maxOrderQuantity();
    for (const item of items) {
      if (!Number.isInteger(item.quantity) || item.quantity < 1) {
        throw new BadRequestException(
          'Quantity must be a whole number of at least 1',
        );
      }
      if (item.quantity > max) {
        throw new BadRequestException(
          `Quantity cannot exceed ${max} per line item`,
        );
      }
    }
  }

  /** Notify a dealer when any of the given products fell to/below the low-stock threshold. */
  private async notifyLowStock(
    dealerId: string,
    productIds: string[],
  ): Promise<void> {
    if (productIds.length === 0) {
      return;
    }
    const lowStocks = await this.prisma.stock.findMany({
      where: {
        dealerId,
        productId: { in: productIds },
        quantity: { lte: LOW_STOCK_THRESHOLD },
      },
      include: { product: { select: { name: true } } },
    });
    for (const stock of lowStocks) {
      void this.notifications.sendToUser(
        dealerId,
        'Low stock',
        `${stock.product.name} is running low (${stock.quantity} left)`,
        { type: 'STOCK', productId: stock.productId },
      );
    }
  }

  private scopeWhere(actor: AuthUser): Prisma.OrderWhereInput {
    const where: Prisma.OrderWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    if (actor.role === UserRole.SHOPKEEPER) {
      where.shopkeeperId = actor.userId;
    } else if (actor.role === UserRole.DEALER) {
      where.OR = [
        { dealerId: actor.userId, kind: OrderKind.SHOPKEEPER },
        { shopkeeperId: actor.userId, kind: OrderKind.DEALER_RESTOCK },
      ];
    }
    return where;
  }

  /**
   * Role-aware redaction of counterparty info on an order.
   * - DEALER: hides the shopkeeper's personal name/email/phone; exposes only
   *   shop name + delivery address/location (safe to share with a delivery partner).
   * - SHOPKEEPER: shows the dealer's name only (no email/phone).
   * - ADMIN/EMPLOYEE: full details.
   */
  private present<T extends Record<string, any>>(
    order: T,
    actor: AuthUser,
  ): T {
    const result: any = { ...order };
    if (actor.role === UserRole.DEALER && result.shopkeeper) {
      const sk = result.shopkeeper;
      result.shopkeeper = {
        id: sk.id,
        shopName: sk.shopName ?? null,
        address: sk.address ?? null,
        latitude: sk.latitude ?? null,
        longitude: sk.longitude ?? null,
      };
    } else if (actor.role === UserRole.SHOPKEEPER && result.dealer) {
      result.dealer = {
        id: result.dealer.id,
        name: result.dealer.name ?? null,
      };
    }
    return result;
  }

  private presentMany<T extends Record<string, any>>(
    orders: T[],
    actor: AuthUser,
  ): T[] {
    return orders.map((order) => this.present(order, actor) as T);
  }

  private async getCompanyWarehouseUser(companyId: string) {
    const admin = await this.prisma.user.findFirst({
      where: { companyId, role: UserRole.ADMIN },
      orderBy: { createdAt: 'asc' },
    });
    if (!admin) {
      throw new BadRequestException(
        'Company warehouse is not configured. Contact admin.',
      );
    }
    return admin;
  }

  private buildLineItems(
    dto: CreateOrderDto,
    productsById: Map<string, { id: string; name: string; dealerPrice: Prisma.Decimal | null; gstRate: Prisma.Decimal; dealerDiscount: Prisma.Decimal; shopkeeperDiscount: Prisma.Decimal; brandType: BrandType; bulkShippingMinQty: number; bulkShippingFee: Prisma.Decimal | null }>,
    stockByProduct: Map<string, number>,
    buyerRole: 'SHOPKEEPER' | 'DEALER',
  ) {
    let totalAmount = new Prisma.Decimal(0);
    let gstAmount = new Prisma.Decimal(0);
    let discountAmount = new Prisma.Decimal(0);

    const lineItems = dto.items.map((item) => {
      const product = productsById.get(item.productId);
      if (!product) {
        throw new NotFoundException(`Product ${item.productId} not found`);
      }

      const availableQty = stockByProduct.get(item.productId) ?? 0;
      if (buyerRole === 'SHOPKEEPER' && availableQty < item.quantity) {
        throw new BadRequestException(
          `Insufficient stock for ${product.name}. Requested ${item.quantity}, available ${availableQty}`,
        );
      }

      const quantity = new Prisma.Decimal(item.quantity);
      if (!product.dealerPrice || !product.gstRate) {
        throw new BadRequestException(
          `Product ${product.name} is missing dealer price/GST configuration.`,
        );
      }
      const dbDealerUnitPrice = new Prisma.Decimal(product.dealerPrice);
      const gstPct = new Prisma.Decimal(product.gstRate).div(100);
      const baseLineAmount = dbDealerUnitPrice.mul(quantity);
      const defaultDisc =
        buyerRole === 'DEALER'
          ? new Prisma.Decimal(10)
          : new Prisma.Decimal(5);
      const lineDiscountPct =
        buyerRole === 'DEALER'
          ? (product.dealerDiscount ?? defaultDisc).div(100)
          : (product.shopkeeperDiscount ?? defaultDisc).div(100);
      const lineDiscount = baseLineAmount.mul(lineDiscountPct);
      const taxableAmount = baseLineAmount.sub(lineDiscount);
      const lineGst = taxableAmount.mul(gstPct);
      const finalLineAmount = taxableAmount.add(lineGst);

      totalAmount = totalAmount.add(baseLineAmount);
      gstAmount = gstAmount.add(lineGst);
      discountAmount = discountAmount.add(lineDiscount);

      const unitPriceAfterDiscount = taxableAmount.div(quantity);

      return {
        productId: product.id,
        quantity: item.quantity,
        price: unitPriceAfterDiscount,
        gstAmount: lineGst,
        discountAmount: lineDiscount,
        finalAmount: finalLineAmount,
      };
    });

    let bulkShippingAmount = new Prisma.Decimal(0);
    for (const item of dto.items) {
      const product = productsById.get(item.productId);
      if (!product) continue;
      const minQty = product.bulkShippingMinQty ?? 10;
      const fee = product.bulkShippingFee;
      if (fee != null && item.quantity >= minQty) {
        bulkShippingAmount = bulkShippingAmount.add(new Prisma.Decimal(fee));
      }
    }

    const finalAmount = totalAmount.sub(discountAmount).add(gstAmount).add(bulkShippingAmount);
    return { lineItems, totalAmount, gstAmount, discountAmount, finalAmount };
  }

  async findAll(actor: AuthUser) {
    const orders = await this.prisma.order.findMany({
      where: this.scopeWhere(actor),
      include: orderInclude,
      orderBy: { createdAt: 'desc' },
    });
    return this.presentMany(orders, actor);
  }

  async findMine(actor: AuthUser) {
    if (actor.role !== UserRole.SHOPKEEPER) {
      throw new BadRequestException('Only shopkeepers can use this endpoint');
    }
    const orders = await this.prisma.order.findMany({
      where: { ...this.scopeWhere(actor), shopkeeperId: actor.userId },
      include: orderInclude,
      orderBy: { createdAt: 'desc' },
    });
    return this.presentMany(orders, actor);
  }

  async findDealer(actor: AuthUser) {
    if (actor.role !== UserRole.DEALER && actor.role !== UserRole.ADMIN && actor.role !== UserRole.EMPLOYEE) {
      throw new BadRequestException('Only dealers or staff can use this endpoint');
    }
    const where = this.scopeWhere(actor);
    const orders = await this.prisma.order.findMany({
      where,
      include: orderInclude,
      orderBy: { createdAt: 'desc' },
    });
    return this.presentMany(orders, actor);
  }

  async mySummary(actor: AuthUser) {
    if (actor.role !== UserRole.SHOPKEEPER) {
      throw new BadRequestException('Only shopkeepers can use this endpoint');
    }
    const rows = await this.prisma.order.findMany({
      where: { ...this.scopeWhere(actor), shopkeeperId: actor.userId },
      select: { status: true, finalAmount: true },
      orderBy: { createdAt: 'desc' },
    });
    const openOrders = rows.filter(
      (r) =>
        r.status === OrderStatus.PENDING ||
        r.status === OrderStatus.DEALER_CONFIRMED ||
        r.status === OrderStatus.OUT_FOR_DELIVERY,
    ).length;
    const inDelivery = rows.filter((r) => r.status === OrderStatus.OUT_FOR_DELIVERY).length;
    const lastCompleted = rows.find((r) => r.status === OrderStatus.DELIVERED);
    const invoicesReady = rows.filter((r) => r.status === OrderStatus.DELIVERED).length;
    return {
      openOrders,
      inDelivery,
      lastTotal: lastCompleted ? Number(lastCompleted.finalAmount) : null,
      invoicesReady,
    };
  }

  async dealerSummary(actor: AuthUser) {
    if (
      actor.role !== UserRole.DEALER &&
      actor.role !== UserRole.ADMIN &&
      actor.role !== UserRole.EMPLOYEE
    ) {
      throw new BadRequestException('Only dealers or staff can use this endpoint');
    }
    const rows = await this.prisma.order.findMany({
      where:
        actor.role === UserRole.DEALER
          ? {
              companyId: actor.companyId ?? undefined,
              kind: OrderKind.SHOPKEEPER,
              dealerId: actor.userId,
            }
          : this.scopeWhere(actor),
      select: { status: true, finalAmount: true, createdAt: true },
      orderBy: { createdAt: 'desc' },
    });
    const pendingOrders = rows.filter((r) => r.status === OrderStatus.PENDING).length;
    const todaysDeliveries = rows.filter(
      (r) =>
        r.status === OrderStatus.DELIVERED &&
        r.createdAt.toISOString().slice(0, 10) === new Date().toISOString().slice(0, 10),
    ).length;
    const weekStart = Date.now() - 7 * 24 * 3600 * 1000;
    const revenue = rows
      .filter((r) => r.status === OrderStatus.DELIVERED && r.createdAt.getTime() >= weekStart)
      .reduce((sum, r) => sum + Number(r.finalAmount), 0);
    return {
      pendingOrders,
      todaysDeliveries,
      weeklyRevenue: Number(revenue.toFixed(2)),
    };
  }

  async findOne(orderId: string, actor: AuthUser) {
    const order = await this.prisma.order.findFirst({
      where: { id: orderId, ...this.scopeWhere(actor) },
      include: orderInclude,
    });
    if (!order) {
      throw new NotFoundException('Order not found');
    }
    return this.present(order, actor);
  }

  async previewReorder(orderId: string, actor: AuthUser): Promise<ReorderPreviewDto> {
    if (actor.role !== UserRole.SHOPKEEPER) {
      throw new ForbiddenException('Only shopkeepers can reorder');
    }

    const order = await this.prisma.order.findFirst({
      where: {
        id: orderId,
        shopkeeperId: actor.userId,
        kind: OrderKind.SHOPKEEPER,
        ...(actor.companyId ? { companyId: actor.companyId } : {}),
      },
      include: { items: true },
    });
    if (!order) {
      throw new NotFoundException('Order not found');
    }

    const shopkeeper = await this.prisma.user.findUnique({
      where: { id: actor.userId },
      include: { area: true },
    });
    const dealerId = shopkeeper?.area?.dealerId;

    const productIds = [...new Set(order.items.map((line) => line.productId))];
    const products = await this.prisma.product.findMany({
      where: { id: { in: productIds } },
      include: { brand: true },
    });
    const productsById = new Map(products.map((product) => [product.id, product]));

    const stockByProduct = new Map<string, number>();
    if (dealerId) {
      const stockRows = await this.prisma.stock.findMany({
        where: { dealerId, productId: { in: productIds } },
      });
      for (const stock of stockRows) {
        stockByProduct.set(stock.productId, stock.quantity);
      }
    }

    const items: ReorderPreviewDto['items'] = [];
    const skipped: ReorderPreviewDto['skipped'] = [];
    const warnings: string[] = [];

    for (const line of order.items) {
      const product = productsById.get(line.productId);
      if (!product) {
        skipped.push({ productId: line.productId, reason: 'deleted' });
        warnings.push(REORDER_UNAVAILABLE_MSG);
        continue;
      }
      if (!product.isActive) {
        skipped.push({
          productId: line.productId,
          productName: product.name,
          reason: 'inactive',
        });
        warnings.push(REORDER_UNAVAILABLE_MSG);
        continue;
      }
      const available = stockByProduct.get(line.productId) ?? 0;
      if (available < line.quantity) {
        skipped.push({
          productId: line.productId,
          productName: product.name,
          reason: 'out_of_stock',
        });
        warnings.push(REORDER_UNAVAILABLE_MSG);
        continue;
      }
      items.push({
        productId: line.productId,
        quantity: line.quantity,
        product: product as unknown as Record<string, unknown>,
      });
    }

    return {
      items,
      warnings: [...new Set(warnings)],
      skipped,
    };
  }

  async createOrder(dto: CreateOrderDto, actor: AuthUser) {
    if (actor.role !== UserRole.SHOPKEEPER) {
      throw new BadRequestException('Only shopkeepers can create orders');
    }

    await this.usersService.assertCanPlaceOrders(actor.userId);
    this.assertValidLineQuantities(dto.items);

    const shopkeeper = await this.prisma.user.findUnique({
      where: { id: actor.userId },
      include: { area: true },
    });
    if (!shopkeeper?.area?.dealerId) {
      throw new BadRequestException(
        'No dealer assigned to this shopkeeper. Contact admin.',
      );
    }

    const dealerId = shopkeeper.area.dealerId;
    const productIds = dto.items.map((item) => item.productId);
    const products = await this.prisma.product.findMany({
      where: { id: { in: productIds } },
    });
    const productsById = new Map(products.map((product) => [product.id, product]));

    if (products.length !== productIds.length) {
      throw new NotFoundException('One or more products were not found');
    }

    const stockRows = await this.prisma.stock.findMany({
      where: { dealerId, productId: { in: productIds } },
    });
    const stockByProduct = new Map(
      stockRows.map((stock) => [stock.productId, stock.quantity]),
    );

    const { lineItems, totalAmount, gstAmount, discountAmount, finalAmount } =
      this.buildLineItems(
        dto,
        productsById,
        stockByProduct,
        'SHOPKEEPER',
      );

    const created = await this.prisma.order.create({
      data: {
        companyId: actor.companyId,
        shopkeeperId: actor.userId,
        dealerId,
        kind: OrderKind.SHOPKEEPER,
        totalAmount,
        gstAmount,
        discountAmount,
        finalAmount,
        status: OrderStatus.PENDING,
        items: {
          create: lineItems,
        },
      },
      include: orderInclude,
    });

    void this.notifications.sendToUser(
      dealerId,
      'New order',
      `New order from ${shopkeeper.name} — ${created.id.slice(0, 8)}`,
      { type: 'ORDER', orderId: created.id },
    );

    return this.present(created, actor);
  }

  async createDealerRestockOrder(dto: CreateOrderDto, actor: AuthUser) {
    if (actor.role !== UserRole.DEALER) {
      throw new BadRequestException('Only dealers can place restock orders');
    }
    await this.usersService.assertCanPlaceOrders(actor.userId);
    this.assertValidLineQuantities(dto.items);
    if (!actor.companyId) {
      throw new BadRequestException('Company scope is required');
    }

    const dealer = await this.prisma.user.findUnique({ where: { id: actor.userId } });
    if (!dealer) {
      throw new NotFoundException('Dealer not found');
    }

    const warehouse = await this.getCompanyWarehouseUser(actor.companyId);
    const productIds = dto.items.map((item) => item.productId);
    const products = await this.prisma.product.findMany({
      where: { id: { in: productIds }, companyId: actor.companyId },
    });
    const productsById = new Map(products.map((product) => [product.id, product]));

    if (products.length !== productIds.length) {
      throw new NotFoundException('One or more products were not found');
    }

    const stockRows = await this.prisma.stock.findMany({
      where: { dealerId: warehouse.id, productId: { in: productIds } },
    });
    const stockByProduct = new Map(
      stockRows.map((stock) => [stock.productId, stock.quantity]),
    );

    const { lineItems, totalAmount, gstAmount, discountAmount, finalAmount } =
      this.buildLineItems(
        dto,
        productsById,
        stockByProduct,
        'DEALER',
      );

    const created = await this.prisma.$transaction(async (tx) => {
      const order = await tx.order.create({
        data: {
          companyId: actor.companyId,
          shopkeeperId: actor.userId,
          dealerId: warehouse.id,
          kind: OrderKind.DEALER_RESTOCK,
          totalAmount,
          gstAmount,
          discountAmount,
          finalAmount,
          status: OrderStatus.PENDING,
          items: {
            create: lineItems,
          },
        },
        include: orderInclude,
      });

      for (const item of dto.items) {
        await tx.stock.upsert({
          where: {
            dealerId_productId: {
              dealerId: actor.userId,
              productId: item.productId,
            },
          },
          update: {
            quantity: { increment: item.quantity },
          },
          create: {
            companyId: actor.companyId,
            dealerId: actor.userId,
            productId: item.productId,
            quantity: item.quantity,
          },
        });
      }

      return order;
    });

    const admins = await this.prisma.user.findMany({
      where: {
        companyId: actor.companyId,
        role: { in: [UserRole.ADMIN, UserRole.EMPLOYEE] },
      },
      select: { id: true },
    });
    for (const staff of admins) {
      void this.notifications.sendToUser(
        staff.id,
        'Dealer restock order',
        `${dealer.name} placed restock order ${created.id.slice(0, 8)}`,
        { type: 'ORDER', orderId: created.id },
      );
    }

    return this.present(created, actor);
  }

  async createDealerRestockWithPayment(
    dto: CreateOrderWithPaymentDto,
    actor: AuthUser,
  ) {
    const paymentMode = dto.paymentMode ?? OrderPaymentMode.COD;
    if (paymentMode === OrderPaymentMode.RAZORPAY) {
      this.paymentsService.assertRazorpayKeysPresent();
    }

    const created = await this.createDealerRestockOrder(dto, actor);
    if (paymentMode === OrderPaymentMode.RAZORPAY) {
      const rp = await this.paymentsService.createRazorpayOrder(
        { orderId: created.id, currency: 'INR' },
        actor,
      );
      return {
        orderId: created.id,
        razorpayOrderId: rp.razorpayOrderId,
        amount: rp.amountPaise,
        currency: rp.currency,
        keyId: rp.keyId,
      };
    }
    return {
      orderId: created.id,
      status: created.status,
      message: 'Restock order placed. Company will confirm shortly.',
    };
  }

  async createOrderWithPayment(
    dto: CreateOrderWithPaymentDto,
    actor: AuthUser,
  ): Promise<
    | {
        orderId: string;
        razorpayOrderId: string;
        amount: number;
        currency: string;
        keyId: string;
      }
    | {
        orderId: string;
        status: OrderStatus;
        message: string;
      }
  > {
    const paymentMode = dto.paymentMode ?? OrderPaymentMode.COD;
    if (paymentMode === OrderPaymentMode.RAZORPAY) {
      this.paymentsService.assertRazorpayKeysPresent();
    }

    const created = await this.createOrder(dto, actor);
    if (paymentMode === OrderPaymentMode.RAZORPAY) {
      const rp = await this.paymentsService.createRazorpayOrder(
        { orderId: created.id, currency: 'INR' },
        actor,
      );
      return {
        orderId: created.id,
        razorpayOrderId: rp.razorpayOrderId,
        amount: rp.amountPaise,
        currency: rp.currency,
        keyId: rp.keyId,
      };
    }
    return {
      orderId: created.id,
      status: created.status,
      message: 'Order placed successfully. Dealer will confirm shortly.',
    };
  }

  async confirmOrder(orderId: string, actor: AuthUser) {
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: { items: true },
    });
    if (!order) {
      throw new NotFoundException('Order not found');
    }
    if (order.status !== OrderStatus.PENDING) {
      throw new BadRequestException('Only pending orders can be confirmed');
    }

    if (order.kind === OrderKind.DEALER_RESTOCK) {
      if (actor.role !== UserRole.ADMIN && actor.role !== UserRole.EMPLOYEE) {
        throw new ForbiddenException('Only company staff can confirm restock orders');
      }
    } else if (actor.role === UserRole.DEALER && actor.userId !== order.dealerId) {
      throw new ForbiddenException('Dealer can only confirm own assigned orders');
    }

    await this.prisma.$transaction(async (tx) => {
      for (const item of order.items) {
        const updateResult = await tx.stock.updateMany({
          where: {
            dealerId: order.dealerId,
            productId: item.productId,
            quantity: { gte: item.quantity },
          },
          data: {
            quantity: { decrement: item.quantity },
          },
        });
        if (updateResult.count === 0) {
          throw new ConflictException('Stock changed, please retry confirmation');
        }
      }

      await tx.order.update({
        where: { id: order.id },
        data: { status: OrderStatus.DEALER_CONFIRMED },
      });
    });

    await this.invoicesService.generateForOrder(order.id, order.companyId);

    const updated = await this.prisma.order.findUnique({
      where: { id: order.id },
      include: orderInclude,
    });

    void this.notifications.sendToUser(
      order.shopkeeperId,
      order.kind === OrderKind.DEALER_RESTOCK ? 'Restock confirmed' : 'Order confirmed',
      order.kind === OrderKind.DEALER_RESTOCK
        ? `Your restock order ${order.id.slice(0, 8)} was confirmed`
        : `Dealer confirmed your order ${order.id.slice(0, 8)}`,
      { type: 'ORDER', orderId: order.id },
    );

    void this.notifyLowStock(
      order.dealerId,
      order.items.map((item) => item.productId),
    );

    return updated ? this.present(updated, actor) : updated;
  }

  async markOutForDelivery(orderId: string, actor: AuthUser) {
    const order = await this.prisma.order.findUnique({ where: { id: orderId } });
    if (!order) {
      throw new NotFoundException('Order not found');
    }
    if (order.kind === OrderKind.DEALER_RESTOCK) {
      if (actor.role !== UserRole.ADMIN && actor.role !== UserRole.EMPLOYEE) {
        throw new ForbiddenException('Only company staff can dispatch restock orders');
      }
    } else if (actor.role === UserRole.DEALER && actor.userId !== order.dealerId) {
      throw new ForbiddenException('Dealer can only update own assigned orders');
    }
    if (order.status !== OrderStatus.DEALER_CONFIRMED) {
      throw new BadRequestException('Order must be confirmed before dispatch');
    }
    const out = await this.prisma.order.update({
      where: { id: order.id },
      data: { status: OrderStatus.OUT_FOR_DELIVERY },
      include: orderInclude,
    });
    void this.notifications.sendToUser(
      order.shopkeeperId,
      'Out for delivery',
      `Order ${order.id.slice(0, 8)} is on the way`,
      { type: 'ORDER', orderId: order.id },
    );
    return this.present(out, actor);
  }

  async markDelivered(orderId: string, actor: AuthUser) {
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: { items: true },
    });
    if (!order) {
      throw new NotFoundException('Order not found');
    }
    if (order.kind === OrderKind.DEALER_RESTOCK) {
      if (actor.role !== UserRole.ADMIN && actor.role !== UserRole.EMPLOYEE) {
        throw new ForbiddenException('Only company staff can mark restock orders delivered');
      }
    } else if (actor.role === UserRole.DEALER && actor.userId !== order.dealerId) {
      throw new ForbiddenException('Dealer can only update own assigned orders');
    }
    if (order.status !== OrderStatus.OUT_FOR_DELIVERY) {
      throw new BadRequestException('Only out-for-delivery orders can be marked delivered');
    }

    const done = await this.prisma.$transaction(async (tx) => {
      return tx.order.update({
        where: { id: order.id },
        data: { status: OrderStatus.DELIVERED },
        include: orderInclude,
      });
    });
    void this.notifications.sendToUser(
      order.shopkeeperId,
      'Delivered',
      `Order ${order.id.slice(0, 8)} has been delivered`,
      { type: 'ORDER', orderId: order.id },
    );
    void this.financeService.recordRevenueOnDelivery(order.id);
    return this.present(done, actor);
  }

  async cancelOrder(orderId: string, actor: AuthUser) {
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: { items: true },
    });
    if (!order) {
      throw new NotFoundException('Order not found');
    }
    if (order.status === OrderStatus.DELIVERED || order.status === OrderStatus.CANCELLED) {
      throw new BadRequestException('Order cannot be cancelled in this state');
    }
    if (actor.role === UserRole.SHOPKEEPER && order.shopkeeperId !== actor.userId) {
      throw new ForbiddenException();
    }
    if (actor.role === UserRole.DEALER) {
      const isShopkeeperOrder = order.dealerId === actor.userId;
      const isOwnRestock =
        order.kind === OrderKind.DEALER_RESTOCK && order.shopkeeperId === actor.userId;
      if (!isShopkeeperOrder && !isOwnRestock) {
        throw new ForbiddenException();
      }
    }
    if (actor.role === UserRole.SHOPKEEPER && order.status !== OrderStatus.PENDING) {
      throw new BadRequestException('Shopkeeper can cancel only while order is pending');
    }
    if (
      actor.role === UserRole.DEALER &&
      order.kind === OrderKind.DEALER_RESTOCK &&
      order.shopkeeperId === actor.userId &&
      order.status !== OrderStatus.PENDING
    ) {
      throw new BadRequestException('Dealer can cancel restock only while pending');
    }

    const updated = await this.prisma.$transaction(async (tx) => {
      if (
        order.kind === OrderKind.DEALER_RESTOCK &&
        order.status === OrderStatus.PENDING
      ) {
        for (const item of order.items) {
          await tx.stock.updateMany({
            where: {
              dealerId: order.shopkeeperId,
              productId: item.productId,
              quantity: { gte: item.quantity },
            },
            data: {
              quantity: { decrement: item.quantity },
            },
          });
        }
      }

      return tx.order.update({
        where: { id: order.id },
        data: { status: OrderStatus.CANCELLED },
        include: orderInclude,
      });
    });

    const peer =
      actor.userId === order.shopkeeperId ? order.dealerId : order.shopkeeperId;
    void this.notifications.sendToUser(
      peer,
      'Order cancelled',
      `Order ${order.id.slice(0, 8)} was cancelled`,
      { type: 'ORDER', orderId: order.id },
    );

    return this.present(updated, actor);
  }

  async requestReturn(orderId: string, dto: OrderReturnRequestDto, actor: AuthUser) {
    await this.returnsService.createLegacyReturn(orderId, dto.reason.trim(), actor);
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: orderInclude,
    });
    if (!order) throw new NotFoundException('Order not found');
    return this.present(order, actor);
  }

  async approveReturn(orderId: string, actor: AuthUser) {
    await this.returnsService.approveLegacyReturn(orderId, actor);
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: orderInclude,
    });
    if (!order) throw new NotFoundException('Order not found');
    return this.present(order, actor);
  }

  async rejectReturn(orderId: string, dto: OrderReturnRejectDto, actor: AuthUser) {
    await this.returnsService.rejectLegacyReturn(orderId, dto.note, actor);
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: orderInclude,
    });
    if (!order) throw new NotFoundException('Order not found');
    return this.present(order, actor);
  }

  mockPaymentSuccess(orderId: string) {
    return {
      orderId,
      paymentGateway: 'MOCK',
      status: 'SUCCESS',
      message: 'Payment gateway integration deferred for demo timeline',
    };
  }
}
