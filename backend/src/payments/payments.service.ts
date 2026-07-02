import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import {
  OrderKind,
  OrderPaymentStatus,
  OrderStatus,
  PaymentProvider,
  PaymentStatus,
  Prisma,
  UserRole,
} from '@prisma/client';
import * as crypto from 'crypto';
import Razorpay from 'razorpay';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { CreateRazorpayOrderDto } from './dto/create-razorpay-order.dto';
import { VerifyRazorpayPaymentDto } from './dto/verify-razorpay-payment.dto';

@Injectable()
export class PaymentsService {
  private readonly razorpay: Razorpay | null;
  private readonly razorpayKeyId: string;
  private readonly razorpayKeySecret: string;
  private readonly webhookSecret: string;

  constructor(
    private readonly prisma: PrismaService,
    private readonly configService: ConfigService,
  ) {
    this.razorpayKeyId = this.configService.get<string>('razorpay.keyId', '');
    this.razorpayKeySecret = this.configService.get<string>(
      'razorpay.keySecret',
      '',
    );
    this.webhookSecret = this.configService.get<string>(
      'razorpay.webhookSecret',
      '',
    );
    this.razorpay =
      this.razorpayKeyId && this.razorpayKeySecret
        ? new Razorpay({
            key_id: this.razorpayKeyId,
            key_secret: this.razorpayKeySecret,
          })
        : null;
  }

  private ensureRazorpayConfigured() {
    if (!this.razorpayKeyId || !this.razorpayKeySecret) {
      throw new BadRequestException(
        'Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.',
      );
    }
  }

  /** Call before creating an order that must be paid via Razorpay (fail fast, no orphan order). */
  assertRazorpayKeysPresent(): void {
    this.ensureRazorpayConfigured();
  }

  private getRazorpayClient(): Razorpay {
    this.ensureRazorpayConfigured();
    if (!this.razorpay) {
      throw new BadRequestException(
        'Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.',
      );
    }
    return this.razorpay;
  }

  private async loadOrderForActor(orderId: string, actor: AuthUser) {
    const order = await this.prisma.order.findUnique({
      where: { id: orderId },
      include: { payments: true },
    });
    if (!order) {
      throw new NotFoundException('Order not found');
    }
    if (actor.companyId && order.companyId && actor.companyId !== order.companyId) {
      throw new ForbiddenException('Order is outside your company scope');
    }
    if (actor.role === UserRole.SHOPKEEPER && order.shopkeeperId !== actor.userId) {
      throw new ForbiddenException('Shopkeeper can only pay own orders');
    }
    if (actor.role === UserRole.DEALER && !this.dealerCanAccessOrder(order, actor.userId)) {
      throw new ForbiddenException('Dealer can only access assigned orders');
    }
    return order;
  }

  /** Shopkeeper orders use dealerId; dealer restock orders store the dealer on shopkeeperId. */
  private dealerCanAccessOrder(
    order: { dealerId: string; shopkeeperId: string; kind: OrderKind },
    dealerUserId: string,
  ): boolean {
    if (order.dealerId === dealerUserId) {
      return true;
    }
    return (
      order.kind === OrderKind.DEALER_RESTOCK && order.shopkeeperId === dealerUserId
    );
  }

