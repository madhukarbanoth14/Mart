import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { CreateProductDto } from './dto/create-product.dto';
import { ProductsQueryDto } from './dto/products-query.dto';
import { UpdateProductDto } from './dto/update-product.dto';

@Injectable()
export class ProductsService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(actor: AuthUser, query?: ProductsQueryDto) {
    const where: Prisma.ProductWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    if (query?.brandId) {
      where.brandId = query.brandId;
    }
    if (query?.shelf) {
      where.shelf = query.shelf;
    }
    if (query?.search?.trim()) {
      const term = query.search.trim();
      where.OR = [
        { name: { contains: term, mode: 'insensitive' } },
        { sku: { contains: term, mode: 'insensitive' } },
        { brand: { name: { contains: term, mode: 'insensitive' } } },
      ];
    }

    const pageRaw = Number.parseInt(query?.page ?? '1', 10);
    const limitRaw = Number.parseInt(query?.limit ?? '200', 10);
    const page = Number.isFinite(pageRaw) && pageRaw > 0 ? pageRaw : 1;
    const limit =
      Number.isFinite(limitRaw) && limitRaw > 0
        ? Math.min(limitRaw, 100)
        : 100;
    const skip = (page - 1) * limit;

    return this.prisma.product.findMany({
      where,
      include: { brand: true },
      orderBy: { name: 'asc' },
      skip,
      take: limit,
    });
  }

  async findAllPaged(actor: AuthUser, query?: ProductsQueryDto) {
    const where: Prisma.ProductWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    if (query?.brandId) {
      where.brandId = query.brandId;
    }
    if (query?.shelf) {
      where.shelf = query.shelf;
    }
    if (query?.search?.trim()) {
      const term = query.search.trim();
      where.OR = [
        { name: { contains: term, mode: 'insensitive' } },
        { sku: { contains: term, mode: 'insensitive' } },
        { brand: { name: { contains: term, mode: 'insensitive' } } },
      ];
    }
    const pageRaw = Number.parseInt(query?.page ?? '1', 10);
    const limitRaw = Number.parseInt(query?.limit ?? '20', 10);
    const page = Number.isFinite(pageRaw) && pageRaw > 0 ? pageRaw : 1;
    const limit =
      Number.isFinite(limitRaw) && limitRaw > 0
        ? Math.min(limitRaw, 100)
        : 20;
    const skip = (page - 1) * limit;

    const [items, total] = await this.prisma.$transaction([
      this.prisma.product.findMany({
        where,
        include: { brand: true },
        orderBy: { name: 'asc' },
        skip,
        take: limit,
      }),
      this.prisma.product.count({ where }),
    ]);

    return {
      items,
      page,
      limit,
      total,
      hasNext: page * limit < total,
    };
  }

  async listShelves(actor: AuthUser): Promise<string[]> {
    const rows = await this.prisma.product.findMany({
      where: actor.companyId ? { companyId: actor.companyId } : {},
      select: { shelf: true },
      distinct: ['shelf'],
      orderBy: { shelf: 'asc' },
    });
    return rows.map((r) => r.shelf);
  }

  async findOne(productId: string, actor: AuthUser) {
    const product = await this.prisma.product.findUnique({
      where: { id: productId },
      include: { brand: true },
    });
    if (!product) {
      throw new NotFoundException('Product not found');
    }
    if (actor.companyId && product.companyId && product.companyId !== actor.companyId) {
      throw new BadRequestException('Product does not belong to your company');
    }
    return product;
  }

  async create(actor: AuthUser, dto: CreateProductDto) {
    if (!actor.companyId) {
      throw new BadRequestException('Company scope is required to create products');
    }
    return this.prisma.product.create({
      data: {
        companyId: actor.companyId,
        name: dto.name.trim(),
        brandType: dto.brandType,
        brandId: dto.brandId,
        imageUrl: dto.imageUrl?.trim() || null,
        shelf: dto.shelf,
        basePrice: dto.basePrice,
        gstPercentage: dto.gstPercentage,
        dealerDiscount: dto.dealerDiscount,
        shopkeeperDiscount: dto.shopkeeperDiscount,
        bulkShippingFee:
          dto.bulkShippingFee != null ? new Prisma.Decimal(dto.bulkShippingFee) : undefined,
        bulkShippingMinQty: dto.bulkShippingMinQty ?? undefined,
      },
    });
  }

  async update(productId: string, actor: AuthUser, dto: UpdateProductDto) {
    const existing = await this.prisma.product.findUnique({
      where: { id: productId },
    });
    if (!existing) {
      throw new NotFoundException('Product not found');
    }
    if (actor.companyId && existing.companyId && existing.companyId !== actor.companyId) {
      throw new BadRequestException('Product does not belong to your company');
    }
    return this.prisma.product.update({
      where: { id: productId },
      data: {
        name: dto.name?.trim(),
        brandType: dto.brandType,
        brandId: dto.brandId,
        imageUrl: dto.imageUrl?.trim() || undefined,
        shelf: dto.shelf,
        basePrice: dto.basePrice,
        gstPercentage: dto.gstPercentage,
        dealerDiscount: dto.dealerDiscount,
        shopkeeperDiscount: dto.shopkeeperDiscount,
        bulkShippingFee:
          dto.bulkShippingFee === null
            ? null
            : dto.bulkShippingFee != null
              ? new Prisma.Decimal(dto.bulkShippingFee)
              : undefined,
        bulkShippingMinQty: dto.bulkShippingMinQty ?? undefined,
      },
    });
  }

  async remove(productId: string, actor: AuthUser) {
    const existing = await this.prisma.product.findUnique({
      where: { id: productId },
    });
    if (!existing) {
      throw new NotFoundException('Product not found');
    }
    if (actor.companyId && existing.companyId && existing.companyId !== actor.companyId) {
      throw new BadRequestException('Product does not belong to your company');
    }
    return this.prisma.product.delete({ where: { id: productId } });
  }
}
