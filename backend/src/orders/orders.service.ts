import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { BrandType, OrderStatus, Prisma, UserRole } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { InvoicesService } from '../invoices/invoices.service';
import { PrismaService } from '../prisma/prisma.service';
import { CreateOrderDto } from './dto/create-order.dto';

@Injectable()
export class OrdersService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly invoicesService: InvoicesService,
  ) {}

  findAll(actor: AuthUser) {
    const where: Prisma.OrderWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    if (actor.role === UserRole.SHOPKEEPER) {
      where.shopkeeperId = actor.userId;
    } else if (actor.role === UserRole.DEALER) {
      where.dealerId = actor.userId;
    }

    return this.prisma.order.findMany({
      where,
      include: {
        items: { include: { product: true } },
        shopkeeper: { select: { id: true, name: true, email: true } },
        dealer: { select: { id: true, name: true, email: true } },
      },
      orderBy: { createdAt: 'desc' },
    });
  }

  async createOrder(dto: CreateOrderDto, actor: AuthUser) {
    if (actor.role !== UserRole.SHOPKEEPER) {
      throw new BadRequestException('Only shopkeepers can create orders');
    }

    const shopkeeper = await this.prisma.user.findUnique({
      where: { id: actor.userId },
      include: { area: true },
    });
    if (!shopkeeper || !shopkeeper.area) {
      throw new BadRequestException(
        'Shopkeeper must be assigned to an area before placing orders',
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

    let totalAmount = new Prisma.Decimal(0);
    let gstAmount = new Prisma.Decimal(0);
    let discountAmount = new Prisma.Decimal(0);

    const lineItems = dto.items.map((item) => {
      const product = productsById.get(item.productId);
      if (!product) {
        throw new NotFoundException(`Product ${item.productId} not found`);
      }

      const availableQty = stockByProduct.get(item.productId) ?? 0;
      if (availableQty < item.quantity) {
        throw new BadRequestException(
          `Insufficient stock for ${product.name}. Requested ${item.quantity}, available ${availableQty}`,
        );
      }

      const quantity = new Prisma.Decimal(item.quantity);
      const baseLineAmount = product.basePrice.mul(quantity);
      const lineDiscountPct =
        product.brandType === BrandType.OWN
          ? product.shopkeeperDiscount.div(100)
          : new Prisma.Decimal(0);
      const lineDiscount = baseLineAmount.mul(lineDiscountPct);
      const taxableAmount = baseLineAmount.sub(lineDiscount);
      const lineGst = taxableAmount.mul(product.gstPercentage.div(100));
      const finalLineAmount = taxableAmount.add(lineGst);

      totalAmount = totalAmount.add(baseLineAmount);
      gstAmount = gstAmount.add(lineGst);
      discountAmount = discountAmount.add(lineDiscount);

      // `price` = unit amount after role discount, before GST (line totals in gst/discount/final)
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

    const finalAmount = totalAmount.sub(discountAmount).add(gstAmount);

    return this.prisma.order.create({
      data: {
        companyId: actor.companyId,
        shopkeeperId: actor.userId,
        dealerId,
        totalAmount,
        gstAmount,
        discountAmount,
        finalAmount,
        status: OrderStatus.PENDING,
        items: {
          create: lineItems,
        },
      },
      include: { items: true },
    });
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
    if (actor.role === UserRole.DEALER && actor.userId !== order.dealerId) {
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
        data: { status: OrderStatus.ACCEPTED },
      });
    });

    await this.invoicesService.generateForOrder(order.id, order.companyId);

    return this.prisma.order.findUnique({
      where: { id: order.id },
      include: {
        items: { include: { product: true } },
        invoice: true,
        shopkeeper: { select: { id: true, name: true } },
        dealer: { select: { id: true, name: true } },
      },
    });
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
