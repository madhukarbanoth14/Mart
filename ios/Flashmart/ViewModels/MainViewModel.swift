import Foundation
import Observation

@Observable
final class MainViewModel {
    var products: LoadState<[Product]> = .idle
    var brands: LoadState<[Brand]> = .idle
    var orders: LoadState<[Order]> = .idle
    var stock: LoadState<[StockRow]> = .idle
    var users: LoadState<[UserRow]> = .idle
    var areas: LoadState<[Area]> = .idle
    var shopkeeperSummary: ShopkeeperSummary?
    var dealerSummary: DealerSummary?
    var pendingCount = 0
    var searchText = ""
    var selectedShelf = "All"
    var selectedBrandId: String?
    var placeOrderMessage: String?
    var placeOrderError: String?
    var paymentMessage: String?
    var lastPlacedOrder: Order?
    var pendingRazorpay: PendingRazorpayCheckout?

    private let apiClient: MartAPIClient
    private let sessionStore: SessionStore
    let cartStore: CartStore
    private let localDemoStore: LocalDemoStore

    init(apiClient: MartAPIClient, sessionStore: SessionStore, cartStore: CartStore, localDemoStore: LocalDemoStore) {
        self.apiClient = apiClient
        self.sessionStore = sessionStore
        self.cartStore = cartStore
        self.localDemoStore = localDemoStore
    }

    var currentUser: SessionUser? { sessionStore.user }
    var cartLines: [CartLine] { cartStore.lines }
    var cartCount: Int { cartStore.itemCount }

    func cartTotal(forRole role: String) -> Double {
        cartLines.reduce(0) { sum, line in
            sum + line.product.catalogUnitPrice(forRole: role) * Double(line.quantity)
        }
    }

    @MainActor
    func refreshForRole() async {
        guard let user = sessionStore.user else { return }
        await loadBrands()
        switch user.userRole {
        case .shopkeeper:
            async let o: Void = loadMyOrders()
            async let s: Void = loadShopkeeperSummary()
            async let p: Void = loadProducts()
            _ = await (o, s, p)
        case .dealer:
            async let o: Void = loadDealerOrders()
            async let s: Void = loadDealerSummary()
            async let st: Void = loadStock()
            async let p: Void = loadProducts()
            _ = await (o, s, st, p)
        case .admin:
            async let o: Void = loadAllOrders()
            async let u: Void = loadUsers()
            async let c: Void = loadPendingCount()
            async let p: Void = loadProducts()
            _ = await (o, u, c, p)
        case .employee:
            async let u: Void = loadUsers()
            async let a: Void = loadAreas()
            _ = await (u, a)
        }
    }

    @MainActor
    func loadProducts() async {
        products = .loading
        do {
            let shelf = selectedShelf == "All" ? nil : selectedShelf
            let list = try await apiClient.products(
                search: searchText.isEmpty ? nil : searchText,
                brandId: selectedBrandId,
                shelf: shelf
            )
            products = .ok(list)
        } catch {
            products = .err(error.localizedDescription)
        }
    }

    @MainActor
    func loadBrands() async {
        brands = .loading
        do {
            brands = .ok(try await apiClient.brands())
        } catch {
            brands = .err(error.localizedDescription)
        }
    }

    @MainActor
    func loadMyOrders() async {
        orders = .loading
        do {
            orders = .ok(try await apiClient.myOrders())
        } catch {
            orders = .err(error.localizedDescription)
        }
    }

    @MainActor
    func loadDealerOrders() async {
        orders = .loading
        do {
            orders = .ok(try await apiClient.dealerOrders())
        } catch {
            orders = .err(error.localizedDescription)
        }
    }

    @MainActor
    func loadAllOrders() async {
        orders = .loading
        do {
            orders = .ok(try await apiClient.allOrders())
        } catch {
            orders = .err(error.localizedDescription)
        }
    }

    @MainActor
    func loadStock() async {
        stock = .loading
        do {
            stock = .ok(try await apiClient.stock())
        } catch {
            stock = .err(error.localizedDescription)
        }
    }

