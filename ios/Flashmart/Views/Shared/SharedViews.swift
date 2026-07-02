import SwiftUI

// MARK: - Cart

struct CartView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath

    var body: some View {
        let role = env.sessionStore.user?.role ?? "SHOPKEEPER"
        let math = ProductPricing.cartMath(lines: env.mainViewModel.cartLines, role: role)

        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    FMTopBar(title: "Cart", subtitle: "\(env.mainViewModel.cartCount) items", onBack: { path.removeLast() })

                    if env.mainViewModel.cartLines.isEmpty {
                        FMCard {
                            Text("Your cart is empty")
                                .font(.system(size: 15))
                                .foregroundStyle(FMTheme.ink3)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 24)
                        }
                    } else {
                        FMCard(padding: 6) {
                            ForEach(Array(env.mainViewModel.cartLines.enumerated()), id: \.element.id) { idx, line in
                                cartLineRow(line, role: role, last: idx == env.mainViewModel.cartLines.count - 1)
                            }
                        }

                        FMCard {
                            FMMoneyRow(label: "Subtotal", value: FMTheme.inr(math.subtotal))
                            FMMoneyRow(label: role == "DEALER" ? "Dealer discount" : "Shopkeeper discount", value: "− \(FMTheme.inr(math.discount))", accent: FMTheme.pos)
                            FMMoneyRow(label: "GST", value: "+ \(FMTheme.inr(math.gst))")
                            Divider().padding(.vertical, 4)
                            FMMoneyRow(label: "Total payable", value: FMTheme.inr(math.total), strong: true)
                        }

                        if let dealer = env.sessionStore.user?.assignedDealer?.name {
                            HStack(alignment: .top, spacing: 10) {
                                Image(systemName: "bolt.fill")
                                    .font(.system(size: 14))
                                    .foregroundStyle(FMTheme.brand)
                                Text("Delivered by \(dealer) · usually within a day")
                                    .font(.system(size: 12.5))
                                    .foregroundStyle(FMTheme.ink3)
                                    .lineSpacing(3)
                            }
                            .padding(.horizontal, 4)
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 120)
            }

            if !env.mainViewModel.cartLines.isEmpty {
                VStack(spacing: 0) {
                    LinearGradient(colors: [FMTheme.bg.opacity(0), FMTheme.bg], startPoint: .top, endPoint: .bottom)
                        .frame(height: 20)
                    FMButton(title: "Checkout · \(FMTheme.inr(math.total))", icon: "arrow.right") {
                        if env.mainViewModel.canProceedToCheckout() {
                            path.append(AppRoute.checkout)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 30)
                    .background(FMTheme.surface2)
                }
            }

            if env.mainViewModel.documentCheckoutBlocked {
                Color.black.opacity(0.35).ignoresSafeArea()
                FMDialog(
                    title: "Document required",
                    confirmLabel: "Upload documents",
                    dismissLabel: "Not now",
                    onConfirm: {
                        env.mainViewModel.dismissDocumentCheckoutBlock()
                        path.append(AppRoute.profileDocuments)
                    },
                    onDismiss: { env.mainViewModel.dismissDocumentCheckoutBlock() }
                ) {
                    Text("To place orders, please upload at least one valid business document for verification.")
                        .font(.system(size: 14))
                        .foregroundStyle(FMTheme.ink3)
                }
            }
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
    }

    private func cartLineRow(_ line: CartLine, role: String, last: Bool) -> some View {
        let lineMath = ProductPricing.lineMath(product: line.product, quantity: line.quantity, role: role)
        let disc = Int(line.product.discountPercent(forRole: role))
        return HStack(alignment: .top, spacing: 12) {
            FMProductThumb(product: line.product, size: 54)
            VStack(alignment: .leading, spacing: 6) {
                Text(line.product.name)
                    .font(.system(size: 14, weight: .bold))
                    .lineLimit(2)
                HStack(spacing: 6) {
                    Text(FMTheme.inr(line.product.catalogUnitPrice(forRole: role)))
                        .font(.system(size: 12, design: .monospaced))
                        .foregroundStyle(FMTheme.ink3)
                    if disc > 0 {
                        Text("·").foregroundStyle(FMTheme.ink4)
                        Text("\(disc)% off")
                            .font(.system(size: 10.5, weight: .bold))
                            .foregroundStyle(FMTheme.goldInk)
                            .padding(.horizontal, 7)
                            .padding(.vertical, 2)
                            .background(FMTheme.goldTint)
                            .clipShape(RoundedRectangle(cornerRadius: 6))
                    }
                }
                FMQuantityInput(
                    value: line.quantity,
                    onChange: { env.mainViewModel.setCartQty(productId: line.product.id, qty: $0) },
                    min: 0,
                    max: env.mainViewModel.maxOrderQuantity,
                    compact: true
                )
            }
            Spacer()
            Text(FMTheme.inr(lineMath.total))
                .font(.system(size: 14.5, weight: .bold, design: .monospaced))
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 10)
        .overlay(alignment: .bottom) {
            if !last { Divider().padding(.leading, 72) }
        }
    }
}

