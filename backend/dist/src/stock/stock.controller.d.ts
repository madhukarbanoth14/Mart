import type { AuthUser } from '../auth/types/auth-user.type';
import { UpsertStockDto } from './dto/upsert-stock.dto';
import { UpdateStockDto } from './dto/update-stock.dto';
import { StockService } from './stock.service';
export declare class StockController {
    private readonly stockService;
    constructor(stockService: StockService);
    findAll(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
        dealer: {
            name: string;
            id: string;
            email: string;
        };
        product: {
            name: string;
            id: string;
            companyId: string | null;
            createdAt: Date;
            brandId: string | null;
            imageUrl: string | null;
            sku: string | null;
            weight: string;
            mrp: import("@prisma/client-runtime-utils").Decimal | null;
            dealerPrice: import("@prisma/client-runtime-utils").Decimal | null;
            caseQty: number | null;
            gstRate: import("@prisma/client-runtime-utils").Decimal;
            isActive: boolean;
            brandType: import("@prisma/client").$Enums.BrandType;
            shelf: import("@prisma/client").$Enums.ProductShelf;
            basePrice: import("@prisma/client-runtime-utils").Decimal;
            gstPercentage: import("@prisma/client-runtime-utils").Decimal;
            dealerDiscount: import("@prisma/client-runtime-utils").Decimal;
            shopkeeperDiscount: import("@prisma/client-runtime-utils").Decimal;
            bulkShippingFee: import("@prisma/client-runtime-utils").Decimal | null;
            bulkShippingMinQty: number;
        };
    } & {
        id: string;
        companyId: string | null;
        updatedAt: Date;
        dealerId: string;
        productId: string;
        quantity: number;
    })[]>;
    available(user: AuthUser): Promise<{
        productId: string;
        quantity: number;
    }[]>;
    updateQuantity(id: string, dto: UpdateStockDto, user: AuthUser): Promise<{
        dealer: {
            name: string;
            id: string;
            email: string;
        };
        product: {
            name: string;
            id: string;
            companyId: string | null;
            createdAt: Date;
            brandId: string | null;
            imageUrl: string | null;
            sku: string | null;
            weight: string;
            mrp: import("@prisma/client-runtime-utils").Decimal | null;
            dealerPrice: import("@prisma/client-runtime-utils").Decimal | null;
            caseQty: number | null;
            gstRate: import("@prisma/client-runtime-utils").Decimal;
            isActive: boolean;
            brandType: import("@prisma/client").$Enums.BrandType;
            shelf: import("@prisma/client").$Enums.ProductShelf;
            basePrice: import("@prisma/client-runtime-utils").Decimal;
            gstPercentage: import("@prisma/client-runtime-utils").Decimal;
            dealerDiscount: import("@prisma/client-runtime-utils").Decimal;
            shopkeeperDiscount: import("@prisma/client-runtime-utils").Decimal;
            bulkShippingFee: import("@prisma/client-runtime-utils").Decimal | null;
            bulkShippingMinQty: number;
        };
    } & {
        id: string;
        companyId: string | null;
        updatedAt: Date;
        dealerId: string;
        productId: string;
        quantity: number;
    }>;
    upsert(dto: UpsertStockDto, user: AuthUser): Promise<{
        dealer: {
            name: string;
            id: string;
            email: string;
        };
        product: {
            name: string;
            id: string;
            companyId: string | null;
            createdAt: Date;
            brandId: string | null;
            imageUrl: string | null;
            sku: string | null;
            weight: string;
            mrp: import("@prisma/client-runtime-utils").Decimal | null;
            dealerPrice: import("@prisma/client-runtime-utils").Decimal | null;
            caseQty: number | null;
            gstRate: import("@prisma/client-runtime-utils").Decimal;
            isActive: boolean;
            brandType: import("@prisma/client").$Enums.BrandType;
            shelf: import("@prisma/client").$Enums.ProductShelf;
            basePrice: import("@prisma/client-runtime-utils").Decimal;
            gstPercentage: import("@prisma/client-runtime-utils").Decimal;
            dealerDiscount: import("@prisma/client-runtime-utils").Decimal;
            shopkeeperDiscount: import("@prisma/client-runtime-utils").Decimal;
            bulkShippingFee: import("@prisma/client-runtime-utils").Decimal | null;
            bulkShippingMinQty: number;
        };
    } & {
        id: string;
        companyId: string | null;
        updatedAt: Date;
        dealerId: string;
        productId: string;
        quantity: number;
    }>;
}
