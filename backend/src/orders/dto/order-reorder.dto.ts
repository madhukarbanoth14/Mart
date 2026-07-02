export class ReorderPreviewItemDto {
  productId!: string;
  quantity!: number;
  product!: Record<string, unknown>;
}

export class ReorderSkippedItemDto {
  productId!: string;
  productName?: string;
  reason!: 'deleted' | 'inactive' | 'out_of_stock';
}

export class ReorderPreviewDto {
  items!: ReorderPreviewItemDto[];
  warnings!: string[];
  skipped!: ReorderSkippedItemDto[];
}