// MARK: - Payment

struct PaymentView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let orderId: String
    @State private var order: Order?
    @State private var paying = false
    @State private var paid = false
    @State private var razorpayAppOrderId: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                FMTopBar(title: "Payment", subtitle: orderId, onBack: { path.removeLast() })

                FMCard {
                    HStack {
                        Image(systemName: "creditcard.fill")
                            .font(.system(size: 28))
                            .foregroundStyle(FMTheme.brand)
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Complete payment")
                                .font(.system(size: 16, weight: .bold))
                            Text(AppConfig.useLocalDemoAuth ? "Demo mode — no real charge" : "Mock or Razorpay")
                                .font(.system(size: 13))
                                .foregroundStyle(FMTheme.ink3)
                        }
                    }
                }

                if let order {
                    FMCard {
                        FMMoneyRow(label: "Order total", value: FMTheme.inr(ProductPricing.orderMath(order: order).total), strong: true)
                        FMMoneyRow(label: "Status", value: order.paymentStatus ?? "UNPAID")
                    }
                }

                if paid {
                    FMCard {
                        HStack(spacing: 10) {
                            Image(systemName: "checkmark.circle.fill").foregroundStyle(FMTheme.pos)
                            Text(env.mainViewModel.paymentMessage ?? "Payment successful")
                                .font(.system(size: 14, weight: .semibold))
                        }
                    }
                    FMButton(title: "View invoice", icon: "doc.text") {
                        path.append(AppRoute.invoice(orderId))
                    }
                    FMButton(title: "Track delivery", variant: .soft, icon: "shippingbox") {
                        path.append(AppRoute.tracking(orderId))
                    }
                } else {
                    FMButton(title: paying ? "Processing…" : "Pay with demo card", enabled: !paying) {
                        Task {
                            paying = true
                            paid = await env.mainViewModel.mockPay(orderId: orderId)
                            if let o = try? await env.apiClient.orderById(orderId) { order = o }
                            paying = false
                        }
                    }
                    if !AppConfig.useLocalDemoAuth && !env.sessionStore.isLocalDemoMode {
                        FMButton(title: paying ? "Opening Razorpay…" : "Pay with Razorpay", variant: .dark, icon: "bolt.fill", enabled: !paying) {
                            Task {
                                paying = true
                                razorpayAppOrderId = orderId
                                if let pending = await env.mainViewModel.initRazorpayForOrder(orderId) {
                                    env.razorpayCoordinator.present(checkout: pending)
                                }
                                paying = false
                            }
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 30)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task {
            order = try? await env.apiClient.orderById(orderId)
        }
        .onChange(of: env.razorpayCoordinator.lastResult) { _, result in
            guard let result, let appId = razorpayAppOrderId else { return }
            Task {
                paying = true
                paid = await env.mainViewModel.onRazorpayResult(result, appOrderId: appId)
                if let o = try? await env.apiClient.orderById(orderId) { order = o }
                razorpayAppOrderId = nil
                env.razorpayCoordinator.clearLastResult()
                paying = false
            }
        }
    }
}

// MARK: - Product detail

