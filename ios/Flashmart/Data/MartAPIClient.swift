import Foundation

enum MartAPIError: LocalizedError {
    case unauthorized
    case http(Int, String)
    case network(Error)
    case decoding(Error)

    var errorDescription: String? {
        switch self {
        case .unauthorized:
            return "Session expired. Please sign in again."
        case .http(let code, let msg):
            if code == 401 { return msg }
            return "HTTP \(code): \(msg)"
        case .network(let e):
            return e.localizedDescription
        case .decoding(let e):
            return "Invalid response: \(e.localizedDescription)"
        }
    }
}

final class MartAPIClient {
    private let sessionStore: SessionStore
    private let localDemoStore: LocalDemoStore
    private let session: URLSession
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder

    init(sessionStore: SessionStore, localDemoStore: LocalDemoStore) {
        self.sessionStore = sessionStore
        self.localDemoStore = localDemoStore
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        self.session = URLSession(configuration: config)
        self.decoder = JSONDecoder()
        self.encoder = JSONEncoder()
    }

    private var useLocalDemo: Bool {
        AppConfig.useLocalDemoAuth || sessionStore.isLocalDemoMode
    }

    private func baseURL() -> URL { AppConfig.apiBaseURL }

    private func isPublicAuthPath(_ path: String) -> Bool {
        path.hasPrefix("auth/login")
            || path.hasPrefix("auth/forgot-password")
            || path.hasPrefix("auth/reset-password")
    }

    private func friendlyAuthFailureMessage(_ raw: String?) -> String {
        let msg = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if msg.isEmpty || msg == "Invalid credentials" || msg == "Unauthorized" {
            return "Invalid email or password"
        }
        return msg
    }

    private func apiErrorMessage(from data: Data) -> String? {
        struct ApiErr: Decodable {
            let message: MessageField?
            enum MessageField: Decodable {
                case string(String)
                case strings([String])
                init(from decoder: Decoder) throws {
                    let container = try decoder.singleValueContainer()
                    if let s = try? container.decode(String.self) {
                        self = .string(s)
                    } else {
                        self = .strings(try container.decode([String].self))
                    }
                }
                var text: String {
                    switch self {
                    case .string(let s): return s
                    case .strings(let arr): return arr.joined(separator: "\n")
                    }
                }
            }
        }
        if let parsed = try? decoder.decode(ApiErr.self, from: data),
           let message = parsed.message {
            let text = message.text.trimmingCharacters(in: .whitespacesAndNewlines)
            if !text.isEmpty { return text }
        }
        return String(data: data, encoding: .utf8)?
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }

    // MARK: - Auth

    func login(identifier: String, password: String) async throws -> LoginResponse {
        if AppConfig.useLocalDemoAuth {
            throw MartAPIError.http(400, "Use local demo login path")
        }
        return try await post("auth/login", body: LoginRequest(identifier: identifier, password: password))
    }

    func me() async throws -> AuthMe {
        if useLocalDemo, let user = sessionStore.user {
            return AuthMe(userId: user.id, email: user.email, role: user.role, companyId: user.companyId)
        }
        return try await get("auth/me")
    }

    func forgotPassword(email: String) async throws -> ForgotPasswordResponse {
        if useLocalDemo {
            return ForgotPasswordResponse(
                message: "Demo mode: use demo password or token from onboard screen.",
                userId: nil,
                loginEmail: email,
                resetPasswordToken: "demo-reset-token",
                resetPasswordExpiresAt: nil
            )
        }
        return try await post("auth/forgot-password", body: ForgotPasswordRequest(email: email))
    }

    func resetPassword(token: String, newPassword: String) async throws -> ResetPasswordResponse {
        if useLocalDemo {
            return ResetPasswordResponse(message: "Password updated (demo mode)")
        }
        return try await post("auth/reset-password", body: ResetPasswordRequest(token: token, newPassword: newPassword))
    }

    // MARK: - Products

    func products(search: String? = nil, brandId: String? = nil, shelf: String? = nil) async throws -> [Product] {
        if useLocalDemo {
            var list = localDemoStore.products()
            if let shelf, shelf != "All", !shelf.isEmpty {
                list = list.filter { $0.shelf?.uppercased() == shelf.uppercased() }
            }
            if let brandId { list = list.filter { $0.brandId == brandId } }
            if let search, !search.isEmpty {
                let q = search.lowercased()
                list = list.filter { $0.name.lowercased().contains(q) || ($0.brand?.name.lowercased().contains(q) ?? false) }
            }
            return list
        }
        var query: [String: String] = [:]
        if let search { query["search"] = search }
        if let brandId { query["brandId"] = brandId }
        if let shelf { query["shelf"] = shelf }
        return try await get("products", query: query)
    }

