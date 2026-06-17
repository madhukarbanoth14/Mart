import { OrderStatus, Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { NotificationsService } from '../notifications/notifications.service';
import { InvoicesService } from '../invoices/invoices.service';
import { PaymentsService } from '../payments/payments.service';
import { PrismaService } from '../prisma/prisma.service';
import { CreateOrderDto, CreateOrderWithPaymentDto } from './dto/create-order.dto';
export declare class OrdersService {
    private readonly prisma;
    private readonly invoicesService;
    private readonly notifications;
    private readonly paymentsService;
    constructor(prisma: PrismaService, invoicesService: InvoicesService, notifications: NotificationsService, paymentsService: PaymentsService);
    private scopeWhere;
    private getCompanyWarehouseUser;
    private buildLineItems;
    findAll(actor: AuthUser): Prisma.PrismaPromise<({
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    })[]>;
    findMine(actor: AuthUser): Prisma.PrismaPromise<({
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    })[]>;
    findDealer(actor: AuthUser): Prisma.PrismaPromise<({
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    })[]>;
    mySummary(actor: AuthUser): Promise<{
        openOrders: number;
        inDelivery: number;
        lastTotal: number | null;
        invoicesReady: number;
    }>;
    dealerSummary(actor: AuthUser): Promise<{
        pendingOrders: number;
        todaysDeliveries: number;
        weeklyRevenue: number;
    }>;
    findOne(orderId: string, actor: AuthUser): Promise<{
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    }>;
    createOrder(dto: CreateOrderDto, actor: AuthUser): Promise<{
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    }>;
    createDealerRestockOrder(dto: CreateOrderDto, actor: AuthUser): Promise<{
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    }>;
    createDealerRestockWithPayment(dto: CreateOrderWithPaymentDto, actor: AuthUser): Promise<{
        orderId: string;
        razorpayOrderId: string;
        amount: number;
        currency: string;
        keyId: string;
        status?: undefined;
        message?: undefined;
    } | {
        orderId: string;
        status: import("@prisma/client").$Enums.OrderStatus;
        message: string;
        razorpayOrderId?: undefined;
        amount?: undefined;
        currency?: undefined;
        keyId?: undefined;
    }>;
    createOrderWithPayment(dto: CreateOrderWithPaymentDto, actor: AuthUser): Promise<{
        orderId: string;
        razorpayOrderId: string;
        amount: number;
        currency: string;
        keyId: string;
    } | {
        orderId: string;
        status: OrderStatus;
        message: string;
    }>;
    confirmOrder(orderId: string, actor: AuthUser): Promise<({
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    }) | null>;
    markOutForDelivery(orderId: string, actor: AuthUser): Promise<{
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    }>;
    markDelivered(orderId: string, actor: AuthUser): Promise<{
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    }>;
    cancelOrder(orderId: string, actor: AuthUser): Promise<{
        dealer: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
        };
        shopkeeper: {
            id: string;
            name: string;
            email: string;
            phone: string | null;
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
        invoice: {
            id: string;
            companyId: string | null;
            orderId: string;
            invoiceNumber: string;
            generatedAt: Date;
            pdfUrl: string | null;
        } | null;
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
    }>;
    mockPaymentSuccess(orderId: string): {
        orderId: string;
        paymentGateway: string;
        status: string;
        message: string;
    };
}
