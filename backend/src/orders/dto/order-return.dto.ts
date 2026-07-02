import { IsOptional, IsString, MinLength } from 'class-validator';

export class OrderReturnRequestDto {
  @IsString()
  @MinLength(3)
  reason!: string;
}

export class OrderReturnRejectDto {
  @IsOptional()
  @IsString()
  note?: string;
}
