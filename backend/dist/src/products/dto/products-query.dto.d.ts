import { ProductShelf } from '@prisma/client';
export declare class ProductsQueryDto {
    search?: string;
    brandId?: string;
    shelf?: ProductShelf;
    page?: string;
    limit?: string;
}
