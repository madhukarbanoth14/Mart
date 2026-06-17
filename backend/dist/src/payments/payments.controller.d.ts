import type { AuthUser } from '../auth/types/auth-user.type';
import { CreateRazorpayOrderDto } from './dto/create-razorpay-order.dto';
import { VerifyRazorpayPaymentDto } from './dto/verify-razorpay-payment.dto';
import { PaymentsService } from './payments.service';
export declare class PaymentsController {
    private readonly paymentsService;
    constructor(paymentsService: PaymentsService);
    createRazorpayOrder(dto: CreateRazorpayOrderDto, user: AuthUser): Promise<{
        orderId: string;
        amountPaise: number;
        currency: string;
        keyId: string;
        razorpayOrderId: string;
        paymentRecordId: string;
    }>;
    createOrderAlias(dto: CreateRazorpayOrderDto, user: AuthUser): Promise<{
        orderId: string;
        amountPaise: number;
        currency: string;
        keyId: string;
        razorpayOrderId: string;
        paymentRecordId: string;
    }>;
    createAliasV2(dto: CreateRazorpayOrderDto, user: AuthUser): Promise<{
        orderId: string;
        amountPaise: number;
        currency: string;
        keyId: string;
        razorpayOrderId: string;
        paymentRecordId: string;
    }>;
    verifyRazorpayPayment(dto: VerifyRazorpayPaymentDto, user: AuthUser): Promise<{
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
    verifyAlias(dto: VerifyRazorpayPaymentDto, user: AuthUser): Promise<{
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
