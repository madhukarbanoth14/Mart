import type { AuthUser } from '../auth/types/auth-user.type';
import { StockService } from './stock.service';
export declare class StockController {
    private readonly stockService;
    constructor(stockService: StockService);
    findAll(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
        product: {
            id: string;
            companyId: string | null;
            brandId: string | null;
            imageUrl: string | null;
            sku: string | null;
            weight: string;
            mrp: import("@prisma/client-runtime-utils").Decimal | null;
            dealerPrice: import("@prisma/client-runtime-utils").Decimal | null;
            caseQty: number | null;
            gstRate: import("@prisma/client-runtime-utils").Decimal;
            isActive: boolean;
            name: string;
            brandType: import("@prisma/client").$Enums.BrandType;
            shelf: import("@prisma/client").$Enums.ProductShelf;
            basePrice: import("@prisma/client-runtime-utils").Decimal;
            gstPercentage: import("@prisma/client-runtime-utils").Decimal;
            dealerDiscount: import("@prisma/client-runtime-utils").Decimal;
            shopkeeperDiscount: import("@prisma/client-runtime-utils").Decimal;
            bulkShippingFee: import("@prisma/client-runtime-utils").Decimal | null;
            bulkShippingMinQty: number;
            createdAt: Date;
        };
        dealer: {
            id: string;
            name: string;
            email: string;
        };
    } & {
        id: string;
        companyId: string | null;
        dealerId: string;
        productId: string;
        quantity: number;
        updatedAt: Date;
    })[]>;
}
