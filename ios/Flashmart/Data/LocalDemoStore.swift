import Foundation

/// In-memory API stand-in for local-demo sessions (mirrors Android LocalDemoMartStore).
final class LocalDemoStore {
    private let lock = NSLock()
    private var orders: [Order] = []
    private var brandRows: [Brand] = []
    private var productRows: [Product] = []
    private var areaRows: [Area] = []
    private var onboardedRows: [UserRow] = []
    private var onboardedSessions: [String: SessionUser] = [:]
    private var onboardedPasswords: [String: String] = [:]

    init() {
        resetForNewSession()
    }

    func resetForNewSession() {
        lock.lock()
        defer { lock.unlock() }
        orders = [seedPendingOrder()]
        brandRows = Self.staticBrands
        productRows = Self.staticProducts
        areaRows = [
            Area(id: "area-central", name: "Central Zone", dealerId: Self.dealerId, dealer: Self.dealerBrief),
        ]
    }

    func areas() -> [Area] {
        lock.lock(); defer { lock.unlock() }
        return areaRows
    }

    func products() -> [Product] {
        lock.lock(); defer { lock.unlock() }
        return productRows
    }

    func brands() -> [Brand] {
        lock.lock(); defer { lock.unlock() }
        return brandRows.sorted { $0.name.lowercased() < $1.name.lowercased() }
    }

    func stock() -> [StockRow] {
        lock.lock(); defer { lock.unlock() }
        return productRows.enumerated().map { idx, p in
            StockRow(id: "demo-stock-\(p.id)", quantity: 60 + idx * 5, product: p, dealer: Self.dealerBrief)
        }
    }

    func users() -> [UserRow] {
        lock.lock(); defer { lock.unlock() }
        return Self.staticUsers + onboardedRows
    }

    func pendingApprovalCount() -> Int {
        lock.lock(); defer { lock.unlock() }
        return onboardedRows.filter {
            $0.status == "PENDING_APPROVAL" && ($0.role == "DEALER" || $0.role == "SHOPKEEPER")
        }.count
    }

    func ordersForActor(userId: String, role: String) -> [Order] {
        lock.lock(); defer { lock.unlock() }
        switch role.uppercased() {
        case "SHOPKEEPER":
            return orders.filter { $0.shopkeeperId == userId }
        case "DEALER":
            return orders.filter {
                $0.dealerId == userId || ($0.kind?.uppercased() == "DEALER_RESTOCK" && $0.shopkeeperId == userId)
            }
        default:
            return orders
        }
    }

    func orderById(_ orderId: String) -> Order? {
        lock.lock(); defer { lock.unlock() }
        return orders.first { $0.id == orderId }
    }

    func createOrder(actorId: String, items: [CreateOrderItem]) -> Order {
        lock.lock()
        let shopkeeper = userBriefById(actorId) ?? UserBrief(id: actorId, name: "Shopkeeper", email: nil)
        let shopkeeperAreaId = (Self.staticUsers + onboardedRows).first { $0.id == actorId }?.area?.id ?? "area-central"
        let assignedDealer = areaRows.first { $0.id == shopkeeperAreaId }?.dealer ?? Self.dealerBrief
        let productById = Dictionary(uniqueKeysWithValues: productRows.map { ($0.id, $0) })
        lock.unlock()

        var orderItems: [OrderItem] = []
        var subtotal = 0.0, gst = 0.0, discount = 0.0

        for line in items {
            guard let p = productById[line.productId] else { continue }
            let unit = p.dealerPrice?.doubleValue ?? p.basePrice?.doubleValue ?? 80
            let discPct = (p.shopkeeperDiscount?.doubleValue ?? 5) / 100
            let lineSub = unit * Double(line.quantity)
            let lineDisc = lineSub * discPct
            let taxable = lineSub - lineDisc
            let lineGst = taxable * 0.18
            let final = taxable + lineGst
            subtotal += lineSub; discount += lineDisc; gst += lineGst
            orderItems.append(OrderItem(
                productId: line.productId, quantity: line.quantity,
                price: .double(unit), gstAmount: .double(lineGst),
                discountAmount: .double(lineDisc), finalAmount: .double(final), product: p
            ))
        }

        let order = Order(
            id: "demo-local-\(Int(Date().timeIntervalSince1970 * 1000))",
            status: "PENDING",
            kind: "SHOPKEEPER_ORDER",
            paymentStatus: "UNPAID",
            shopkeeperId: actorId,
            dealerId: assignedDealer.id,
            totalAmount: .double(Self.round2(subtotal)),
            gstAmount: .double(Self.round2(gst)),
            discountAmount: .double(Self.round2(discount)),
            finalAmount: .double(Self.round2(subtotal - discount + gst)),
            items: orderItems,
            shopkeeper: shopkeeper,
            dealer: assignedDealer
        )

        lock.lock()
        orders.insert(order, at: 0)
        lock.unlock()
        return order
    }