struct ProductDetailView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let productId: String
    @State private var product: Product?
    @State private var qty = 1

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                FMTopBar(title: "Product", onBack: { path.removeLast() })

                if let product {
                    FMCard {
                        HStack(spacing: 16) {
                            FMProductThumb(product: product, size: 80)
                            VStack(alignment: .leading, spacing: 6) {
                                Text(product.brand?.name.uppercased() ?? "BRAND")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundStyle(FMTheme.brand)
                                Text(product.name)
                                    .font(.system(size: 18, weight: .bold))
                                Text(FMTheme.inr(product.catalogUnitPrice(forRole: env.sessionStore.user?.role ?? "SHOPKEEPER")))
                                    .font(.system(size: 20, weight: .bold, design: .monospaced))
                                    .foregroundStyle(FMTheme.brand)
                            }
                        }
                    }

                    FMCard {
                        detailRow("Shelf", product.shelf?.replacingOccurrences(of: "_", with: " ") ?? "—")
                        detailRow("GST", "\(product.gstPercentage?.doubleValue ?? 18)%")
                        detailRow("Shopkeeper discount", "\(Int(product.shopkeeperDiscount?.doubleValue ?? 5))%")
                        detailRow("Dealer discount", "\(Int(product.dealerDiscount?.doubleValue ?? 10))%")
                        detailRow("Brand type", product.brandType)
                    }

                    if env.sessionStore.user?.role == "SHOPKEEPER" || env.sessionStore.user?.role == "DEALER" {
                        FMCard {
                            FMSectionLabel(title: "Quantity")
                            FMQuantityInput(
                                value: qty,
                                onChange: { qty = $0 },
                                min: 1,
                                max: env.mainViewModel.maxOrderQuantity
                            )
                        }
                        FMButton(title: "Add to cart · \(FMTheme.inr(product.catalogUnitPrice(forRole: env.sessionStore.user?.role ?? "SHOPKEEPER") * Double(qty)))", icon: "cart.badge.plus") {
                            env.mainViewModel.addToCart(product, qty: qty)
                            path.append(AppRoute.cart)
                        }
                    }
                } else {
                    ProgressView().frame(maxWidth: .infinity).padding()
                }
            }
            .padding(.horizontal, 16)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task {
            product = try? await env.apiClient.productById(productId)
        }
    }

    private func detailRow(_ label: String, _ value: String) -> some View {
        HStack {
            Text(label).font(.system(size: 13)).foregroundStyle(FMTheme.ink3)
            Spacer()
            Text(value).font(.system(size: 14, weight: .semibold))
        }
        .padding(.vertical, 6)
    }
}

// MARK: - Order detail

