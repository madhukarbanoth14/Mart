import { PrismaService } from '../prisma/prisma.service';
export declare class OrderItemsService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    findByOrderId(orderId: string): import("@prisma/client").Prisma.PrismaPromise<({
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
    } & {
        id: string;
        productId: string;
        quantity: number;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        orderId: string;
        price: import("@prisma/client-runtime-utils").Decimal;
    })[]>;
}
