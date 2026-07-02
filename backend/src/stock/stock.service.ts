import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { Prisma, UserRole } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { UpsertStockDto } from './dto/upsert-stock.dto';

@Injectable()
export class StockService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(actor: AuthUser) {
    const where: Prisma.StockWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    if (actor.role === UserRole.DEALER) {
      where.dealerId = actor.userId;
    }

    return this.prisma.stock.findMany({
      where,
      include: {
        product: true,
        dealer: { select: { id: true, name: true, email: true } },
      },
      orderBy: { updatedAt: 'desc' },
    });
  }

  /**
   * Product availability for the shopkeeper's assigned dealer.
   * Returns a lightweight productId -> quantity list so the catalog can gate
   * adding out-of-stock items to the cart.
   */
  async availableForShopkeeper(actor: AuthUser) {
    if (actor.role !== UserRole.SHOPKEEPER) {
      throw new ForbiddenException('Only shopkeepers can view availability');
    }
    const shopkeeper = await this.prisma.user.findUnique({
      where: { id: actor.userId },
      include: { area: true },
    });
    const dealerId = shopkeeper?.area?.dealerId;
    if (!dealerId) {
      return [] as { productId: string; quantity: number }[];
    }
    return this.prisma.stock.findMany({
      where: { dealerId },
      select: { productId: true, quantity: true },
    });
  }

  async updateQuantity(stockId: string, actor: AuthUser, quantity: number) {
    const row = await this.prisma.stock.findUnique({
      where: { id: stockId },
      include: { product: true },
    });
    if (!row) {
      throw new NotFoundException('Stock row not found');
    }
    this.assertCanManageStock(actor, row.dealerId, row.companyId);
    return this.prisma.stock.update({
      where: { id: stockId },
      data: { quantity },
      include: {
        product: true,
        dealer: { select: { id: true, name: true, email: true } },
      },
    });
  }

  async upsertForDealer(actor: AuthUser, dto: UpsertStockDto) {
    if (actor.role !== UserRole.DEALER) {
      throw new ForbiddenException('Only dealers can add stock rows from the app');
    }
    if (!actor.companyId) {
      throw new BadRequestException('Company scope is required');
    }
    const product = await this.prisma.product.findFirst({
      where: { id: dto.productId, companyId: actor.companyId },
    });
    if (!product) {
      throw new NotFoundException('Product not found in your catalog');
    }
    return this.prisma.stock.upsert({
      where: {
        dealerId_productId: {
          dealerId: actor.userId,
          productId: dto.productId,
        },
      },
      create: {
        companyId: actor.companyId,
        dealerId: actor.userId,
        productId: dto.productId,
        quantity: dto.quantity,
      },
      update: { quantity: dto.quantity },
      include: {
        product: true,
        dealer: { select: { id: true, name: true, email: true } },
      },
    });
  }

  private assertCanManageStock(
    actor: AuthUser,
    dealerId: string,
    companyId: string | null,
  ): void {
    if (actor.role === UserRole.ADMIN || actor.role === UserRole.EMPLOYEE) {
      if (actor.companyId && companyId && actor.companyId !== companyId) {
        throw new ForbiddenException('Stock is outside your company');
      }
      return;
    }
    if (actor.role === UserRole.DEALER && actor.userId === dealerId) {
      return;
    }
    throw new ForbiddenException('You cannot update this stock row');
  }
}
