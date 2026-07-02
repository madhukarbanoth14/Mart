import Foundation

// MARK: - Auth

struct LoginRequest: Codable {
    let identifier: String
    let password: String
}

struct LoginResponse: Codable {
    let accessToken: String
    let user: LoginUser
}

struct LoginUser: Codable {
    let id: String
    let name: String
    let email: String
    let role: String
    let companyId: String?
}

struct AuthMe: Codable {
    let userId: String
    let email: String
    let role: String
    let companyId: String?
    var name: String?
    var phone: String?
    var documentUploaded: Bool?
    var canPlaceOrders: Bool?
    var documentStatus: String?
    var area: UserAreaBrief?
    var assignedDealer: UserBrief?
}

struct SessionUser: Codable, Equatable, Identifiable {
    let id: String
    let name: String
    let email: String
    let role: String
    var companyId: String?
    var canPlaceOrders: Bool = true
    var documentStatus: String = "NOT_UPLOADED"
    var areaName: String? = nil
    var assignedDealer: UserBrief? = nil

    var userRole: UserRole { UserRole(apiRole: role) }
}

struct ForgotPasswordRequest: Codable { let email: String }

struct ForgotPasswordResponse: Codable {
    let message: String
    let userId: String?
    let loginEmail: String?
    let resetPasswordToken: String?
    let resetPasswordExpiresAt: String?
}

struct ResetPasswordRequest: Codable {
    let token: String
    let newPassword: String
}

struct ResetPasswordResponse: Codable { let message: String }

struct SendOtpRequest: Codable {
    let phone: String
    var purpose: String = "REGISTER"
}

struct SendOtpResponse: Codable {
    let success: Bool
    let message: String
    var expiresInSeconds: Int?
    var devOtp: String?
}

struct VerifyOtpRequest: Codable {
    let phone: String
    let code: String
}

struct VerifyOtpResponse: Codable {
    let success: Bool
    let verificationToken: String
    let phone: String
}

struct RegistrationGeoResponse: Codable {
    let states: [RegistrationState]
}

struct RegistrationState: Codable, Equatable {
    let name: String
    let districts: [String]
}

struct RegistrationArea: Codable, Identifiable, Equatable {
    let id: String
    let name: String
    var state: String?
    var district: String?
    var dealerId: String?
}

struct SelfRegisterRequest: Codable {
    var verificationToken: String?
    var phone: String?
    let name: String
    var email: String?
    var password: String?
    let areaId: String
    let state: String
    let district: String
    let address: String
    let shopName: String
    var latitude: Double?
    var longitude: Double?
    var referralCode: String?
}

struct EmptyResponse: Codable {}

// MARK: - Product

struct Brand: Codable, Identifiable, Equatable, Hashable {
    let id: String
    let name: String
    let logoUrl: String?
    var manufacturer: String?
}

struct Product: Codable, Identifiable, Equatable, Hashable {
    let id: String
    let name: String
    let brandType: String
    var brandId: String?
    var brand: Brand?
    var shelf: String?
    var basePrice: APIValue?
    var gstPercentage: APIValue?
    var dealerDiscount: APIValue?
    var shopkeeperDiscount: APIValue?
    var imageUrl: String?
    var sku: String?
    var weight: String?
    var caseQty: Int?
    var gstRate: APIValue?
    var isActive: Bool?
    var mrp: APIValue?
    var dealerPrice: APIValue?
}

struct ProductsPagedResponse: Codable {
    let items: [Product]
    let page: Int
    let limit: Int
    let total: Int
    let hasNext: Bool
}

struct CreateProductRequest: Codable {
    let name: String
    let brandType: String
    let brandId: String?
    let shelf: String
    let basePrice: Double
    let gstPercentage: Double
    let dealerDiscount: Double
    let shopkeeperDiscount: Double
}

struct UpdateProductRequest: Codable {
    var name: String?
    var brandType: String?
    var brandId: String?
    var shelf: String?
    var basePrice: Double?
    var gstPercentage: Double?
    var dealerDiscount: Double?
    var shopkeeperDiscount: Double?
}

struct CreateBrandRequest: Codable {
    let name: String
    let logoUrl: String?
}

