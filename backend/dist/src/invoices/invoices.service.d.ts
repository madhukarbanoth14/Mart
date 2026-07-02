import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
export declare class InvoicesService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    findAll(actor: AuthUser): Prisma.PrismaPromise<({
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
                orderId: string;
                gstAmount: Prisma.Decimal;
                discountAmount: Prisma.Decimal;
                finalAmount: Prisma.Decimal;
                productId: string;
                quantity: number;
                price: Prisma.Decimal;
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
            totalAmount: Prisma.Decimal;
            gstAmount: Prisma.Decimal;
            discountAmount: Prisma.Decimal;
            finalAmount: Prisma.Decimal;
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
    getInvoiceDocument(orderId: string): Promise<{
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
                orderId: string;
                gstAmount: Prisma.Decimal;
                discountAmount: Prisma.Decimal;
                finalAmount: Prisma.Decimal;
                productId: string;
                quantity: number;
                price: Prisma.Decimal;
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
            totalAmount: Prisma.Decimal;
            gstAmount: Prisma.Decimal;
            discountAmount: Prisma.Decimal;
            finalAmount: Prisma.Decimal;
            paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
            returnReason: string | null;
            returnRequestedAt: Date | null;
            returnedAt: Date | null;
            refundedAt: Date | null;
        };
    } | null>;
    generateForOrder(orderId: string, companyId?: string | null): Promise<{
        id: string;
        companyId: string | null;
        orderId: string;
        invoiceNumber: string;
        generatedAt: Date;
        pdfUrl: string | null;
    }>;
}
