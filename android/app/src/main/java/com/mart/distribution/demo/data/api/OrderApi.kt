package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.CreateOrderRequest
import com.mart.distribution.demo.data.api.dto.CreateOrderWithPaymentRequest
import com.mart.distribution.demo.data.api.dto.CreateOrderWithPaymentResponse
import com.mart.distribution.demo.data.api.dto.CreateRazorpayOrderRequest
import com.mart.distribution.demo.data.api.dto.CreateRazorpayOrderResponse
import com.mart.distribution.demo.data.api.dto.DealerSummaryDto
import com.mart.distribution.demo.data.api.dto.MockPaymentResponse
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.ReorderPreviewDto
import com.mart.distribution.demo.data.api.dto.ShopkeeperSummaryDto
import com.mart.distribution.demo.data.api.dto.VerifyRazorpayPaymentRequest
import com.mart.distribution.demo.data.api.dto.VerifyRazorpayPaymentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {
    @GET("orders")
    suspend fun orders(): List<OrderDto>

    @GET("orders/my")
    suspend fun myOrders(): List<OrderDto>

    @GET("orders/my/summary")
    suspend fun myOrderSummary(): ShopkeeperSummaryDto

    @GET("orders/dealer")
    suspend fun dealerOrders(): List<OrderDto>

    @GET("orders/dealer/summary")
    suspend fun dealerOrderSummary(): DealerSummaryDto

    @POST("orders")
    suspend fun createOrder(
        @Body body: CreateOrderRequest,
    ): OrderDto

    @POST("orders/create")
    suspend fun createOrderWithPayment(
        @Body body: CreateOrderWithPaymentRequest,
    ): CreateOrderWithPaymentResponse

    @POST("orders/dealer-restock")
    suspend fun createDealerRestockOrder(
        @Body body: CreateOrderRequest,
    ): OrderDto

    @POST("orders/dealer-restock/create")
    suspend fun createDealerRestockOrderWithPayment(
        @Body body: CreateOrderWithPaymentRequest,
    ): CreateOrderWithPaymentResponse

    @GET("orders/{id}")
    suspend fun orderById(
        @Path("id") id: String,
    ): OrderDto

    @GET("orders/{id}/reorder-preview")
    suspend fun reorderPreview(
        @Path("id") id: String,
    ): ReorderPreviewDto

    @PATCH("orders/{id}/confirm")
    suspend fun confirmOrder(
        @Path("id") id: String,
    ): OrderDto

  @PATCH("orders/{id}/deliver")
  suspend fun markDelivered(
      @Path("id") id: String,
  ): OrderDto

  @PATCH("orders/{id}/dispatch")
  suspend fun markOutForDelivery(
      @Path("id") id: String,
  ): OrderDto

  @PATCH("orders/{id}/cancel")
  suspend fun cancelOrder(
      @Path("id") id: String,
  ): OrderDto

  @PATCH("orders/{id}/return-request")
  suspend fun requestOrderReturn(
      @Path("id") id: String,
      @Body body: com.mart.distribution.demo.data.api.dto.OrderReturnRequest,
  ): OrderDto

  @PATCH("orders/{id}/return/approve")
  suspend fun approveOrderReturn(
      @Path("id") id: String,
  ): OrderDto

  @PATCH("orders/{id}/return/reject")
  suspend fun rejectOrderReturn(
      @Path("id") id: String,
      @Body body: com.mart.distribution.demo.data.api.dto.OrderReturnRejectRequest,
  ): OrderDto

    @POST("orders/{id}/payment/mock")
    suspend fun mockPayment(
        @Path("id") id: String,
    ): MockPaymentResponse

    @POST("payments/razorpay/order")
    suspend fun createRazorpayOrder(
        @Body body: CreateRazorpayOrderRequest,
    ): CreateRazorpayOrderResponse

    @POST("payments/create")
    suspend fun createRazorpayOrderV2(
        @Body body: CreateRazorpayOrderRequest,
    ): CreateRazorpayOrderResponse

    @POST("payments/razorpay/verify")
    suspend fun verifyRazorpayPayment(
        @Body body: VerifyRazorpayPaymentRequest,
    ): VerifyRazorpayPaymentResponse
}
