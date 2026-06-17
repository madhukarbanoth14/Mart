import { ConfigService } from '@nestjs/config';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { CreateRazorpayOrderDto } from './dto/create-razorpay-order.dto';
import { VerifyRazorpayPaymentDto } from './dto/verify-razorpay-payment.dto';
export declare class PaymentsService {
    private readonly prisma;
    private readonly configService;
    private readonly razorpay;
    private readonly razorpayKeyId;
    private readonly razorpayKeySecret;
    private readonly webhookSecret;
    constructor(prisma: PrismaService, configService: ConfigService);
    private ensureRazorpayConfigured;
    assertRazorpayKeysPresent(): void;
    private getRazorpayClient;
    private loadOrderForActor;
    createRazorpayOrder(dto: CreateRazorpayOrderDto, actor: AuthUser): Promise<{
        orderId: string;
        amountPaise: number;
        currency: string;
        keyId: string;
        razorpayOrderId: string;
        paymentRecordId: string;
    }>;
    verifyRazorpayPayment(dto: VerifyRazorpayPaymentDto, actor: AuthUser): Promise<{
        verified: boolean;
        orderId: string;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
        payment: {
            id: string;
            provider: "RAZORPAY";
            status: import("@prisma/client").$Enums.PaymentStatus;
            gatewayOrderId: string | null;
            gatewayPaymentId: string | null;
        };
    }>;
    handleRazorpayWebhook(signature: string | undefined, payload: unknown): Promise<{
        received: boolean;
        ignored: boolean;
        reason: string;
        event?: undefined;
    } | {
        received: boolean;
        event: string;
        ignored?: undefined;
        reason?: undefined;
    }>;
}