struct OrderDetailView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let orderId: String
    @State private var order: Order?
    @State private var showReturnDialog = false
    @State private var returnReasonEnum = "DAMAGED_PRODUCT"
    @State private var returnComments = ""
    @State private var returnBusy = false
    @State private var reorderBusy = false
    @State private var reorderAlert: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Order", subtitle: orderId, onBack: { path.removeLast() })

                if let order {
                    FMCard {
                        HStack {
                            FMBadge(status: order.status)
                            if let ps = order.paymentStatus { FMBadge(status: ps) }
                            Spacer()
                            Text(FMTheme.inr(ProductPricing.orderMath(order: order).total))
                                .font(.system(size: 18, weight: .bold, design: .monospaced))
                        }
                    }

                    FMCard(padding: 6) {
                        ForEach(order.items ?? [], id: \.productId) { item in
                            HStack {
                                if let p = item.product { FMProductThumb(product: p, size: 44) }
                                VStack(alignment: .leading) {
                                    Text(item.product?.name ?? item.productId)
                                        .font(.system(size: 13.5, weight: .semibold))
                                    Text("Qty \(item.quantity)")
                                        .font(.system(size: 12, design: .monospaced))
                                        .foregroundStyle(FMTheme.ink3)
                                }
                                Spacer()
                                Text(FMTheme.inr(item.finalAmount?.doubleValue ?? 0))
                                    .font(.system(size: 13, weight: .bold, design: .monospaced))
                            }
                            .padding(10)
                        }
                    }

                    HStack(spacing: 10) {
                        if order.paymentStatus?.uppercased() != "PAID" && env.sessionStore.user?.role == "SHOPKEEPER" {
                            FMButton(title: "Pay now", fullWidth: true) { path.append(AppRoute.payment(orderId)) }
                        }
                        FMButton(title: "Track", variant: .soft, fullWidth: true) { path.append(AppRoute.tracking(orderId)) }
                    }
                    if order.paymentStatus?.uppercased() == "PAID" || order.status.uppercased() != "PENDING" {
                        FMButton(title: "Invoice", variant: .outline, icon: "doc.text") { path.append(AppRoute.invoice(orderId)) }
                    }
                    if env.sessionStore.user?.role == "SHOPKEEPER" {
                        FMButton(title: reorderBusy ? "Preparing cart…" : "Reorder", variant: .soft, icon: "arrow.clockwise") {
                            Task {
                                reorderBusy = true
                                let result = await env.mainViewModel.reorderFromOrder(orderId: orderId)
                                reorderBusy = false
                                if result.success {
                                    path.append(AppRoute.cart)
                                    if let msg = result.message { reorderAlert = msg }
                                } else {
                                    reorderAlert = result.message ?? "Could not reorder"
                                }
                            }
                        }
                    }

                    returnActions(order)
                    dealerActions(order)
                } else {
                    ProgressView().padding()
                }
            }
            .padding(.horizontal, 16)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task { order = try? await env.apiClient.orderById(orderId) }
        .alert("Request return", isPresented: $showReturnDialog) {
            Button("Submit") {
                Task {
                    returnBusy = true
                    let items = (order?.items ?? []).map {
                        CreateReturnItemRequest(productId: $0.productId, quantity: $0.quantity)
                    }
                    let labels: [String: String] = [
                        "DAMAGED_PRODUCT": "Damaged product",
                        "EXPIRED_PRODUCT": "Expired product",
                        "WRONG_PRODUCT": "Wrong product",
                        "QUALITY_ISSUE": "Quality issue",
                        "EXCESS_QUANTITY": "Excess quantity",
                        "OTHER": "Other",
                    ]
                    if await env.mainViewModel.createDetailedReturn(
                        orderId: orderId,
                        reason: returnReasonEnum,
                        reasonText: labels[returnReasonEnum],
                        comments: returnComments.isEmpty ? nil : returnComments,
                        items: items
                    ) {
                        order = try? await env.apiClient.orderById(orderId)
                        returnComments = ""
                    }
                    returnBusy = false
                }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Full order quantity will be requested for return (\(returnReasonEnum.replacingOccurrences(of: "_", with: " ").lowercased()))")
        }
        .alert("Reorder", isPresented: Binding(
            get: { reorderAlert != nil },
            set: { if !$0 { reorderAlert = nil } }
        )) {
            Button("OK", role: .cancel) { reorderAlert = nil }
        } message: {
            Text(reorderAlert ?? "")
        }
    }

    @ViewBuilder
    private func returnActions(_ ord: Order) -> some View {
        let role = env.sessionStore.user?.role ?? ""
        let status = ord.status.uppercased()

        if let reason = ord.returnReason, !reason.isEmpty {
            FMCard {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Return notes").font(.system(size: 13, weight: .semibold))
                    Text(reason).font(.system(size: 13)).foregroundStyle(FMTheme.ink3)
                    if let at = ord.returnRequestedAt {
                        Text("Requested: \(String(at.prefix(10)))")
                            .font(.system(size: 11, design: .monospaced))
                            .foregroundStyle(FMTheme.ink4)
                    }
                }
            }
        }

        if role == "SHOPKEEPER" && status == "DELIVERED" {
            FMButton(title: returnBusy ? "Submitting…" : "Request return", variant: .outline, icon: "arrow.uturn.left") {
                showReturnDialog = true
            }
        }

        if (role == "DEALER" || role == "ADMIN" || role == "EMPLOYEE") && status == "RETURN_REQUESTED" {
            FMButton(title: "Approve return", variant: .pos, icon: "checkmark.circle") {
                Task { await refreshAfter { _ = await env.mainViewModel.approveOrderReturn(orderId: orderId) } }
            }
            FMButton(title: "Reject return", variant: .outline) {
                Task { await refreshAfter { _ = await env.mainViewModel.rejectOrderReturn(orderId: orderId, note: nil) } }
            }
        }
    }

    @ViewBuilder
    private func dealerActions(_ order: Order) -> some View {
        let role = env.sessionStore.user?.role ?? ""
        let isRestock = order.kind?.uppercased() == "DEALER_RESTOCK"

        if isRestock && (role == "ADMIN" || role == "EMPLOYEE") {
            restockStaffActions(order)
        } else if role == "DEALER" && !isRestock {
            switch order.status.uppercased() {
            case "PENDING":
                FMButton(title: "Accept order") { Task { await refreshAfter { await env.mainViewModel.confirmOrder(orderId) } } }
            case "DEALER_CONFIRMED", "ACCEPTED":
                FMButton(title: "Mark out for delivery") { Task { await refreshAfter { await env.mainViewModel.dispatchOrder(orderId) } } }
            case "OUT_FOR_DELIVERY":
                FMButton(title: "Mark delivered", variant: .pos) { Task { await refreshAfter { await env.mainViewModel.deliverOrder(orderId) } } }
            default:
                EmptyView()
            }
        } else if isRestock && role == "DEALER" {
            FMCard {
                Text("Restock order — awaiting admin confirmation and delivery.")
                    .font(.system(size: 13))
                    .foregroundStyle(FMTheme.ink3)
            }
        } else {
            EmptyView()
        }
    }

    @ViewBuilder
    private func restockStaffActions(_ order: Order) -> some View {
        switch order.status.uppercased() {
        case "PENDING":
            FMButton(title: "Confirm restock") { Task { await refreshAfter { await env.mainViewModel.confirmOrder(orderId) } } }
        case "DEALER_CONFIRMED", "ACCEPTED":
            FMButton(title: "Mark out for delivery") { Task { await refreshAfter { await env.mainViewModel.dispatchOrder(orderId) } } }
        case "OUT_FOR_DELIVERY":
            FMButton(title: "Mark delivered", variant: .pos) { Task { await refreshAfter { await env.mainViewModel.deliverOrder(orderId) } } }
        default:
            EmptyView()
        }
    }

    private func refreshAfter(_ action: () async -> Void) async {
        await action()
        order = try? await env.apiClient.orderById(orderId)
    }
}

