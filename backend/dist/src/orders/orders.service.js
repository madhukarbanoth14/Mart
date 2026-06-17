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
exports.OrdersService = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@prisma/client");
const notifications_service_1 = require("../notifications/notifications.service");
const invoices_service_1 = require("../invoices/invoices.service");
const payments_service_1 = require("../payments/payments.service");
const prisma_service_1 = require("../prisma/prisma.service");
const create_order_dto_1 = require("./dto/create-order.dto");
const orderInclude = {
    items: { include: { product: true } },
    shopkeeper: { select: { id: true, name: true, email: true, phone: true } },
    dealer: { select: { id: true, name: true, email: true, phone: true } },
    invoice: true,
};
let OrdersService = class OrdersService {
    prisma;
    invoicesService;
    notifications;
    paymentsService;
    constructor(prisma, invoicesService, notifications, paymentsService) {
        this.prisma = prisma;
        this.invoicesService = invoicesService;
        this.notifications = notifications;
        this.paymentsService = paymentsService;
    }
    scopeWhere(actor) {
        const where = {};
        if (actor.companyId) {
            where.companyId = actor.companyId;
        }
        if (actor.role === client_1.UserRole.SHOPKEEPER) {
            where.shopkeeperId = actor.userId;
        }
        else if (actor.role === client_1.UserRole.DEALER) {
            where.OR = [
                { dealerId: actor.userId, kind: client_1.OrderKind.SHOPKEEPER },
                { shopkeeperId: actor.userId, kind: client_1.OrderKind.DEALER_RESTOCK },
            ];
        }
        return where;
    }
    async getCompanyWarehouseUser(companyId) {
        const admin = await this.prisma.user.findFirst({
            where: { companyId, role: client_1.UserRole.ADMIN },
            orderBy: { createdAt: 'asc' },
        });
        if (!admin) {
            throw new common_1.BadRequestException('Company warehouse is not configured. Contact admin.');
        }
        return admin;
    }
    buildLineItems(dto, productsById, stockByProduct, buyerRole) {
        let totalAmount = new client_1.Prisma.Decimal(0);
        let gstAmount = new client_1.Prisma.Decimal(0);
        let discountAmount = new client_1.Prisma.Decimal(0);
        const lineItems = dto.items.map((item) => {
            const product = productsById.get(item.productId);
            if (!product) {
                throw new common_1.NotFoundException(`Product ${item.productId} not found`);
            }
            const availableQty = stockByProduct.get(item.productId) ?? 0;
            if (availableQty < item.quantity) {
                throw new common_1.BadRequestException(`Insufficient stock for ${product.name}. Requested ${item.quantity}, available ${availableQty}`);
            }
            const quantity = new client_1.Prisma.Decimal(item.quantity);
            if (!product.dealerPrice || !product.gstRate) {
                throw new common_1.BadRequestException(`Product ${product.name} is missing dealer price/GST configuration.`);
            }
            const dbDealerUnitPrice = new client_1.Prisma.Decimal(product.dealerPrice);
            const gstPct = new client_1.Prisma.Decimal(product.gstRate).div(100);
            const baseLineAmount = dbDealerUnitPrice.mul(quantity);
            const lineGst = baseLineAmount.mul(gstPct);
            const defaultDisc = buyerRole === 'DEALER'
                ? new client_1.Prisma.Decimal(10)
                : new client_1.Prisma.Decimal(5);
            const lineDiscountPct = buyerRole === 'DEALER'
                ? (product.dealerDiscount ?? defaultDisc).div(100)
                : (product.shopkeeperDiscount ?? defaultDisc).div(100);
            const lineDiscount = baseLineAmount.mul(lineDiscountPct);
            const taxableAmount = baseLineAmount.sub(lineDiscount);
            const finalLineAmount = taxableAmount.add(lineGst);
            totalAmount = totalAmount.add(baseLineAmount);
            gstAmount = gstAmount.add(lineGst);
            discountAmount = discountAmount.add(lineDiscount);
            const unitPriceAfterDiscount = taxableAmount.div(quantity);
            return {
                productId: product.id,
                quantity: item.quantity,
                price: unitPriceAfterDiscount,
                gstAmount: lineGst,
                discountAmount: lineDiscount,
                finalAmount: finalLineAmount,
            };
        });
        let bulkShippingAmount = new client_1.Prisma.Decimal(0);
        for (const item of dto.items) {
            const product = productsById.get(item.productId);
            if (!product)
                continue;
            const minQty = product.bulkShippingMinQty ?? 10;
            const fee = product.bulkShippingFee;
            if (fee != null && item.quantity >= minQty) {
                bulkShippingAmount = bulkShippingAmount.add(new client_1.Prisma.Decimal(fee));
            }
        }
        const finalAmount = totalAmount.sub(discountAmount).add(gstAmount).add(bulkShippingAmount);
        return { lineItems, totalAmount, gstAmount, discountAmount, finalAmount };
    }
    findAll(actor) {
        return this.prisma.order.findMany({
            where: this.scopeWhere(actor),
            include: orderInclude,
            orderBy: { createdAt: 'desc' },
        });
    }
    findMine(actor) {
        if (actor.role !== client_1.UserRole.SHOPKEEPER) {
            throw new common_1.BadRequestException('Only shopkeepers can use this endpoint');
        }
        return this.prisma.order.findMany({
            where: { ...this.scopeWhere(actor), shopkeeperId: actor.userId },
            include: orderInclude,
            orderBy: { createdAt: 'desc' },
        });
    }
    findDealer(actor) {
        if (actor.role !== client_1.UserRole.DEALER && actor.role !== client_1.UserRole.ADMIN && actor.role !== client_1.UserRole.EMPLOYEE) {
            throw new common_1.BadRequestException('Only dealers or staff can use this endpoint');
        }
        const where = this.scopeWhere(actor);
        return this.prisma.order.findMany({
            where,
            include: orderInclude,
            orderBy: { createdAt: 'desc' },
        });
    }
    async mySummary(actor) {
        if (actor.role !== client_1.UserRole.SHOPKEEPER) {
            throw new common_1.BadRequestException('Only shopkeepers can use this endpoint');
        }
        const rows = await this.prisma.order.findMany({
            where: { ...this.scopeWhere(actor), shopkeeperId: actor.userId },
            select: { status: true, finalAmount: true },
            orderBy: { createdAt: 'desc' },
        });
        const openOrders = rows.filter((r) => r.status === client_1.OrderStatus.PENDING ||
            r.status === client_1.OrderStatus.DEALER_CONFIRMED ||
            r.status === client_1.OrderStatus.OUT_FOR_DELIVERY).length;
        const inDelivery = rows.filter((r) => r.status === client_1.OrderStatus.OUT_FOR_DELIVERY).length;
        const lastCompleted = rows.find((r) => r.status === client_1.OrderStatus.DELIVERED);
        const invoicesReady = rows.filter((r) => r.status === client_1.OrderStatus.DELIVERED).length;
        return {
            openOrders,
            inDelivery,
            lastTotal: lastCompleted ? Number(lastCompleted.finalAmount) : null,
            invoicesReady,
        };
    }
    async dealerSummary(actor) {
        if (actor.role !== client_1.UserRole.DEALER &&
            actor.role !== client_1.UserRole.ADMIN &&
            actor.role !== client_1.UserRole.EMPLOYEE) {
            throw new common_1.BadRequestException('Only dealers or staff can use this endpoint');
        }
        const rows = await this.prisma.order.findMany({
            where: actor.role === client_1.UserRole.DEALER
                ? {
                    companyId: actor.companyId ?? undefined,
                    kind: client_1.OrderKind.SHOPKEEPER,
                    dealerId: actor.userId,
                }
                : this.scopeWhere(actor),
            select: { status: true, finalAmount: true, createdAt: true },
            orderBy: { createdAt: 'desc' },
        });
        const pendingOrders = rows.filter((r) => r.status === client_1.OrderStatus.PENDING).length;
        const todaysDeliveries = rows.filter((r) => r.status === client_1.OrderStatus.DELIVERED &&
            r.createdAt.toISOString().slice(0, 10) === new Date().toISOString().slice(0, 10)).length;
        const weekStart = Date.now() - 7 * 24 * 3600 * 1000;
        const revenue = rows
            .filter((r) => r.status === client_1.OrderStatus.DELIVERED && r.createdAt.getTime() >= weekStart)
            .reduce((sum, r) => sum + Number(r.finalAmount), 0);
        return {
            pendingOrders,
            todaysDeliveries,
            weeklyRevenue: Number(revenue.toFixed(2)),
        };
    }
    async findOne(orderId, actor) {
        const order = await this.prisma.order.findFirst({
            where: { id: orderId, ...this.scopeWhere(actor) },
            include: orderInclude,
        });
        if (!order) {
            throw new common_1.NotFoundException('Order not found');
        }
        return order;
    }
    async createOrder(dto, actor) {
        if (actor.role !== client_1.UserRole.SHOPKEEPER) {
            throw new common_1.BadRequestException('Only shopkeepers can create orders');
        }
        const shopkeeper = await this.prisma.user.findUnique({
            where: { id: actor.userId },
            include: { area: true },
        });
        if (!shopkeeper?.area?.dealerId) {
            throw new common_1.BadRequestException('No dealer assigned to this shopkeeper. Contact admin.');
        }
        const dealerId = shopkeeper.area.dealerId;
        const productIds = dto.items.map((item) => item.productId);
        const products = await this.prisma.product.findMany({
            where: { id: { in: productIds } },
        });
        const productsById = new Map(products.map((product) => [product.id, product]));
        if (products.length !== productIds.length) {
            throw new common_1.NotFoundException('One or more products were not found');
        }
        const stockRows = await this.prisma.stock.findMany({
            where: { dealerId, productId: { in: productIds } },
        });
        const stockByProduct = new Map(stockRows.map((stock) => [stock.productId, stock.quantity]));
        const { lineItems, totalAmount, gstAmount, discountAmount, finalAmount } = this.buildLineItems(dto, productsById, stockByProduct, 'SHOPKEEPER');
        const created = await this.prisma.order.create({
            data: {
                companyId: actor.companyId,
                shopkeeperId: actor.userId,
                dealerId,
                kind: client_1.OrderKind.SHOPKEEPER,
                totalAmount,
                gstAmount,
                discountAmount,
                finalAmount,
                status: client_1.OrderStatus.PENDING,
                items: {
                    create: lineItems,
                },
            },
            include: orderInclude,
        });
        void this.notifications.sendToUser(dealerId, 'New order', `New order from ${shopkeeper.name} — ${created.id.slice(0, 8)}`);
        return created;
    }
    async createDealerRestockOrder(dto, actor) {
        if (actor.role !== client_1.UserRole.DEALER) {
            throw new common_1.BadRequestException('Only dealers can place restock orders');
        }
        if (!actor.companyId) {
            throw new common_1.BadRequestException('Company scope is required');
        }
        const dealer = await this.prisma.user.findUnique({ where: { id: actor.userId } });
        if (!dealer) {
            throw new common_1.NotFoundException('Dealer not found');
        }
        const warehouse = await this.getCompanyWarehouseUser(actor.companyId);
        const productIds = dto.items.map((item) => item.productId);
        const products = await this.prisma.product.findMany({
            where: { id: { in: productIds }, companyId: actor.companyId },
        });
        const productsById = new Map(products.map((product) => [product.id, product]));
        if (products.length !== productIds.length) {
            throw new common_1.NotFoundException('One or more products were not found');
        }
        const stockRows = await this.prisma.stock.findMany({
            where: { dealerId: warehouse.id, productId: { in: productIds } },
        });
        const stockByProduct = new Map(stockRows.map((stock) => [stock.productId, stock.quantity]));
        const { lineItems, totalAmount, gstAmount, discountAmount, finalAmount } = this.buildLineItems(dto, productsById, stockByProduct, 'DEALER');
        const created = await this.prisma.order.create({
            data: {
                companyId: actor.companyId,
                shopkeeperId: actor.userId,
                dealerId: warehouse.id,
                kind: client_1.OrderKind.DEALER_RESTOCK,
                totalAmount,
                gstAmount,
                discountAmount,
                finalAmount,
                status: client_1.OrderStatus.PENDING,
                items: {
                    create: lineItems,
                },
            },
            include: orderInclude,
        });
        const admins = await this.prisma.user.findMany({
            where: {
                companyId: actor.companyId,
                role: { in: [client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE] },
            },
            select: { id: true },
        });
        for (const staff of admins) {
            void this.notifications.sendToUser(staff.id, 'Dealer restock order', `${dealer.name} placed restock order ${created.id.slice(0, 8)}`);
        }
        return created;
    }
    async createDealerRestockWithPayment(dto, actor) {
        const paymentMode = dto.paymentMode ?? create_order_dto_1.OrderPaymentMode.COD;
        if (paymentMode === create_order_dto_1.OrderPaymentMode.RAZORPAY) {
            this.paymentsService.assertRazorpayKeysPresent();
        }
        const created = await this.createDealerRestockOrder(dto, actor);
        if (paymentMode === create_order_dto_1.OrderPaymentMode.RAZORPAY) {
            const rp = await this.paymentsService.createRazorpayOrder({ orderId: created.id, currency: 'INR' }, actor);
            return {
                orderId: created.id,
                razorpayOrderId: rp.razorpayOrderId,
                amount: rp.amountPaise,
                currency: rp.currency,
                keyId: rp.keyId,
            };
        }
        return {
            orderId: created.id,
            status: created.status,
            message: 'Restock order placed. Company will confirm shortly.',
        };
    }
    async createOrderWithPayment(dto, actor) {
        const paymentMode = dto.paymentMode ?? create_order_dto_1.OrderPaymentMode.COD;
        if (paymentMode === create_order_dto_1.OrderPaymentMode.RAZORPAY) {
            this.paymentsService.assertRazorpayKeysPresent();
        }
        const created = await this.createOrder(dto, actor);
        if (paymentMode === create_order_dto_1.OrderPaymentMode.RAZORPAY) {
            const rp = await this.paymentsService.createRazorpayOrder({ orderId: created.id, currency: 'INR' }, actor);
            return {
                orderId: created.id,
                razorpayOrderId: rp.razorpayOrderId,
                amount: rp.amountPaise,
                currency: rp.currency,
                keyId: rp.keyId,
            };
        }
        return {
            orderId: created.id,
            status: created.status,
            message: 'Order placed successfully. Dealer will confirm shortly.',
        };
    }
    async confirmOrder(orderId, actor) {
        const order = await this.prisma.order.findUnique({
            where: { id: orderId },
            include: { items: true },
        });
        if (!order) {
            throw new common_1.NotFoundException('Order not found');
        }
        if (order.status !== client_1.OrderStatus.PENDING) {
            throw new common_1.BadRequestException('Only pending orders can be confirmed');
        }
        if (order.kind === client_1.OrderKind.DEALER_RESTOCK) {
            if (actor.role !== client_1.UserRole.ADMIN && actor.role !== client_1.UserRole.EMPLOYEE) {
                throw new common_1.ForbiddenException('Only company staff can confirm restock orders');
            }
        }
        else if (actor.role === client_1.UserRole.DEALER && actor.userId !== order.dealerId) {
            throw new common_1.ForbiddenException('Dealer can only confirm own assigned orders');
        }
        await this.prisma.$transaction(async (tx) => {
            for (const item of order.items) {
                const updateResult = await tx.stock.updateMany({
                    where: {
                        dealerId: order.dealerId,
                        productId: item.productId,
                        quantity: { gte: item.quantity },
                    },
                    data: {
                        quantity: { decrement: item.quantity },
                    },
                });
                if (updateResult.count === 0) {
                    throw new common_1.ConflictException('Stock changed, please retry confirmation');
                }
            }
            await tx.order.update({
                where: { id: order.id },
                data: { status: client_1.OrderStatus.DEALER_CONFIRMED },
            });
        });
        await this.invoicesService.generateForOrder(order.id, order.companyId);
        const updated = await this.prisma.order.findUnique({
            where: { id: order.id },
            include: orderInclude,
        });
        void this.notifications.sendToUser(order.shopkeeperId, order.kind === client_1.OrderKind.DEALER_RESTOCK ? 'Restock confirmed' : 'Order confirmed', order.kind === client_1.OrderKind.DEALER_RESTOCK
            ? `Your restock order ${order.id.slice(0, 8)} was confirmed`
            : `Dealer confirmed your order ${order.id.slice(0, 8)}`);
        return updated;
    }
    async markOutForDelivery(orderId, actor) {
        const order = await this.prisma.order.findUnique({ where: { id: orderId } });
        if (!order) {
            throw new common_1.NotFoundException('Order not found');
        }
        if (order.kind === client_1.OrderKind.DEALER_RESTOCK) {
            if (actor.role !== client_1.UserRole.ADMIN && actor.role !== client_1.UserRole.EMPLOYEE) {
                throw new common_1.ForbiddenException('Only company staff can dispatch restock orders');
            }
        }
        else if (actor.role === client_1.UserRole.DEALER && actor.userId !== order.dealerId) {
            throw new common_1.ForbiddenException('Dealer can only update own assigned orders');
        }
        if (order.status !== client_1.OrderStatus.DEALER_CONFIRMED) {
            throw new common_1.BadRequestException('Order must be confirmed before dispatch');
        }
        const out = await this.prisma.order.update({
            where: { id: order.id },
            data: { status: client_1.OrderStatus.OUT_FOR_DELIVERY },
            include: orderInclude,
        });
        void this.notifications.sendToUser(order.shopkeeperId, 'Out for delivery', `Order ${order.id.slice(0, 8)} is on the way`);
        return out;
    }
    async markDelivered(orderId, actor) {
        const order = await this.prisma.order.findUnique({
            where: { id: orderId },
            include: { items: true },
        });
        if (!order) {
            throw new common_1.NotFoundException('Order not found');
        }
        if (order.kind === client_1.OrderKind.DEALER_RESTOCK) {
            if (actor.role !== client_1.UserRole.ADMIN && actor.role !== client_1.UserRole.EMPLOYEE) {
                throw new common_1.ForbiddenException('Only company staff can mark restock orders delivered');
            }
        }
        else if (actor.role === client_1.UserRole.DEALER && actor.userId !== order.dealerId) {
            throw new common_1.ForbiddenException('Dealer can only update own assigned orders');
        }
        if (order.status !== client_1.OrderStatus.OUT_FOR_DELIVERY) {
            throw new common_1.BadRequestException('Only out-for-delivery orders can be marked delivered');
        }
        const done = await this.prisma.$transaction(async (tx) => {
            if (order.kind === client_1.OrderKind.DEALER_RESTOCK) {
                for (const item of order.items) {
                    await tx.stock.upsert({
                        where: {
                            dealerId_productId: {
                                dealerId: order.shopkeeperId,
                                productId: item.productId,
                            },
                        },
                        update: {
                            quantity: { increment: item.quantity },
                        },
                        create: {
                            companyId: order.companyId,
                            dealerId: order.shopkeeperId,
                            productId: item.productId,
                            quantity: item.quantity,
                        },
                    });
                }
            }
            return tx.order.update({
                where: { id: order.id },
                data: { status: client_1.OrderStatus.DELIVERED },
                include: orderInclude,
            });
        });
        void this.notifications.sendToUser(order.shopkeeperId, 'Delivered', `Order ${order.id.slice(0, 8)} has been delivered`);
        return done;
    }
    async cancelOrder(orderId, actor) {
        const order = await this.prisma.order.findUnique({ where: { id: orderId } });
        if (!order) {
            throw new common_1.NotFoundException('Order not found');
        }
        if (order.status === client_1.OrderStatus.DELIVERED || order.status === client_1.OrderStatus.CANCELLED) {
            throw new common_1.BadRequestException('Order cannot be cancelled in this state');
        }
        if (actor.role === client_1.UserRole.SHOPKEEPER && order.shopkeeperId !== actor.userId) {
            throw new common_1.ForbiddenException();
        }
        if (actor.role === client_1.UserRole.DEALER) {
            const isShopkeeperOrder = order.dealerId === actor.userId;
            const isOwnRestock = order.kind === client_1.OrderKind.DEALER_RESTOCK && order.shopkeeperId === actor.userId;
            if (!isShopkeeperOrder && !isOwnRestock) {
                throw new common_1.ForbiddenException();
            }
        }
        if (actor.role === client_1.UserRole.SHOPKEEPER && order.status !== client_1.OrderStatus.PENDING) {
            throw new common_1.BadRequestException('Shopkeeper can cancel only while order is pending');
        }
        if (actor.role === client_1.UserRole.DEALER &&
            order.kind === client_1.OrderKind.DEALER_RESTOCK &&
            order.shopkeeperId === actor.userId &&
            order.status !== client_1.OrderStatus.PENDING) {
            throw new common_1.BadRequestException('Dealer can cancel restock only while pending');
        }
        const updated = await this.prisma.order.update({
            where: { id: order.id },
            data: { status: client_1.OrderStatus.CANCELLED },
            include: orderInclude,
        });
        const peer = actor.userId === order.shopkeeperId ? order.dealerId : order.shopkeeperId;
        void this.notifications.sendToUser(peer, 'Order cancelled', `Order ${order.id.slice(0, 8)} was cancelled`);
        return updated;
    }
    mockPaymentSuccess(orderId) {
        return {
            orderId,
            paymentGateway: 'MOCK',
            status: 'SUCCESS',
            message: 'Payment gateway integration deferred for demo timeline',
        };
    }
};
exports.OrdersService = OrdersService;
exports.OrdersService = OrdersService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        invoices_service_1.InvoicesService,
        notifications_service_1.NotificationsService,
        payments_service_1.PaymentsService])
], OrdersService);
//# sourceMappingURL=orders.service.js.map