package com.mart.distribution.demo.data.api

/**
 * Single Retrofit service combining domain APIs (same base URL / client).
 */
interface MartApi :
    AuthApi,
    BrandApi,
    ProductApi,
    OrderApi,
    InvoiceApi,
    UserApi,
    StockApi,
    AreasApi