    @MainActor
    func loadUsers(role: String? = nil) async {
        users = .loading
        do {
            users = .ok(try await apiClient.users(role: role))
        } catch {
            users = .err(error.localizedDescription)
        }
    }

    @MainActor
    func loadAreas() async {
        areas = .loading
        do {
            areas = .ok(try await apiClient.areas())
        } catch {
            areas = .err(error.localizedDescription)
        }
    }

    @MainActor
    func loadShopkeeperSummary() async {
        do { shopkeeperSummary = try await apiClient.shopkeeperSummary() } catch { }
    }

    @MainActor
    func loadDealerSummary() async {
        do { dealerSummary = try await apiClient.dealerSummary() } catch { }
    }

    @MainActor
    func loadPendingCount() async {
        do { pendingCount = try await apiClient.pendingApprovalCount() } catch { pendingCount = 0 }
    }

    @MainActor
    func placeOrderFromCart() async -> Order? {
        guard !cartStore.lines.isEmpty else {
            placeOrderError = "Cart is empty"
            return nil
        }
        placeOrderError = nil
        placeOrderMessage = nil
        pendingRazorpay = nil
        let items = cartStore.lines.map { CreateOrderItem(productId: $0.product.id, quantity: $0.quantity) }
        do {
            let order = try await createOrderForCurrentRole(items: items)
            lastPlacedOrder = order
            cartStore.clear()
            placeOrderMessage = order.kind?.uppercased() == "DEALER_RESTOCK"
                ? "Restock order \(order.id) submitted for admin approval"
                : "Order \(order.id) placed"
            await refreshOrdersForCurrentRole()
            return order
        } catch {
            placeOrderError = error.localizedDescription
            return nil
        }
    }

    private func createOrderForCurrentRole(items: [CreateOrderItem]) async throws -> Order {
        let role = sessionStore.user?.role.uppercased() ?? "SHOPKEEPER"
        if role == "DEALER" {
            return try await apiClient.createDealerRestockOrder(items: items)
        }
        return try await apiClient.createOrder(items: items)
    }

    private func createOrderWithPaymentForCurrentRole(
        items: [CreateOrderItem],
        paymentMode: String
    ) async throws -> CreateOrderWithPaymentResponse {
        let role = sessionStore.user?.role.uppercased() ?? "SHOPKEEPER"
        if role == "DEALER" {
            return try await apiClient.createDealerRestockOrderWithPayment(items: items, paymentMode: paymentMode)
        }
        return try await apiClient.createOrderWithPayment(items: items, paymentMode: paymentMode)
    }

    /// Demo card/UPI/COD — creates order and marks mock-paid (matches Android `placeOrderFromCartWithDemoPayment`).
    @MainActor
    func placeOrderWithDemoPayment(method: String) async -> Order? {
        guard !cartStore.lines.isEmpty else {
            placeOrderError = "Cart is empty"
            return nil
        }
        placeOrderError = nil
        placeOrderMessage = nil
        pendingRazorpay = nil
        let items = cartStore.lines.map { CreateOrderItem(productId: $0.product.id, quantity: $0.quantity) }
        let role = sessionStore.user?.role.uppercased() ?? "SHOPKEEPER"
        do {
            let order: Order
            if role == "DEALER", method.lowercased() == "wallet" || method.lowercased() == "cod" {
                let created = try await createOrderWithPaymentForCurrentRole(items: items, paymentMode: "COD")
                order = try await apiClient.orderById(created.orderId)
            } else {
                order = try await createOrderForCurrentRole(items: items)
                _ = try await apiClient.mockPayment(orderId: order.id)
            }
            lastPlacedOrder = order
            cartStore.clear()
            let label: String
            if role == "DEALER" {
                switch method.lowercased() {
                case "wallet", "cod":
                    label = "Restock order placed. Pay on delivery."
                case "card":
                    label = "Restock order placed. Demo card payment successful."
                case "upi":
                    label = "Restock order placed. Demo UPI payment successful."
                default:
                    label = "Restock order placed and paid."
                }
            } else {
                switch method.lowercased() {
                case "card": label = "Order placed. Demo card payment successful."
                case "upi": label = "Order placed. Demo UPI payment successful."
                case "wallet", "cod": label = "Order placed. Pay on delivery."
                default: label = "Order placed and paid."
                }
            }
            placeOrderMessage = label
            paymentMessage = (method.lowercased() == "wallet" || method.lowercased() == "cod") ? nil : "Payment successful."
            await refreshOrdersForCurrentRole()
            return order
        } catch {
            placeOrderError = error.localizedDescription
            return nil
        }
    }

