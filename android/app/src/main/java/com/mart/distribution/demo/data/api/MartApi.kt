package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.AuthMeDto
import com.mart.distribution.demo.data.api.dto.CreateOrderRequest
import com.mart.distribution.demo.data.api.dto.LoginRequest
import com.mart.distribution.demo.data.api.dto.LoginResponse
import com.mart.distribution.demo.data.api.dto.InvoiceDocumentDto
import com.mart.distribution.demo.data.api.dto.MockPaymentResponse
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.StockRowDto
import com.mart.distribution.demo.data.api.dto.UserRowDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface MartApi {
    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest,
    ): LoginResponse

    @GET("auth/me")
    suspend fun me(): AuthMeDto

    @GET("products")
    suspend fun products(): List<ProductDto>

    @GET("orders")
    suspend fun orders(): List<OrderDto>

    @POST("orders")
    suspend fun createOrder(
        @Body body: CreateOrderRequest,
    ): OrderDto

    @PATCH("orders/{id}/confirm")
    suspend fun confirmOrder(
        @Path("id") id: String,
    ): OrderDto

    @POST("orders/{id}/payment/mock")
    suspend fun mockPayment(
        @Path("id") id: String,
    ): MockPaymentResponse

    @GET("invoices/by-order/{orderId}")
    suspend fun invoiceByOrder(
        @Path("orderId") orderId: String,
    ): InvoiceDocumentDto

    @GET("stock")
    suspend fun stock(): List<StockRowDto>

    @GET("users")
    suspend fun users(): List<UserRowDto>
}
