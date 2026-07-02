package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.CreateReturnRequest
import com.mart.distribution.demo.data.api.dto.DealerRevenueDashboardDto
import com.mart.distribution.demo.data.api.dto.DealerShopkeeperRowDto
import com.mart.distribution.demo.data.api.dto.ProcessRefundRequest
import com.mart.distribution.demo.data.api.dto.RaiseRefundRequest
import com.mart.distribution.demo.data.api.dto.RefundRequestDto
import com.mart.distribution.demo.data.api.dto.ReturnActionRequest
import com.mart.distribution.demo.data.api.dto.ReturnRequestDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReturnsApi {
    @POST("returns/orders/{orderId}")
    suspend fun createReturn(
        @Path("orderId") orderId: String,
        @Body body: CreateReturnRequest,
    ): ReturnRequestDto

    @GET("returns")
    suspend fun listReturns(
        @Query("status") status: String? = null,
        @Query("shopkeeperId") shopkeeperId: String? = null,
        @Query("area") area: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
    ): List<ReturnRequestDto>

    @GET("returns/{id}")
    suspend fun getReturn(@Path("id") id: String): ReturnRequestDto

    @PATCH("returns/{id}/approve")
    suspend fun approveReturn(
        @Path("id") id: String,
        @Body body: ReturnActionRequest = ReturnActionRequest(),
    ): ReturnRequestDto

    @PATCH("returns/{id}/reject")
    suspend fun rejectReturn(
        @Path("id") id: String,
        @Body body: ReturnActionRequest,
    ): ReturnRequestDto

    @POST("returns/{id}/refund-request")
    suspend fun raiseRefundRequest(
        @Path("id") id: String,
        @Body body: RaiseRefundRequest = RaiseRefundRequest(),
    ): RefundRequestDto

    @GET("refunds")
    suspend fun listRefunds(
        @Query("status") status: String? = null,
        @Query("dealerId") dealerId: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
    ): List<RefundRequestDto>

    @GET("refunds/{id}")
    suspend fun getRefund(@Path("id") id: String): RefundRequestDto

    @PATCH("refunds/{id}/approve")
    suspend fun approveRefund(
        @Path("id") id: String,
        @Body body: ReturnActionRequest = ReturnActionRequest(),
    ): RefundRequestDto

    @PATCH("refunds/{id}/reject")
    suspend fun rejectRefund(
        @Path("id") id: String,
        @Body body: ReturnActionRequest,
    ): RefundRequestDto

    @POST("refunds/{id}/process")
    suspend fun processRefund(
        @Path("id") id: String,
        @Body body: ProcessRefundRequest,
    ): RefundRequestDto

    @GET("finance/dealer/dashboard")
    suspend fun dealerRevenueDashboard(
        @Query("period") period: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
    ): DealerRevenueDashboardDto

    @GET("finance/dealer/shopkeepers")
    suspend fun dealerShopkeeperRevenue(
        @Query("shopkeeper") shopkeeper: String? = null,
        @Query("area") area: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("orderStatus") orderStatus: String? = null,
    ): List<DealerShopkeeperRowDto>

    @GET("finance/dealer/reports/{type}")
    @Streaming
    suspend fun downloadDealerReport(
        @Path("type") type: String,
        @Query("format") format: String,
        @Query("period") period: String? = null,
    ): ResponseBody
}
