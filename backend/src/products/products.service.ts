import { Injectable } from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class ProductsService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(actor: AuthUser) {
    const where: Prisma.ProductWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }

    return this.prisma.product.findMany({
      where,
      orderBy: { name: 'asc' },
    });
  }
}