    /// Live API: create order then stage Razorpay checkout (matches Android `placeOrderFromCartRazorpay`).
    @MainActor
    func placeOrderForRazorpay() async -> PendingRazorpayCheckout? {
        guard !cartStore.lines.isEmpty else {
            placeOrderError = "Cart is empty"
            return nil
        }
        placeOrderError = nil
        placeOrderMessage = nil
        pendingRazorpay = nil

        if AppConfig.useLocalDemoAuth || sessionStore.isLocalDemoMode {
            if let order = await placeOrderWithDemoPayment(method: "card") {
                placeOrderMessage = "Demo mode — Razorpay skipped. Order \(order.id) paid."
            }
            return nil
        }

        let items = cartStore.lines.map { CreateOrderItem(productId: $0.product.id, quantity: $0.quantity) }
        let email = sessionStore.user?.email ?? ""
        let role = sessionStore.user?.role.uppercased() ?? "SHOPKEEPER"
        do {
            let created: CreateOrderWithPaymentResponse
            do {
                created = try await createOrderWithPaymentForCurrentRole(items: items, paymentMode: "RAZORPAY")
            } catch {
                let order = try await createOrderForCurrentRole(items: items)
                let rz = try await apiClient.createRazorpayOrder(orderId: order.id)
                cartStore.clear()
                lastPlacedOrder = order
                await refreshOrdersForCurrentRole()
                let pending = PendingRazorpayCheckout(
                    appOrderId: order.id, gatewayOrderId: rz.razorpayOrderId,
                    keyId: rz.keyId, amountPaise: rz.amountPaise, currency: rz.currency, userEmail: email
                )
                pendingRazorpay = pending
                placeOrderMessage = role == "DEALER"
                    ? "Restock order created. Complete Razorpay payment."
                    : "Order created. Complete Razorpay payment."
                return pending
            }

            let gatewayId: String
            let keyId: String
            let amountPaise: Int
            let currency: String
            if let rzId = created.razorpayOrderId, let k = created.keyId, let amt = created.amount {
                gatewayId = rzId
                keyId = k
                amountPaise = amt
                currency = created.currency ?? "INR"
            } else {
                let rz = try await apiClient.createRazorpayOrder(orderId: created.orderId)
                gatewayId = rz.razorpayOrderId
                keyId = rz.keyId
                amountPaise = rz.amountPaise
                currency = rz.currency
            }

            cartStore.clear()
            if let order = try? await apiClient.orderById(created.orderId) {
                lastPlacedOrder = order
            }
            await refreshOrdersForCurrentRole()

            let pending = PendingRazorpayCheckout(
                appOrderId: created.orderId, gatewayOrderId: gatewayId,
                keyId: keyId, amountPaise: amountPaise, currency: currency, userEmail: email
            )
            pendingRazorpay = pending
            placeOrderMessage = role == "DEALER"
                ? "Restock order created. Complete Razorpay payment."
                : "Order created. Complete Razorpay payment."
            return pending
        } catch {
            placeOrderError = error.localizedDescription
            return nil
        }
    }