    func createDealerRestockOrder(dealerId: String, items: [CreateOrderItem]) -> Order {
        lock.lock()
        let dealer = userBriefById(dealerId) ?? Self.dealerBrief
        let warehouse = UserBrief(id: "demo-user-admin", name: "FlashMart Admin", email: "admin@martdemo.com")
        let productById = Dictionary(uniqueKeysWithValues: productRows.map { ($0.id, $0) })
        lock.unlock()

        var orderItems: [OrderItem] = []
        var subtotal = 0.0, gst = 0.0, discount = 0.0

        for line in items {
            guard let p = productById[line.productId] else { continue }
            let unit = p.dealerPrice?.doubleValue ?? p.basePrice?.doubleValue ?? 80
            let discPct = (p.dealerDiscount?.doubleValue ?? 10) / 100
            let lineSub = unit * Double(line.quantity)
            let lineDisc = lineSub * discPct
            let taxable = lineSub - lineDisc
            let lineGst = taxable * 0.18
            subtotal += lineSub; discount += lineDisc; gst += lineGst
            orderItems.append(OrderItem(
                productId: line.productId, quantity: line.quantity,
                price: .double(unit), gstAmount: .double(lineGst),
                discountAmount: .double(lineDisc), finalAmount: .double(taxable + lineGst), product: p
            ))
        }

        let order = Order(
            id: "demo-restock-\(Int(Date().timeIntervalSince1970 * 1000))",
            status: "PENDING",
            kind: "DEALER_RESTOCK",
            paymentStatus: "UNPAID",
            shopkeeperId: dealerId,
            dealerId: warehouse.id,
            totalAmount: .double(Self.round2(subtotal)),
            gstAmount: .double(Self.round2(gst)),
            discountAmount: .double(Self.round2(discount)),
            finalAmount: .double(Self.round2(subtotal - discount + gst)),
            items: orderItems,
            shopkeeper: dealer,
            dealer: warehouse
        )

        lock.lock()
        orders.insert(order, at: 0)
        lock.unlock()
        return order
    }

    func mockPayment(orderId: String) -> MockPaymentResponse {
        lock.lock()
        defer { lock.unlock() }
        guard let idx = orders.firstIndex(where: { $0.id == orderId }) else {
            fatalError("Order not found")
        }
        orders[idx].paymentStatus = "PAID"
        return MockPaymentResponse(orderId: orderId, paymentGateway: "MOCK_CARD", status: "SUCCEEDED", message: "Payment successful (demo card)")
    }

    func confirmOrder(orderId: String) -> Order {
        lock.lock(); defer { lock.unlock() }
        guard let idx = orders.firstIndex(where: { $0.id == orderId }) else { fatalError("Order not found") }
        orders[idx].status = "DEALER_CONFIRMED"
        return orders[idx]
    }

    func markOutForDelivery(orderId: String) -> Order {
        lock.lock(); defer { lock.unlock() }
        guard let idx = orders.firstIndex(where: { $0.id == orderId }) else { fatalError("Order not found") }
        orders[idx].status = "OUT_FOR_DELIVERY"
        return orders[idx]
    }

