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
            shopkeeper: {
                name: string;
                id: string;
                email: string;
            };
            dealer: {
                name: string;
                id: string;
                email: string;
            };
            items: ({
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
                orderId: string;
                gstAmount: import("@prisma/client-runtime-utils").Decimal;
                discountAmount: import("@prisma/client-runtime-utils").Decimal;
                finalAmount: import("@prisma/client-runtime-utils").Decimal;
                productId: string;
                quantity: number;
                price: import("@prisma/client-runtime-utils").Decimal;
            })[];
        } & {
            id: string;
            companyId: string | null;
            status: import("@prisma/client").$Enums.OrderStatus;
            createdAt: Date;
            updatedAt: Date;
            shopkeeperId: string;
            dealerId: string;
            kind: import("@prisma/client").$Enums.OrderKind;
            totalAmount: import("@prisma/client-runtime-utils").Decimal;
            gstAmount: import("@prisma/client-runtime-utils").Decimal;
            discountAmount: import("@prisma/client-runtime-utils").Decimal;
            finalAmount: import("@prisma/client-runtime-utils").Decimal;
            paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
            returnReason: string | null;
            returnRequestedAt: Date | null;
            returnedAt: Date | null;
            refundedAt: Date | null;
        };
    }>;
    findAll(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
        order: {
            shopkeeper: {
                name: string;
                id: string;
            };
            dealer: {
                name: string;
                id: string;
            };
            items: ({
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
                orderId: string;
                gstAmount: import("@prisma/client-runtime-utils").Decimal;
                discountAmount: import("@prisma/client-runtime-utils").Decimal;
                finalAmount: import("@prisma/client-runtime-utils").Decimal;
                productId: string;
                quantity: number;
                price: import("@prisma/client-runtime-utils").Decimal;
            })[];
        } & {
            id: string;
            companyId: string | null;
            status: import("@prisma/client").$Enums.OrderStatus;
            createdAt: Date;
            updatedAt: Date;
            shopkeeperId: string;
            dealerId: string;
            kind: import("@prisma/client").$Enums.OrderKind;
            totalAmount: import("@prisma/client-runtime-utils").Decimal;
            gstAmount: import("@prisma/client-runtime-utils").Decimal;
            discountAmount: import("@prisma/client-runtime-utils").Decimal;
            finalAmount: import("@prisma/client-runtime-utils").Decimal;
            paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
            returnReason: string | null;
            returnRequestedAt: Date | null;
            returnedAt: Date | null;
            refundedAt: Date | null;
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
