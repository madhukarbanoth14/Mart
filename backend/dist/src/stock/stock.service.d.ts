import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { UpsertStockDto } from './dto/upsert-stock.dto';
export declare class StockService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    findAll(actor: AuthUser): Prisma.PrismaPromise<({
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
            mrp: Prisma.Decimal | null;
            dealerPrice: Prisma.Decimal | null;
            caseQty: number | null;
            gstRate: Prisma.Decimal;
            isActive: boolean;
            brandType: import("@prisma/client").$Enums.BrandType;
            shelf: import("@prisma/client").$Enums.ProductShelf;
            basePrice: Prisma.Decimal;
            gstPercentage: Prisma.Decimal;
            dealerDiscount: Prisma.Decimal;
            shopkeeperDiscount: Prisma.Decimal;
            bulkShippingFee: Prisma.Decimal | null;
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
    availableForShopkeeper(actor: AuthUser): Promise<{
        productId: string;
        quantity: number;
    }[]>;
    updateQuantity(stockId: string, actor: AuthUser, quantity: number): Promise<{
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
            mrp: Prisma.Decimal | null;
            dealerPrice: Prisma.Decimal | null;
            caseQty: number | null;
            gstRate: Prisma.Decimal;
            isActive: boolean;
            brandType: import("@prisma/client").$Enums.BrandType;
            shelf: import("@prisma/client").$Enums.ProductShelf;
            basePrice: Prisma.Decimal;
            gstPercentage: Prisma.Decimal;
            dealerDiscount: Prisma.Decimal;
            shopkeeperDiscount: Prisma.Decimal;
            bulkShippingFee: Prisma.Decimal | null;
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
    upsertForDealer(actor: AuthUser, dto: UpsertStockDto): Promise<{
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
            mrp: Prisma.Decimal | null;
            dealerPrice: Prisma.Decimal | null;
            caseQty: number | null;
            gstRate: Prisma.Decimal;
            isActive: boolean;
            brandType: import("@prisma/client").$Enums.BrandType;
            shelf: import("@prisma/client").$Enums.ProductShelf;
            basePrice: Prisma.Decimal;
            gstPercentage: Prisma.Decimal;
            dealerDiscount: Prisma.Decimal;
            shopkeeperDiscount: Prisma.Decimal;
            bulkShippingFee: Prisma.Decimal | null;
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
    private assertCanManageStock;
}