// MARK: - Invoice

struct InvoiceView: View {
    @Environment(AppEnvironment.self) private var env
    let orderId: String
    @State private var invoice: InvoiceDocument?
    @State private var shareURL: URL?
    @State private var showShare = false
    @State private var shareError: String?
    @State private var exporting = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Tax invoice", subtitle: invoice?.invoiceNumber)

                if let invoice, let order = invoice.order {
                    FMCard {
                        VStack(spacing: 0) {
                            HStack {
                                Text("Tax Invoice")
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundStyle(.white)
                                Spacer()
                                Text(invoice.invoiceNumber)
                                    .font(.system(size: 11, design: .monospaced))
                                    .foregroundStyle(.white.opacity(0.6))
                            }
                            .padding(14)
                            .background(FMTheme.inkSurface)
                        }
                        if let items = order.items, !items.isEmpty {
                            VStack(spacing: 0) {
                                ForEach(items) { item in
                                    HStack {
                                        Text(item.product?.name ?? "Item")
                                            .font(.system(size: 13))
                                            .lineLimit(1)
                                        Spacer()
                                        Text("×\(item.quantity)")
                                            .font(.system(size: 12, design: .monospaced))
                                            .foregroundStyle(FMTheme.ink3)
                                        Text(FMTheme.inr(item.finalAmount?.doubleValue ?? 0))
                                            .font(.system(size: 13, weight: .semibold, design: .monospaced))
                                    }
                                    .padding(.vertical, 6)
                                    Divider()
                                }
                            }
                            .padding(.horizontal, 14)
                            .padding(.top, 8)
                        }
                        VStack(spacing: 4) {
                            FMMoneyRow(label: "Subtotal", value: FMTheme.inr(order.totalAmount?.doubleValue ?? 0))
                            FMMoneyRow(label: "Discount", value: "− \(FMTheme.inr(order.discountAmount?.doubleValue ?? 0))", accent: FMTheme.pos)
                            FMMoneyRow(label: "GST", value: "+ \(FMTheme.inr(order.gstAmount?.doubleValue ?? 0))")
                            Divider().padding(.vertical, 4)
                            FMMoneyRow(label: "Total", value: FMTheme.inr(order.finalAmount?.doubleValue ?? 0), accent: FMTheme.pos, strong: true)
                        }
                        .padding(14)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: FMTheme.cardRadius))
                    .rotationEffect(.degrees(-1))

