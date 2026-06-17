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
exports.BrandsService = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@prisma/client");
const prisma_service_1 = require("../prisma/prisma.service");
let BrandsService = class BrandsService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    findAll() {
        return this.prisma.brand.findMany({
            select: {
                id: true,
                name: true,
                logoUrl: true,
                manufacturer: true,
            },
            orderBy: { name: 'asc' },
        });
    }
    async findOne(id) {
        const brand = await this.prisma.brand.findUnique({
            where: { id },
            select: {
                id: true,
                name: true,
                logoUrl: true,
                manufacturer: true,
                category: true,
                companyId: true,
            },
        });
        if (!brand) {
            throw new common_1.NotFoundException('Brand not found');
        }
        return brand;
    }
    async create(actor, dto) {
        if (!actor.companyId) {
            throw new common_1.BadRequestException('Company scope is required to create brands');
        }
        try {
            return await this.prisma.brand.create({
                data: {
                    name: dto.name.trim(),
                    logoUrl: dto.logoUrl?.trim() || null,
                    companyId: actor.companyId,
                },
            });
        }
        catch (error) {
            if (error instanceof client_1.Prisma.PrismaClientKnownRequestError &&
                error.code === 'P2002') {
                throw new common_1.BadRequestException('Brand name already exists');
            }
            throw error;
        }
    }
    async update(id, actor, dto) {
        const existing = await this.prisma.brand.findUnique({ where: { id } });
        if (!existing) {
            throw new common_1.NotFoundException('Brand not found');
        }
        if (actor.companyId && existing.companyId !== actor.companyId) {
            throw new common_1.BadRequestException('Brand does not belong to your company');
        }
        try {
            return await this.prisma.brand.update({
                where: { id },
                data: {
                    name: dto.name?.trim(),
                    logoUrl: dto.logoUrl?.trim() || null,
                },
            });
        }
        catch (error) {
            if (error instanceof client_1.Prisma.PrismaClientKnownRequestError &&
                error.code === 'P2002') {
                throw new common_1.BadRequestException('Brand name already exists');
            }
            throw error;
        }
    }
    async remove(id, actor) {
        const existing = await this.prisma.brand.findUnique({ where: { id } });
        if (!existing) {
            throw new common_1.NotFoundException('Brand not found');
        }
        if (actor.companyId && existing.companyId !== actor.companyId) {
            throw new common_1.BadRequestException('Brand does not belong to your company');
        }
        return this.prisma.brand.delete({ where: { id } });
    }
};
exports.BrandsService = BrandsService;
exports.BrandsService = BrandsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], BrandsService);
//# sourceMappingURL=brands.service.js.map