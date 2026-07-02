import { CommissionRuleType, SettlementPaymentMethod, SettlementStatus } from '@prisma/client';
import { IsDateString, IsEnum, IsNumber, IsOptional, IsString, IsUUID, Min } from 'class-validator';

export class UpsertCommissionRuleDto {
  @IsEnum(CommissionRuleType)
  ruleType!: CommissionRuleType;

  @IsNumber()
  @Min(0)
  rate!: number;

  @IsOptional()
  @IsUUID()
  dealerId?: string;

  @IsOptional()
  @IsUUID()
  productId?: string;
}

export class GenerateSettlementDto {
  @IsUUID()
  dealerId!: string;

  @IsDateString()
  startDate!: string;

  @IsDateString()
  endDate!: string;
}

export class RecordSettlementPaymentDto {
  @IsNumber()
  @Min(0.01)
  amount!: number;

  @IsEnum(SettlementPaymentMethod)
  paymentMethod!: SettlementPaymentMethod;

  @IsOptional()
  @IsString()
  utrNumber?: string;

  @IsOptional()
  @IsString()
  transactionReference?: string;

  @IsDateString()
  paymentDate!: string;

  @IsOptional()
  @IsString()
  remarks?: string;
}

export class FinanceQueryDto {
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
  @IsUUID()
  dealerId?: string;

  @IsOptional()
  @IsEnum(SettlementStatus)
  status?: SettlementStatus;
}