struct UpdateBrandRequest: Codable {
    var name: String?
    var logoUrl: String?
}

// MARK: - Orders

struct CreateOrderRequest: Codable {
    let items: [CreateOrderItem]
}

struct CreateOrderItem: Codable {
    let productId: String
    let quantity: Int
}

struct Order: Codable, Identifiable, Equatable {
    let id: String
    var status: String
    var kind: String?
    var paymentStatus: String?
    var createdAt: String?
    var shopkeeperId: String?
    var dealerId: String?
    var totalAmount: APIValue?
    var gstAmount: APIValue?
    var discountAmount: APIValue?
    var finalAmount: APIValue?
    var items: [OrderItem]?
    var shopkeeper: UserBrief?
    var dealer: UserBrief?
    var returnReason: String?
    var returnRequestedAt: String?
    var returnedAt: String?
    var refundedAt: String?
}

struct OrderItem: Codable, Identifiable, Equatable {
    var id: String?
    let productId: String
    let quantity: Int
    var price: APIValue?
    var gstAmount: APIValue?
    var discountAmount: APIValue?
    var finalAmount: APIValue?
    var product: Product?
}

struct UserBrief: Codable, Equatable {
    let id: String
    var name: String?
    var email: String?
}

struct MockPaymentResponse: Codable {
    let orderId: String?
    let paymentGateway: String?
    let status: String?
    let message: String?
}

struct CreateOrderWithPaymentRequest: Codable {
    let items: [CreateOrderItem]
    let paymentMode: String
}

struct CreateOrderWithPaymentResponse: Codable {
    let orderId: String
    var status: String?
    var message: String?
    var razorpayOrderId: String?
    var amount: Int?
    var currency: String?
    var keyId: String?
}

struct CreateRazorpayOrderRequest: Codable {
    let orderId: String
    let currency: String
}

struct CreateRazorpayOrderResponse: Codable {
    let orderId: String
    let amountPaise: Int
    let currency: String
    let keyId: String
    let razorpayOrderId: String
    let paymentRecordId: String?
}

struct VerifyRazorpayPaymentRequest: Codable {
    let orderId: String
    let razorpayOrderId: String
    let razorpayPaymentId: String
    let razorpaySignature: String
}

struct VerifyRazorpayPaymentResponse: Codable {
    let verified: Bool
    let orderId: String
    let paymentStatus: String
}

struct PendingRazorpayCheckout: Equatable {
    let appOrderId: String
    let gatewayOrderId: String
    let keyId: String
    let amountPaise: Int
    let currency: String
    let userEmail: String
    let userPhone: String?
    let paymentMethod: String?
}

struct ShopkeeperSummary: Codable {
    let openOrders: Int
    let inDelivery: Int
    let lastTotal: Double?
    let invoicesReady: Int
}

struct DealerSummary: Codable {
    let pendingOrders: Int
    let todaysDeliveries: Int
    let weeklyRevenue: Double
}

// MARK: - Invoice

struct InvoiceDocument: Codable {
    let invoiceNumber: String
    let generatedAt: String?
    let pdfUrl: String?
    let order: Order?
}

// MARK: - Stock

struct StockRow: Codable, Identifiable {
    let id: String
    var quantity: Int
    let product: Product?
    var dealer: UserBrief?
}

struct UpdateStockRequest: Codable { let quantity: Int }

struct UpsertStockRequest: Codable {
    let productId: String
    let quantity: Int
}

struct CreateAreaRequest: Codable { let name: String }

struct UpdateAreaRequest: Codable {
    var name: String?
    var dealerId: String?
}

struct OrderReturnRequest: Codable { let reason: String }

struct OrderReturnRejectRequest: Codable { var note: String? }

// MARK: - Users & areas

struct UserAreaBrief: Codable, Equatable {
    var id: String?
    var name: String?
}

struct Area: Codable, Identifiable, Equatable {
    let id: String
    let name: String
    var dealerId: String?
    var dealer: UserBrief?
}