    func markDelivered(orderId: String) -> Order {
        lock.lock(); defer { lock.unlock() }
        guard let idx = orders.firstIndex(where: { $0.id == orderId }) else { fatalError("Order not found") }
        orders[idx].status = "DELIVERED"
        return orders[idx]
    }

    func cancelOrder(orderId: String) -> Order {
        lock.lock(); defer { lock.unlock() }
        guard let idx = orders.firstIndex(where: { $0.id == orderId }) else { fatalError("Order not found") }
        orders[idx].status = "CANCELLED"
        return orders[idx]
    }

    func invoiceByOrder(orderId: String) -> InvoiceDocument {
        lock.lock()
        let order = orders.first { $0.id == orderId }
        lock.unlock()
        guard let o = order else { fatalError("Order not found") }
        return InvoiceDocument(
            invoiceNumber: "INV-OFFLINE-\(String(o.id.suffix(8)).uppercased())",
            generatedAt: ISO8601DateFormatter().string(from: Date()),
            pdfUrl: nil, order: o
        )
    }

    func shopkeeperSummary(userId: String) -> ShopkeeperSummary {
        let mine = ordersForActor(userId: userId, role: "SHOPKEEPER")
        return ShopkeeperSummary(
            openOrders: mine.filter { !["DELIVERED", "CANCELLED"].contains($0.status.uppercased()) }.count,
            inDelivery: mine.filter { $0.status.uppercased() == "OUT_FOR_DELIVERY" }.count,
            lastTotal: mine.first.flatMap { $0.finalAmount?.doubleValue },
            invoicesReady: mine.filter { $0.paymentStatus?.uppercased() == "PAID" }.count
        )
    }

    func dealerSummary(userId: String) -> DealerSummary {
        let mine = ordersForActor(userId: userId, role: "DEALER")
        let paid = mine.filter { $0.paymentStatus?.uppercased() == "PAID" }
        return DealerSummary(
            pendingOrders: mine.filter { $0.status.uppercased() == "PENDING" }.count,
            todaysDeliveries: mine.filter { $0.status.uppercased() == "OUT_FOR_DELIVERY" }.count,
            weeklyRevenue: paid.reduce(0) { $0 + ($1.finalAmount?.doubleValue ?? 0) }
        )
    }

    func createShopkeeper(name: String, email: String, phone: String?, password: String, areaId: String, onboardedByEmployeeId: String, notes: String?, actorRole: String) -> UserRow {
        lock.lock(); defer { lock.unlock() }
        let key = email.trimmingCharacters(in: .whitespaces).lowercased()
        let id = "demo-user-local-\(UUID().uuidString.replacingOccurrences(of: "-", with: "").prefix(12))"
        let status = actorRole.uppercased() == "ADMIN" ? "ACTIVE" : "PENDING_APPROVAL"
        let row = UserRow(
            id: id, name: name.trimmingCharacters(in: .whitespaces), email: key, role: "SHOPKEEPER",
            phone: phone, area: UserAreaBrief(id: areaId, name: "Central Zone"),
            onboardedById: onboardedByEmployeeId, onboardingNotes: notes,
            status: status, onboardedBy: userBriefById(onboardedByEmployeeId)
        )
        onboardedRows.append(row)
        onboardedPasswords[key] = password
        if status == "ACTIVE" { registerActiveSession(row) }
        return row
    }

    func attachOnboardingDocuments(userId: String, documents: [OnboardingDocument]) {
        lock.lock(); defer { lock.unlock() }
        guard let idx = onboardedRows.firstIndex(where: { $0.id == userId }) else { return }
        var row = onboardedRows[idx]
        row.onboardingDocuments = documents
        onboardedRows[idx] = row
    }

