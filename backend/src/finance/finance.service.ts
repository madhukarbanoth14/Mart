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
  SettlementPaymentMethod,
  SettlementStatus,
  UserRole,
} from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { CommissionService } from './commission.service';
import { FinanceAuditService } from './finance-audit.service';

type PeriodFilter = 'today' | 'yesterday' | 'week' | 'month' | 'year' | 'custom';

@Injectable()
export class FinanceService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly commission: CommissionService,
    private readonly audit: FinanceAuditService,
  ) {}

  private decimal(n: number) {
    return new Prisma.Decimal(n.toFixed(2));
  }

  private periodRange(filter: PeriodFilter, start?: string, end?: string) {
    const now = new Date();
    const startOfDay = (d: Date) =>
      new Date(d.getFullYear(), d.getMonth(), d.getDate());
    const endOfDay = (d: Date) =>
      new Date(d.getFullYear(), d.getMonth(), d.getDate(), 23, 59, 59, 999);

    switch (filter) {
      case 'today':
        return { from: startOfDay(now), to: endOfDay(now) };
      case 'yesterday': {
        const y = new Date(now);
        y.setDate(y.getDate() - 1);
        return { from: startOfDay(y), to: endOfDay(y) };
      }
      case 'week': {
        const from = new Date(now);
        from.setDate(from.getDate() - 6);
        return { from: startOfDay(from), to: endOfDay(now) };
      }
      case 'month':
        return {
          from: new Date(now.getFullYear(), now.getMonth(), 1),
          to: endOfDay(now),
        };
      case 'year':
        return {
          from: new Date(now.getFullYear(), 0, 1),
          to: endOfDay(now),
        };
      case 'custom':
        if (!start || !end) {
          throw new BadRequestException('Custom period requires startDate and endDate');
        }
        return { from: new Date(start), to: endOfDay(new Date(end)) };
      default:
        return { from: startOfDay(now), to: endOfDay(now) };
    }
  }

  /** Called when an order is marked delivered — records platform revenue if paid. */
  async recordRevenueOnDelivery(orderId: string) {
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: {
        items: true,
        payments: { where: { status: 'SUCCESS' }, orderBy: { createdAt: 'desc' }, take: 1 },
      },
    });
    if (!order) return null;
    if (order.kind !== OrderKind.SHOPKEEPER) return null;
    if (order.status !== OrderStatus.DELIVERED) return null;
    if (order.paymentStatus !== OrderPaymentStatus.PAID) return null;

    const existing = await this.prisma.platformRevenue.findUnique({
      where: { orderId: order.id },
    });
    if (existing) return existing;

    const { commissionAmount, effectiveRate } =
      await this.commission.calculateOrderCommission({
        companyId: order.companyId,
        dealerId: order.dealerId,
        items: order.items.map((i) => ({
          productId: i.productId,
          finalAmount: i.finalAmount,
        })),
      });

    const gross = Number(order.finalAmount);
    const gst = Number(order.gstAmount);
    const dealerPayable = gross - commissionAmount;

    const revenue = await this.prisma.platformRevenue.create({
      data: {
        companyId: order.companyId ?? undefined,
        orderId: order.id,
        paymentId: order.payments[0]?.id,
        dealerId: order.dealerId,
        grossAmount: this.decimal(gross),
        gstAmount: this.decimal(gst),
        commissionAmount: this.decimal(commissionAmount),
        dealerPayable: this.decimal(dealerPayable),
        commissionRate: this.decimal(effectiveRate),
        settlementStatus: SettlementStatus.PENDING,
        revenueDate: new Date(),
      },
    });

    await this.audit.log({
      companyId: order.companyId,
      action: FinanceAuditAction.COMMISSION_CALCULATED,
      entityType: 'PlatformRevenue',
      entityId: revenue.id,
      details: {
        orderId: order.id,
        gross,
        commissionAmount,
        dealerPayable,
        effectiveRate,
      },
    });

    return revenue;
  }

  async dashboardOverview(companyId: string | null, period: PeriodFilter, start?: string, end?: string) {
    const { from, to } = this.periodRange(period, start, end);

    const revenues = await this.prisma.platformRevenue.findMany({
      where: {
        ...(companyId ? { companyId } : {}),
        revenueDate: { gte: from, lte: to },
      },
    });

    const payments = await this.prisma.payment.findMany({
      where: {
        ...(companyId ? { companyId } : {}),
        createdAt: { gte: from, lte: to },
      },
    });

    const settlements = await this.prisma.dealerSettlement.findMany({
      where: companyId ? { companyId } : undefined,
    });

    const sum = (rows: Array<{ grossAmount?: Prisma.Decimal; commissionAmount?: Prisma.Decimal; dealerPayable?: Prisma.Decimal; settledAmount?: Prisma.Decimal; balanceAmount?: Prisma.Decimal }>, key: string) =>
      rows.reduce((acc, r) => acc + Number((r as Record<string, Prisma.Decimal>)[key] ?? 0), 0);

    const gmv = sum(revenues, 'grossAmount');
    const commission = sum(revenues, 'commissionAmount');
    const dealerPayables = sum(revenues, 'dealerPayable');
    const settled = settlements.reduce((a, s) => a + Number(s.settledAmount), 0);
    const pendingSettlement = settlements.reduce((a, s) => a + Number(s.balanceAmount), 0);

    const successfulPayments = payments.filter((p) => p.status === 'SUCCESS').length;
    const failedPayments = payments.filter((p) => p.status === 'FAILED').length;
    const refundedPayments = payments.filter((p) => p.status === 'REFUNDED').length;
    const pendingPayments = payments.filter((p) => p.status === 'INITIATED').length;

    return {
      period: { from, to, filter: period },
      collections: {
        total: payments.filter((p) => p.status === 'SUCCESS').reduce((a, p) => a + Number(p.amount), 0),
        successful: successfulPayments,
        failed: failedPayments,
        refunded: refundedPayments,
        pending: pendingPayments,
      },
      revenue: {
        gmv,
        platformCommission: commission,
        dealerPayables,
        settledAmount: settled,
        pendingSettlement,
        netPlatformEarnings: commission,
        refunds: payments.filter((p) => p.status === 'REFUNDED').reduce((a, p) => a + Number(p.amount), 0),
      },
    };
  }

  async investorDashboard(companyId: string | null) {
    const { from, to } = this.periodRange('month');
    const overview = await this.dashboardOverview(companyId, 'month');

    const orders = await this.prisma.order.count({
      where: companyId ? { companyId } : undefined,
    });
    const delivered = await this.prisma.order.count({
      where: {
        ...(companyId ? { companyId } : {}),
        status: OrderStatus.DELIVERED,
      },
    });
    const activeDealers = await this.prisma.user.count({
      where: { role: UserRole.DEALER, status: 'ACTIVE', ...(companyId ? { companyId } : {}) },
    });
    const activeShopkeepers = await this.prisma.user.count({
      where: { role: UserRole.SHOPKEEPER, status: 'ACTIVE', ...(companyId ? { companyId } : {}) },
    });
    const activeEmployees = await this.prisma.user.count({
      where: { role: UserRole.EMPLOYEE, status: 'ACTIVE', ...(companyId ? { companyId } : {}) },
    });

    const dailyTrend = await this.revenueTrend(companyId, 30);

    return {
      ...overview,
      business: {
        totalOrders: orders,
        deliveredOrders: delivered,
        activeDealers,
        activeShopkeepers,
        activeEmployees,
      },
      charts: {
        dailyRevenueTrend: dailyTrend,
      },
    };
  }

  private async revenueTrend(companyId: string | null, days: number) {
    const from = new Date();
    from.setDate(from.getDate() - (days - 1));
    from.setHours(0, 0, 0, 0);

    const rows = await this.prisma.platformRevenue.findMany({
      where: {
        ...(companyId ? { companyId } : {}),
        revenueDate: { gte: from },
      },
      select: { revenueDate: true, grossAmount: true, commissionAmount: true },
    });

    const byDay = new Map<string, { gmv: number; commission: number }>();
    for (let i = 0; i < days; i++) {
      const d = new Date(from);
      d.setDate(d.getDate() + i);
      byDay.set(d.toISOString().slice(0, 10), { gmv: 0, commission: 0 });
    }
    for (const row of rows) {
      const key = row.revenueDate.toISOString().slice(0, 10);
      const bucket = byDay.get(key) ?? { gmv: 0, commission: 0 };
      bucket.gmv += Number(row.grossAmount);
      bucket.commission += Number(row.commissionAmount);
      byDay.set(key, bucket);
    }
    return [...byDay.entries()].map(([date, v]) => ({ date, ...v }));
  }

  async listSettlements(
    companyId: string | null,
    filters: { dealerId?: string; status?: SettlementStatus; period?: PeriodFilter; start?: string; end?: string },
  ) {
    const where: Prisma.DealerSettlementWhereInput = companyId ? { companyId } : {};
    if (filters.dealerId) where.dealerId = filters.dealerId;
    if (filters.status) where.settlementStatus = filters.status;
    if (filters.period) {
      const { from, to } = this.periodRange(filters.period, filters.start, filters.end);
      where.settlementStartDate = { gte: from };
      where.settlementEndDate = { lte: to };
    }

    return this.prisma.dealerSettlement.findMany({
      where,
      include: {
        dealer: { select: { id: true, name: true, shopName: true, phone: true } },
        createdBy: { select: { id: true, name: true } },
        payments: { orderBy: { paymentDate: 'desc' } },
      },
      orderBy: { createdAt: 'desc' },
    });
  }

  async generateSettlement(
    actor: AuthUser,
    dto: { dealerId: string; startDate: string; endDate: string },
  ) {
    const start = new Date(dto.startDate);
    const end = new Date(dto.endDate);
    end.setHours(23, 59, 59, 999);

    const revenues = await this.prisma.platformRevenue.findMany({
      where: {
        dealerId: dto.dealerId,
        settlementStatus: SettlementStatus.PENDING,
        settlementId: null,
        revenueDate: { gte: start, lte: end },
        order: { status: OrderStatus.DELIVERED, paymentStatus: OrderPaymentStatus.PAID },
      },
      include: { order: { include: { items: true } } },
    });

    if (revenues.length === 0) {
      throw new BadRequestException('No eligible delivered orders for settlement in this period');
    }

    const totalOrders = revenues.length;
    const totalQuantity = revenues.reduce(
      (sum, r) => sum + r.order.items.reduce((s, i) => s + i.quantity, 0),
      0,
    );
    const grossSales = revenues.reduce((s, r) => s + Number(r.grossAmount), 0);
    const gstAmount = revenues.reduce((s, r) => s + Number(r.gstAmount), 0);
    const commissionAmount = revenues.reduce((s, r) => s + Number(r.commissionAmount), 0);
    const dealerPayable = revenues.reduce((s, r) => s + Number(r.dealerPayable), 0);

    const code = `STL-${Date.now().toString(36).toUpperCase()}`;

    const settlement = await this.prisma.$transaction(async (tx) => {
      const created = await tx.dealerSettlement.create({
        data: {
          settlementCode: code,
          companyId: actor.companyId ?? undefined,
          dealerId: dto.dealerId,
          settlementStartDate: start,
          settlementEndDate: end,
          totalOrders,
          totalQuantity,
          grossSales: this.decimal(grossSales),
          gstAmount: this.decimal(gstAmount),
          commissionAmount: this.decimal(commissionAmount),
          dealerPayable: this.decimal(dealerPayable),
          settledAmount: this.decimal(0),
          balanceAmount: this.decimal(dealerPayable),
          settlementStatus: SettlementStatus.PENDING,
          createdById: actor.userId,
        },
      });

      await tx.platformRevenue.updateMany({
        where: { id: { in: revenues.map((r) => r.id) } },
        data: { settlementId: created.id },
      });

      return created;
    });

    await this.audit.log({
      companyId: actor.companyId,
      action: FinanceAuditAction.SETTLEMENT_CREATED,
      entityType: 'DealerSettlement',
      entityId: settlement.id,
      actorId: actor.userId,
      details: { dealerId: dto.dealerId, totalOrders, dealerPayable },
    });

    return this.getSettlement(settlement.id);
  }

  async getSettlement(id: string) {
    const row = await this.prisma.dealerSettlement.findUnique({
      where: { id },
      include: {
        dealer: { select: { id: true, name: true, shopName: true, phone: true, email: true } },
        createdBy: { select: { id: true, name: true } },
        payments: { orderBy: { paymentDate: 'desc' }, include: { createdBy: { select: { id: true, name: true } } } },
        revenues: {
          include: {
            order: { select: { id: true, finalAmount: true, createdAt: true, status: true } },
          },
        },
      },
    });
    if (!row) throw new NotFoundException('Settlement not found');
    return row;
  }

  async recordSettlementPayment(
    actor: AuthUser,
    settlementId: string,
    dto: {
      amount: number;
      paymentMethod: SettlementPaymentMethod;
      utrNumber?: string;
      transactionReference?: string;
      paymentDate: string;
      remarks?: string;
    },
  ) {
    const settlement = await this.prisma.dealerSettlement.findUnique({
      where: { id: settlementId },
    });
    if (!settlement) throw new NotFoundException('Settlement not found');

    const amount = Number(dto.amount);
    if (!Number.isFinite(amount) || amount <= 0) {
      throw new BadRequestException('Payment amount must be positive');
    }
    if (!dto.utrNumber?.trim() && !dto.transactionReference?.trim()) {
      throw new BadRequestException('UTR number or transaction reference is required');
    }

    const balance = Number(settlement.balanceAmount);
    if (amount > balance + 0.01) {
      throw new BadRequestException('Payment exceeds settlement balance');
    }

    const updated = await this.prisma.$transaction(async (tx) => {
      await tx.dealerPaymentHistory.create({
        data: {
          settlementId,
          dealerId: settlement.dealerId,
          amount: this.decimal(amount),
          paymentMethod: dto.paymentMethod,
          utrNumber: dto.utrNumber?.trim(),
          transactionReference: dto.transactionReference?.trim(),
          paymentDate: new Date(dto.paymentDate),
          remarks: dto.remarks?.trim(),
          createdById: actor.userId,
        },
      });

      const newSettled = Number(settlement.settledAmount) + amount;
      const newBalance = Number(settlement.dealerPayable) - newSettled;
      const status =
        newBalance <= 0.01
          ? SettlementStatus.SETTLED
          : SettlementStatus.PARTIALLY_SETTLED;

      const row = await tx.dealerSettlement.update({
        where: { id: settlementId },
        data: {
          settledAmount: this.decimal(newSettled),
          balanceAmount: this.decimal(Math.max(0, newBalance)),
          settlementStatus: status,
          paymentMethod: dto.paymentMethod,
          utrNumber: dto.utrNumber?.trim() ?? settlement.utrNumber,
          transactionReference:
            dto.transactionReference?.trim() ?? settlement.transactionReference,
          paymentDate: new Date(dto.paymentDate),
          remarks: dto.remarks?.trim() ?? settlement.remarks,
        },
      });

      if (status === SettlementStatus.SETTLED) {
        await tx.platformRevenue.updateMany({
          where: { settlementId },
          data: { settlementStatus: SettlementStatus.SETTLED },
        });
      }

      return row;
    });

    await this.audit.log({
      companyId: actor.companyId,
      action: FinanceAuditAction.SETTLEMENT_PAYMENT,
      entityType: 'DealerSettlement',
      entityId: settlementId,
      actorId: actor.userId,
      details: dto,
    });

    return this.getSettlement(updated.id);
  }

  async dealerPerformance(dealerId: string, period: PeriodFilter, start?: string, end?: string) {
    const { from, to } = this.periodRange(period, start, end);

    const revenues = await this.prisma.platformRevenue.findMany({
      where: { dealerId, revenueDate: { gte: from, lte: to } },
      include: { order: { include: { items: { include: { product: true } } } } },
    });

    const orders = await this.prisma.order.findMany({
      where: {
        dealerId,
        createdAt: { gte: from, lte: to },
      },
    });

    const gross = revenues.reduce((s, r) => s + Number(r.grossAmount), 0);
    const commission = revenues.reduce((s, r) => s + Number(r.commissionAmount), 0);
    const earnings = revenues.reduce((s, r) => s + Number(r.dealerPayable), 0);
    const pending = revenues
      .filter((r) => r.settlementStatus === SettlementStatus.PENDING)
      .reduce((s, r) => s + Number(r.dealerPayable), 0);

    const productMap = new Map<string, { name: string; qty: number; revenue: number }>();
    for (const r of revenues) {
      for (const item of r.order.items) {
        const key = item.productId;
        const cur = productMap.get(key) ?? {
          name: item.product.name,
          qty: 0,
          revenue: 0,
        };
        cur.qty += item.quantity;
        cur.revenue += Number(item.finalAmount);
        productMap.set(key, cur);
      }
    }

    return {
      period: { from, to },
      summary: {
        ordersDelivered: revenues.length,
        ordersTotal: orders.length,
        grossSales: gross,
        commissionDeducted: commission,
        dealerEarnings: earnings,
        pendingSettlement: pending,
      },
      topProducts: [...productMap.values()]
        .sort((a, b) => b.revenue - a.revenue)
        .slice(0, 10),
      dailySales: await this.dealerDailySales(dealerId, from, to),
    };
  }

  private async dealerDailySales(dealerId: string, from: Date, to: Date) {
    const rows = await this.prisma.platformRevenue.findMany({
      where: { dealerId, revenueDate: { gte: from, lte: to } },
      select: { revenueDate: true, grossAmount: true },
    });
    const map = new Map<string, number>();
    for (const r of rows) {
      const k = r.revenueDate.toISOString().slice(0, 10);
      map.set(k, (map.get(k) ?? 0) + Number(r.grossAmount));
    }
    return [...map.entries()].map(([date, amount]) => ({ date, amount }));
  }

  async exportReport(
    actor: AuthUser,
    type: string,
    format: 'csv' | 'xlsx',
    period: PeriodFilter,
    start?: string,
    end?: string,
  ) {
    const { from, to } = this.periodRange(period, start, end);
    let rows: Record<string, string | number>[] = [];
    let filename = `${type}-${from.toISOString().slice(0, 10)}`;

    if (type === 'settlements') {
      const list = await this.listSettlements(actor.companyId, { period, start, end });
      rows = list.map((s) => ({
        settlementCode: s.settlementCode,
        dealer: s.dealer.shopName ?? s.dealer.name,
        periodStart: s.settlementStartDate.toISOString().slice(0, 10),
        periodEnd: s.settlementEndDate.toISOString().slice(0, 10),
        grossSales: Number(s.grossSales),
        commission: Number(s.commissionAmount),
        dealerPayable: Number(s.dealerPayable),
        settled: Number(s.settledAmount),
        balance: Number(s.balanceAmount),
        status: s.settlementStatus,
      }));
    } else if (type === 'revenue') {
      const list = await this.prisma.platformRevenue.findMany({
        where: {
          ...(actor.companyId ? { companyId: actor.companyId } : {}),
          revenueDate: { gte: from, lte: to },
        },
        include: { dealer: { select: { name: true, shopName: true } }, order: { select: { id: true } } },
      });
      rows = list.map((r) => ({
        orderId: r.orderId,
        dealer: r.dealer.shopName ?? r.dealer.name,
        gross: Number(r.grossAmount),
        gst: Number(r.gstAmount),
        commission: Number(r.commissionAmount),
        dealerPayable: Number(r.dealerPayable),
        status: r.settlementStatus,
        date: r.revenueDate.toISOString().slice(0, 10),
      }));
    } else if (type === 'collections') {
      const list = await this.prisma.payment.findMany({
        where: {
          ...(actor.companyId ? { companyId: actor.companyId } : {}),
          createdAt: { gte: from, lte: to },
        },
        include: { order: { select: { id: true } } },
      });
      rows = list.map((p) => ({
        paymentId: p.id,
        orderId: p.orderId,
        amount: Number(p.amount),
        status: p.status,
        provider: p.provider,
        date: p.createdAt.toISOString(),
      }));
    } else if (type === 'return-requests') {
      const list = await this.prisma.returnRequest.findMany({
        where: {
          ...(actor.companyId ? { companyId: actor.companyId } : {}),
          createdAt: { gte: from, lte: to },
        },
        include: {
          dealer: { select: { name: true, shopName: true } },
          shopkeeper: { select: { name: true, shopName: true } },
        },
      });
      rows = list.map((r) => ({
        returnCode: r.returnCode,
        dealer: r.dealer.shopName ?? r.dealer.name,
        shopkeeper: r.shopkeeper.shopName ?? r.shopkeeper.name,
        orderId: r.orderId,
        reason: r.reason,
        amount: Number(r.refundAmount),
        status: r.status,
        date: r.createdAt.toISOString().slice(0, 10),
      }));
    } else if (type === 'refund-history' || type === 'pending-refunds' || type === 'completed-refunds') {
      const statusFilter =
        type === 'pending-refunds'
          ? { in: ['PENDING', 'PROCESSING'] as const }
          : type === 'completed-refunds'
            ? 'REFUNDED'
            : undefined;
      const list = await this.prisma.refundRequest.findMany({
        where: {
          ...(actor.companyId ? { companyId: actor.companyId } : {}),
          createdAt: { gte: from, lte: to },
          ...(statusFilter
            ? typeof statusFilter === 'string'
              ? { status: statusFilter }
              : { status: { in: [...statusFilter.in] } }
            : {}),
        },
        include: {
          dealer: { select: { name: true, shopName: true } },
          returnRequest: {
            include: { shopkeeper: { select: { name: true, shopName: true } } },
          },
        },
      });
      rows = list.map((r) => ({
        refundCode: r.refundCode,
        dealer: r.dealer.shopName ?? r.dealer.name,
        shopkeeper: r.returnRequest.shopkeeper.shopName ?? r.returnRequest.shopkeeper.name,
        orderId: r.orderId,
        amount: Number(r.amount),
        status: r.status,
        method: r.refundMethod ?? '',
        reference: r.transactionReference ?? '',
        date: r.createdAt.toISOString().slice(0, 10),
      }));
    } else {
      throw new BadRequestException('Unknown report type');
    }

    await this.audit.log({
      companyId: actor.companyId,
      action: FinanceAuditAction.REPORT_DOWNLOADED,
      entityType: 'Report',
      entityId: type,
      actorId: actor.userId,
      details: { format, period, from, to, rowCount: rows.length },
    });

    if (format === 'csv') {
      const headers = rows.length > 0 ? Object.keys(rows[0]) : [];
      const csv = [
        headers.join(','),
        ...rows.map((r) => headers.map((h) => JSON.stringify(r[h] ?? '')).join(',')),
      ].join('\n');
      return { filename: `${filename}.csv`, contentType: 'text/csv', body: csv };
    }

    // Lightweight TSV labeled as xlsx-compatible export for mobile clients
    const headers = rows.length > 0 ? Object.keys(rows[0]) : [];
    const tsv = [headers.join('\t'), ...rows.map((r) => headers.map((h) => r[h] ?? '').join('\t'))].join('\n');
    return {
      filename: `${filename}.xlsx`,
      contentType: 'application/vnd.ms-excel',
      body: tsv,
    };
  }

  async backfillRevenues(companyId: string | null) {
    const orders = await this.prisma.order.findMany({
      where: {
        ...(companyId ? { companyId } : {}),
        kind: OrderKind.SHOPKEEPER,
        status: OrderStatus.DELIVERED,
        paymentStatus: OrderPaymentStatus.PAID,
        platformRevenue: null,
      },
      select: { id: true },
    });
    let created = 0;
    for (const o of orders) {
      const r = await this.recordRevenueOnDelivery(o.id);
      if (r) created += 1;
    }
    return { processed: orders.length, created };
  }

  async dealerRevenueDashboard(
    dealerId: string,
    companyId: string | null,
    period: PeriodFilter,
    start?: string,
    end?: string,
  ) {
    const today = this.periodRange('today');
    const week = this.periodRange('week');
    const month = this.periodRange('month');

    const baseWhere = {
      dealerId,
      ...(companyId ? { companyId } : {}),
      kind: OrderKind.SHOPKEEPER,
    };

    const orders = await this.prisma.order.findMany({
      where: baseWhere,
      include: { items: { include: { product: true } } },
    });

    const delivered = orders.filter((o) => o.status === OrderStatus.DELIVERED);
    const returned = orders.filter((o) => o.status === OrderStatus.RETURNED);
    const pending = orders.filter(
      (o) =>
        o.status === OrderStatus.PENDING ||
        o.status === OrderStatus.DEALER_CONFIRMED ||
        o.status === OrderStatus.OUT_FOR_DELIVERY,
    );
    const returnRequested = orders.filter((o) => o.status === OrderStatus.RETURN_REQUESTED);

    const revenues = await this.prisma.platformRevenue.findMany({
      where: { dealerId, ...(companyId ? { companyId } : {}) },
    });

    const revenueInRange = (from: Date, to: Date) =>
      revenues
        .filter((r) => r.revenueDate >= from && r.revenueDate <= to)
        .reduce((s, r) => s + Number(r.grossAmount), 0);

    const qtySold = delivered.reduce((s, o) => s + o.items.reduce((a, i) => a + i.quantity, 0), 0);

    const pendingSettlement = revenues
      .filter((r) => r.settlementStatus === SettlementStatus.PENDING)
      .reduce((s, r) => s + Number(r.dealerPayable), 0);
    const amountReceived = revenues
      .filter((r) => r.settlementStatus === SettlementStatus.SETTLED)
      .reduce((s, r) => s + Number(r.dealerPayable), 0);

    const { from, to } = this.periodRange(period, start, end);
    const perf = await this.dealerPerformance(dealerId, period, start, end);

    const categoryMap = new Map<string, number>();
    const productMap = new Map<string, { name: string; qty: number; revenue: number }>();
    const shopkeeperMap = new Map<string, number>();

    for (const r of revenues.filter(
      (rev) => rev.revenueDate >= from && rev.revenueDate <= to,
    )) {
      const order = orders.find((o) => o.id === r.orderId);
      if (!order) continue;
      shopkeeperMap.set(
        order.shopkeeperId,
        (shopkeeperMap.get(order.shopkeeperId) ?? 0) + Number(r.grossAmount),
      );
      for (const item of order.items) {
        const shelf = item.productId;
        categoryMap.set(shelf, (categoryMap.get(shelf) ?? 0) + Number(item.finalAmount));
        const cur = productMap.get(item.productId) ?? {
          name: item.productId,
          qty: 0,
          revenue: 0,
        };
        cur.qty += item.quantity;
        cur.revenue += Number(item.finalAmount);
        productMap.set(item.productId, cur);
      }
    }

    const products = await this.prisma.product.findMany({
      where: { id: { in: [...productMap.keys()] } },
      select: { id: true, name: true, shelf: true },
    });
    const productNames = new Map(products.map((p) => [p.id, p.name]));
    const shelfTotals = new Map<string, number>();
    for (const item of delivered.flatMap((o) => o.items)) {
      const prod = products.find((p) => p.id === item.productId);
      const shelf = prod?.shelf ?? 'OTHER';
      shelfTotals.set(shelf, (shelfTotals.get(shelf) ?? 0) + Number(item.finalAmount));
    }

    const shopkeepers = await this.prisma.user.findMany({
      where: { id: { in: [...shopkeeperMap.keys()] } },
      select: { id: true, name: true, shopName: true },
    });

    return {
      summary: {
        todayRevenue: revenueInRange(today.from, today.to),
        weeklyRevenue: revenueInRange(week.from, week.to),
        monthlyRevenue: revenueInRange(month.from, month.to),
        totalOrdersReceived: orders.length,
        totalOrdersDelivered: delivered.length,
        totalProductsSold: productMap.size,
        totalQuantitySold: qtySold,
        pendingOrders: pending.length,
        returnedOrders: returned.length + returnRequested.length,
        pendingSettlementAmount: pendingSettlement,
        amountReceivedFromFlashMart: amountReceived,
      },
      charts: {
        dailyRevenue: perf.dailySales,
        weeklyRevenue: revenueInRange(week.from, week.to),
        monthlyRevenue: revenueInRange(month.from, month.to),
        productWiseSales: [...productMap.entries()].map(([id, v]) => ({
          productId: id,
          name: productNames.get(id) ?? v.name,
          quantity: v.qty,
          revenue: v.revenue,
        })),
        categoryWiseSales: [...shelfTotals.entries()].map(([category, revenue]) => ({
          category,
          revenue,
        })),
        topSellingProducts: [...productMap.entries()]
          .map(([id, v]) => ({
            productId: id,
            name: productNames.get(id) ?? v.name,
            quantity: v.qty,
            revenue: v.revenue,
          }))
          .sort((a, b) => b.revenue - a.revenue)
          .slice(0, 10),
        shopkeeperWiseRevenue: [...shopkeeperMap.entries()].map(([id, revenue]) => {
          const sk = shopkeepers.find((s) => s.id === id);
          return {
            shopkeeperId: id,
            name: sk?.shopName ?? sk?.name ?? id,
            revenue,
          };
        }),
      },
      period: { from, to },
    };
  }

  async dealerShopkeeperRevenue(
    dealerId: string,
    companyId: string | null,
    q: {
      shopkeeper?: string;
      area?: string;
      startDate?: string;
      endDate?: string;
      orderStatus?: string;
    },
  ) {
    const where: Prisma.OrderWhereInput = {
      dealerId,
      kind: OrderKind.SHOPKEEPER,
      ...(companyId ? { companyId } : {}),
    };
    if (q.startDate || q.endDate) {
      where.createdAt = {};
      if (q.startDate) where.createdAt.gte = new Date(q.startDate);
      if (q.endDate) where.createdAt.lte = new Date(q.endDate);
    }
    if (q.orderStatus) where.status = q.orderStatus as OrderStatus;
    if (q.shopkeeper || q.area) {
      where.shopkeeper = {
        ...(q.shopkeeper
          ? {
              OR: [
                { name: { contains: q.shopkeeper, mode: 'insensitive' } },
                { shopName: { contains: q.shopkeeper, mode: 'insensitive' } },
              ],
            }
          : {}),
        ...(q.area ? { area: { name: { contains: q.area, mode: 'insensitive' } } } : {}),
      };
    }

    const orders = await this.prisma.order.findMany({
      where,
      include: {
        shopkeeper: { select: { id: true, name: true, shopName: true, area: { select: { name: true } } } },
      },
      orderBy: { createdAt: 'desc' },
    });

    const byShopkeeper = new Map<
      string,
      {
        shopkeeperId: string;
        name: string;
        area: string | null;
        totalOrders: number;
        totalPurchaseValue: number;
        outstandingOrders: number;
        returnedOrders: number;
        totalRevenue: number;
        lastOrderDate: Date | null;
      }
    >();

    for (const o of orders) {
      const sk = o.shopkeeper;
      const key = sk.id;
      const cur = byShopkeeper.get(key) ?? {
        shopkeeperId: key,
        name: sk.shopName ?? sk.name,
        area: sk.area?.name ?? null,
        totalOrders: 0,
        totalPurchaseValue: 0,
        outstandingOrders: 0,
        returnedOrders: 0,
        totalRevenue: 0,
        lastOrderDate: null,
      };
      cur.totalOrders += 1;
      cur.totalPurchaseValue += Number(o.finalAmount);
      if (
        o.status === OrderStatus.PENDING ||
        o.status === OrderStatus.DEALER_CONFIRMED ||
        o.status === OrderStatus.OUT_FOR_DELIVERY
      ) {
        cur.outstandingOrders += 1;
      }
      if (o.status === OrderStatus.RETURNED || o.status === OrderStatus.RETURN_REQUESTED) {
        cur.returnedOrders += 1;
      }
      if (o.status === OrderStatus.DELIVERED) {
        cur.totalRevenue += Number(o.finalAmount);
      }
      if (!cur.lastOrderDate || o.createdAt > cur.lastOrderDate) {
        cur.lastOrderDate = o.createdAt;
      }
      byShopkeeper.set(key, cur);
    }

    return [...byShopkeeper.values()].map((r) => ({
      ...r,
      totalPurchaseValue: Number(r.totalPurchaseValue.toFixed(2)),
      totalRevenue: Number(r.totalRevenue.toFixed(2)),
      lastOrderDate: r.lastOrderDate?.toISOString() ?? null,
    }));
  }

  async exportDealerReport(
    actor: AuthUser,
    type: string,
    format: 'csv' | 'xlsx',
    period: PeriodFilter,
    start?: string,
    end?: string,
  ) {
    if (actor.role !== UserRole.DEALER) {
      throw new ForbiddenException('Dealer access only');
    }
    const { from, to } = this.periodRange(period, start, end);
    let rows: Record<string, string | number>[] = [];
    const filename = `${type}-${from.toISOString().slice(0, 10)}`;

    if (type === 'revenue') {
      const dash = await this.dealerRevenueDashboard(
        actor.userId,
        actor.companyId,
        period,
        start,
        end,
      );
      rows = [
        { metric: 'Today Revenue', value: dash.summary.todayRevenue },
        { metric: 'Weekly Revenue', value: dash.summary.weeklyRevenue },
        { metric: 'Monthly Revenue', value: dash.summary.monthlyRevenue },
        { metric: 'Total Orders', value: dash.summary.totalOrdersReceived },
        { metric: 'Delivered', value: dash.summary.totalOrdersDelivered },
        { metric: 'Pending Settlement', value: dash.summary.pendingSettlementAmount },
      ];
    } else if (type === 'shopkeeper-revenue') {
      const list = await this.dealerShopkeeperRevenue(actor.userId, actor.companyId, {});
      rows = list.map((s) => ({
        shopkeeper: s.name,
        area: s.area ?? '',
        totalOrders: s.totalOrders,
        purchaseValue: s.totalPurchaseValue,
        outstanding: s.outstandingOrders,
        returned: s.returnedOrders,
        revenue: s.totalRevenue,
        lastOrder: s.lastOrderDate ?? '',
      }));
    } else if (type === 'returns') {
      const list = await this.prisma.returnRequest.findMany({
        where: {
          dealerId: actor.userId,
          createdAt: { gte: from, lte: to },
        },
        include: { shopkeeper: { select: { name: true, shopName: true } } },
      });
      rows = list.map((r) => ({
        returnCode: r.returnCode,
        orderId: r.orderId,
        shopkeeper: r.shopkeeper.shopName ?? r.shopkeeper.name,
        reason: r.reason,
        amount: Number(r.refundAmount),
        status: r.status,
        date: r.createdAt.toISOString().slice(0, 10),
      }));
    } else if (type === 'refunds') {
      const list = await this.prisma.refundRequest.findMany({
        where: {
          dealerId: actor.userId,
          createdAt: { gte: from, lte: to },
        },
      });
      rows = list.map((r) => ({
        refundCode: r.refundCode,
        orderId: r.orderId,
        amount: Number(r.amount),
        status: r.status,
        method: r.refundMethod ?? '',
        reference: r.transactionReference ?? '',
        date: r.createdAt.toISOString().slice(0, 10),
      }));
    } else {
      throw new BadRequestException('Unknown report type');
    }

    return this.buildExportFile(rows, filename, format);
  }

  private buildExportFile(
    rows: Record<string, string | number>[],
    filename: string,
    format: 'csv' | 'xlsx',
  ) {
    if (format === 'csv') {
      const headers = rows.length > 0 ? Object.keys(rows[0]) : [];
      const csv = [
        headers.join(','),
        ...rows.map((r) => headers.map((h) => JSON.stringify(r[h] ?? '')).join(',')),
      ].join('\n');
      return { filename: `${filename}.csv`, contentType: 'text/csv', body: csv };
    }
    const headers = rows.length > 0 ? Object.keys(rows[0]) : [];
    const tsv = [headers.join('\t'), ...rows.map((r) => headers.map((h) => r[h] ?? '').join('\t'))].join('\n');
    return {
      filename: `${filename}.xlsx`,
      contentType: 'application/vnd.ms-excel',
      body: tsv,
    };
  }
}