struct UserRow: Codable, Identifiable, Equatable {
    let id: String
    let name: String
    let email: String
    let role: String
    var phone: String?
    var area: UserAreaBrief?
    var onboardedById: String?
    var onboardingNotes: String?
    var onboardingDocuments: [OnboardingDocument]?
    var createdAt: String?
    var status: String
    var statusReason: String?
    var approvedAt: String?
    var documentUploaded: Bool?
    var canPlaceOrders: Bool?
    var documentStatus: String?
    var lastFollowUpAt: String?
    var totalOrders: Int?
    var onboardedBy: UserBrief?
    var approvedBy: UserBrief?
}

struct CreateShopkeeperRequest: Codable {
    let name: String
    let email: String
    let phone: String?
    let password: String
    let areaId: String
    let onboardingNotes: String?
}

struct CreateDealerRequest: Codable {
    let name: String
    let email: String
    let phone: String?
    let password: String?
    let areaId: String
    let onboardingNotes: String?
}

struct CreateEmployeeRequest: Codable {
    let name: String
    let email: String
    let phone: String?
    let password: String?
}

struct CreateEmployeeResponse: Codable {
    let id: String
    let name: String
    let email: String
    let role: String
    var phone: String?
    var loginEmail: String?
    var loginPassword: String?
    var message: String?
    var emailSent: Bool?
    var emailError: String?
}

struct CreateDealerResponse: Codable {
    let id: String
    let name: String
    let email: String
    let role: String
    var message: String?
}

struct PendingCountResponse: Codable { let count: Int }

struct ApproveUserResponse: Codable {
    let id: String
    let name: String
    let email: String
    let role: String
    let status: String
    var message: String?
    var loginEmail: String?
    var loginPassword: String?
    var resetPasswordToken: String?
    var resetPasswordExpiresAt: String?
    var emailSent: Bool?
    var emailError: String?
}

struct UpdateUserStatusRequest: Codable { var reason: String? }

struct OnboardingDocument: Codable, Identifiable, Equatable {
    let id: String
    let label: String
    let fileName: String
    var documentType: String?
    var mimeType: String?
    var fileSize: Int64?
    var uploadedAt: String?
    var verificationStatus: String?
    var verifiedAt: String?
    var rejectionReason: String?
}

// MARK: - API value helper

enum APIValue: Codable, Equatable, Hashable {
    case double(Double)
    case string(String)
    case int(Int)

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let d = try? container.decode(Double.self) {
            self = .double(d)
        } else if let i = try? container.decode(Int.self) {
            self = .int(i)
        } else if let s = try? container.decode(String.self), let d = Double(s) {
            self = .double(d)
        } else {
            self = .string(try container.decode(String.self))
        }
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        switch self {
        case .double(let d): try container.encode(d)
        case .int(let i): try container.encode(i)
        case .string(let s): try container.encode(s)
        }
    }

    var doubleValue: Double? {
        switch self {
        case .double(let d): d
        case .int(let i): Double(i)
        case .string(let s): Double(s)
        }
    }
}

// MARK: - Ordering config & reorder

struct OrderingConfig: Codable {
    let minOrderQuantity: Int
    let maxOrderQuantity: Int
    let quickQuantityChips: [Int]
}

struct ReorderPreviewItem: Codable {
    let productId: String
    let quantity: Int
    let product: Product?
}

struct ReorderSkippedItem: Codable {
    let productId: String
    let productName: String?
    let reason: String
}

struct ReorderPreview: Codable {
    let items: [ReorderPreviewItem]
    let warnings: [String]
    let skipped: [ReorderSkippedItem]
}

// MARK: - Cart

struct CartLine: Identifiable, Equatable {
    let product: Product
    var quantity: Int

    var id: String { product.id }
}

// MARK: - Finance & settlements

struct FinanceOverview: Codable {
    let period: FinancePeriod?
    let collections: FinanceCollections?
    let revenue: FinanceRevenue?
}

struct FinancePeriod: Codable {
    let from: String?
    let to: String?
    let filter: String?
}

struct FinanceCollections: Codable {
    let total: Double?
    let successful: Int?
    let failed: Int?
    let refunded: Int?
    let pending: Int?
}

struct FinanceRevenue: Codable {
    let gmv: Double?
    let platformCommission: Double?
    let dealerPayables: Double?
    let settledAmount: Double?
    let pendingSettlement: Double?
    let netPlatformEarnings: Double?
    let refunds: Double?
}

