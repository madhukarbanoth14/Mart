import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import {
  FinanceAuditAction,
  OrderKind,
  OrderPaymentStatus,
  OrderStatus,
  Prisma,
  RefundMethod,
  RefundRequestStatus,
  ReturnRequestStatus,
  UserRole,
} from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { FinanceAuditService } from '../finance/finance-audit.service';
import { NotificationsService } from '../notifications/notifications.service';
import { PaymentsService } from '../payments/payments.service';
import { PrismaService } from '../prisma/prisma.service';
import {
  CreateReturnRequestDto,
  ProcessRefundDto,
  RaiseRefundRequestDto,
  RefundRejectDto,
  ReturnActionDto,
  ReturnsQueryDto,
  RefundsQueryDto,
} from './dto/returns.dto';

const returnInclude = {
  items: { include: { product: { select: { id: true, name: true, sku: true } } } },
  order: {
    select: {
      id: true,
      status: true,
      finalAmount: true,
      paymentStatus: true,
      createdAt: true,
    },
  },
  shopkeeper: {
    select: {
      id: true,
      name: true,
      shopName: true,
      area: { select: { id: true, name: true } },
    },
  },
  dealer: { select: { id: true, name: true, shopName: true } },
  refundRequest: true,
} satisfies Prisma.ReturnRequestInclude;

const refundInclude = {
  returnRequest: {
    include: {
      items: true,
      order: { select: { id: true, finalAmount: true } },
      shopkeeper: {
        select: {
          id: true,
          name: true,
          shopName: true,
          area: { select: { name: true } },
        },
      },
    },
  },
  dealer: { select: { id: true, name: true, shopName: true } },
  processedBy: { select: { id: true, name: true } },
} satisfies Prisma.RefundRequestInclude;

