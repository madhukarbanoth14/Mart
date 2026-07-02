package com.mart.distribution.demo.data.api.dto

data class ReturnRequestDto(
    val id: String,
    val returnCode: String,
    val orderId: String,
    val reason: String,
    val reasonText: String? = null,
    val comments: String? = null,
    val imageUrls: List<String>? = null,
    val status: String,
    val dealerRemarks: String? = null,
    val refundAmount: Double = 0.0,
    val createdAt: String? = null,
    val items: List<ReturnRequestItemDto> = emptyList(),
    val shopkeeper: ReturnUserDto? = null,
    val dealer: ReturnUserDto? = null,
    val order: ReturnOrderDto? = null,
    val refundRequest: RefundRequestDto? = null,
)

data class ReturnRequestItemDto(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitAmount: Double = 0.0,
    val lineAmount: Double = 0.0,
)

data class ReturnUserDto(
    val id: String,
    val name: String,
    val shopName: String? = null,
    val area: ReturnAreaDto? = null,
)

data class ReturnAreaDto(val id: String? = null, val name: String? = null)

data class ReturnOrderDto(
    val id: String,
    val status: String,
    val finalAmount: Double = 0.0,
    val paymentStatus: String? = null,
)

data class RefundRequestDto(
    val id: String,
    val refundCode: String,
    val returnRequestId: String,
    val orderId: String,
    val amount: Double = 0.0,
    val status: String,
    val dealerRemarks: String? = null,
    val adminRemarks: String? = null,
    val refundMethod: String? = null,
    val transactionReference: String? = null,
    val refundDate: String? = null,
    val createdAt: String? = null,
    val dealer: ReturnUserDto? = null,
    val returnRequest: ReturnRequestDto? = null,
)

data class CreateReturnItemRequest(val productId: String, val quantity: Int)

data class CreateReturnRequest(
    val reason: String,
    val reasonText: String? = null,
    val comments: String? = null,
    val imageUrls: List<String>? = null,
    val items: List<CreateReturnItemRequest>,
)

data class ReturnActionRequest(val remarks: String? = null)

data class RaiseRefundRequest(val remarks: String? = null)

data class ProcessRefundRequest(
    val refundMethod: String,
    val transactionReference: String,
    val remarks: String? = null,
    val refundDate: String? = null,
)

data class DealerRevenueSummaryDto(
    val todayRevenue: Double = 0.0,
    val weeklyRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val totalOrdersReceived: Int = 0,
    val totalOrdersDelivered: Int = 0,
    val totalProductsSold: Int = 0,
    val totalQuantitySold: Int = 0,
    val pendingOrders: Int = 0,
    val returnedOrders: Int = 0,
    val pendingSettlementAmount: Double = 0.0,
    val amountReceivedFromFlashMart: Double = 0.0,
)

data class DealerRevenueChartsDto(
    val dailyRevenue: List<DealerTrendPointDto> = emptyList(),
    val productWiseSales: List<DealerProductSaleDto> = emptyList(),
    val categoryWiseSales: List<DealerCategorySaleDto> = emptyList(),
    val topSellingProducts: List<DealerProductSaleDto> = emptyList(),
    val shopkeeperWiseRevenue: List<DealerShopkeeperRevenuePointDto> = emptyList(),
)

data class DealerTrendPointDto(val date: String, val amount: Double = 0.0)

data class DealerProductSaleDto(
    val productId: String? = null,
    val name: String,
    val quantity: Int = 0,
    val revenue: Double = 0.0,
)

data class DealerCategorySaleDto(val category: String, val revenue: Double = 0.0)

data class DealerShopkeeperRevenuePointDto(
    val shopkeeperId: String,
    val name: String,
    val revenue: Double = 0.0,
)

data class DealerRevenueDashboardDto(
    val summary: DealerRevenueSummaryDto = DealerRevenueSummaryDto(),
    val charts: DealerRevenueChartsDto = DealerRevenueChartsDto(),
)

data class DealerShopkeeperRowDto(
    val shopkeeperId: String,
    val name: String,
    val area: String? = null,
    val totalOrders: Int = 0,
    val totalPurchaseValue: Double = 0.0,
    val outstandingOrders: Int = 0,
    val returnedOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val lastOrderDate: String? = null,
)