struct InvestorDashboard: Codable {
    let period: FinancePeriod?
    let collections: FinanceCollections?
    let revenue: FinanceRevenue?
    let business: InvestorBusiness?
    let charts: InvestorCharts?
}

struct InvestorBusiness: Codable {
    let totalOrders: Int?
    let deliveredOrders: Int?
    let activeDealers: Int?
    let activeShopkeepers: Int?
    let activeEmployees: Int?
}

struct InvestorCharts: Codable {
    let dailyRevenueTrend: [FinanceTrendPoint]?
}

struct FinanceTrendPoint: Codable {
    let date: String
    let gmv: Double?
    let commission: Double?
}

struct CommissionRule: Codable, Identifiable {
    let id: String
    let ruleType: String
    let rate: APIValue
    let dealerId: String?
    let productId: String?
    let active: Bool?
    let dealer: FinanceUserBrief?
    let product: FinanceProductBrief?
}

struct FinanceUserBrief: Codable {
    let id: String
    let name: String
    let shopName: String?
    let phone: String?
    let email: String?
}

struct FinanceProductBrief: Codable {
    let id: String
    let name: String
}

struct UpsertCommissionRuleRequest: Encodable {
    let ruleType: String
    let rate: Double
    let dealerId: String?
    let productId: String?
}

struct DealerSettlement: Codable, Identifiable {
    let id: String
    let settlementCode: String
    let dealerId: String
    let settlementStartDate: String
    let settlementEndDate: String
    let totalOrders: Int?
    let totalQuantity: Int?
    let grossSales: APIValue?
    let gstAmount: APIValue?
    let commissionAmount: APIValue?
    let dealerPayable: APIValue?
    let settledAmount: APIValue?
    let balanceAmount: APIValue?
    let settlementStatus: String
    let paymentMethod: String?
    let transactionReference: String?
    let utrNumber: String?
    let paymentDate: String?
    let remarks: String?
    let dealer: FinanceUserBrief?
    let payments: [DealerPaymentHistory]?
}

struct DealerPaymentHistory: Codable, Identifiable {
    let id: String
    let amount: APIValue
    let paymentMethod: String
    let utrNumber: String?
    let transactionReference: String?
    let paymentDate: String
    let remarks: String?
}

struct GenerateSettlementRequest: Encodable {
    let dealerId: String
    let startDate: String
    let endDate: String
}

struct RecordSettlementPaymentRequest: Encodable {
    let amount: Double
    let paymentMethod: String
    let utrNumber: String?
    let transactionReference: String?
    let paymentDate: String
    let remarks: String?
}

struct BackfillRevenuesResponse: Decodable {
    let created: Int?
}

struct DealerPerformance: Codable {
    let period: FinancePeriod?
    let summary: DealerPerformanceSummary?
    let topProducts: [DealerTopProduct]?
    let dailySales: [FinanceTrendPoint]?
}

struct DealerPerformanceSummary: Codable {
    let ordersDelivered: Int?
    let ordersTotal: Int?
    let grossSales: Double?
    let commissionDeducted: Double?
    let dealerEarnings: Double?
    let pendingSettlement: Double?
}

struct DealerTopProduct: Codable, Identifiable {
    var id: String { name }
    let name: String
    let qty: Int?
    let revenue: Double?
}

struct FinanceAuditLog: Codable, Identifiable {
    let id: String
    let action: String
    let entityType: String
    let entityId: String
    let createdAt: String
    let actor: FinanceUserBrief?
}

// MARK: - Returns & refunds

struct ReturnRequestItem: Codable, Identifiable {
    let id: String
    let productId: String
    let productName: String
    let quantity: Int
    let unitAmount: Double?
    let lineAmount: Double?
}

struct RefundRequestSummary: Codable, Identifiable {
    let id: String
    let refundCode: String
    let status: String
    let amount: Double?
}

struct ReturnRequestSummary: Codable, Identifiable {
    let id: String
    let returnCode: String
    let reason: String
    let reasonText: String?
    let status: String
    let refundAmount: Double?
    let shopkeeper: FinanceUserBrief?
}

