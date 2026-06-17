import { BrandType, ProductShelf } from '@prisma/client';
import {
  IsEnum,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  IsUUID,
  Max,
  Min,
} from 'class-validator';

export class CreateProductDto {
  @IsString()
  @IsNotEmpty()
  name!: string;

  @IsEnum(BrandType)
  brandType!: BrandType;

  @IsOptional()
  @IsUUID()
  brandId?: string;

  @IsOptional()
  @IsString()
  imageUrl?: string;

  @IsEnum(ProductShelf)
  shelf!: ProductShelf;

  @IsNumber()
  @Min(0)
  basePrice!: number;

  @IsNumber()
  @Min(0)
  @Max(100)
  gstPercentage!: number;

  @IsNumber()
  @Min(0)
  @Max(100)
  dealerDiscount!: number;

  @IsNumber()
  @Min(0)
  @Max(100)
  shopkeeperDiscount!: number;

  @IsOptional()
  @IsNumber()
  @Min(0)
  bulkShippingFee?: number;

  @IsOptional()
  @IsNumber()
  @Min(1)
  @Max(100000)
  bulkShippingMinQty?: number;
}