    func productById(_ id: String) async throws -> Product {
        if useLocalDemo {
            guard let p = localDemoStore.products().first(where: { $0.id == id }) else {
                throw MartAPIError.http(404, "Product not found")
            }
            return p
        }
        return try await get("products/\(id)")
    }

    func brands() async throws -> [Brand] {
        if useLocalDemo { return localDemoStore.brands() }
        return try await get("brands")
    }

    // MARK: - Orders

    func myOrders() async throws -> [Order] {
        if useLocalDemo, let user = sessionStore.user {
            return localDemoStore.ordersForActor(userId: user.id, role: user.role)
        }
        return try await get("orders/my")
    }

    func dealerOrders() async throws -> [Order] {
        if useLocalDemo, let user = sessionStore.user {
            return localDemoStore.ordersForActor(userId: user.id, role: user.role)
        }
        return try await get("orders/dealer")
    }

    func allOrders() async throws -> [Order] {
        if useLocalDemo { return localDemoStore.ordersForActor(userId: "", role: "ADMIN") }
        return try await get("orders")
    }

    func orderById(_ id: String) async throws -> Order {
        if useLocalDemo {
            guard let o = localDemoStore.orderById(id) else { throw MartAPIError.http(404, "Order not found") }
            return o
        }
        return try await get("orders/\(id)")
    }

    func shopkeeperSummary() async throws -> ShopkeeperSummary {
        if useLocalDemo, let user = sessionStore.user {
            return localDemoStore.shopkeeperSummary(userId: user.id)
        }
        return try await get("orders/my/summary")
    }

    func dealerSummary() async throws -> DealerSummary {
        if useLocalDemo, let user = sessionStore.user {
            return localDemoStore.dealerSummary(userId: user.id)
        }
        return try await get("orders/dealer/summary")
    }

    func createOrder(items: [CreateOrderItem]) async throws -> Order {
        if useLocalDemo, let user = sessionStore.user {
            if user.role.uppercased() == "DEALER" {
                return localDemoStore.createDealerRestockOrder(dealerId: user.id, items: items)
            }
            return localDemoStore.createOrder(actorId: user.id, items: items)
        }
        return try await post("orders", body: CreateOrderRequest(items: items))
    }

    func createDealerRestockOrder(items: [CreateOrderItem]) async throws -> Order {
        if useLocalDemo, let user = sessionStore.user {
            return localDemoStore.createDealerRestockOrder(dealerId: user.id, items: items)
        }
        return try await post("orders/dealer-restock", body: CreateOrderRequest(items: items))
    }

    func createDealerRestockOrderWithPayment(
        items: [CreateOrderItem],
        paymentMode: String = "RAZORPAY"
    ) async throws -> CreateOrderWithPaymentResponse {
        if useLocalDemo, let user = sessionStore.user {
            let order = localDemoStore.createDealerRestockOrder(dealerId: user.id, items: items)
            return CreateOrderWithPaymentResponse(
                orderId: order.id, status: order.status, message: "Restock order created (demo)",
                razorpayOrderId: nil, amount: Int((order.finalAmount?.doubleValue ?? 0) * 100), currency: "INR", keyId: AppConfig.razorpayKeyId
            )
        }
        return try await post(
            "orders/dealer-restock/create",
            body: CreateOrderWithPaymentRequest(items: items, paymentMode: paymentMode)
        )
    }

    func createOrderWithPayment(items: [CreateOrderItem], paymentMode: String = "RAZORPAY") async throws -> CreateOrderWithPaymentResponse {
        if useLocalDemo, let user = sessionStore.user {
            let order: Order
            if user.role.uppercased() == "DEALER" {
                order = localDemoStore.createDealerRestockOrder(dealerId: user.id, items: items)
            } else {
                order = localDemoStore.createOrder(actorId: user.id, items: items)
            }
            return CreateOrderWithPaymentResponse(
                orderId: order.id, status: order.status, message: "Order created (demo)",
                razorpayOrderId: nil, amount: Int((order.finalAmount?.doubleValue ?? 0) * 100), currency: "INR", keyId: AppConfig.razorpayKeyId
            )
        }
        return try await post("orders/create", body: CreateOrderWithPaymentRequest(items: items, paymentMode: paymentMode))
    }

