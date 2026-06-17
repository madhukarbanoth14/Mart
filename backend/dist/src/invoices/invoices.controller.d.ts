import type { AuthUser } from '../auth/types/auth-user.type';
import { InvoicesService } from './invoices.service';
export declare class InvoicesController {
    private readonly invoicesService;
    constructor(invoicesService: InvoicesService);
    documentForOrder(orderId: string): Promise<{
        invoiceNumber: string;
        generatedAt: Date;
        pdfUrl: string | null;
        order: {
            dealer: {
                id: string;
                name: string;
                email: string;
            };
            shopkeeper: {
                id: string;
                name: string;
                email: string;
            };
            items: ({
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
            })[];
        } & {
            id: string;
            companyId: string | null;
            createdAt: Date;
            status: import("@prisma/client").$Enums.OrderStatus;
            dealerId: string;
            updatedAt: Date;
            shopkeeperId: string;
            kind: import("@prisma/client").$Enums.OrderKind;
            totalAmount: import("@prisma/client-runtime-utils").Decimal;
            gstAmount: import("@prisma/client-runtime-utils").Decimal;
            discountAmount: import("@prisma/client-runtime-utils").Decimal;
            finalAmount: import("@prisma/client-runtime-utils").Decimal;
            paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
        };
    }>;
    findAll(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
        order: {
            dealer: {
                id: string;
                name: string;
            };
            shopkeeper: {
                id: string;
                name: string;
            };
            items: ({
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
            })[];
        } & {
            id: string;
            companyId: string | null;
            createdAt: Date;
            status: import("@prisma/client").$Enums.OrderStatus;
            dealerId: string;
            updatedAt: Date;
            shopkeeperId: string;
            kind: import("@prisma/client").$Enums.OrderKind;
            totalAmount: import("@prisma/client-runtime-utils").Decimal;
            gstAmount: import("@prisma/client-runtime-utils").Decimal;
            discountAmount: import("@prisma/client-runtime-utils").Decimal;
            finalAmount: import("@prisma/client-runtime-utils").Decimal;
            paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
        };
    } & {
        id: string;
        companyId: string | null;
        orderId: string;
        invoiceNumber: string;
        generatedAt: Date;
        pdfUrl: string | null;
    })[]>;
}