    func createDealer(name: String, email: String, phone: String?, password: String, areaId: String, onboardedByEmployeeId: String, notes: String?, actorRole: String) -> UserRow {
        lock.lock(); defer { lock.unlock() }
        let key = email.trimmingCharacters(in: .whitespaces).lowercased()
        let id = "demo-user-dealer-local-\(UUID().uuidString.replacingOccurrences(of: "-", with: "").prefix(12))"
        let status = actorRole.uppercased() == "ADMIN" ? "ACTIVE" : "PENDING_APPROVAL"
        let row = UserRow(
            id: id, name: name.trimmingCharacters(in: .whitespaces), email: key, role: "DEALER",
            phone: phone, onboardedById: onboardedByEmployeeId, onboardingNotes: notes,
            status: status, onboardedBy: userBriefById(onboardedByEmployeeId)
        )
        onboardedRows.append(row)
        onboardedPasswords[key] = password
        if let idx = areaRows.firstIndex(where: { $0.id == areaId }) {
            areaRows[idx].dealerId = id
            areaRows[idx].dealer = UserBrief(id: id, name: name, email: key)
        }
        if status == "ACTIVE" { registerActiveSession(row) }
        return row
    }

    func createEmployee(name: String, email: String, phone: String?) -> UserRow {
        lock.lock(); defer { lock.unlock() }
        let key = email.trimmingCharacters(in: .whitespaces).lowercased()
        let id = "demo-user-employee-local-\(UUID().uuidString.replacingOccurrences(of: "-", with: "").prefix(12))"
        let row = UserRow(
            id: id, name: name.trimmingCharacters(in: .whitespaces), email: key, role: "EMPLOYEE",
            phone: phone, status: "ACTIVE"
        )
        onboardedRows.append(row)
        registerActiveSession(row)
        return row
    }

    func approveUser(userId: String, adminId: String) -> UserRow {
        lock.lock(); defer { lock.unlock() }
        guard let idx = onboardedRows.firstIndex(where: { $0.id == userId }) else { fatalError("User not found") }
        var row = onboardedRows[idx]
        row.status = "ACTIVE"
        row.approvedAt = ISO8601DateFormatter().string(from: Date())
        row.approvedBy = userBriefById(adminId)
        onboardedRows[idx] = row
        registerActiveSession(row)
        return row
    }

    func rejectUser(userId: String, reason: String?) -> UserRow {
        lock.lock(); defer { lock.unlock() }
        guard let idx = onboardedRows.firstIndex(where: { $0.id == userId }) else { fatalError("User not found") }
        onboardedSessions.removeValue(forKey: onboardedRows[idx].email.lowercased())
        var row = onboardedRows[idx]
        row.status = "REJECTED"
        row.statusReason = reason ?? "Rejected by admin"
        onboardedRows[idx] = row
        return row
    }

    func deactivateUser(userId: String, reason: String?) -> UserRow {
        lock.lock(); defer { lock.unlock() }
        guard let idx = onboardedRows.firstIndex(where: { $0.id == userId }) else { fatalError("User not found") }
        onboardedSessions.removeValue(forKey: onboardedRows[idx].email.lowercased())
        var row = onboardedRows[idx]
        row.status = "DEACTIVATED"
        row.statusReason = reason ?? "Deactivated by admin"
        onboardedRows[idx] = row
        return row
    }

    func reactivateUser(userId: String, actorId: String) -> UserRow {
        lock.lock(); defer { lock.unlock() }
        guard let idx = onboardedRows.firstIndex(where: { $0.id == userId }) else { fatalError("User not found") }
        var row = onboardedRows[idx]
        row.status = "ACTIVE"
        row.statusReason = nil
        row.approvedAt = ISO8601DateFormatter().string(from: Date())
        row.approvedBy = userBriefById(actorId)
        onboardedRows[idx] = row
        registerActiveSession(row)
        return row
    }

    func createProduct(_ body: CreateProductRequest) -> Product {
        lock.lock(); defer { lock.unlock() }
        let id = "demo-product-local-\(UUID().uuidString.prefix(8))"
        let brand = brandRows.first { $0.id == body.brandId }
        let product = Product(
            id: id, name: body.name, brandType: body.brandType, brandId: body.brandId, brand: brand,
            shelf: body.shelf, basePrice: .double(body.basePrice), gstPercentage: .double(body.gstPercentage),
            dealerDiscount: .double(body.dealerDiscount), shopkeeperDiscount: .double(body.shopkeeperDiscount),
            imageUrl: nil, sku: nil, weight: nil, caseQty: nil, gstRate: nil, isActive: true,
            mrp: .double(body.basePrice * 1.1), dealerPrice: .double(body.basePrice)
        )
        productRows.append(product)
        return product
    }