struct ReturnRequest: Codable, Identifiable {
    let id: String
    let returnCode: String
    let orderId: String
    let reason: String
    let reasonText: String?
    let comments: String?
    let status: String
    let dealerRemarks: String?
    let refundAmount: Double?
    let createdAt: String?
    let items: [ReturnRequestItem]?
    let shopkeeper: FinanceUserBrief?
    let dealer: FinanceUserBrief?
    let refundRequest: RefundRequestSummary?
}

struct RefundRequest: Codable, Identifiable {
    let id: String
    let refundCode: String
    let returnRequestId: String
    let orderId: String
    let amount: Double?
    let status: String
    let dealerRemarks: String?
    let adminRemarks: String?
    let refundMethod: String?
    let transactionReference: String?
    let refundDate: String?
    let createdAt: String?
    let dealer: FinanceUserBrief?
    let returnRequest: ReturnRequestSummary?
}

struct CreateReturnItemRequest: Codable {
    let productId: String
    let quantity: Int
}

struct CreateReturnRequest: Codable {
    let reason: String
    let reasonText: String?
    let comments: String?
    let items: [CreateReturnItemRequest]
}

struct ReturnActionRequest: Codable { let remarks: String? }

struct ProcessRefundRequest: Codable {
    let refundMethod: String
    let transactionReference: String
    let remarks: String?
}

struct DealerRevenueSummary: Codable {
    let todayRevenue: Double?
    let weeklyRevenue: Double?
    let monthlyRevenue: Double?
    let totalOrdersReceived: Int?
    let totalOrdersDelivered: Int?
    let totalProductsSold: Int?
    let totalQuantitySold: Int?
    let pendingOrders: Int?
    let returnedOrders: Int?
    let pendingSettlementAmount: Double?
    let amountReceivedFromFlashMart: Double?
}

struct DealerTrendPoint: Codable {
    let date: String
    let amount: Double?
}

struct DealerProductSale: Codable, Identifiable {
    var id: String { productId ?? name }
    let productId: String?
    let name: String
    let quantity: Int?
    let revenue: Double?
}

struct DealerRevenueCharts: Codable {
    let dailyRevenue: [DealerTrendPoint]?
    let topSellingProducts: [DealerProductSale]?
}

struct DealerRevenueDashboard: Codable {
    let summary: DealerRevenueSummary?
    let charts: DealerRevenueCharts?
}

struct DealerShopkeeperRow: Codable, Identifiable {
    var id: String { shopkeeperId }
    let shopkeeperId: String
    let name: String
    let area: String?
    let totalOrders: Int?
    let totalPurchaseValue: Double?
    let outstandingOrders: Int?
    let returnedOrders: Int?
    let totalRevenue: Double?
    let lastOrderDate: String?
}

// MARK: - Navigation routes

enum AppRoute: Hashable {
    case productDetail(String)
    case orderDetail(String)
    case cart
    case payment(String)
    case invoice(String)
    case tracking(String)
    case orderConfirmation(String)
    case resetPassword(token: String?)
    case onboardShopkeeper
    case onboardDealer
    case createEmployee
    case skuManagement
    case brandsManagement
    case areasManagement
    case checkout
    case wallet
    case profileStoreAddress
    case profilePaymentMethods
    case profileGstDetails
    case profileNotifications
    case profileHelp
    case profileDocuments
    case privacyPolicy
    case adminReview(String)
    case financeSettlement(String)
    case financeDealer(String, String)
    case dealerRevenue
    case dealerShopkeepers
    case dealerReturns
    case dealerReturnDetail(String)
    case adminRefundDetail(String)
}

// MARK: - Load state

enum LoadState<T> {
    case idle
    case loading
    case ok(T)
    case err(String)
}

// MARK: - Shelf labels

enum ProductShelf: String, CaseIterable {
    case all = "All"
    case staples = "STAPLES"
    case oils = "OILS_GHEE"
    case sugar = "SUGAR_SALT_BASICS"
    case beverages = "BEVERAGES"
    case snacks = "SNACKS_BISCUITS"
    case home = "HOME_CARE"

    var label: String {
        switch self {
        case .all: "All"
        case .staples: "Staples"
        case .oils: "Oils"
        case .sugar: "Basics"
        case .beverages: "Beverages"
        case .snacks: "Snacks"
        case .home: "Home care"
        }
    }
}
