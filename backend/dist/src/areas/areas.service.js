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
exports.AreasService = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@prisma/client");
const prisma_service_1 = require("../prisma/prisma.service");
const area_name_util_1 = require("./area-name.util");
let AreasService = class AreasService {
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
        return this.prisma.area.findMany({
            where,
            include: { dealer: { select: { id: true, name: true, email: true } } },
            orderBy: { name: 'asc' },
        });
    }
    async create(actor, dto) {
        if (actor.role !== client_1.UserRole.ADMIN) {
            throw new common_1.ForbiddenException('Only admins can add areas');
        }
        if (!actor.companyId) {
            throw new common_1.BadRequestException('Company scope is required to add areas');
        }
        const name = (0, area_name_util_1.normalizeAreaName)(dto.name);
        const existing = await this.prisma.area.findFirst({
            where: {
                companyId: actor.companyId,
                name: { equals: name, mode: 'insensitive' },
            },
        });
        if (existing) {
            throw new common_1.ConflictException('An area with this name already exists');
        }
        return this.prisma.area.create({
            data: { name, companyId: actor.companyId },
            include: { dealer: { select: { id: true, name: true, email: true } } },
        });
    }
    async update(actor, areaId, dto) {
        if (actor.role !== client_1.UserRole.ADMIN) {
            throw new common_1.ForbiddenException('Only admins can rename areas');
        }
        if (!actor.companyId) {
            throw new common_1.BadRequestException('Company scope is required to update areas');
        }
        const existing = await this.prisma.area.findFirst({
            where: { id: areaId, companyId: actor.companyId },
        });
        if (!existing) {
            throw new common_1.NotFoundException('Area not found');
        }
        const name = (0, area_name_util_1.normalizeAreaName)(dto.name);
        const duplicate = await this.prisma.area.findFirst({
            where: {
                companyId: actor.companyId,
                id: { not: areaId },
                name: { equals: name, mode: 'insensitive' },
            },
        });
        if (duplicate) {
            throw new common_1.ConflictException('An area with this name already exists');
        }
        return this.prisma.area.update({
            where: { id: areaId },
            data: { name },
            include: { dealer: { select: { id: true, name: true, email: true } } },
        });
    }
    async defaultCompanyId() {
        const fromEnv = process.env.MART_COMPANY_ID?.trim();
        if (fromEnv)
            return fromEnv;
        const company = await this.prisma.company.findFirst({
            orderBy: { createdAt: 'asc' },
            select: { id: true },
        });
        return company?.id ?? null;
    }
    async ensureDefaultCompanyId() {
        const existing = await this.defaultCompanyId();
        if (existing)
            return existing;
        const company = await this.prisma.company.create({
            data: { name: 'FlashMart Distribution' },
            select: { id: true },
        });
        return company.id;
    }
    async ensureRegistrationAreas(companyId) {
        const count = await this.prisma.area.count({ where: { companyId } });
        if (count > 0)
            return;
        for (const area of DEFAULT_REGISTRATION_AREAS) {
            await this.prisma.area.create({
                data: {
                    name: area.name,
                    state: area.state,
                    district: area.district,
                    companyId,
                },
            });
        }
    }
    async findForRegistration(state, district) {
        const companyId = await this.ensureDefaultCompanyId();
        await this.ensureRegistrationAreas(companyId);
        const baseWhere = { companyId };
        const withGeo = { ...baseWhere };
        const stateTrim = state?.trim();
        const districtTrim = district?.trim();
        if (stateTrim) {
            withGeo.state = { equals: stateTrim, mode: 'insensitive' };
        }
        if (districtTrim && !isAllDistrictsLabel(districtTrim)) {
            withGeo.district = { equals: districtTrim, mode: 'insensitive' };
        }
        const select = {
            id: true,
            name: true,
            state: true,
            district: true,
            dealerId: true,
        };
        let areas = await this.prisma.area.findMany({
            where: withGeo,
            select,
            orderBy: { name: 'asc' },
        });
        if (areas.length === 0 && (stateTrim || districtTrim)) {
            areas = await this.prisma.area.findMany({
                where: baseWhere,
                select,
                orderBy: { name: 'asc' },
            });
        }
        return areas;
    }
    async listRegistrationGeo() {
        const companyId = await this.ensureDefaultCompanyId();
        await this.ensureRegistrationAreas(companyId);
        const areas = await this.prisma.area.findMany({
            where: { companyId },
            select: { state: true, district: true },
        });
        const states = new Map();
        for (const area of areas) {
            const state = area.state?.trim() || 'Telangana';
            const district = area.district?.trim() || ALL_DISTRICTS_LABEL;
            if (!states.has(state))
                states.set(state, new Set());
            states.get(state).add(district);
        }
        if (states.size === 0) {
            return {
                states: [
                    {
                        name: 'Telangana',
                        districts: ['Warangal', 'Hanamkonda', 'Jangaon'],
                    },
                ],
            };
        }
        return {
            states: [...states.entries()].map(([name, districts]) => ({
                name,
                districts: [...districts].sort(),
            })),
        };
    }
};
exports.AreasService = AreasService;
exports.AreasService = AreasService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], AreasService);
const ALL_DISTRICTS_LABEL = 'All areas';
const DEFAULT_REGISTRATION_AREAS = [
    { name: 'Warangal', state: 'Telangana', district: 'Warangal' },
    { name: 'Hanamkonda', state: 'Telangana', district: 'Hanamkonda' },
    { name: 'Jangaon', state: 'Telangana', district: 'Jangaon' },
];
function isAllDistrictsLabel(district) {
    const d = district.trim().toLowerCase();
    return d === ALL_DISTRICTS_LABEL.toLowerCase() || d === 'other';
}
//# sourceMappingURL=areas.service.js.map