    func updateProduct(id: String, body: UpdateProductRequest) -> Product {
        lock.lock(); defer { lock.unlock() }
        guard let idx = productRows.firstIndex(where: { $0.id == id }) else { fatalError("Product not found") }
        let old = productRows[idx]
        let brand = brandRows.first { $0.id == (body.brandId ?? old.brandId) }
        let updated = Product(
            id: old.id,
            name: body.name ?? old.name,
            brandType: body.brandType ?? old.brandType,
            brandId: body.brandId ?? old.brandId,
            brand: brand ?? old.brand,
            shelf: body.shelf ?? old.shelf,
            basePrice: body.basePrice.map { .double($0) } ?? old.basePrice,
            gstPercentage: body.gstPercentage.map { .double($0) } ?? old.gstPercentage,
            dealerDiscount: body.dealerDiscount.map { .double($0) } ?? old.dealerDiscount,
            shopkeeperDiscount: body.shopkeeperDiscount.map { .double($0) } ?? old.shopkeeperDiscount,
            imageUrl: old.imageUrl,
            sku: old.sku,
            weight: old.weight,
            caseQty: old.caseQty,
            gstRate: old.gstRate,
            isActive: old.isActive,
            mrp: old.mrp,
            dealerPrice: body.basePrice.map { .double($0) } ?? old.dealerPrice
        )
        productRows[idx] = updated
        return updated
    }

    func deleteProduct(id: String) {
        lock.lock(); defer { lock.unlock() }
        productRows.removeAll { $0.id == id }
    }

    func createBrand(_ body: CreateBrandRequest) -> Brand {
        lock.lock(); defer { lock.unlock() }
        let brand = Brand(id: "demo-brand-local-\(UUID().uuidString.prefix(8))", name: body.name, logoUrl: body.logoUrl)
        brandRows.append(brand)
        return brand
    }

    func updateBrand(id: String, body: UpdateBrandRequest) -> Brand {
        lock.lock(); defer { lock.unlock() }
        guard let idx = brandRows.firstIndex(where: { $0.id == id }) else { fatalError("Brand not found") }
        let old = brandRows[idx]
        let updated = Brand(id: old.id, name: body.name ?? old.name, logoUrl: body.logoUrl ?? old.logoUrl)
        brandRows[idx] = updated
        return updated
    }

    func deleteBrand(id: String) {
        lock.lock(); defer { lock.unlock() }
        brandRows.removeAll { $0.id == id }
    }

    func approvalLoginPassword(forUserId userId: String) -> String? {
        lock.lock(); defer { lock.unlock() }
        guard let row = onboardedRows.first(where: { $0.id == userId }) else { return nil }
        return onboardedPasswords[row.email.lowercased()]
    }

    func tryResolveOnboardedSession(email: String, password: String) -> SessionUser? {
        let key = email.trimmingCharacters(in: .whitespaces).lowercased()
        lock.lock(); defer { lock.unlock() }
        if let row = onboardedRows.first(where: { $0.email.lowercased() == key }), row.status != "ACTIVE" {
            return nil
        }
        let expected = onboardedPasswords[key] ?? LocalDemoAuth.demoPassword
        guard password == expected else { return nil }
        return onboardedSessions[key]
    }

    func loginStatusMessage(email: String) -> String? {
        let key = email.trimmingCharacters(in: .whitespaces).lowercased()
        lock.lock(); defer { lock.unlock() }
        guard let row = onboardedRows.first(where: { $0.email.lowercased() == key }) else { return nil }
        switch row.status {
        case "PENDING_APPROVAL": return "Your account is pending admin approval. Please try again after approval."
        case "REJECTED": return "Your account was not approved. Contact your administrator."
        case "DEACTIVATED": return "Your account has been deactivated. Contact your administrator."
        default: return nil
        }
    }

