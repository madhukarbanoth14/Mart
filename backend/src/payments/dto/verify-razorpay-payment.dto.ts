import { IsOptional, IsString, IsUUID } from 'class-validator';

export class VerifyRazorpayPaymentDto {
  @IsUUID()
  orderId!: string;

  @IsString()
  razorpayOrderId!: string;

  @IsString()
  razorpayPaymentId!: string;

  /** Optional when the mobile SDK omits it; server will confirm via Razorpay API. */
  @IsOptional()
  @IsString()
  razorpaySignature?: string;
}
