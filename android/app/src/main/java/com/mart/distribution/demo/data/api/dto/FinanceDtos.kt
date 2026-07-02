package com.mart.distribution.demo.data.api.dto

data class FinanceOverviewDto(
    val period: FinancePeriodDto? = null,
    val collections: FinanceCollectionsDto? = null,
    val revenue: FinanceRevenueDto? = null,
)

data class FinancePeriodDto(
    val from: String? = null,
    val to: String? = null,
    val filter: String? = null,
)

data class FinanceCollectionsDto(
    val total: Double = 0.0,
    val successful: Int = 0,
    val failed: Int = 0,
    val refunded: Int = 0,
    val pending: Int = 0,
)

data class FinanceRevenueDto(
    val gmv: Double = 0.0,
    val platformCommission: Double = 0.0,
    val dealerPayables: Double = 0.0,
    val settledAmount: Double = 0.0,
    val pendingSettlement: Double = 0.0,
    val netPlatformEarnings: Double = 0.0,
    val refunds: Double = 0.0,
)

data class InvestorDashboardDto(
    val period: FinancePeriodDto? = null,
    val collections: FinanceCollectionsDto? = null,
    val revenue: FinanceRevenueDto? = null,
    val business: InvestorBusinessDto? = null,
    val charts: InvestorChartsDto? = null,
)

data class InvestorBusinessDto(
    val totalOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val activeDealers: Int = 0,
    val activeShopkeepers: Int = 0,
    val activeEmployees: Int = 0,
)

data class InvestorChartsDto(
    val dailyRevenueTrend: List<FinanceTrendPointDto> = emptyList(),
)

data class FinanceTrendPointDto(
    val date: String,
    val gmv: Double = 0.0,
    val commission: Double = 0.0,
)

data class CommissionRuleDto(
    val id: String,
    val ruleType: String,
    val rate: Number,
    val dealerId: String? = null,
    val productId: String? = null,
    val active: Boolean = true,
    val dealer: FinanceUserBriefDto? = null,
    val product: FinanceProductBriefDto? = null,
)

data class FinanceUserBriefDto(
    val id: String,
    val name: String,
    val shopName: String? = null,
    val phone: String? = null,
    val email: String? = null,
)

data class FinanceProductBriefDto(
    val id: String,
    val name: String,
)

data class UpsertCommissionRuleRequest(
    val ruleType: String,
    val rate: Double,
    val dealerId: String? = null,
    val productId: String? = null,
)

data class DealerSettlementDto(
    val id: String,
    val settlementCode: String,
    val dealerId: String,
    val settlementStartDate: String,
    val settlementEndDate: String,
    val totalOrders: Int = 0,
    val totalQuantity: Int = 0,
    val grossSales: Number = 0,
    val gstAmount: Number = 0,
    val commissionAmount: Number = 0,
    val dealerPayable: Number = 0,
    val settledAmount: Number = 0,
    val balanceAmount: Number = 0,
    val settlementStatus: String,
    val paymentMethod: String? = null,
    val transactionReference: String? = null,
    val utrNumber: String? = null,
    val paymentDate: String? = null,
    val remarks: String? = null,
    val dealer: FinanceUserBriefDto? = null,
    val payments: List<DealerPaymentHistoryDto> = emptyList(),
)

data class DealerPaymentHistoryDto(
    val id: String,
    val amount: Number,
    val paymentMethod: String,
    val utrNumber: String? = null,
    val transactionReference: String? = null,
    val paymentDate: String,
    val remarks: String? = null,
)

data class GenerateSettlementRequest(
    val dealerId: String,
    val startDate: String,
    val endDate: String,
)

data class RecordSettlementPaymentRequest(
    val amount: Double,
    val paymentMethod: String,
    val utrNumber: String? = null,
    val transactionReference: String? = null,
    val paymentDate: String,
    val remarks: String? = null,
)

data class DealerPerformanceDto(
    val period: FinancePeriodDto? = null,
    val summary: DealerPerformanceSummaryDto? = null,
    val topProducts: List<DealerTopProductDto> = emptyList(),
    val dailySales: List<FinanceTrendPointDto> = emptyList(),
)

data class DealerPerformanceSummaryDto(
    val ordersDelivered: Int = 0,
    val ordersTotal: Int = 0,
    val grossSales: Double = 0.0,
    val commissionDeducted: Double = 0.0,
    val dealerEarnings: Double = 0.0,
    val pendingSettlement: Double = 0.0,
)

data class DealerTopProductDto(
    val name: String,
    val qty: Int = 0,
    val revenue: Double = 0.0,
)

data class FinanceAuditLogDto(
    val id: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val createdAt: String,
    val actor: FinanceUserBriefDto? = null,
)