    func createRazorpayOrder(orderId: String, currency: String = "INR") async throws -> CreateRazorpayOrderResponse {
        if useLocalDemo {
            let order = localDemoStore.orderById(orderId)
            let paise = Int((order?.finalAmount?.doubleValue ?? 100) * 100)
            return CreateRazorpayOrderResponse(
                orderId: orderId, amountPaise: paise, currency: currency,
                keyId: AppConfig.razorpayKeyId, razorpayOrderId: "order_demo_\(orderId)", paymentRecordId: nil
            )
        }
        return try await post("payments/razorpay/order", body: CreateRazorpayOrderRequest(orderId: orderId, currency: currency))
    }

    func verifyRazorpayPayment(_ body: VerifyRazorpayPaymentRequest) async throws -> VerifyRazorpayPaymentResponse {
        if useLocalDemo {
            _ = localDemoStore.mockPayment(orderId: body.orderId)
            return VerifyRazorpayPaymentResponse(verified: true, orderId: body.orderId, paymentStatus: "PAID")
        }
        return try await post("payments/razorpay/verify", body: body)
    }

    func mockPayment(orderId: String) async throws -> MockPaymentResponse {
        if useLocalDemo { return localDemoStore.mockPayment(orderId: orderId) }
        return try await post("orders/\(orderId)/payment/mock")
    }

    func confirmOrder(orderId: String) async throws -> Order {
        if useLocalDemo { return localDemoStore.confirmOrder(orderId: orderId) }
        return try await patch("orders/\(orderId)/confirm")
    }

    func markOutForDelivery(orderId: String) async throws -> Order {
        if useLocalDemo { return localDemoStore.markOutForDelivery(orderId: orderId) }
        return try await patch("orders/\(orderId)/dispatch")
    }

    func markDelivered(orderId: String) async throws -> Order {
        if useLocalDemo { return localDemoStore.markDelivered(orderId: orderId) }
        return try await patch("orders/\(orderId)/deliver")
    }

    func cancelOrder(orderId: String) async throws -> Order {
        if useLocalDemo { return localDemoStore.cancelOrder(orderId: orderId) }
        return try await patch("orders/\(orderId)/cancel")
    }

    // MARK: - Invoice

    func invoiceByOrder(_ orderId: String) async throws -> InvoiceDocument {
        if useLocalDemo { return localDemoStore.invoiceByOrder(orderId: orderId) }
        return try await get("invoices/by-order/\(orderId)")
    }

    // MARK: - Stock

    func stock() async throws -> [StockRow] {
        if useLocalDemo { return localDemoStore.stock() }
        return try await get("stock")
    }

    // MARK: - Users & areas

    func users(role: String? = nil, status: String? = nil) async throws -> [UserRow] {
        if useLocalDemo {
            var list = localDemoStore.users()
            if let role { list = list.filter { $0.role.uppercased() == role.uppercased() } }
            if let status { list = list.filter { $0.status.uppercased() == status.uppercased() } }
            return list
        }
        var query: [String: String] = [:]
        if let role { query["role"] = role }
        if let status { query["status"] = status }
        return try await get("users", query: query)
    }

    func pendingApprovalCount() async throws -> Int {
        if useLocalDemo { return localDemoStore.pendingApprovalCount() }
        let resp: PendingCountResponse = try await get("users/pending-count")
        return resp.count
    }

    func areas() async throws -> [Area] {
        if useLocalDemo { return localDemoStore.areas() }
        return try await get("areas")
    }

    func createShopkeeper(_ body: CreateShopkeeperRequest) async throws -> UserRow {
        if useLocalDemo, let actor = sessionStore.user {
            return localDemoStore.createShopkeeper(
                name: body.name, email: body.email, phone: body.phone,
                password: body.password,
                areaId: body.areaId, onboardedByEmployeeId: actor.id,
                notes: body.onboardingNotes, actorRole: actor.role
            )
        }
        return try await post("users/shopkeepers", body: body)
    }

    func createDealer(_ body: CreateDealerRequest) async throws -> CreateDealerResponse {
        if useLocalDemo, let actor = sessionStore.user {
            let row = localDemoStore.createDealer(
                name: body.name, email: body.email, phone: body.phone,
                password: body.password ?? LocalDemoAuth.demoPassword,
                areaId: body.areaId, onboardedByEmployeeId: actor.id,
                notes: body.onboardingNotes, actorRole: actor.role
            )
            return CreateDealerResponse(id: row.id, name: row.name, email: row.email, role: row.role, message: "Dealer onboarded (demo)")
        }
        return try await post("users/dealers", body: body)
    }

