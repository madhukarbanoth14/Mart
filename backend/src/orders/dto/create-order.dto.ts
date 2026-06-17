import {
  ArrayMinSize,
  IsArray,
  IsEnum,
  IsInt,
  IsOptional,
  IsString,
  Min,
  ValidateNested,
} from 'class-validator';
import { NestedType } from '../../common/nested-type.decorator';

export class CreateOrderItemDto {
  @IsString()
  productId!: string;

  @IsInt()
  @Min(1)
  quantity!: number;
}

export class CreateOrderDto {
  @IsArray()
  @ArrayMinSize(1)
  @ValidateNested({ each: true })
  @NestedType(CreateOrderItemDto)
  items!: CreateOrderItemDto[];
}

export enum OrderPaymentMode {
  COD = 'COD',
  RAZORPAY = 'RAZORPAY',
}

export class CreateOrderWithPaymentDto extends CreateOrderDto {
  @IsOptional()
  @IsEnum(OrderPaymentMode)
  paymentMode?: OrderPaymentMode;
}
