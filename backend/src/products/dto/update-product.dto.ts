import { BrandType, ProductShelf } from '@prisma/client';
import {
  IsEnum,
  IsNumber,
  IsOptional,
  IsString,
  IsUUID,
  Max,
  Min,
} from 'class-validator';

export class UpdateProductDto {
  @IsOptional()
  @IsString()
  name?: string;

  @IsOptional()
  @IsEnum(BrandType)
  brandType?: BrandType;

  @IsOptional()
  @IsUUID()
  brandId?: string;

  @IsOptional()
  @IsString()
  imageUrl?: string;

  @IsOptional()
  @IsEnum(ProductShelf)
  shelf?: ProductShelf;

  @IsOptional()
  @IsNumber()
  @Min(0)
  basePrice?: number;

  @IsOptional()
  @IsNumber()
  @Min(0)
  @Max(100)
  gstPercentage?: number;

  @IsOptional()
  @IsNumber()
  @Min(0)
  @Max(100)
  dealerDiscount?: number;

  @IsOptional()
  @IsNumber()
  @Min(0)
  @Max(100)
  shopkeeperDiscount?: number;

  @IsOptional()
  @IsNumber()
  @Min(0)
  bulkShippingFee?: number | null;

  @IsOptional()
  @IsNumber()
  @Min(1)
  @Max(100000)
  bulkShippingMinQty?: number;
}
