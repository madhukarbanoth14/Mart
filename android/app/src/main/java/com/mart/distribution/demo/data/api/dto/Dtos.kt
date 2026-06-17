package com.mart.distribution.demo.data.api.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val identifier: String,
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
    val companyId: String? = null,
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
    val brandId: String? = null,
    val brand: Brand? = null,
    /** FMCG shelf: STAPLES, OILS_GHEE, … (see backend `ProductShelf`). */
    val shelf: String? = null,
    @SerializedName("basePrice") val basePrice: Any?, // Prisma Decimal: string or number
    @SerializedName("gstPercentage") val gstPercentage: Any?,
    @SerializedName("dealerDiscount") val dealerDiscount: Any?,
    @SerializedName("shopkeeperDiscount") val shopkeeperDiscount: Any?,
    val imageUrl: String? = null,
    val sku: String? = null,
    val weight: String? = null,
    val caseQty: Int? = null,
    @SerializedName("gstRate") val gstRate: Any? = null,
    val isActive: Boolean? = true,
    @SerializedName("mrp") val mrp: Any? = null,
    @SerializedName("dealerPrice") val dealerPrice: Any? = null,
    @SerializedName("bulkShippingFee") val bulkShippingFee: Any? = null,
    val bulkShippingMinQty: Int? = null,
)

data class Brand(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val manufacturer: String? = null,
)

/** Response from `GET /products/paged`. */
data class ProductsPagedResponse(
    val items: List<ProductDto>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val hasNext: Boolean,
)

data class CreateProductRequest(
    val name: String,
    val brandType: String,
    val brandId: String? = null,
    val shelf: String,
    val basePrice: Double,
    val gstPercentage: Double,
    val dealerDiscount: Double,
    val shopkeeperDiscount: Double,
)

data class UpdateProductRequest(
    val name: String? = null,
    val brandType: String? = null,
    val brandId: String? = null,
    val shelf: String? = null,
    val basePrice: Double? = null,
    val gstPercentage: Double? = null,
    val dealerDiscount: Double? = null,
    val shopkeeperDiscount: Double? = null,
)

data class CreateBrandRequest(
    val name: String,
    val logoUrl: String? = null,
)

data class UpdateBrandRequest(
    val name: String? = null,
    val logoUrl: String? = null,
)

data class CreateOrderRequest(
    val items: List<CreateOrderItemDto>,
)

data class CreateOrderWithPaymentRequest(
    val items: List<CreateOrderItemDto>,
    val paymentMode: String = "COD",
)

data class CreateOrderItemDto(
    val productId: String,
    val quantity: Int,
)

data class OrderDto(
    val id: String,
    val status: String,
    val kind: String? = null,
    val paymentStatus: String? = null,
    val createdAt: String? = null,
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

data class CreateRazorpayOrderRequest(
    val orderId: String,
    val currency: String = "INR",
)

data class CreateRazorpayOrderResponse(
    val orderId: String,
    val amountPaise: Int,
    val currency: String,
    val keyId: String,
    val razorpayOrderId: String,
    val paymentRecordId: String,
)

data class VerifyRazorpayPaymentRequest(
    val orderId: String,
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String,
)

data class VerifyRazorpayPaymentResponse(
    val verified: Boolean,
    val orderId: String,
    val paymentStatus: String,
)

data class CreateOrderWithPaymentResponse(
    val orderId: String,
    val status: String? = null,
    val message: String? = null,
    val razorpayOrderId: String? = null,
    val amount: Int? = null,
    val currency: String? = null,
    val keyId: String? = null,
)

data class InvoiceDocumentDto(
    val invoiceNumber: String,
    val generatedAt: String?,
    val pdfUrl: String?,
    val order: OrderDto?,
)

data class StockRowDto(
    val id: String,
    val quantity: Int,
    val product: ProductDto?,
    val dealer: UserBriefDto? = null,
)

data class UserAreaBriefDto(
    val id: String? = null,
    val name: String? = null,
)

/** GET /areas — dealer is optional on client; server may include nested dealer. */
data class AreaDto(
    val id: String,
    val name: String,
    val dealerId: String? = null,
    val dealer: UserBriefDto? = null,
)

data class CreateShopkeeperRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val password: String,
    val areaId: String,
    val onboardingNotes: String? = null,
)

data class CreateDealerRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val password: String? = null,
    val areaId: String,
    val onboardingNotes: String? = null,
)

data class CreateEmployeeRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val password: String? = null,
)

data class CreateEmployeeResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val loginEmail: String? = null,
    val loginPassword: String? = null,
    val message: String? = null,
    val emailSent: Boolean? = null,
    val emailError: String? = null,
)

data class CreateDealerResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val loginEmail: String? = null,
    val userId: String? = null,
    val loginPassword: String? = null,
    val resetPasswordToken: String? = null,
    val resetPasswordExpiresAt: String? = null,
    val message: String? = null,
    val emailSent: Boolean? = null,
    val emailError: String? = null,
)

data class ForgotPasswordRequest(
    val email: String,
)

data class ForgotPasswordResponse(
    val message: String,
    val userId: String? = null,
    val loginEmail: String? = null,
    val resetPasswordToken: String? = null,
    val resetPasswordExpiresAt: String? = null,
    val emailSent: Boolean? = null,
    val emailError: String? = null,
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
)

data class ResetPasswordResponse(
    val message: String,
)

data class OnboardingDocumentDto(
    val id: String,
    val label: String,
    val fileName: String,
    val mimeType: String? = null,
    val fileSize: Long? = null,
    val uploadedAt: String? = null,
    /** Offline demo: absolute path on device for admin preview. */
    val localPath: String? = null,
)

data class UserRowDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val area: UserAreaBriefDto? = null,
    val onboardedById: String? = null,
    val onboardingNotes: String? = null,
    val onboardingDocuments: List<OnboardingDocumentDto> = emptyList(),
    val createdAt: String? = null,
    val status: String = "ACTIVE",
    val statusReason: String? = null,
    val approvedAt: String? = null,
    val onboardedBy: UserBriefDto? = null,
    val approvedBy: UserBriefDto? = null,
)

data class PendingCountResponse(
    val count: Int,
)

data class UpdateUserStatusRequest(
    val reason: String? = null,
)

data class ApproveUserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val message: String? = null,
    val loginPassword: String? = null,
    val resetPasswordToken: String? = null,
    val resetPasswordExpiresAt: String? = null,
    val emailSent: Boolean? = null,
    val emailError: String? = null,
)

data class ShopkeeperSummaryDto(
    val openOrders: Int,
    val inDelivery: Int,
    val lastTotal: Double? = null,
    val invoicesReady: Int,
)

data class DealerSummaryDto(
    val pendingOrders: Int,
    val todaysDeliveries: Int,
    val weeklyRevenue: Double,
)
