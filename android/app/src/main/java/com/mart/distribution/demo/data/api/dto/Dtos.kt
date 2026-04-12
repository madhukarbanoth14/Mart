package com.mart.distribution.demo.data.api.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    val accessToken: String,
    val user: LoginUserDto,
)

data class LoginUserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
)

data class AuthMeDto(
    val userId: String,
    val email: String,
    val role: String,
    val companyId: String?,
)

data class ProductDto(
    val id: String,
    val name: String,
    val brandType: String,
    @SerializedName("basePrice") val basePrice: Any?, // Prisma Decimal: string or number
    @SerializedName("gstPercentage") val gstPercentage: Any?,
    @SerializedName("dealerDiscount") val dealerDiscount: Any?,
    @SerializedName("shopkeeperDiscount") val shopkeeperDiscount: Any?,
)

data class CreateOrderRequest(
    val items: List<CreateOrderItemDto>,
)

data class CreateOrderItemDto(
    val productId: String,
    val quantity: Int,
)

data class OrderDto(
    val id: String,
    val status: String,
    val shopkeeperId: String? = null,
    val dealerId: String? = null,
    val totalAmount: Any? = null,
    val gstAmount: Any? = null,
    val discountAmount: Any? = null,
    val finalAmount: Any? = null,
    val items: List<OrderItemDto>? = null,
    val shopkeeper: UserBriefDto? = null,
    val dealer: UserBriefDto? = null,
)

data class OrderItemDto(
    val id: String? = null,
    val productId: String,
    val quantity: Int,
    val price: Any? = null,
    val gstAmount: Any? = null,
    val discountAmount: Any? = null,
    val finalAmount: Any? = null,
    val product: ProductDto? = null,
)

data class UserBriefDto(
    val id: String,
    val name: String? = null,
    val email: String? = null,
)

data class MockPaymentResponse(
    val orderId: String? = null,
    val paymentGateway: String? = null,
    val status: String? = null,
    val message: String? = null,
)
