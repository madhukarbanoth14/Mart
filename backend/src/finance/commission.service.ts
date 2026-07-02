import { BadRequestException, Injectable } from '@nestjs/common';
import { CommissionRuleType, Prisma } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { FinanceAuditService } from './finance-audit.service';

const DEFAULT_GLOBAL_RATE = 8;

@Injectable()
export class CommissionService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly audit: FinanceAuditService,
  ) {}

  async ensureGlobalRule(companyId: string | null) {
    const existing = await this.prisma.commissionRule.findFirst({
      where: {
        ruleType: CommissionRuleType.GLOBAL,
        companyId: companyId ?? null,
        active: true,
      },
    });
    if (existing) return existing;
    return this.prisma.commissionRule.create({
      data: {
        companyId: companyId ?? undefined,
        ruleType: CommissionRuleType.GLOBAL,
        rate: DEFAULT_GLOBAL_RATE,
      },
    });
  }

  listRules(companyId: string | null) {
    return this.prisma.commissionRule.findMany({
      where: companyId ? { companyId } : undefined,
      include: {
        dealer: { select: { id: true, name: true, shopName: true } },
        product: { select: { id: true, name: true } },
      },
      orderBy: [{ ruleType: 'asc' }, { updatedAt: 'desc' }],
    });
  }

  async upsertRule(
    companyId: string | null,
    actorId: string,
    dto: {
      ruleType: CommissionRuleType;
      rate: number;
      dealerId?: string;
      productId?: string;
    },
  ) {
    const rate = Number(dto.rate);
    if (!Number.isFinite(rate) || rate < 0 || rate > 100) {
      throw new BadRequestException('Commission rate must be between 0 and 100');
    }

    let rule;
    if (dto.ruleType === CommissionRuleType.GLOBAL) {
      const existing = await this.prisma.commissionRule.findFirst({
        where: { ruleType: CommissionRuleType.GLOBAL, companyId: companyId ?? null },
      });
      rule = existing
        ? await this.prisma.commissionRule.update({
            where: { id: existing.id },
            data: { rate, active: true },
          })
        : await this.prisma.commissionRule.create({
            data: {
              companyId: companyId ?? undefined,
              ruleType: CommissionRuleType.GLOBAL,
              rate,
            },
          });
    } else if (dto.ruleType === CommissionRuleType.DEALER && dto.dealerId) {
      const existing = await this.prisma.commissionRule.findFirst({
        where: {
          ruleType: CommissionRuleType.DEALER,
          dealerId: dto.dealerId,
          companyId: companyId ?? null,
        },
      });
      rule = existing
        ? await this.prisma.commissionRule.update({
            where: { id: existing.id },
            data: { rate, active: true },
          })
        : await this.prisma.commissionRule.create({
            data: {
              companyId: companyId ?? undefined,
              ruleType: CommissionRuleType.DEALER,
              dealerId: dto.dealerId,
              rate,
            },
          });
    } else if (dto.ruleType === CommissionRuleType.PRODUCT && dto.productId) {
      const existing = await this.prisma.commissionRule.findFirst({
        where: {
          ruleType: CommissionRuleType.PRODUCT,
          productId: dto.productId,
          companyId: companyId ?? null,
        },
      });
      rule = existing
        ? await this.prisma.commissionRule.update({
            where: { id: existing.id },
            data: { rate, active: true },
          })
        : await this.prisma.commissionRule.create({
            data: {
              companyId: companyId ?? undefined,
              ruleType: CommissionRuleType.PRODUCT,
              productId: dto.productId,
              rate,
            },
          });
    } else {
      throw new BadRequestException('Invalid commission rule payload');
    }

    await this.audit.log({
      companyId,
      action: 'COMMISSION_RULE_UPDATED',
      entityType: 'CommissionRule',
      entityId: rule.id,
      actorId,
      details: { ruleType: dto.ruleType, rate, dealerId: dto.dealerId, productId: dto.productId },
    });

    return rule;
  }

  /** Product → Dealer → Global priority per line item. */
  async calculateOrderCommission(params: {
    companyId: string | null;
    dealerId: string;
    items: Array<{ productId: string; finalAmount: Prisma.Decimal }>;
  }) {
    await this.ensureGlobalRule(params.companyId);

    const rules = await this.prisma.commissionRule.findMany({
      where: {
        active: true,
        OR: [
          { ruleType: CommissionRuleType.GLOBAL, companyId: params.companyId ?? null },
          { ruleType: CommissionRuleType.DEALER, dealerId: params.dealerId },
          {
            ruleType: CommissionRuleType.PRODUCT,
            productId: { in: params.items.map((i) => i.productId) },
          },
        ],
      },
    });

    const globalRate =
      rules.find((r) => r.ruleType === CommissionRuleType.GLOBAL)?.rate.toNumber() ??
      DEFAULT_GLOBAL_RATE;
    const dealerRate = rules.find(
      (r) => r.ruleType === CommissionRuleType.DEALER && r.dealerId === params.dealerId,
    )?.rate.toNumber();
    const productRates = new Map(
      rules
        .filter((r) => r.ruleType === CommissionRuleType.PRODUCT && r.productId)
        .map((r) => [r.productId!, r.rate.toNumber()]),
    );

    let commissionTotal = 0;
    let grossTotal = 0;
    let weightedRateSum = 0;

    for (const item of params.items) {
      const lineGross = Number(item.finalAmount);
      grossTotal += lineGross;
      const rate = productRates.get(item.productId) ?? dealerRate ?? globalRate;
      const lineCommission = (lineGross * rate) / 100;
      commissionTotal += lineCommission;
      weightedRateSum += rate * lineGross;
    }

    const effectiveRate = grossTotal > 0 ? weightedRateSum / grossTotal : globalRate;
    return {
      commissionAmount: Math.round(commissionTotal * 100) / 100,
      effectiveRate: Math.round(effectiveRate * 100) / 100,
    };
  }
}
