import { IsString, IsUUID } from 'class-validator';

export class VerifyRazorpayPaymentDto {
  @IsUUID()
  orderId!: string;

  @IsString()
  razorpayOrderId!: string;

  @IsString()
  razorpayPaymentId!: string;

  @IsString()
  razorpaySignature!: string;
}