    private func registerActiveSession(_ row: UserRow) {
        onboardedSessions[row.email.lowercased()] = SessionUser(
            id: row.id, name: row.name, email: row.email, role: row.role, companyId: "demo-company"
        )
    }

    private func userBriefById(_ id: String) -> UserBrief? {
        if let u = (Self.staticUsers + onboardedRows).first(where: { $0.id == id }) {
            return UserBrief(id: u.id, name: u.name, email: u.email)
        }
        if id == Self.dealerId { return Self.dealerBrief }
        return nil
    }

    private func seedPendingOrder() -> Order {
        let p = productRows.first ?? Self.staticProducts[0]
        let unit = p.basePrice?.doubleValue ?? 80
        let qty = 2
        let lineSub = unit * Double(qty)
        let lineDisc = lineSub * 0.05
        let taxable = lineSub - lineDisc
        let lineGst = taxable * 0.18
        let item = OrderItem(
            productId: p.id, quantity: qty, price: .double(unit),
            gstAmount: .double(lineGst), discountAmount: .double(lineDisc),
            finalAmount: .double(taxable + lineGst), product: p
        )
        return Order(
            id: "demo-local-seed-1", status: "PENDING", paymentStatus: "UNPAID",
            shopkeeperId: Self.shop1Id, dealerId: Self.dealerId,
            totalAmount: .double(Self.round2(lineSub)), gstAmount: .double(Self.round2(lineGst)),
            discountAmount: .double(Self.round2(lineDisc)), finalAmount: .double(Self.round2(taxable + lineGst)),
            items: [item], shopkeeper: userBriefById(Self.shop1Id), dealer: Self.dealerBrief
        )
    }

    // MARK: - Static seed data

    private static let dealerId = "demo-user-dealer"
    private static let shop1Id = "demo-user-shop1"
    private static let dealerBrief = UserBrief(id: dealerId, name: "City Dealer", email: "dealer@martdemo.com")

    private static let staticBrands: [Brand] = [
        Brand(id: "demo-brand-coca-cola", name: "Coca-Cola", logoUrl: nil),
        Brand(id: "demo-brand-varun-beverages", name: "Varun Beverages Ltd (Pepsi)", logoUrl: nil),
        Brand(id: "demo-brand-everest", name: "Everest", logoUrl: nil),
        Brand(id: "demo-brand-marico", name: "Marico", logoUrl: nil),
        Brand(id: "demo-brand-dabur", name: "Dabur", logoUrl: nil),
        Brand(id: "demo-brand-tata-consumer", name: "Tata Consumer Products", logoUrl: nil),
        Brand(id: "demo-brand-godrej", name: "Godrej", logoUrl: nil),
        Brand(id: "demo-brand-aachi", name: "Aachi", logoUrl: nil),
        Brand(id: "demo-brand-mtr", name: "MTR", logoUrl: nil),
        Brand(id: "demo-brand-anmol", name: "Anmol", logoUrl: nil),
        Brand(id: "demo-brand-wipro-consumer", name: "Wipro Consumer", logoUrl: nil),
        Brand(id: "demo-brand-hul", name: "Hindustan Unilever Limited (HUL)", logoUrl: nil),
        Brand(id: "demo-brand-pg", name: "P&G", logoUrl: nil),
        Brand(id: "demo-brand-itc", name: "ITC Limited", logoUrl: nil),
        Brand(id: "demo-brand-jumbofarms", name: "Jumbofarms", logoUrl: nil),
        Brand(id: "demo-brand-britannia", name: "Britannia", logoUrl: nil),
        Brand(id: "demo-brand-nestle", name: "Nestlé", logoUrl: nil),
        Brand(id: "demo-brand-cadbury", name: "Cadbury", logoUrl: nil),
        Brand(id: "demo-brand-lavian", name: "Lávian", logoUrl: nil),
        Brand(id: "demo-brand-colgate", name: "Colgate", logoUrl: nil),
        Brand(id: "demo-brand-reckitt", name: "Reckitt", logoUrl: nil),
    ]

