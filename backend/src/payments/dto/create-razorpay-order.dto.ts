import { IsNotEmpty, IsOptional, IsString, IsUUID } from 'class-validator';

export class CreateRazorpayOrderDto {
  @IsUUID()
  orderId!: string;

  @IsOptional()
  @IsString()
  @IsNotEmpty()
  currency?: string;
}