    @MainActor
    func initRazorpayForOrder(_ orderId: String) async -> PendingRazorpayCheckout? {
        placeOrderError = nil
        let email = sessionStore.user?.email ?? ""
        if AppConfig.useLocalDemoAuth || sessionStore.isLocalDemoMode {
            _ = try? await apiClient.mockPayment(orderId: orderId)
            paymentMessage = "Demo mode payment marked successful."
            await refreshOrdersForCurrentRole()
            return nil
        }
        do {
            let rz = try await apiClient.createRazorpayOrder(orderId: orderId)
            let pending = PendingRazorpayCheckout(
                appOrderId: orderId, gatewayOrderId: rz.razorpayOrderId,
                keyId: rz.keyId, amountPaise: rz.amountPaise, currency: rz.currency, userEmail: email
            )
            pendingRazorpay = pending
            return pending
        } catch {
            placeOrderError = error.localizedDescription
            return nil
        }
    }

    @MainActor
    func onRazorpayResult(_ result: RazorpayPaymentResult, appOrderId: String) async -> Bool {
        pendingRazorpay = nil
        guard result.success,
              let gatewayOrderId = result.orderId,
              let paymentId = result.paymentId,
              let signature = result.signature else {
            paymentMessage = result.error ?? "Payment cancelled"
            return false
        }
        do {
            let verified = try await apiClient.verifyRazorpayPayment(VerifyRazorpayPaymentRequest(
                orderId: appOrderId,
                razorpayOrderId: gatewayOrderId,
                razorpayPaymentId: paymentId,
                razorpaySignature: signature
            ))
            paymentMessage = verified.verified ? "Payment successful." : "Payment verification failed."
            await refreshOrdersForCurrentRole()
            return verified.verified
        } catch {
            paymentMessage = error.localizedDescription
            return false
        }
    }

    func clearPendingRazorpay() {
        pendingRazorpay = nil
    }

    @MainActor
    func mockPay(orderId: String) async -> Bool {
        do {
            let resp = try await apiClient.mockPayment(orderId: orderId)
            paymentMessage = resp.message ?? "Payment successful"
            await refreshOrdersForCurrentRole()
            return true
        } catch {
            paymentMessage = error.localizedDescription
            return false
        }
    }

    @MainActor
    func confirmOrder(_ orderId: String) async {
        do {
            _ = try await apiClient.confirmOrder(orderId: orderId)
            await refreshOrdersForCurrentRole()
        } catch { placeOrderError = error.localizedDescription }
    }

    @MainActor
    func dispatchOrder(_ orderId: String) async {
        do {
            _ = try await apiClient.markOutForDelivery(orderId: orderId)
            await refreshOrdersForCurrentRole()
        } catch { placeOrderError = error.localizedDescription }
    }

    @MainActor
    func deliverOrder(_ orderId: String) async {
        do {
            _ = try await apiClient.markDelivered(orderId: orderId)
            await refreshOrdersForCurrentRole()
            if sessionStore.user?.role.uppercased() == "DEALER" {
                await loadStock()
            }
        } catch { placeOrderError = error.localizedDescription }
    }

    @MainActor
    func cancelOrder(_ orderId: String) async {
        do {
            _ = try await apiClient.cancelOrder(orderId: orderId)
            await refreshOrdersForCurrentRole()
        } catch { placeOrderError = error.localizedDescription }
    }

    @MainActor
    func approveUser(_ userId: String) async -> String? {
        do {
            let resp = try await apiClient.approveUser(userId)
            await loadUsers()
            await loadPendingCount()
            var msg = resp.message ?? "User approved."
            if resp.emailSent == true {
                placeOrderError = nil
                return msg
            }
            if let err = resp.emailError, !err.isEmpty {
                msg += "\n\nEmail not sent: \(err)"
            }
            if let login = resp.loginEmail ?? Optional(resp.email), let pwd = resp.loginPassword {
                msg += "\n\nShare login credentials:\nEmail: \(login)\nPassword: \(pwd)"
            }
            placeOrderError = nil
            return msg
        } catch {
            placeOrderError = error.localizedDescription
            return nil
        }
    }

    @MainActor
    func rejectUser(_ userId: String) async {
        do {
            _ = try await apiClient.rejectUser(userId, reason: "Rejected by admin")
            await loadUsers()
            await loadPendingCount()
        } catch { placeOrderError = error.localizedDescription }
    }

