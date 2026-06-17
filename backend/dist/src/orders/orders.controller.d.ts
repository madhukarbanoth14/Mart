import type { AuthUser } from '../auth/types/auth-user.type';
import { CreateOrderDto, CreateOrderWithPaymentDto } from './dto/create-order.dto';
import { OrdersService } from './orders.service';
export declare class OrdersController {
    private readonly ordersService;
    constructor(ordersService: OrdersService);
    findAll(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    })[]>;
    findMine(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    })[]>;
    mySummary(user: AuthUser): Promise<{
        openOrders: number;
        inDelivery: number;
        lastTotal: number | null;
        invoicesReady: number;
    }>;
    findDealer(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    })[]>;
    dealerSummary(user: AuthUser): Promise<{
        pendingOrders: number;
        todaysDeliveries: number;
        weeklyRevenue: number;
    }>;
    findOne(id: string, user: AuthUser): Promise<{
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    }>;
    create(dto: CreateOrderDto, user: AuthUser): Promise<{
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    }>;
    createWithPayment(dto: CreateOrderWithPaymentDto, user: AuthUser): Promise<{
        orderId: string;
        razorpayOrderId: string;
        amount: number;
        currency: string;
        keyId: string;
    } | {
        orderId: string;
        status: import("@prisma/client").OrderStatus;
        message: string;
    }>;
    createDealerRestock(dto: CreateOrderDto, user: AuthUser): Promise<{
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    }>;
    createDealerRestockWithPayment(dto: CreateOrderWithPaymentDto, user: AuthUser): Promise<{
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
    confirm(id: string, user: AuthUser): Promise<({
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    }) | null>;
    outForDelivery(id: string, user: AuthUser): Promise<{
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    }>;
    dispatch(id: string, user: AuthUser): Promise<{
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    }>;
    deliver(id: string, user: AuthUser): Promise<{
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    }>;
    cancel(id: string, user: AuthUser): Promise<{
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
        totalAmount: import("@prisma/client-runtime-utils").Decimal;
        gstAmount: import("@prisma/client-runtime-utils").Decimal;
        discountAmount: import("@prisma/client-runtime-utils").Decimal;
        finalAmount: import("@prisma/client-runtime-utils").Decimal;
        paymentStatus: import("@prisma/client").$Enums.OrderPaymentStatus;
    }>;
    mockPayment(id: string): {
        orderId: string;
        paymentGateway: string;
        status: string;
        message: string;
    };
}
