"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ProductsService = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@prisma/client");
const prisma_service_1 = require("../prisma/prisma.service");
let ProductsService = class ProductsService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    findAll(actor, query) {
        const where = {};
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
        const limit = Number.isFinite(limitRaw) && limitRaw > 0
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
    async findAllPaged(actor, query) {
        const where = {};
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
        const limit = Number.isFinite(limitRaw) && limitRaw > 0
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
    async listShelves(actor) {
        const rows = await this.prisma.product.findMany({
            where: actor.companyId ? { companyId: actor.companyId } : {},
            select: { shelf: true },
            distinct: ['shelf'],
            orderBy: { shelf: 'asc' },
        });
        return rows.map((r) => r.shelf);
    }
    async findOne(productId, actor) {
        const product = await this.prisma.product.findUnique({
            where: { id: productId },
            include: { brand: true },
        });
        if (!product) {
            throw new common_1.NotFoundException('Product not found');
        }
        if (actor.companyId && product.companyId && product.companyId !== actor.companyId) {
            throw new common_1.BadRequestException('Product does not belong to your company');
        }
        return product;
    }
    async create(actor, dto) {
        if (!actor.companyId) {
            throw new common_1.BadRequestException('Company scope is required to create products');
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
                bulkShippingFee: dto.bulkShippingFee != null ? new client_1.Prisma.Decimal(dto.bulkShippingFee) : undefined,
                bulkShippingMinQty: dto.bulkShippingMinQty ?? undefined,
            },
        });
    }
    async update(productId, actor, dto) {
        const existing = await this.prisma.product.findUnique({
            where: { id: productId },
        });
        if (!existing) {
            throw new common_1.NotFoundException('Product not found');
        }
        if (actor.companyId && existing.companyId && existing.companyId !== actor.companyId) {
            throw new common_1.BadRequestException('Product does not belong to your company');
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
                bulkShippingFee: dto.bulkShippingFee === null
                    ? null
                    : dto.bulkShippingFee != null
                        ? new client_1.Prisma.Decimal(dto.bulkShippingFee)
                        : undefined,
                bulkShippingMinQty: dto.bulkShippingMinQty ?? undefined,
            },
        });
    }
    async remove(productId, actor) {
        const existing = await this.prisma.product.findUnique({
            where: { id: productId },
        });
        if (!existing) {
            throw new common_1.NotFoundException('Product not found');
        }
        if (actor.companyId && existing.companyId && existing.companyId !== actor.companyId) {
            throw new common_1.BadRequestException('Product does not belong to your company');
        }
        return this.prisma.product.delete({ where: { id: productId } });
    }
};
exports.ProductsService = ProductsService;
exports.ProductsService = ProductsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], ProductsService);
//# sourceMappingURL=products.service.js.map