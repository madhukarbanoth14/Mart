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
exports.StockService = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@prisma/client");
const prisma_service_1 = require("../prisma/prisma.service");
let StockService = class StockService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    findAll(actor) {
        const where = {};
        if (actor.companyId) {
            where.companyId = actor.companyId;
        }
        if (actor.role === client_1.UserRole.DEALER) {
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
    async availableForShopkeeper(actor) {
        if (actor.role !== client_1.UserRole.SHOPKEEPER) {
            throw new common_1.ForbiddenException('Only shopkeepers can view availability');
        }
        const shopkeeper = await this.prisma.user.findUnique({
            where: { id: actor.userId },
            include: { area: true },
        });
        const dealerId = shopkeeper?.area?.dealerId;
        if (!dealerId) {
            return [];
        }
        return this.prisma.stock.findMany({
            where: { dealerId },
            select: { productId: true, quantity: true },
        });
    }
    async updateQuantity(stockId, actor, quantity) {
        const row = await this.prisma.stock.findUnique({
            where: { id: stockId },
            include: { product: true },
        });
        if (!row) {
            throw new common_1.NotFoundException('Stock row not found');
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
    async upsertForDealer(actor, dto) {
        if (actor.role !== client_1.UserRole.DEALER) {
            throw new common_1.ForbiddenException('Only dealers can add stock rows from the app');
        }
        if (!actor.companyId) {
            throw new common_1.BadRequestException('Company scope is required');
        }
        const product = await this.prisma.product.findFirst({
            where: { id: dto.productId, companyId: actor.companyId },
        });
        if (!product) {
            throw new common_1.NotFoundException('Product not found in your catalog');
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
    assertCanManageStock(actor, dealerId, companyId) {
        if (actor.role === client_1.UserRole.ADMIN || actor.role === client_1.UserRole.EMPLOYEE) {
            if (actor.companyId && companyId && actor.companyId !== companyId) {
                throw new common_1.ForbiddenException('Stock is outside your company');
            }
            return;
        }
        if (actor.role === client_1.UserRole.DEALER && actor.userId === dealerId) {
            return;
        }
        throw new common_1.ForbiddenException('You cannot update this stock row');
    }
};
exports.StockService = StockService;
exports.StockService = StockService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], StockService);
//# sourceMappingURL=stock.service.js.map