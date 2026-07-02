import { Injectable } from '@nestjs/common';
import { FinanceAuditAction, Prisma } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class FinanceAuditService {
  constructor(private readonly prisma: PrismaService) {}

  async log(params: {
    companyId?: string | null;
    action: FinanceAuditAction;
    entityType: string;
    entityId: string;
    actorId?: string | null;
    details?: Prisma.InputJsonValue;
  }) {
    await this.prisma.financeAuditLog.create({
      data: {
        companyId: params.companyId ?? undefined,
        action: params.action,
        entityType: params.entityType,
        entityId: params.entityId,
        actorId: params.actorId ?? undefined,
        details: params.details,
      },
    });
  }

  list(companyId: string | null, limit = 100) {
    return this.prisma.financeAuditLog.findMany({
      where: companyId ? { companyId } : undefined,
      orderBy: { createdAt: 'desc' },
      take: limit,
      include: {
        actor: { select: { id: true, name: true, email: true } },
      },
    });
  }
}
