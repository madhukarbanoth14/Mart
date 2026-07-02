"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.PaymentsService = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const client_1 = require("@prisma/client");
const crypto = __importStar(require("crypto"));
const razorpay_1 = __importDefault(require("razorpay"));
const prisma_service_1 = require("../prisma/prisma.service");
let PaymentsService = class PaymentsService {
    prisma;
    configService;
    razorpay;
    razorpayKeyId;
    razorpayKeySecret;
    webhookSecret;
    constructor(prisma, configService) {
        this.prisma = prisma;
        this.configService = configService;
        this.razorpayKeyId = this.configService.get('razorpay.keyId', '');
        this.razorpayKeySecret = this.configService.get('razorpay.keySecret', '');
        this.webhookSecret = this.configService.get('razorpay.webhookSecret', '');
        this.razorpay =
            this.razorpayKeyId && this.razorpayKeySecret
                ? new razorpay_1.default({
                    key_id: this.razorpayKeyId,
                    key_secret: this.razorpayKeySecret,
                })
                : null;
    }
    ensureRazorpayConfigured() {
        if (!this.razorpayKeyId || !this.razorpayKeySecret) {
            throw new common_1.BadRequestException('Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.');
        }
    }
    assertRazorpayKeysPresent() {
        this.ensureRazorpayConfigured();
    }
    getRazorpayClient() {
        this.ensureRazorpayConfigured();
        if (!this.razorpay) {
            throw new common_1.BadRequestException('Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.');
        }
        return this.razorpay;
    }
    async loadOrderForActor(orderId, actor) {
        const order = await this.prisma.order.findUnique({
            where: { id: orderId },
            include: { payments: true },
        });
        if (!order) {
            throw new common_1.NotFoundException('Order not found');
        }
        if (actor.companyId && order.companyId && actor.companyId !== order.companyId) {
            throw new common_1.ForbiddenException('Order is outside your company scope');
        }
        if (actor.role === client_1.UserRole.SHOPKEEPER && order.shopkeeperId !== actor.userId) {
            throw new common_1.ForbiddenException('Shopkeeper can only pay own orders');
        }
        if (actor.role === client_1.UserRole.DEALER && !this.dealerCanAccessOrder(order, actor.userId)) {
            throw new common_1.ForbiddenException('Dealer can only access assigned orders');
        }
        return order;
    }
    dealerCanAccessOrder(order, dealerUserId) {
        if (order.dealerId === dealerUserId) {
            return true;
        }
        return (order.kind === client_1.OrderKind.DEALER_RESTOCK && order.shopkeeperId === dealerUserId);
    }
    async createRazorpayOrder(dto, actor) {
        const razorpay = this.getRazorpayClient();
        const order = await this.loadOrderForActor(dto.orderId, actor);
        const payable = [
            client_1.OrderStatus.PENDING,
            client_1.OrderStatus.DEALER_CONFIRMED,
            client_1.OrderStatus.OUT_FOR_DELIVERY,
        ];
        if (!payable.includes(order.status)) {
            throw new common_1.BadRequestException('Payment is allowed only for active orders');
        }
        if (order.paymentStatus === client_1.OrderPaymentStatus.PAID) {
            throw new common_1.BadRequestException('Order is already paid');
        }
        const amountPaise = Math.round(Number(order.finalAmount) * 100);
        if (!Number.isFinite(amountPaise) || amountPaise <= 0) {
            throw new common_1.BadRequestException('Invalid order amount for payment');
        }
        const currency = dto.currency?.trim().toUpperCase() || 'INR';
        const receipt = `knsr_${order.id.replace(/-/g, '').slice(0, 20)}`;
        const created = await razorpay.orders.create({
            amount: amountPaise,
            currency,
            receipt,
            notes: {
                orderId: order.id,
                companyId: order.companyId ?? '',
            },
        });
        const payment = await this.prisma.payment.create({
            data: {
                companyId: order.companyId,
                orderId: order.id,
                provider: client_1.PaymentProvider.RAZORPAY,
                status: client_1.PaymentStatus.INITIATED,
                amount: new client_1.Prisma.Decimal(order.finalAmount.toString()),
                currency,
                gatewayOrderId: created.id,
                metadata: JSON.parse(JSON.stringify({ razorpayOrder: created })),
            },
        });
        return {
            orderId: order.id,
            amountPaise,
            currency,
            keyId: this.razorpayKeyId,
            razorpayOrderId: created.id,
            paymentRecordId: payment.id,
        };
    }
    async verifyRazorpayPayment(dto, actor) {
        this.ensureRazorpayConfigured();
        const order = await this.loadOrderForActor(dto.orderId, actor);
        const payment = await this.prisma.payment.findFirst({
            where: {
                orderId: order.id,
                provider: client_1.PaymentProvider.RAZORPAY,
                gatewayOrderId: dto.razorpayOrderId,
            },
        });
        if (!payment) {
            throw new common_1.NotFoundException('Payment initiation record not found');
        }
        const signature = dto.razorpaySignature?.trim() ?? '';
        if (signature) {
            const digest = crypto
                .createHmac('sha256', this.razorpayKeySecret)
                .update(`${dto.razorpayOrderId}|${dto.razorpayPaymentId}`)
                .digest('hex');
            if (digest !== signature) {
                await this.prisma.payment.update({
                    where: { id: payment.id },
                    data: {
                        status: client_1.PaymentStatus.FAILED,
                        gatewayPaymentId: dto.razorpayPaymentId,
                        gatewaySignature: signature,
                        failureReason: 'INVALID_SIGNATURE',
                    },
                });
                await this.prisma.order.update({
                    where: { id: order.id },
                    data: { paymentStatus: client_1.OrderPaymentStatus.FAILED },
                });
                throw new common_1.BadRequestException('Invalid Razorpay payment signature');
            }
        }
        else {
            const razorpay = this.getRazorpayClient();
            const fetched = (await razorpay.payments.fetch(dto.razorpayPaymentId));
            if (fetched.order_id !== dto.razorpayOrderId) {
                throw new common_1.BadRequestException('Payment is not linked to this Razorpay order');
            }
            const okStatus = fetched.status === 'captured' || fetched.status === 'authorized';
            if (!okStatus) {
                throw new common_1.BadRequestException(`Payment is not complete (status: ${fetched.status ?? 'unknown'})`);
            }
        }
        const [updatedPayment, updatedOrder] = await this.prisma.$transaction([
            this.prisma.payment.update({
                where: { id: payment.id },
                data: {
                    status: client_1.PaymentStatus.SUCCESS,
                    gatewayPaymentId: dto.razorpayPaymentId,
                    gatewaySignature: dto.razorpaySignature,
                    failureReason: null,
                },
            }),
            this.prisma.order.update({
                where: { id: order.id },
                data: { paymentStatus: client_1.OrderPaymentStatus.PAID },
            }),
        ]);
        return {
            verified: true,
            orderId: updatedOrder.id,
            paymentStatus: updatedOrder.paymentStatus,
            payment: {
                id: updatedPayment.id,
                provider: updatedPayment.provider,
                status: updatedPayment.status,
                gatewayOrderId: updatedPayment.gatewayOrderId,
                gatewayPaymentId: updatedPayment.gatewayPaymentId,
            },
        };
    }
    async handleRazorpayWebhook(signature, payload) {
        if (!this.webhookSecret) {
            throw new common_1.BadRequestException('RAZORPAY_WEBHOOK_SECRET is not configured');
        }
        if (!signature) {
            throw new common_1.BadRequestException('Missing x-razorpay-signature header');
        }
        const rawPayload = typeof payload === 'string' ? payload : JSON.stringify(payload ?? {});
        const digest = crypto
            .createHmac('sha256', this.webhookSecret)
            .update(rawPayload)
            .digest('hex');
        if (digest !== signature) {
            throw new common_1.BadRequestException('Invalid webhook signature');
        }
        const parsed = typeof payload === 'string'
            ? JSON.parse(payload)
            : payload;
        const event = parsed.event ?? '';
        const gatewayOrderId = parsed.payload?.payment?.entity?.order_id;
        const gatewayPaymentId = parsed.payload?.payment?.entity?.id;
        if (!gatewayOrderId) {
            return { received: true, ignored: true, reason: 'NO_ORDER_ID' };
        }
        const payment = await this.prisma.payment.findFirst({
            where: {
                provider: client_1.PaymentProvider.RAZORPAY,
                gatewayOrderId,
            },
        });
        if (!payment) {
            return { received: true, ignored: true, reason: 'PAYMENT_NOT_FOUND' };
        }
        if (event === 'payment.captured') {
            await this.prisma.$transaction([
                this.prisma.payment.update({
                    where: { id: payment.id },
                    data: {
                        status: client_1.PaymentStatus.SUCCESS,
                        gatewayPaymentId: gatewayPaymentId ?? payment.gatewayPaymentId,
                        failureReason: null,
                    },
                }),
                this.prisma.order.update({
                    where: { id: payment.orderId },
                    data: { paymentStatus: client_1.OrderPaymentStatus.PAID },
                }),
            ]);
        }
        else if (event === 'payment.failed') {
            await this.prisma.$transaction([
                this.prisma.payment.update({
                    where: { id: payment.id },
                    data: {
                        status: client_1.PaymentStatus.FAILED,
                        gatewayPaymentId: gatewayPaymentId ?? payment.gatewayPaymentId,
                        failureReason: 'PAYMENT_FAILED_WEBHOOK',
                    },
                }),
                this.prisma.order.update({
                    where: { id: payment.orderId },
                    data: { paymentStatus: client_1.OrderPaymentStatus.FAILED },
                }),
            ]);
        }
        return { received: true, event };
    }
    async refundOrderPayment(orderId, actor, amountInr, options) {
        const order = await this.loadOrderForActor(orderId, actor);
        if (order.paymentStatus === client_1.OrderPaymentStatus.REFUNDED) {
            throw new common_1.BadRequestException('Order is already refunded');
        }
        if (order.paymentStatus !== client_1.OrderPaymentStatus.PAID) {
            throw new common_1.BadRequestException('Only paid orders can be refunded');
        }
        const payment = await this.prisma.payment.findFirst({
            where: {
                orderId: order.id,
                provider: client_1.PaymentProvider.RAZORPAY,
                status: client_1.PaymentStatus.SUCCESS,
            },
            orderBy: { createdAt: 'desc' },
        });
        const refundAmount = amountInr ?? Number(order.finalAmount);
        const amountPaise = Math.round(refundAmount * 100);
        if (!Number.isFinite(amountPaise) || amountPaise <= 0) {
            throw new common_1.BadRequestException('Invalid order amount for refund');
        }
        let gatewayRefundId = null;
        if (payment?.gatewayPaymentId) {
            const refund = await this.getRazorpayClient().payments.refund(payment.gatewayPaymentId, { amount: amountPaise });
            gatewayRefundId = refund.id ?? null;
            if (!gatewayRefundId) {
                throw new common_1.BadRequestException('Razorpay refund did not return a refund id');
            }
        }
        const now = new Date();
        const paymentUpdate = payment
            ? this.prisma.payment.update({
                where: { id: payment.id },
                data: {
                    status: client_1.PaymentStatus.REFUNDED,
                    gatewayRefundId,
                    failureReason: null,
                },
            })
            : this.prisma.payment.create({
                data: {
                    companyId: order.companyId,
                    orderId: order.id,
                    provider: client_1.PaymentProvider.RAZORPAY,
                    status: client_1.PaymentStatus.REFUNDED,
                    amount: this.decimal(refundAmount),
                    currency: 'INR',
                    gatewayRefundId,
                },
            });
        if (options?.skipOrderUpdate) {
            const updatedPayment = await paymentUpdate;
            return {
                orderId: order.id,
                paymentStatus: order.paymentStatus,
                refundedAt: now,
                refund: {
                    id: updatedPayment.id,
                    gatewayRefundId,
                    status: client_1.PaymentStatus.REFUNDED,
                },
            };
        }
        const [updatedPayment, updatedOrder] = await this.prisma.$transaction([
            paymentUpdate,
            this.prisma.order.update({
                where: { id: order.id },
                data: {
                    paymentStatus: client_1.OrderPaymentStatus.REFUNDED,
                    refundedAt: now,
                },
            }),
        ]);
        return {
            orderId: updatedOrder.id,
            paymentStatus: client_1.OrderPaymentStatus.REFUNDED,
            refundedAt: now,
            refund: {
                id: updatedPayment.id,
                gatewayRefundId,
                status: client_1.PaymentStatus.REFUNDED,
            },
        };
    }
    decimal(n) {
        return new client_1.Prisma.Decimal(n.toFixed(2));
    }
};
exports.PaymentsService = PaymentsService;
exports.PaymentsService = PaymentsService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        config_1.ConfigService])
], PaymentsService);
//# sourceMappingURL=payments.service.js.map