    func uploadOnboardingDocuments(userId: String, documents: [PendingOnboardingDocument]) async throws {
        if useLocalDemo {
            let persisted = OnboardingDocumentStorage.persistForUser(userId: userId, pending: documents)
            localDemoStore.attachOnboardingDocuments(userId: userId, documents: persisted)
            return
        }
        for doc in documents {
            _ = try await uploadOnboardingDocument(
                userId: userId,
                label: doc.label,
                fileURL: doc.localURL,
                fileName: doc.displayName,
                mimeType: doc.mimeType ?? "application/octet-stream"
            )
        }
    }

    func uploadOnboardingDocument(
        userId: String,
        label: String,
        fileURL: URL,
        fileName: String,
        mimeType: String
    ) async throws -> OnboardingDocument {
        if useLocalDemo {
            let pending = OnboardingDocumentStorage.stageDocument(from: fileURL, label: label)
            let docs = pending.map { OnboardingDocumentStorage.persistForUser(userId: userId, pending: [$0]) } ?? []
            if let doc = docs.first {
                localDemoStore.attachOnboardingDocuments(userId: userId, documents: docs)
                return doc
            }
            throw MartAPIError.http(400, "Could not stage document")
        }

        let boundary = "Boundary-\(UUID().uuidString)"
        var req = URLRequest(url: baseURL().appendingPathComponent("users/\(userId)/onboarding-documents"))
        req.httpMethod = "POST"
        req.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        if let token = sessionStore.token {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        let fileData = try Data(contentsOf: fileURL)
        var body = Data()
        body.appendFormField(name: "label", value: label, boundary: boundary)
        body.appendFileField(
            name: "file",
            fileName: fileName,
            mimeType: mimeType,
            fileData: fileData,
            boundary: boundary
        )
        body.append("--\(boundary)--\r\n".data(using: .utf8)!)
        req.httpBody = body

        let (data, response) = try await session.data(for: req)
        guard let http = response as? HTTPURLResponse else {
            throw MartAPIError.http(0, "Invalid response")
        }
        guard (200...299).contains(http.statusCode) else {
            throw MartAPIError.http(http.statusCode, apiErrorMessage(from: data) ?? "Upload failed")
        }
        return try decoder.decode(OnboardingDocument.self, from: data)
    }

    func createEmployee(_ body: CreateEmployeeRequest) async throws -> CreateEmployeeResponse {
        if useLocalDemo {
            let row = localDemoStore.createEmployee(name: body.name, email: body.email, phone: body.phone)
            return CreateEmployeeResponse(
                id: row.id, name: row.name, email: row.email, role: row.role,
                phone: row.phone,
                message: "Employee created (demo). Sign in with demo password.",
                emailSent: false
            )
        }
        return try await post("users/employees", body: body)
    }

    func approveUser(_ userId: String) async throws -> ApproveUserResponse {
        if useLocalDemo, let admin = sessionStore.user {
            let row = localDemoStore.approveUser(userId: userId, adminId: admin.id)
            let pwd = localDemoStore.approvalLoginPassword(forUserId: userId)
            return ApproveUserResponse(
                id: row.id, name: row.name, email: row.email, role: row.role, status: row.status,
                message: "User approved (demo). Login confirmation would be emailed.",
                loginEmail: row.email, loginPassword: pwd, emailSent: true
            )
        }
        return try await patch("users/\(userId)/approve")
    }

    func rejectUser(_ userId: String, reason: String?) async throws -> UserRow {
        if useLocalDemo { return localDemoStore.rejectUser(userId: userId, reason: reason) }
        return try await patch("users/\(userId)/reject", body: UpdateUserStatusRequest(reason: reason))
    }

    func deactivateUser(_ userId: String, reason: String?) async throws -> UserRow {
        if useLocalDemo { return localDemoStore.deactivateUser(userId: userId, reason: reason) }
        return try await patch("users/\(userId)/deactivate", body: UpdateUserStatusRequest(reason: reason))
    }

    func reactivateUser(_ userId: String) async throws -> UserRow {
        if useLocalDemo, let admin = sessionStore.user {
            return localDemoStore.reactivateUser(userId: userId, actorId: admin.id)
        }
        return try await patch("users/\(userId)/reactivate")
    }

    // MARK: - Catalog admin

    func createProduct(_ body: CreateProductRequest) async throws -> Product {
        if useLocalDemo { return localDemoStore.createProduct(body) }
        return try await post("products", body: body)
    }

    func updateProduct(_ id: String, body: UpdateProductRequest) async throws -> Product {
        if useLocalDemo { return localDemoStore.updateProduct(id: id, body: body) }
        return try await patch("products/\(id)", body: body)
    }

    func deleteProduct(_ id: String) async throws {
        if useLocalDemo {
            localDemoStore.deleteProduct(id: id)
            return
        }
        try await deleteRequest("products/\(id)")
    }

    func createBrand(_ body: CreateBrandRequest) async throws -> Brand {
        if useLocalDemo { return localDemoStore.createBrand(body) }
        return try await post("brands", body: body)
    }

    func updateBrand(_ id: String, body: UpdateBrandRequest) async throws -> Brand {
        if useLocalDemo { return localDemoStore.updateBrand(id: id, body: body) }
        return try await patch("brands/\(id)", body: body)
    }

    func deleteBrand(_ id: String) async throws {
        if useLocalDemo {
            localDemoStore.deleteBrand(id: id)
            return
        }
        try await deleteRequest("brands/\(id)")
    }

    // MARK: - HTTP helpers

    private func get<T: Decodable>(_ path: String, query: [String: String] = [:]) async throws -> T {
        try await request(path, method: "GET", query: query, body: Optional<EmptyResponse>.none)
    }

    private func post<T: Decodable>(_ path: String) async throws -> T {
        try await request(path, method: "POST", body: Optional<EmptyResponse>.none)
    }

    private func post<T: Decodable, B: Encodable>(_ path: String, body: B? = nil) async throws -> T {
        try await request(path, method: "POST", body: body)
    }

    private func patch<T: Decodable>(_ path: String) async throws -> T {
        try await request(path, method: "PATCH", body: Optional<EmptyResponse>.none)
    }

    private func patch<T: Decodable, B: Encodable>(_ path: String, body: B? = nil) async throws -> T {
        try await request(path, method: "PATCH", body: body)
    }

    private func deleteRequest(_ path: String) async throws {
        let _: EmptyResponse = try await request(path, method: "DELETE", body: Optional<EmptyResponse>.none)
    }

    private func request<T: Decodable, B: Encodable>(
        _ path: String, method: String, query: [String: String] = [:], body: B? = nil
    ) async throws -> T {
        var components = URLComponents(url: baseURL().appendingPathComponent(path), resolvingAgainstBaseURL: false)!
        if !query.isEmpty {
            components.queryItems = query.map { URLQueryItem(name: $0.key, value: $0.value) }
        }
        guard let url = components.url else { throw MartAPIError.http(0, "Invalid URL") }

        var req = URLRequest(url: url)
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        if let token = sessionStore.token, !isPublicAuthPath(path) {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        if let body {
            req.setValue("application/json", forHTTPHeaderField: "Content-Type")
            req.httpBody = try encoder.encode(body)
        }

        do {
            let (data, response) = try await session.data(for: req)
            guard let http = response as? HTTPURLResponse else {
                throw MartAPIError.http(0, "No response")
            }
            if http.statusCode == 401 {
                let serverMsg = apiErrorMessage(from: data)
                if isPublicAuthPath(path) {
                    throw MartAPIError.http(401, friendlyAuthFailureMessage(serverMsg))
                }
                sessionStore.clear()
                throw MartAPIError.unauthorized
            }
            guard (200...299).contains(http.statusCode) else {
                let msg = apiErrorMessage(from: data) ?? "Request failed"
                throw MartAPIError.http(http.statusCode, msg)
            }
            do {
                return try decoder.decode(T.self, from: data)
            } catch {
                throw MartAPIError.decoding(error)
            }
        } catch let e as MartAPIError {
            throw e
        } catch {
            throw MartAPIError.network(error)
        }
    }
}

private extension Data {
    mutating func appendFormField(name: String, value: String, boundary: String) {
        append("--\(boundary)\r\n".data(using: .utf8)!)
        append("Content-Disposition: form-data; name=\"\(name)\"\r\n\r\n".data(using: .utf8)!)
        append(value.data(using: .utf8)!)
        append("\r\n".data(using: .utf8)!)
    }

    mutating func appendFileField(
        name: String,
        fileName: String,
        mimeType: String,
        fileData: Data,
        boundary: String
    ) {
        append("--\(boundary)\r\n".data(using: .utf8)!)
        append("Content-Disposition: form-data; name=\"\(name)\"; filename=\"\(fileName)\"\r\n".data(using: .utf8)!)
        append("Content-Type: \(mimeType)\r\n\r\n".data(using: .utf8)!)
        append(fileData)
        append("\r\n".data(using: .utf8)!)
    }
}
