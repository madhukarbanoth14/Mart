import type { AuthUser } from '../auth/types/auth-user.type';
import { CreateOrderDto, CreateOrderWithPaymentDto } from './dto/create-order.dto';
import { OrderReturnRejectDto, OrderReturnRequestDto } from './dto/order-return.dto';
import { OrdersService } from './orders.service';
export declare class OrdersController {
    private readonly ordersService;
    constructor(ordersService: OrdersService);
    findAll(user: AuthUser): Promise<({
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    })[]>;
    findMine(user: AuthUser): Promise<({
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    })[]>;
    mySummary(user: AuthUser): Promise<{
        openOrders: number;
        inDelivery: number;
        lastTotal: number | null;
        invoicesReady: number;
    }>;
    findDealer(user: AuthUser): Promise<({
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    })[]>;
    dealerSummary(user: AuthUser): Promise<{
        pendingOrders: number;
        todaysDeliveries: number;
        weeklyRevenue: number;
    }>;
    previewReorder(id: string, user: AuthUser): Promise<import("./dto/order-reorder.dto").ReorderPreviewDto>;
    findOne(id: string, user: AuthUser): Promise<{
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    }>;
    create(dto: CreateOrderDto, user: AuthUser): Promise<{
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    }) | null>;
    outForDelivery(id: string, user: AuthUser): Promise<{
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    }>;
    dispatch(id: string, user: AuthUser): Promise<{
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    }>;
    deliver(id: string, user: AuthUser): Promise<{
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    }>;
    cancel(id: string, user: AuthUser): Promise<{
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    }>;
    requestReturn(id: string, dto: OrderReturnRequestDto, user: AuthUser): Promise<{
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    }>;
    approveReturn(id: string, user: AuthUser): Promise<{
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    }>;
    rejectReturn(id: string, dto: OrderReturnRejectDto, user: AuthUser): Promise<{
        shopkeeper: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
        };
        dealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
            shopName: string | null;
            address: string | null;
            latitude: number | null;
            longitude: number | null;
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
    }>;
    mockPayment(id: string): {
        orderId: string;
        paymentGateway: string;
        status: string;
        message: string;
    };
}
