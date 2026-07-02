package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.CommissionRuleDto
import com.mart.distribution.demo.data.api.dto.DealerPerformanceDto
import com.mart.distribution.demo.data.api.dto.DealerSettlementDto
import com.mart.distribution.demo.data.api.dto.FinanceAuditLogDto
import com.mart.distribution.demo.data.api.dto.FinanceOverviewDto
import com.mart.distribution.demo.data.api.dto.GenerateSettlementRequest
import com.mart.distribution.demo.data.api.dto.InvestorDashboardDto
import com.mart.distribution.demo.data.api.dto.RecordSettlementPaymentRequest
import com.mart.distribution.demo.data.api.dto.UpsertCommissionRuleRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface FinanceApi {
    @GET("finance/dashboard/overview")
    suspend fun financeOverview(
        @Query("period") period: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
    ): FinanceOverviewDto

    @GET("finance/dashboard/investor")
    suspend fun investorDashboard(): InvestorDashboardDto

    @GET("finance/commission-rules")
    suspend fun commissionRules(): List<CommissionRuleDto>

    @POST("finance/commission-rules")
    suspend fun upsertCommissionRule(@Body body: UpsertCommissionRuleRequest): CommissionRuleDto

    @GET("finance/settlements")
    suspend fun settlements(
        @Query("period") period: String? = null,
        @Query("dealerId") dealerId: String? = null,
        @Query("status") status: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
    ): List<DealerSettlementDto>

    @POST("finance/settlements/generate")
    suspend fun generateSettlement(@Body body: GenerateSettlementRequest): DealerSettlementDto

    @GET("finance/settlements/{id}")
    suspend fun settlementDetail(@Path("id") id: String): DealerSettlementDto

    @POST("finance/settlements/{id}/payments")
    suspend fun recordSettlementPayment(
        @Path("id") id: String,
        @Body body: RecordSettlementPaymentRequest,
    ): DealerSettlementDto

    @GET("finance/dealers/{dealerId}/performance")
    suspend fun dealerPerformance(
        @Path("dealerId") dealerId: String,
        @Query("period") period: String? = null,
    ): DealerPerformanceDto

    @GET("finance/audit")
    suspend fun financeAudit(): List<FinanceAuditLogDto>

    @POST("finance/backfill-revenues")
    suspend fun backfillRevenues(): Map<String, Int>

    @GET("finance/reports/{type}")
    @Streaming
    suspend fun downloadReport(
        @Path("type") type: String,
        @Query("format") format: String,
        @Query("period") period: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
    ): ResponseBody
}
