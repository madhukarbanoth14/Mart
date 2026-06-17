export declare class CreateOrderItemDto {
    productId: string;
    quantity: number;
}
export declare class CreateOrderDto {
    items: CreateOrderItemDto[];
}
export declare enum OrderPaymentMode {
    COD = "COD",
    RAZORPAY = "RAZORPAY"
}
export declare class CreateOrderWithPaymentDto extends CreateOrderDto {
    paymentMode?: OrderPaymentMode;
}
