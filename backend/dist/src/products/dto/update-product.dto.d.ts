import { BrandType, ProductShelf } from '@prisma/client';
export declare class UpdateProductDto {
    name?: string;
    brandType?: BrandType;
    brandId?: string;
    imageUrl?: string;
    shelf?: ProductShelf;
    basePrice?: number;
    gstPercentage?: number;
    dealerDiscount?: number;
    shopkeeperDiscount?: number;
    bulkShippingFee?: number | null;
    bulkShippingMinQty?: number;
}
