import {
  ReturnReason,
  ReturnRequestStatus,
  RefundMethod,
  RefundRequestStatus,
} from '@prisma/client';
import {
  ArrayMinSize,
  IsArray,
  IsDateString,
  IsEnum,
  IsInt,
  IsOptional,
  IsString,
  IsUUID,
  Min,
  MinLength,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';

export class ReturnItemDto {
  @IsUUID()
  productId!: string;

  @IsInt()
  @Min(1)
  quantity!: number;
}

export class CreateReturnRequestDto {
  @IsEnum(ReturnReason)
  reason!: ReturnReason;

  @IsOptional()
  @IsString()
  reasonText?: string;

  @IsOptional()
  @IsString()
  comments?: string;

  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  imageUrls?: string[];

  @IsArray()
  @ArrayMinSize(1)
  @ValidateNested({ each: true })
  @Type(() => ReturnItemDto)
  items!: ReturnItemDto[];
}

export class ReturnActionDto {
  @IsOptional()
  @IsString()
  remarks?: string;
}

export class RaiseRefundRequestDto {
  @IsOptional()
  @IsString()
  remarks?: string;
}

export class ProcessRefundDto {
  @IsEnum(RefundMethod)
  refundMethod!: RefundMethod;

  @IsString()
  @MinLength(3)
  transactionReference!: string;

  @IsOptional()
  @IsString()
  remarks?: string;

  @IsOptional()
  @IsDateString()
  refundDate?: string;
}

export class RefundRejectDto {
  @IsOptional()
  @IsString()
  remarks?: string;
}

export class ReturnsQueryDto {
  @IsOptional()
  @IsEnum(ReturnRequestStatus)
  status?: ReturnRequestStatus;

  @IsOptional()
  @IsUUID()
  shopkeeperId?: string;

  @IsOptional()
  @IsString()
  area?: string;

  @IsOptional()
  @IsDateString()
  startDate?: string;

  @IsOptional()
  @IsDateString()
  endDate?: string;
}

export class RefundsQueryDto {
  @IsOptional()
  @IsEnum(RefundRequestStatus)
  status?: RefundRequestStatus;

  @IsOptional()
  @IsUUID()
  dealerId?: string;

  @IsOptional()
  @IsDateString()
  startDate?: string;

  @IsOptional()
  @IsDateString()
  endDate?: string;
}

export class DealerFinanceQueryDto {
  @IsOptional()
  @IsString()
  period?: 'today' | 'yesterday' | 'week' | 'month' | 'year' | 'custom';

  @IsOptional()
  @IsDateString()
  startDate?: string;

  @IsOptional()
  @IsDateString()
  endDate?: string;

  @IsOptional()
  @IsString()
  shopkeeper?: string;

  @IsOptional()
  @IsString()
  area?: string;

  @IsOptional()
  @IsString()
  orderStatus?: string;
}
