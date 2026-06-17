import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
export declare class InvoicesService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    findAll(actor: AuthUser): Prisma.PrismaPromise<({
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
            } & {
                id: string;
                productId: string;
                quantity: number;
                gstAmount: Prisma.Decimal;
                discountAmount: Prisma.Decimal;
                finalAmount: Prisma.Decimal;
                orderId: string;
                price: Prisma.Decimal;
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
            totalAmount: Prisma.Decimal;
            gstAmount: Prisma.Decimal;
            discountAmount: Prisma.Decimal;
            finalAmount: Prisma.Decimal;
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
    getInvoiceDocument(orderId: string): Promise<{
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
            } & {
                id: string;
                productId: string;
                quantity: number;
                gstAmount: Prisma.Decimal;
                discountAmount: Prisma.Decimal;
                finalAmount: Prisma.Decimal;
                orderId: string;
                price: Prisma.Decimal;
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
            totalAmount: Prisma.Decimal;
            gstAmount: Prisma.Decimal;
            discountAmount: Prisma.Decimal;
            finalAmount: Prisma.Decimal;
            paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
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