@Injectable()
export class ReturnsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly notifications: NotificationsService,
    private readonly audit: FinanceAuditService,
    private readonly payments: PaymentsService,
  ) {}

  private code(prefix: string) {
    return `${prefix}-${Date.now().toString(36).toUpperCase()}`;
  }

  private decimal(n: number) {
    return new Prisma.Decimal(n.toFixed(2));
  }

  private presentReturn(row: Prisma.ReturnRequestGetPayload<{ include: typeof returnInclude }>) {
    return {
      ...row,
      refundAmount: Number(row.refundAmount),
      items: row.items.map((i) => ({
        ...i,
        unitAmount: Number(i.unitAmount),
        lineAmount: Number(i.lineAmount),
      })),
      order: row.order
        ? { ...row.order, finalAmount: Number(row.order.finalAmount) }
        : row.order,
    };
  }

  private presentRefund(row: Prisma.RefundRequestGetPayload<{ include: typeof refundInclude }>) {
    return {
      ...row,
      amount: Number(row.amount),
      returnRequest: row.returnRequest
        ? {
            ...row.returnRequest,
            refundAmount: Number(row.returnRequest.refundAmount),
            items: row.returnRequest.items.map((i) => ({
              ...i,
              unitAmount: Number(i.unitAmount),
              lineAmount: Number(i.lineAmount),
            })),
          }
        : row.returnRequest,
    };
  }

  private async adminUserIds(companyId: string | null) {
    const admins = await this.prisma.user.findMany({
      where: {
        ...(companyId ? { companyId } : {}),
        role: { in: [UserRole.ADMIN, UserRole.EMPLOYEE] },
      },
      select: { id: true },
    });
    return admins.map((a) => a.id);
  }

  async createReturn(orderId: string, dto: CreateReturnRequestDto, actor: AuthUser) {
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: { items: true },
    });
    if (!order) throw new NotFoundException('Order not found');
    if (actor.role !== UserRole.SHOPKEEPER || order.shopkeeperId !== actor.userId) {
      throw new ForbiddenException('Only the shopkeeper can request a return');
    }
    if (order.kind !== OrderKind.SHOPKEEPER) {
      throw new BadRequestException('Returns apply to shopkeeper orders only');
    }
    if (order.status !== OrderStatus.DELIVERED) {
      throw new BadRequestException('Return can be requested only for delivered orders');
    }

    const existing = await this.prisma.returnRequest.findFirst({
      where: {
        orderId,
        status: {
          in: [
            ReturnRequestStatus.REQUESTED,
            ReturnRequestStatus.UNDER_REVIEW,
            ReturnRequestStatus.APPROVED,
          ],
        },
      },
    });
    if (existing) {
      throw new BadRequestException('An active return request already exists for this order');
    }

    const orderItemsByProduct = new Map(order.items.map((i) => [i.productId, i]));
    let refundAmount = 0;
    const lineData: Prisma.ReturnRequestItemCreateManyInput[] = [];

    for (const item of dto.items) {
      const orderItem = orderItemsByProduct.get(item.productId);
      if (!orderItem) {
        throw new BadRequestException(`Product ${item.productId} is not in this order`);
      }
      if (item.quantity > orderItem.quantity) {
        throw new BadRequestException('Return quantity exceeds ordered quantity');
      }
      const unit = Number(orderItem.finalAmount) / orderItem.quantity;
      const line = unit * item.quantity;
      refundAmount += line;
      lineData.push({
        returnRequestId: '',
        orderItemId: orderItem.id,
        productId: orderItem.productId,
        productName: orderItem.productId,
        quantity: item.quantity,
        unitAmount: this.decimal(unit),
        lineAmount: this.decimal(line),
      });
    }

    const products = await this.prisma.product.findMany({
      where: { id: { in: dto.items.map((i) => i.productId) } },
      select: { id: true, name: true },
    });
    const names = new Map(products.map((p) => [p.id, p.name]));
    for (const line of lineData) {
      line.productName = names.get(line.productId) ?? line.productName;
    }

    const created = await this.prisma.$transaction(async (tx) => {
      const returnRequest = await tx.returnRequest.create({
        data: {
          companyId: order.companyId ?? undefined,
          returnCode: this.code('RET'),
          orderId: order.id,
          shopkeeperId: order.shopkeeperId,
          dealerId: order.dealerId,
          reason: dto.reason,
          reasonText: dto.reasonText?.trim(),
          comments: dto.comments?.trim(),
          imageUrls: dto.imageUrls?.length ? dto.imageUrls : undefined,
          status: ReturnRequestStatus.REQUESTED,
          refundAmount: this.decimal(refundAmount),
        },
      });

      await tx.returnRequestItem.createMany({
        data: lineData.map((l) => ({ ...l, returnRequestId: returnRequest.id })),
      });

      await tx.order.update({
        where: { id: order.id },
        data: {
          status: OrderStatus.RETURN_REQUESTED,
          returnReason: dto.reasonText?.trim() || dto.reason,
          returnRequestedAt: new Date(),
        },
      });

      return tx.returnRequest.findUniqueOrThrow({
        where: { id: returnRequest.id },
        include: returnInclude,
      });
    });

    await this.audit.log({
      companyId: order.companyId,
      action: FinanceAuditAction.RETURN_REQUESTED,
      entityType: 'ReturnRequest',
      entityId: created.id,
      actorId: actor.userId,
      details: { orderId, refundAmount },
    });

    void this.notifications.sendToUser(
      order.dealerId,
      'New return request',
      `Return request ${created.returnCode} for order ${order.id.slice(0, 8)}`,
      { type: 'RETURN', returnId: created.id, orderId: order.id },
    );
    void this.notifications.sendToUser(
      order.shopkeeperId,
      'Return request received',
      `Your return request ${created.returnCode} was submitted`,
      { type: 'RETURN', returnId: created.id, orderId: order.id },
    );

    return this.presentReturn(created);
  }

  async listReturns(actor: AuthUser, q: ReturnsQueryDto) {
    const where: Prisma.ReturnRequestWhereInput = {};
    if (actor.companyId) where.companyId = actor.companyId;
    if (actor.role === UserRole.SHOPKEEPER) where.shopkeeperId = actor.userId;
    if (actor.role === UserRole.DEALER) where.dealerId = actor.userId;
    if (q.status) where.status = q.status;
    if (q.shopkeeperId) where.shopkeeperId = q.shopkeeperId;
    if (q.startDate || q.endDate) {
      where.createdAt = {};
      if (q.startDate) where.createdAt.gte = new Date(q.startDate);
      if (q.endDate) where.createdAt.lte = new Date(q.endDate);
    }
    if (q.area) {
      where.shopkeeper = { area: { name: { contains: q.area, mode: 'insensitive' } } };
    }

    const rows = await this.prisma.returnRequest.findMany({
      where,
      include: returnInclude,
      orderBy: { createdAt: 'desc' },
    });
    return rows.map((r) => this.presentReturn(r));
  }

  async getReturn(id: string, actor: AuthUser) {
    const row = await this.prisma.returnRequest.findUnique({
      where: { id },
      include: returnInclude,
    });
    if (!row) throw new NotFoundException('Return request not found');
    this.assertReturnAccess(row, actor);
    return this.presentReturn(row);
  }

  private assertReturnAccess(
    row: { shopkeeperId: string; dealerId: string; companyId: string | null },
    actor: AuthUser,
  ) {
    if (actor.role === UserRole.ADMIN || actor.role === UserRole.EMPLOYEE) return;
    if (actor.role === UserRole.SHOPKEEPER && row.shopkeeperId === actor.userId) return;
    if (actor.role === UserRole.DEALER && row.dealerId === actor.userId) return;
    throw new ForbiddenException('Not allowed to view this return');
  }

  async approveReturn(id: string, dto: ReturnActionDto, actor: AuthUser) {
    const row = await this.prisma.returnRequest.findUnique({
      where: { id },
      include: { items: true, order: { include: { items: true } } },
    });
    if (!row) throw new NotFoundException('Return request not found');
    this.assertDealerOrStaff(row.dealerId, actor);

    if (
      row.status !== ReturnRequestStatus.REQUESTED &&
      row.status !== ReturnRequestStatus.UNDER_REVIEW
    ) {
      throw new BadRequestException('Return is not pending review');
    }

    const updated = await this.prisma.$transaction(async (tx) => {
      for (const item of row.items) {
        await tx.stock.upsert({
          where: {
            dealerId_productId: { dealerId: row.dealerId, productId: item.productId },
          },
          create: {
            dealerId: row.dealerId,
            productId: item.productId,
            quantity: item.quantity,
          },
          update: { quantity: { increment: item.quantity } },
        });
      }

      return tx.returnRequest.update({
        where: { id },
        data: {
          status: ReturnRequestStatus.APPROVED,
          dealerRemarks: dto.remarks?.trim(),
          approvedAt: new Date(),
        },
        include: returnInclude,
      });
    });

    await this.audit.log({
      companyId: row.companyId,
      action: FinanceAuditAction.RETURN_APPROVED,
      entityType: 'ReturnRequest',
      entityId: id,
      actorId: actor.userId,
    });

    void this.notifications.sendToUser(
      row.shopkeeperId,
      'Return approved',
      `Return ${row.returnCode} was approved by dealer`,
      { type: 'RETURN', returnId: id, orderId: row.orderId },
    );

    return this.presentReturn(updated);
  }

  async rejectReturn(id: string, dto: ReturnActionDto, actor: AuthUser) {
    const row = await this.prisma.returnRequest.findUnique({ where: { id } });
    if (!row) throw new NotFoundException('Return request not found');
    this.assertDealerOrStaff(row.dealerId, actor);

    if (
      row.status !== ReturnRequestStatus.REQUESTED &&
      row.status !== ReturnRequestStatus.UNDER_REVIEW
    ) {
      throw new BadRequestException('Return is not pending review');
    }

    const updated = await this.prisma.$transaction(async (tx) => {
      await tx.order.update({
        where: { id: row.orderId },
        data: { status: OrderStatus.DELIVERED },
      });
      return tx.returnRequest.update({
        where: { id },
        data: {
          status: ReturnRequestStatus.REJECTED,
          dealerRemarks: dto.remarks?.trim(),
          rejectedAt: new Date(),
        },
        include: returnInclude,
      });
    });

    await this.audit.log({
      companyId: row.companyId,
      action: FinanceAuditAction.RETURN_REJECTED,
      entityType: 'ReturnRequest',
      entityId: id,
      actorId: actor.userId,
    });

    void this.notifications.sendToUser(
      row.shopkeeperId,
      'Return rejected',
      `Return ${row.returnCode} was rejected`,
      { type: 'RETURN', returnId: id, orderId: row.orderId },
    );

    return this.presentReturn(updated);
  }

  async raiseRefundRequest(id: string, dto: RaiseRefundRequestDto, actor: AuthUser) {
    const row = await this.prisma.returnRequest.findUnique({
      where: { id },
      include: { refundRequest: true },
    });
    if (!row) throw new NotFoundException('Return request not found');
    if (actor.role !== UserRole.DEALER || row.dealerId !== actor.userId) {
      throw new ForbiddenException('Only the assigned dealer can raise a refund request');
    }
    if (row.status !== ReturnRequestStatus.APPROVED) {
      throw new BadRequestException('Only approved returns can be forwarded for refund');
    }
    if (row.refundRequest) {
      throw new BadRequestException('Refund request already exists');
    }

    const refund = await this.prisma.refundRequest.create({
      data: {
        companyId: row.companyId ?? undefined,
        refundCode: this.code('RFD'),
        returnRequestId: row.id,
        orderId: row.orderId,
        dealerId: row.dealerId,
        shopkeeperId: row.shopkeeperId,
        amount: row.refundAmount,
        dealerRemarks: dto.remarks?.trim(),
        status: RefundRequestStatus.PENDING,
      },
      include: refundInclude,
    });

    await this.audit.log({
      companyId: row.companyId,
      action: FinanceAuditAction.REFUND_REQUESTED,
      entityType: 'RefundRequest',
      entityId: refund.id,
      actorId: actor.userId,
    });

    const adminIds = await this.adminUserIds(row.companyId);
    void this.notifications.sendToUsers(
      adminIds,
      'New refund request',
      `Dealer raised refund ${refund.refundCode} for return ${row.returnCode}`,
      { type: 'REFUND', refundId: refund.id, returnId: row.id },
    );
    void this.notifications.sendToUser(
      row.shopkeeperId,
      'Refund request submitted',
      `Refund request ${refund.refundCode} sent to admin for processing`,
      { type: 'REFUND', refundId: refund.id },
    );

    return this.presentRefund(refund);
  }

  async listRefunds(actor: AuthUser, q: RefundsQueryDto) {
    const where: Prisma.RefundRequestWhereInput = {};
    if (actor.companyId) where.companyId = actor.companyId;
    if (actor.role === UserRole.DEALER) where.dealerId = actor.userId;
    if (actor.role === UserRole.SHOPKEEPER) where.shopkeeperId = actor.userId;
    if (q.status) where.status = q.status;
    if (q.dealerId) where.dealerId = q.dealerId;
    if (q.startDate || q.endDate) {
      where.createdAt = {};
      if (q.startDate) where.createdAt.gte = new Date(q.startDate);
      if (q.endDate) where.createdAt.lte = new Date(q.endDate);
    }

    const rows = await this.prisma.refundRequest.findMany({
      where,
      include: refundInclude,
      orderBy: { createdAt: 'desc' },
    });
    return rows.map((r) => this.presentRefund(r));
  }

  async getRefund(id: string, actor: AuthUser) {
    const row = await this.prisma.refundRequest.findUnique({
      where: { id },
      include: refundInclude,
    });
    if (!row) throw new NotFoundException('Refund request not found');
    this.assertRefundAccess(row, actor);
    return this.presentRefund(row);
  }

  private assertRefundAccess(
    row: { dealerId: string; shopkeeperId: string },
    actor: AuthUser,
  ) {
    if (actor.role === UserRole.ADMIN || actor.role === UserRole.EMPLOYEE) return;
    if (actor.role === UserRole.DEALER && row.dealerId === actor.userId) return;
    if (actor.role === UserRole.SHOPKEEPER && row.shopkeeperId === actor.userId) return;
    throw new ForbiddenException('Not allowed to view this refund');
  }

  async approveRefund(id: string, dto: RefundRejectDto, actor: AuthUser) {
    this.assertAdmin(actor);
    const row = await this.prisma.refundRequest.findUnique({ where: { id } });
    if (!row) throw new NotFoundException('Refund request not found');
    if (row.status !== RefundRequestStatus.PENDING) {
      throw new BadRequestException('Refund is not pending approval');
    }

    const updated = await this.prisma.refundRequest.update({
      where: { id },
      data: {
        status: RefundRequestStatus.PROCESSING,
        adminRemarks: dto.remarks?.trim(),
      },
      include: refundInclude,
    });

    await this.audit.log({
      companyId: row.companyId,
      action: FinanceAuditAction.REFUND_APPROVED,
      entityType: 'RefundRequest',
      entityId: id,
      actorId: actor.userId,
    });

    void this.notifications.sendToUser(
      row.dealerId,
      'Refund approved by admin',
      `Refund ${row.refundCode} approved — awaiting processing`,
      { type: 'REFUND', refundId: id },
    );

    return this.presentRefund(updated);
  }

  async rejectRefund(id: string, dto: RefundRejectDto, actor: AuthUser) {
    this.assertAdmin(actor);
    const row = await this.prisma.refundRequest.findUnique({ where: { id } });
    if (!row) throw new NotFoundException('Refund request not found');
    if (row.status !== RefundRequestStatus.PENDING && row.status !== RefundRequestStatus.PROCESSING) {
      throw new BadRequestException('Refund cannot be rejected in current status');
    }

    const updated = await this.prisma.refundRequest.update({
      where: { id },
      data: {
        status: RefundRequestStatus.REJECTED,
        adminRemarks: dto.remarks?.trim(),
      },
      include: refundInclude,
    });

    await this.audit.log({
      companyId: row.companyId,
      action: FinanceAuditAction.REFUND_REJECTED,
      entityType: 'RefundRequest',
      entityId: id,
      actorId: actor.userId,
    });

    void this.notifications.sendToUser(
      row.dealerId,
      'Refund rejected',
      `Refund ${row.refundCode} was rejected by admin`,
      { type: 'REFUND', refundId: id },
    );
    void this.notifications.sendToUser(
      row.shopkeeperId,
      'Refund rejected',
      `Refund for return was rejected`,
      { type: 'REFUND', refundId: id },
    );

    return this.presentRefund(updated);
  }

  async processRefund(id: string, dto: ProcessRefundDto, actor: AuthUser) {
    this.assertAdmin(actor);
    const row = await this.prisma.refundRequest.findUnique({
      where: { id },
      include: {
        returnRequest: { include: { items: true } },
        order: true,
      },
    });
    if (!row) throw new NotFoundException('Refund request not found');
    if (
      row.status !== RefundRequestStatus.PENDING &&
      row.status !== RefundRequestStatus.PROCESSING
    ) {
      throw new BadRequestException('Refund cannot be processed in current status');
    }

    const refundDate = dto.refundDate ? new Date(dto.refundDate) : new Date();

    if (dto.refundMethod === RefundMethod.RAZORPAY && row.order.paymentStatus === OrderPaymentStatus.PAID) {
      await this.payments.refundOrderPayment(row.orderId, actor, Number(row.amount), {
        skipOrderUpdate: true,
      });
    }

    const updated = await this.prisma.$transaction(async (tx) => {
      await tx.order.update({
        where: { id: row.orderId },
        data: {
          status: OrderStatus.RETURNED,
          returnedAt: refundDate,
          paymentStatus:
            dto.refundMethod === RefundMethod.RAZORPAY
              ? OrderPaymentStatus.REFUNDED
              : row.order.paymentStatus,
        },
      });

      await tx.returnRequest.update({
        where: { id: row.returnRequestId },
        data: {
          status: ReturnRequestStatus.RETURN_COMPLETED,
          completedAt: refundDate,
        },
      });

      return tx.refundRequest.update({
        where: { id },
        data: {
          status: RefundRequestStatus.REFUNDED,
          refundMethod: dto.refundMethod,
          transactionReference: dto.transactionReference.trim(),
          adminRemarks: dto.remarks?.trim() ?? row.adminRemarks,
          refundDate,
          processedById: actor.userId,
        },
        include: refundInclude,
      });
    });

    await this.audit.log({
      companyId: row.companyId,
      action: FinanceAuditAction.REFUND_PROCESSED,
      entityType: 'RefundRequest',
      entityId: id,
      actorId: actor.userId,
      details: {
        method: dto.refundMethod,
        transactionReference: dto.transactionReference,
        amount: Number(row.amount),
      },
    });

    void this.notifications.sendToUser(
      row.shopkeeperId,
      'Refund completed',
      `Refund ${row.refundCode} processed — ${dto.transactionReference}`,
      { type: 'REFUND', refundId: id, orderId: row.orderId },
    );
    void this.notifications.sendToUser(
      row.dealerId,
      'Refund completed',
      `Refund ${row.refundCode} has been processed`,
      { type: 'REFUND', refundId: id },
    );

    return this.presentRefund(updated);
  }

  private assertDealerOrStaff(dealerId: string, actor: AuthUser) {
    const ok =
      actor.role === UserRole.ADMIN ||
      actor.role === UserRole.EMPLOYEE ||
      (actor.role === UserRole.DEALER && dealerId === actor.userId);
    if (!ok) throw new ForbiddenException('Not allowed');
  }

  private assertAdmin(actor: AuthUser) {
    if (actor.role !== UserRole.ADMIN && actor.role !== UserRole.EMPLOYEE) {
      throw new ForbiddenException('Admin access required');
    }
  }

  /** Legacy order endpoint — simple text reason, full order return. */
  async createLegacyReturn(orderId: string, reason: string, actor: AuthUser) {
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: { items: true },
    });
    if (!order) throw new NotFoundException('Order not found');

    return this.createReturn(
      orderId,
      {
        reason: 'OTHER',
        reasonText: reason,
        items: order.items.map((i) => ({ productId: i.productId, quantity: i.quantity })),
      },
      actor,
    );
  }

  async approveLegacyReturn(orderId: string, actor: AuthUser, remarks?: string) {
    const row = await this.prisma.returnRequest.findFirst({
      where: {
        orderId,
        status: { in: [ReturnRequestStatus.REQUESTED, ReturnRequestStatus.UNDER_REVIEW] },
      },
      orderBy: { createdAt: 'desc' },
    });
    if (!row) throw new BadRequestException('No pending return request for this order');
    return this.approveReturn(row.id, { remarks }, actor);
  }

  async rejectLegacyReturn(orderId: string, note: string | undefined, actor: AuthUser) {
    const row = await this.prisma.returnRequest.findFirst({
      where: {
        orderId,
        status: { in: [ReturnRequestStatus.REQUESTED, ReturnRequestStatus.UNDER_REVIEW] },
      },
      orderBy: { createdAt: 'desc' },
    });
    if (!row) throw new BadRequestException('No pending return request for this order');
    return this.rejectReturn(row.id, { remarks: note }, actor);
  }
}