    @MainActor
    func deactivateUser(_ userId: String, reason: String? = nil) async {
        do {
            _ = try await apiClient.deactivateUser(userId, reason: reason)
            await loadUsers()
            await loadPendingCount()
        } catch { placeOrderError = error.localizedDescription }
    }

    @MainActor
    func reactivateUser(_ userId: String) async {
        do {
            _ = try await apiClient.reactivateUser(userId)
            await loadUsers()
            await loadPendingCount()
        } catch { placeOrderError = error.localizedDescription }
    }

    @MainActor
    func createEmployee(name: String, email: String, phone: String, password: String?) async -> String? {
        if let phoneErr = FieldValidators.phoneOptional(phone) {
            placeOrderError = phoneErr
            return nil
        }
        let normalizedPhone = phone.isEmpty ? nil : FieldValidators.normalizedPhone(phone)
        do {
            let resp = try await apiClient.createEmployee(CreateEmployeeRequest(
                name: name,
                email: email,
                phone: normalizedPhone,
                password: password?.isEmpty == false ? password : nil
            ))
            await loadUsers()
            var msg = resp.message ?? "Employee created."
            if let pwd = resp.loginPassword {
                let login = resp.loginEmail ?? email
                msg += "\n\nShare credentials:\nEmail: \(login)\nPassword: \(pwd)"
            }
            placeOrderError = nil
            return msg
        } catch {
            placeOrderError = error.localizedDescription
            return nil
        }
    }

    @MainActor
    func onboardShopkeeper(
        name: String,
        email: String,
        phone: String,
        password: String,
        areaId: String,
        notes: String,
        documents: [PendingOnboardingDocument] = []
    ) async -> Bool {
        if let docErr = OnboardingDocumentStorage.validateRequired(
            slots: OnboardingDocumentStorage.shopkeeperSlots(),
            documents: documents
        ) {
            placeOrderError = docErr
            return false
        }
        if let phoneErr = FieldValidators.phoneOptional(phone) {
            placeOrderError = phoneErr
            return false
        }
        if let pwdErr = FieldValidators.password(password) {
            placeOrderError = pwdErr
            return false
        }
        let trimmedAreaId = areaId.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmedAreaId.isEmpty {
            placeOrderError = "Area is required"
            return false
        }
        let normalizedPhone = phone.isEmpty ? nil : FieldValidators.normalizedPhone(phone)
        let trimmedPassword = password.trimmingCharacters(in: .whitespacesAndNewlines)
        do {
            let created = try await apiClient.createShopkeeper(CreateShopkeeperRequest(
                name: name, email: email, phone: normalizedPhone,
                password: trimmedPassword, areaId: trimmedAreaId, onboardingNotes: notes.isEmpty ? nil : notes
            ))
            if !documents.isEmpty {
                try await apiClient.uploadOnboardingDocuments(userId: created.id, documents: documents)
            }
            await loadUsers()
            placeOrderError = nil
            return true
        } catch {
            placeOrderError = error.localizedDescription
            return false
        }
    }

    @MainActor
    func onboardDealer(
        name: String,
        email: String,
        phone: String,
        password: String,
        areaId: String,
        notes: String,
        documents: [PendingOnboardingDocument] = []
    ) async -> Bool {
        if let docErr = OnboardingDocumentStorage.validateRequired(
            slots: OnboardingDocumentStorage.dealerSlots(),
            documents: documents
        ) {
            placeOrderError = docErr
            return false
        }
        if let phoneErr = FieldValidators.phoneOptional(phone) {
            placeOrderError = phoneErr
            return false
        }
        if let pwdErr = FieldValidators.password(password) {
            placeOrderError = pwdErr
            return false
        }
        let trimmedAreaId = areaId.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmedAreaId.isEmpty {
            placeOrderError = "Area is required"
            return false
        }
        let normalizedPhone = phone.isEmpty ? nil : FieldValidators.normalizedPhone(phone)
        let trimmedPassword = password.trimmingCharacters(in: .whitespacesAndNewlines)
        do {
            let created = try await apiClient.createDealer(CreateDealerRequest(
                name: name, email: email, phone: normalizedPhone,
                password: trimmedPassword, areaId: trimmedAreaId, onboardingNotes: notes.isEmpty ? nil : notes
            ))
            if !documents.isEmpty {
                try await apiClient.uploadOnboardingDocuments(userId: created.id, documents: documents)
            }
            await loadUsers()
            await loadAreas()
            placeOrderError = nil
            return true
        } catch {
            placeOrderError = error.localizedDescription
            return false
        }
    }