                    FMCard {
                        Text("Generated: \(invoice.generatedAt ?? "—")")
                            .font(.system(size: 12))
                            .foregroundStyle(FMTheme.ink3)
                        Text("Order: \(orderId)")
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundStyle(FMTheme.ink4)
                    }

                    FMButton(
                        title: exporting ? "Preparing PDF…" : "Share invoice PDF",
                        variant: .soft,
                        icon: "square.and.arrow.up",
                        enabled: !exporting
                    ) {
                        Task {
                            exporting = true
                            shareError = nil
                            defer { exporting = false }
                            do {
                                shareURL = try InvoicePdfExporter.write(invoice: invoice)
                                showShare = true
                            } catch {
                                shareError = error.localizedDescription
                            }
                        }
                    }
                    if let shareError {
                        Text(shareError)
                            .font(.system(size: 12))
                            .foregroundStyle(FMTheme.neg)
                    }
                } else {
                    ProgressView().padding()
                }
            }
            .padding(.horizontal, 16)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task { invoice = try? await env.apiClient.invoiceByOrder(orderId) }
        .sheet(isPresented: $showShare) {
            if let shareURL {
                ShareSheet(items: [shareURL])
            }
        }
    }
}

// MARK: - Order confirmation

struct OrderConfirmationView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let orderId: String
    @State private var order: Order?
    @State private var loading = true
    @State private var error: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: FMSpacing.itemGap) {
                FMTopBar(title: "Order confirmed", subtitle: "Order #\(orderId.suffix(8).uppercased())")

                if loading {
                    FMLoadingState(message: "Confirming your order…")
                } else if let error {
                    FMErrorBanner(text: error)
                    FMButton(title: "Go back", variant: .outline) { path.removeLast() }
                } else if let order {
                    confirmationBody(order)
                } else {
                    FMEmptyState(icon: "checkmark.circle", title: "Order unavailable", message: "We could not load confirmation details.", actionTitle: "Go back") {
                        path.removeLast()
                    }
                }
            }
            .padding(FMSpacing.screenH)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task { await load() }
    }

    @ViewBuilder
    private func confirmationBody(_ order: Order) -> some View {
        let paid = order.paymentStatus?.uppercased() == "PAID"
        VStack(spacing: 16) {
            ZStack {
                Circle().fill(FMTheme.posTint).frame(width: 104, height: 104)
                Text("✓").font(.system(size: 48, weight: .bold)).foregroundStyle(FMTheme.pos)
            }
            .frame(maxWidth: .infinity)

            Text(paid ? "Payment successful!" : "Order placed!")
                .font(.system(size: 26, weight: .heavy))
                .frame(maxWidth: .infinity)

            Text("Your dealer has been notified and will confirm shortly.")
                .font(.system(size: 14.5))
                .foregroundStyle(FMTheme.ink3)
                .multilineTextAlignment(.center)

            FMCard(padding: 18) {
                FMMoneyRow(label: "Order number", value: order.id.suffix(8).uppercased())
                Divider().padding(.vertical, 8)
                FMMoneyRow(
                    label: paid ? "Amount paid" : "Order total",
                    value: FMTheme.inr(order.finalAmount?.doubleValue ?? order.totalAmount?.doubleValue ?? 0)
                )
                FMMoneyRow(label: "Expected delivery", value: "Tomorrow, by 6 PM", accent: FMTheme.pos)
            }

            FMButton(title: "Track order", icon: "shippingbox") {
                path.append(AppRoute.tracking(order.id))
            }
            FMButton(title: "View invoice", variant: .soft, icon: "doc.text") {
                path.append(AppRoute.invoice(order.id))
            }
        }
    }

    private func load() async {
        loading = true
        error = nil
        defer { loading = false }
        do {
            order = try await env.apiClient.orderById(orderId)
            if order == nil { error = "Order not found" }
        } catch let e {
            error = (e as? LocalizedError)?.errorDescription ?? e.localizedDescription
        }
    }
}

