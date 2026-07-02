import { IsInt, IsString, Min, MinLength } from 'class-validator';

export class UpsertStockDto {
  @IsString()
  @MinLength(1)
  productId!: string;

  @IsInt()
  @Min(0)
  quantity!: number;
}