  async createRazorpayOrder(
    dto: CreateRazorpayOrderDto,
    actor: AuthUser,
  ): Promise<{
    orderId: string;
    amountPaise: number;
    currency: string;
    keyId: string;
    razorpayOrderId: string;
    paymentRecordId: string;
  }> {
    const razorpay = this.getRazorpayClient();
    const order = await this.loadOrderForActor(dto.orderId, actor);
    const payable: OrderStatus[] = [
      OrderStatus.PENDING,
      OrderStatus.DEALER_CONFIRMED,
      OrderStatus.OUT_FOR_DELIVERY,
    ];
    if (!payable.includes(order.status)) {
      throw new BadRequestException('Payment is allowed only for active orders');
    }
    if (order.paymentStatus === OrderPaymentStatus.PAID) {
      throw new BadRequestException('Order is already paid');
    }

    const amountPaise = Math.round(Number(order.finalAmount) * 100);
    if (!Number.isFinite(amountPaise) || amountPaise <= 0) {
      throw new BadRequestException('Invalid order amount for payment');
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
        provider: PaymentProvider.RAZORPAY,
        status: PaymentStatus.INITIATED,
        amount: new Prisma.Decimal(order.finalAmount.toString()),
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

  async verifyRazorpayPayment(dto: VerifyRazorpayPaymentDto, actor: AuthUser) {
    this.ensureRazorpayConfigured();
    const order = await this.loadOrderForActor(dto.orderId, actor);
    const payment = await this.prisma.payment.findFirst({
      where: {
        orderId: order.id,
        provider: PaymentProvider.RAZORPAY,
        gatewayOrderId: dto.razorpayOrderId,
      },
    });
    if (!payment) {
      throw new NotFoundException('Payment initiation record not found');
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
            status: PaymentStatus.FAILED,
            gatewayPaymentId: dto.razorpayPaymentId,
            gatewaySignature: signature,
            failureReason: 'INVALID_SIGNATURE',
          },
        });
        await this.prisma.order.update({
          where: { id: order.id },
          data: { paymentStatus: OrderPaymentStatus.FAILED },
        });
        throw new BadRequestException('Invalid Razorpay payment signature');
      }
    } else {
      const razorpay = this.getRazorpayClient();
      const fetched = (await razorpay.payments.fetch(
        dto.razorpayPaymentId,
      )) as {
        order_id?: string;
        status?: string;
      };
      if (fetched.order_id !== dto.razorpayOrderId) {
        throw new BadRequestException(
          'Payment is not linked to this Razorpay order',
        );
      }
      const okStatus = fetched.status === 'captured' || fetched.status === 'authorized';
      if (!okStatus) {
        throw new BadRequestException(
          `Payment is not complete (status: ${fetched.status ?? 'unknown'})`,
        );
      }
    }

    const [updatedPayment, updatedOrder] = await this.prisma.$transaction([
      this.prisma.payment.update({
        where: { id: payment.id },
        data: {
          status: PaymentStatus.SUCCESS,
          gatewayPaymentId: dto.razorpayPaymentId,
          gatewaySignature: dto.razorpaySignature,
          failureReason: null,
        },
      }),
      this.prisma.order.update({
        where: { id: order.id },
        data: { paymentStatus: OrderPaymentStatus.PAID },
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

  async handleRazorpayWebhook(signature: string | undefined, payload: unknown) {
    if (!this.webhookSecret) {
      throw new BadRequestException('RAZORPAY_WEBHOOK_SECRET is not configured');
    }
    if (!signature) {
      throw new BadRequestException('Missing x-razorpay-signature header');
    }

    const rawPayload =
      typeof payload === 'string' ? payload : JSON.stringify(payload ?? {});
    const digest = crypto
      .createHmac('sha256', this.webhookSecret)
      .update(rawPayload)
      .digest('hex');
    if (digest !== signature) {
      throw new BadRequestException('Invalid webhook signature');
    }

    const parsed =
      typeof payload === 'string'
        ? (JSON.parse(payload) as {
            event?: string;
            payload?: {
              payment?: { entity?: { order_id?: string; id?: string } };
            };
          })
        : (payload as {
            event?: string;
            payload?: {
              payment?: { entity?: { order_id?: string; id?: string } };
            };
          });
    const event = parsed.event ?? '';
    const gatewayOrderId = parsed.payload?.payment?.entity?.order_id;
    const gatewayPaymentId = parsed.payload?.payment?.entity?.id;
    if (!gatewayOrderId) {
      return { received: true, ignored: true, reason: 'NO_ORDER_ID' };
    }

    const payment = await this.prisma.payment.findFirst({
      where: {
        provider: PaymentProvider.RAZORPAY,
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
            status: PaymentStatus.SUCCESS,
            gatewayPaymentId: gatewayPaymentId ?? payment.gatewayPaymentId,
            failureReason: null,
          },
        }),
        this.prisma.order.update({
          where: { id: payment.orderId },
          data: { paymentStatus: OrderPaymentStatus.PAID },
        }),
      ]);
    } else if (event === 'payment.failed') {
      await this.prisma.$transaction([
        this.prisma.payment.update({
          where: { id: payment.id },
          data: {
            status: PaymentStatus.FAILED,
            gatewayPaymentId: gatewayPaymentId ?? payment.gatewayPaymentId,
            failureReason: 'PAYMENT_FAILED_WEBHOOK',
          },
        }),
        this.prisma.order.update({
          where: { id: payment.orderId },
          data: { paymentStatus: OrderPaymentStatus.FAILED },
        }),
      ]);
    }

    return { received: true, event };
  }

  async refundOrderPayment(
    orderId: string,
    actor: AuthUser,
    amountInr?: number,
    options?: { skipOrderUpdate?: boolean },
  ) {
    const order = await this.loadOrderForActor(orderId, actor);
    if (order.paymentStatus === OrderPaymentStatus.REFUNDED) {
      throw new BadRequestException('Order is already refunded');
    }
    if (order.paymentStatus !== OrderPaymentStatus.PAID) {
      throw new BadRequestException('Only paid orders can be refunded');
    }

    const payment = await this.prisma.payment.findFirst({
      where: {
        orderId: order.id,
        provider: PaymentProvider.RAZORPAY,
        status: PaymentStatus.SUCCESS,
      },
      orderBy: { createdAt: 'desc' },
    });

    const refundAmount = amountInr ?? Number(order.finalAmount);
    const amountPaise = Math.round(refundAmount * 100);
    if (!Number.isFinite(amountPaise) || amountPaise <= 0) {
      throw new BadRequestException('Invalid order amount for refund');
    }

    let gatewayRefundId: string | null = null;

    if (payment?.gatewayPaymentId) {
      const refund = await this.getRazorpayClient().payments.refund(
        payment.gatewayPaymentId,
        { amount: amountPaise },
      );
      gatewayRefundId = refund.id ?? null;
      if (!gatewayRefundId) {
        throw new BadRequestException('Razorpay refund did not return a refund id');
      }
    }

    const now = new Date();
    const paymentUpdate = payment
      ? this.prisma.payment.update({
          where: { id: payment.id },
          data: {
            status: PaymentStatus.REFUNDED,
            gatewayRefundId,
            failureReason: null,
          },
        })
      : this.prisma.payment.create({
          data: {
            companyId: order.companyId,
            orderId: order.id,
            provider: PaymentProvider.RAZORPAY,
            status: PaymentStatus.REFUNDED,
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
          status: PaymentStatus.REFUNDED,
        },
      };
    }

    const [updatedPayment, updatedOrder] = await this.prisma.$transaction([
      paymentUpdate,
      this.prisma.order.update({
        where: { id: order.id },
        data: {
          paymentStatus: OrderPaymentStatus.REFUNDED,
          refundedAt: now,
        },
      }),
    ]);

    return {
      orderId: updatedOrder.id,
      paymentStatus: OrderPaymentStatus.REFUNDED,
      refundedAt: now,
      refund: {
        id: updatedPayment.id,
        gatewayRefundId,
        status: PaymentStatus.REFUNDED,
      },
    };
  }

  private decimal(n: number) {
    return new Prisma.Decimal(n.toFixed(2));
  }
}
