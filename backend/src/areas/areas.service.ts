import { Injectable } from '@nestjs/common';
import { Prisma, UserRole } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class AreasService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(actor: AuthUser) {
    const where: Prisma.AreaWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    if (actor.role === UserRole.DEALER) {
      where.dealerId = actor.userId;
    }

    return this.prisma.area.findMany({
      where,
      include: { dealer: { select: { id: true, name: true, email: true } } },
      orderBy: { name: 'asc' },
    });
  }
}