    private static let brandIds: [String: String] = Dictionary(uniqueKeysWithValues: staticBrands.map { ($0.name, $0.id) })

    private static let staticProducts: [Product] = [
        makeProduct("demo-product-1", "KNSR Premium Sona Masoori Rice (25 kg)", "Jumbofarms", "STAPLES", 1549),
        makeProduct("demo-product-2", "KNSR Fortified Wheat Atta (10 kg)", "Jumbofarms", "STAPLES", 389),
        makeProduct("demo-product-3", "KNSR Unpolished Toor Dal (5 kg)", "Jumbofarms", "STAPLES", 675),
        makeProduct("demo-product-4", "KNSR Refined Sunflower Oil (5 L)", "Marico", "OILS_GHEE", 899),
        makeProduct("demo-product-5", "KNSR Crystal Sugar (5 kg)", "Jumbofarms", "SUGAR_SALT_BASICS", 265),
        makeProduct("demo-product-6", "KNSR Iodized Salt (12 × 1 kg)", "Aachi", "SUGAR_SALT_BASICS", 198),
        makeProduct("demo-product-7", "National Kitchen King Masala (200 g)", "Everest", "STAPLES", 185),
        makeProduct("demo-product-8", "Tata Tea Gold (500 g)", "Tata Consumer Products", "BEVERAGES", 378),
        makeProduct("demo-product-9", "Nescafé Classic Coffee (200 g)", "Nestlé", "BEVERAGES", 520),
        makeProduct("demo-product-10", "Lizol Citrus Disinfectant (2 L)", "Reckitt", "HOME_CARE", 295),
        makeProduct("demo-product-11", "Britannia Good Day Cookies (Family pack)", "Britannia", "SNACKS_BISCUITS", 145),
        makeProduct("demo-product-12", "Bisleri Packaged Water (20 L jar)", "Coca-Cola", "BEVERAGES", 85),
    ]

    private static let staticUsers: [UserRow] = [
        UserRow(id: "demo-user-admin", name: "Super Admin", email: "admin@martdemo.com", role: "ADMIN", phone: "9000000001", status: "ACTIVE"),
        UserRow(id: "demo-user-employee", name: "Field Employee", email: "employee@martdemo.com", role: "EMPLOYEE", phone: "9000000002", status: "ACTIVE"),
        UserRow(id: "demo-user-dealer", name: "City Dealer", email: "dealer@martdemo.com", role: "DEALER", phone: "9000000003", status: "ACTIVE"),
        UserRow(id: "demo-user-shop1", name: "Shopkeeper One", email: "shop1@martdemo.com", role: "SHOPKEEPER", phone: "9000000004", area: UserAreaBrief(id: "area-central", name: "Central Zone"), status: "ACTIVE"),
        UserRow(id: "demo-user-shop2", name: "Shopkeeper Two", email: "shop2@martdemo.com", role: "SHOPKEEPER", phone: "9000000005", area: UserAreaBrief(id: "area-central", name: "Central Zone"), status: "ACTIVE"),
    ]

    private static let ownProductIds: Set<String> = [
        "demo-product-1", "demo-product-2", "demo-product-3",
        "demo-product-4", "demo-product-5", "demo-product-6",
    ]

    private static func makeProduct(_ id: String, _ name: String, _ brandName: String, _ shelf: String, _ price: Double) -> Product {
        let brandId = brandIds[brandName]
        let brand = staticBrands.first { $0.id == brandId }
        return Product(
            id: id, name: name, brandType: ownProductIds.contains(id) ? "OWN" : "OTHER",
            brandId: brandId, brand: brand, shelf: shelf,
            basePrice: .double(price), gstPercentage: .double(18),
            dealerDiscount: .double(10), shopkeeperDiscount: .double(5),
            dealerPrice: .double(price)
        )
    }

    private static func round2(_ v: Double) -> Double { (v * 100).rounded() / 100 }
}
