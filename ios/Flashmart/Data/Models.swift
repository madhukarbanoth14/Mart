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

struct EmptyResponse: Codable {}

// MARK: - Session

struct SessionUser: Codable, Equatable, Identifiable {
    let id: String
    let name: String
    let email: String
    let role: String
    var companyId: String?

    var userRole: UserRole { UserRole(apiRole: role) }
}

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
    let quantity: Int
    let product: Product?
    var dealer: UserBrief?
}

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
    var mimeType: String?
    var fileSize: Int64?
    var uploadedAt: String?
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

// MARK: - Cart

struct CartLine: Identifiable, Equatable {
    let product: Product
    var quantity: Int

    var id: String { product.id }
}

// MARK: - Navigation routes

enum AppRoute: Hashable {
    case productDetail(String)
    case orderDetail(String)
    case cart
    case payment(String)
    case invoice(String)
    case tracking(String)
    case resetPassword(token: String?)
    case onboardShopkeeper
    case onboardDealer
    case createEmployee
    case skuManagement
    case brandsManagement
    case checkout
    case profileStoreAddress
    case profilePaymentMethods
    case profileGstDetails
    case profileNotifications
    case profileHelp
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
