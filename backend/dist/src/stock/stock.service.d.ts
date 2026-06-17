import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
export declare class StockService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    findAll(actor: AuthUser): Prisma.PrismaPromise<({
        product: {
            id: string;
            companyId: string | null;
            brandId: string | null;
            imageUrl: string | null;
            sku: string | null;
            weight: string;
            mrp: Prisma.Decimal | null;
            dealerPrice: Prisma.Decimal | null;
            caseQty: number | null;
            gstRate: Prisma.Decimal;
            isActive: boolean;
            name: string;
            brandType: import("@prisma/client").$Enums.BrandType;
            shelf: import("@prisma/client").$Enums.ProductShelf;
            basePrice: Prisma.Decimal;
            gstPercentage: Prisma.Decimal;
            dealerDiscount: Prisma.Decimal;
            shopkeeperDiscount: Prisma.Decimal;
            bulkShippingFee: Prisma.Decimal | null;
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