// MARK: - Privacy policy

struct PrivacyPolicyView: View {
    @Binding var path: NavigationPath

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Privacy policy", onBack: { path.removeLast() })
                FMCard {
                    Text("FlashMart collects business contact details, order history, and verification documents to operate the distribution platform. Documents are reviewed for eligibility only. We do not sell personal data to third parties.")
                        .font(.system(size: 14))
                        .foregroundStyle(FMTheme.ink2)
                        .lineSpacing(4)
                }
            }
            .padding(16)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
    }
}

// MARK: - Tracking

struct TrackingView: View {
    @Environment(AppEnvironment.self) private var env
    let orderId: String
    @State private var order: Order?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Track order", subtitle: orderId)

                FMCard(padding: 18) {
                    ZStack {
                        RoundedRectangle(cornerRadius: FMTheme.cardRadius, style: .continuous)
                            .fill(FMTheme.heroGradient)
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Estimated delivery")
                                .font(.system(size: 12.5, weight: .semibold))
                                .foregroundStyle(.white.opacity(0.82))
                            Text(etaText)
                                .font(.system(size: 22, weight: .bold))
                                .foregroundStyle(.white)
                            Text(statusText)
                                .font(.system(size: 13))
                                .foregroundStyle(.white.opacity(0.82))
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }

                FMCard {
                    FMSectionLabel(title: "Order journey")
                    FMOrderTimeline(steps: timelineSteps)
                }

                FMCard {
                    HStack(spacing: 13) {
                        FMAvatar(name: order?.dealer?.name ?? "Dealer", tint: FMTheme.pos)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(order?.dealer?.name ?? "City Dealer")
                                .font(.system(size: 14.5, weight: .bold))
                            Text("Your dealer · Central Zone")
                                .font(.system(size: 12.5))
                                .foregroundStyle(FMTheme.ink3)
                        }
                        Spacer()
                        FMGlyphButton(systemName: "phone", accent: FMTheme.pos)
                    }
                }
            }
            .padding(.horizontal, 16)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task { order = try? await env.apiClient.orderById(orderId) }
    }

    private var statusText: String {
        FMBadge.defaultLabel(for: order?.status ?? "PENDING")
    }

    private var etaText: String {
        switch order?.status.uppercased() ?? "" {
        case "DELIVERED": "Delivered"
        case "OUT_FOR_DELIVERY": "Today, by 6:00 PM"
        case "DEALER_CONFIRMED", "ACCEPTED": "Tomorrow"
        default: "Pending confirmation"
        }
    }

    private var timelineSteps: [FMOrderTimeline.TimelineStep] {
        let status = order?.status.uppercased() ?? "PENDING"
        let paid = order?.paymentStatus?.uppercased() == "PAID"
        return [
            .init(label: "Order placed", time: "Placed", state: .done),
            .init(label: "Payment successful", time: paid ? "Paid via mock card" : "Pending", state: paid ? .done : .todo),
            .init(label: "Dealer accepted", time: order?.dealer?.name ?? "Dealer", state: ["DEALER_CONFIRMED", "ACCEPTED", "OUT_FOR_DELIVERY", "DELIVERED"].contains(status) ? .done : status == "PENDING" ? .todo : .active),
            .init(label: "Out for delivery", time: "On the way to your store", state: status == "OUT_FOR_DELIVERY" ? .active : status == "DELIVERED" ? .done : .todo),
            .init(label: "Delivered", time: status == "DELIVERED" ? "Completed" : "Pending", state: status == "DELIVERED" ? .done : .todo),
        ]
    }
}
