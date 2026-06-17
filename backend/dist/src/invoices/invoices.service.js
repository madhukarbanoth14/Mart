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
exports.InvoicesService = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@prisma/client");
const prisma_service_1 = require("../prisma/prisma.service");
let InvoicesService = class InvoicesService {
    prisma;
    constructor(prisma) {
        this.prisma = prisma;
    }
    findAll(actor) {
        const where = {};
        if (actor.companyId) {
            where.companyId = actor.companyId;
        }
        if (actor.role === client_1.UserRole.SHOPKEEPER) {
            where.order = { shopkeeperId: actor.userId };
        }
        else if (actor.role === client_1.UserRole.DEALER) {
            where.order = { dealerId: actor.userId };
        }
        return this.prisma.invoice.findMany({
            where,
            include: {
                order: {
                    include: {
                        items: { include: { product: true } },
                        shopkeeper: { select: { id: true, name: true } },
                        dealer: { select: { id: true, name: true } },
                    },
                },
            },
            orderBy: { generatedAt: 'desc' },
        });
    }
    async getInvoiceDocument(orderId) {
        const invoice = await this.prisma.invoice.findUnique({
            where: { orderId },
            include: {
                order: {
                    include: {
                        items: { include: { product: true } },
                        shopkeeper: { select: { id: true, name: true, email: true } },
                        dealer: { select: { id: true, name: true, email: true } },
                    },
                },
            },
        });
        if (!invoice) {
            return null;
        }
        return {
            invoiceNumber: invoice.invoiceNumber,
            generatedAt: invoice.generatedAt,
            pdfUrl: invoice.pdfUrl,
            order: invoice.order,
        };
    }
    async generateForOrder(orderId, companyId) {
        const invoiceNumber = `INV-${orderId.replace(/-/g, '').toUpperCase()}`;
        return this.prisma.invoice.upsert({
            where: { orderId },
            create: {
                orderId,
                companyId: companyId ?? null,
                invoiceNumber,
            },
            update: {},
        });
    }
};
exports.InvoicesService = InvoicesService;
exports.InvoicesService = InvoicesService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService])
], InvoicesService);
//# sourceMappingURL=invoices.service.js.map