    func addToCart(_ product: Product, qty: Int = 1) {
        cartStore.add(product: product, quantity: qty)
    }

    func setCartQty(productId: String, qty: Int) {
        cartStore.setQuantity(productId: productId, quantity: qty)
    }

    @MainActor
    func createProduct(
        name: String,
        brandType: String = "OWN",
        shelf: String,
        basePrice: Double,
        gstPercentage: Double = 18,
        dealerDiscount: Double = 10,
        shopkeeperDiscount: Double = 5
    ) async -> Bool {
        do {
            _ = try await apiClient.createProduct(CreateProductRequest(
                name: name, brandType: brandType, brandId: nil, shelf: shelf,
                basePrice: basePrice, gstPercentage: gstPercentage,
                dealerDiscount: dealerDiscount, shopkeeperDiscount: shopkeeperDiscount
            ))
            await loadProducts()
            placeOrderError = nil
            return true
        } catch {
            placeOrderError = error.localizedDescription
            return false
        }
    }

    @MainActor
    func updateProduct(
        id: String,
        name: String,
        brandType: String,
        shelf: String,
        basePrice: Double,
        gstPercentage: Double,
        dealerDiscount: Double,
        shopkeeperDiscount: Double
    ) async -> Bool {
        do {
            _ = try await apiClient.updateProduct(id, body: UpdateProductRequest(
                name: name, brandType: brandType, brandId: nil, shelf: shelf,
                basePrice: basePrice, gstPercentage: gstPercentage,
                dealerDiscount: dealerDiscount, shopkeeperDiscount: shopkeeperDiscount
            ))
            await loadProducts()
            placeOrderError = nil
            return true
        } catch {
            placeOrderError = error.localizedDescription
            return false
        }
    }

    @MainActor
    func bulkCreateProducts(_ rows: [CreateProductRequest]) async -> Bool {
        do {
            for row in rows {
                _ = try await apiClient.createProduct(row)
            }
            await loadProducts()
            placeOrderError = nil
            return true
        } catch {
            placeOrderError = error.localizedDescription
            return false
        }
    }

    @MainActor
    func deleteProduct(_ id: String) async {
        do {
            try await apiClient.deleteProduct(id)
            await loadProducts()
        } catch { placeOrderError = error.localizedDescription }
    }

    @MainActor
    func createBrand(name: String, logoUrl: String? = nil) async -> Bool {
        do {
            _ = try await apiClient.createBrand(CreateBrandRequest(name: name, logoUrl: logoUrl))
            await loadBrands()
            return true
        } catch {
            placeOrderError = error.localizedDescription
            return false
        }
    }

    @MainActor
    func deleteBrand(_ id: String) async {
        do {
            try await apiClient.deleteBrand(id)
            await loadBrands()
        } catch { placeOrderError = error.localizedDescription }
    }

    @MainActor
    private func refreshOrdersForCurrentRole() async {
        guard let user = sessionStore.user else { return }
        switch user.userRole {
        case .shopkeeper: await loadMyOrders()
        case .dealer: await loadDealerOrders()
        case .admin: await loadAllOrders()
        default: break
        }
    }

    func filteredOrders(segment: String) -> [Order] {
        guard case .ok(let list) = orders else { return [] }
        switch segment {
        case "Pending":
            return list.filter { $0.status.uppercased() == "PENDING" }
        case "Active":
            return list.filter { ["DEALER_CONFIRMED", "ACCEPTED", "OUT_FOR_DELIVERY"].contains($0.status.uppercased()) }
        case "Done":
            return list.filter { ["DELIVERED", "CANCELLED"].contains($0.status.uppercased()) }
        default:
            return list
        }
    }
}
