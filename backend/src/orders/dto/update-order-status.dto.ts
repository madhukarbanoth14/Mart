import { OrderStatus } from '@prisma/client';
import { IsEnum, IsOptional } from 'class-validator';

/** Reserved for PATCH endpoints that accept a body (currently routes use dedicated actions). */
export class UpdateOrderStatusDto {
  @IsOptional()
  @IsEnum(OrderStatus)
  status?: OrderStatus;
}
