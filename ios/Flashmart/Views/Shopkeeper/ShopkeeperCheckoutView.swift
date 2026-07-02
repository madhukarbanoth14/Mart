import SwiftUI

/// Figma checkout — delivery address, dealer, payment method, order summary.
struct ShopkeeperCheckoutView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath

    @State private var method = "upi"
    @State private var paying = false
    @State private var paidOrderId: String?
    @State private var showCardDialog = false
    @State private var showUpiDialog = false
    @State private var showPaymentSheet = false
    @State private var methodError: String?

    private var role: String { env.sessionStore.user?.role ?? "SHOPKEEPER" }
    private var math: OrderMath {
        ProductPricing.cartMath(lines: env.mainViewModel.cartLines, role: role)
    }
    private var isDealer: Bool { role.uppercased() == "DEALER" }
    private var useRazorpayCheckout: Bool {
        !AppConfig.useLocalDemoAuth && !env.sessionStore.isLocalDemoMode
    }
    private var methods: [PaymentMethodOption] {
        checkoutPaymentOptions(outstanding: outstandingLabel, useRazorpayCheckout: useRazorpayCheckout)
    }
    private var selected: PaymentMethodOption? {
        methods.first { $0.id == method }
    }
    private var outstandingLabel: String? {
        guard case .ok(let list) = env.mainViewModel.orders else { return nil }
        let amt = list
            .filter { ($0.paymentStatus?.uppercased() ?? "") != "PAID" && $0.status.uppercased() != "CANCELLED" }
            .reduce(0.0) { $0 + ($1.finalAmount?.doubleValue ?? $1.totalAmount?.doubleValue ?? 0) }
        return amt > 0 ? FMTheme.inr(amt) : nil
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            checkoutScrollContent
            checkoutBottomBar
            checkoutPaymentOverlay
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .onChange(of: env.razorpayCoordinator.lastResult) { _, result in
            handleRazorpayResult(result)
        }
        .alert("Enter card details", isPresented: $showCardDialog) {
            Button("Pay \(FMTheme.inr(math.total))") { Task { await payDemo(method: "card") } }
            Button("Cancel", role: .cancel) { }
        } message: {
            Text("Demo card 4111 1111 1111 1111\nExpiry 12/28 · CVV 123")
        }
        .alert("Pay with UPI", isPresented: $showUpiDialog) {
            Button("Pay now") { Task { await payDemo(method: "upi") } }
            Button("Cancel", role: .cancel) { }
        } message: {
            Text("Confirm demo UPI payment of \(FMTheme.inr(math.total))?")
        }
        .alert(
            "Document required",
            isPresented: Binding(
                get: { env.mainViewModel.documentCheckoutBlocked },
                set: { env.mainViewModel.documentCheckoutBlocked = $0 }
            )
        ) {
            Button("Upload documents") { path.append(AppRoute.profileDocuments) }
            Button("Not now", role: .cancel) {}
        } message: {
            Text("Please upload at least one valid business document before placing orders.")
        }
    }

    @ViewBuilder
    private var checkoutScrollContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(
                    title: isDealer ? "Restock checkout" : "Checkout",
                    onBack: { path.removeLast() }
                )
                checkoutAddressSection
                checkoutDealerSection
                checkoutPaymentSection
                checkoutSummarySection
                if let methodError {
                    Text(methodError).font(.system(size: 13)).foregroundStyle(FMTheme.neg)
                }
                if let err = env.mainViewModel.placeOrderError {
                    Text(err).font(.system(size: 13)).foregroundStyle(FMTheme.neg)
                }
                if let orderId = paidOrderId {
                    successCard(orderId: orderId)
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 130)
        }
    }

    @ViewBuilder
    private var checkoutAddressSection: some View {
        FMSectionLabel(title: "Delivery address", actionTitle: "Change") {
            path.append(AppRoute.profileStoreAddress)
        }
        FMCard(padding: 14) {
            HStack(alignment: .top, spacing: 13) {
                Image(systemName: "mappin.circle.fill")
                    .font(.system(size: 28))
                    .foregroundStyle(FMTheme.brand)
                VStack(alignment: .leading, spacing: 2) {
                    Text(env.sessionStore.user?.name ?? "Your store")
                        .font(.system(size: 14.5, weight: .bold))
                    Text(ShopkeeperProfileStore.storeAddress.isEmpty ? "Add your store address in profile" : ShopkeeperProfileStore.storeAddress)
                        .font(.system(size: 13))
                        .foregroundStyle(FMTheme.ink3)
                        .lineSpacing(3)
                }
            }
        }
    }

    @ViewBuilder
    private var checkoutDealerSection: some View {
        if let dealer = env.sessionStore.user?.assignedDealer?.name {
            FMCard(padding: 14) {
                HStack(spacing: 13) {
                    FMAvatar(name: dealer, tint: FMTheme.dealerBlue)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(dealer).font(.system(size: 14, weight: .bold))
                        Text("Your dealer · \(env.sessionStore.user?.areaName ?? "—")")
                            .font(.system(size: 12.5))
                            .foregroundStyle(FMTheme.ink3)
                    }
                    Spacer()
                    FMBadge(status: "ACTIVE")
                }
            }
        }
    }

    @ViewBuilder
    private var checkoutPaymentSection: some View {
        FMSectionLabel(title: "Payment method")
        FMCard(padding: 6) {
            ForEach(methods) { option in
                checkoutMethodRow(option: option)
            }
        }
    }

    @ViewBuilder
    private var checkoutSummarySection: some View {
        FMCard {
            FMMoneyRow(label: "Subtotal", value: FMTheme.inr(math.subtotal))
            FMMoneyRow(label: "Discount", value: "− \(FMTheme.inr(math.discount))", accent: FMTheme.pos)
            FMMoneyRow(label: "GST", value: "+ \(FMTheme.inr(math.gst))")
            Divider().padding(.vertical, 4)
            FMMoneyRow(label: "Total payable", value: FMTheme.inr(math.total), strong: true)
        }
    }

    @ViewBuilder
    private var checkoutBottomBar: some View {
        if paidOrderId == nil {
            VStack(spacing: 0) {
                LinearGradient(colors: [FMTheme.bg.opacity(0), FMTheme.bg], startPoint: .top, endPoint: .bottom)
                    .frame(height: 16)
                FMButton(
                    title: paying ? "Placing order…" : "Place order · \(FMTheme.inr(math.total))",
                    enabled: !paying && !env.mainViewModel.cartLines.isEmpty
                ) { startPay() }
                .padding(.horizontal, 16)
                .padding(.bottom, 30)
                .background(FMTheme.bg)
            }
        }
    }

    @ViewBuilder
    private var checkoutPaymentOverlay: some View {
        if showPaymentSheet && paidOrderId == nil {
            Color.black.opacity(0.45).ignoresSafeArea()
            razorpayPaymentSheet
        }
    }

    private func handleRazorpayResult(_ result: RazorpayPaymentResult?) {
        guard let result else { return }
        let appOrderId = env.mainViewModel.pendingRazorpay?.appOrderId
            ?? env.mainViewModel.lastPlacedOrder?.id
        guard let appOrderId else { return }
        Task {
            paying = true
            let ok = await env.mainViewModel.onRazorpayResult(result, appOrderId: appOrderId)
            if ok { paidOrderId = appOrderId }
            env.mainViewModel.clearPendingRazorpay()
            env.razorpayCoordinator.clearLastResult()
            showPaymentSheet = false
            paying = false
        }
    }

    private var razorpayPaymentSheet: some View {
        VStack(spacing: 0) {
            Spacer()
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(FMTheme.brand700)
                        .frame(width: 40, height: 40)
                        .overlay(Text("FM").font(.system(size: 14, weight: .heavy)).foregroundStyle(.white))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("FlashMart").font(.system(size: 16, weight: .heavy)).foregroundStyle(.white)
                        Text("Secured by Razorpay").font(.system(size: 12)).foregroundStyle(.white.opacity(0.75))
                    }
                    Spacer()
                    VStack(alignment: .trailing) {
                        Text(FMTheme.inr(math.total)).font(.system(size: 18, weight: .bold, design: .monospaced)).foregroundStyle(.white)
                    }
                }
                .padding(18)
                .background(FMTheme.brand700)

                VStack(alignment: .leading, spacing: 16) {
                    FMSectionLabel(title: "UPI")
                    HStack(spacing: 10) {
                        upiTile("GPay", color: Color(hex: 0x1A73E8))
                        upiTile("PhonePe", color: Color(hex: 0x5F259F))
                        upiTile("Paytm", color: Color(hex: 0x00B9F1))
                    }
                    FMSectionLabel(title: "Other methods")
                    Text("Continue in Razorpay for cards, wallets & net banking")
                        .font(.system(size: 13))
                        .foregroundStyle(FMTheme.ink3)
                    FMButton(title: paying ? "Opening…" : "Pay \(FMTheme.inr(math.total))", enabled: !paying) {
                        Task { await payRazorpay(method: method) }
                    }
                }
                .padding(18)
                .background(FMTheme.surface)
            }
            .clipShape(RoundedRectangle(cornerRadius: 28, style: .continuous))
        }
        .ignoresSafeArea(edges: .bottom)
    }

    private func upiTile(_ name: String, color: Color) -> some View {
        VStack(spacing: 8) {
            RoundedRectangle(cornerRadius: 10)
                .fill(color)
                .frame(width: 36, height: 36)
                .overlay(Text(String(name.prefix(1))).font(.system(size: 16, weight: .heavy)).foregroundStyle(.white))
            Text(name).font(.system(size: 12, weight: .bold))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .background(FMTheme.surface2)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(FMTheme.line))
    }

    private func checkoutMethodRow(option: PaymentMethodOption) -> some View {
        let selectedOn = method == option.id
        let enabled = option.isEnabled(useRazorpayCheckout: useRazorpayCheckout)
        return Button {
            guard enabled else { return }
            method = option.id
            methodError = nil
        } label: {
            HStack(spacing: 13) {
                Image(systemName: option.icon)
                    .font(.system(size: 18))
                    .foregroundStyle(FMTheme.ink3)
                    .frame(width: 38, height: 38)
                    .background(FMTheme.surface2)
                    .clipShape(RoundedRectangle(cornerRadius: 11))
                VStack(alignment: .leading, spacing: 2) {
                    Text(option.label).font(.system(size: 14.5, weight: .bold)).foregroundStyle(FMTheme.ink)
                    Text(option.subtitle).font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                }
                Spacer()
                Circle()
                    .strokeBorder(selectedOn && enabled ? FMTheme.brand : FMTheme.line2, lineWidth: selectedOn && enabled ? 7 : 2)
                    .frame(width: 22, height: 22)
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 13)
        }
        .buttonStyle(.plain)
        .opacity(enabled ? 1 : 0.45)
        .disabled(!enabled)
    }

    private func startPay() {
        methodError = nil
        guard selected != nil else { return }
        switch method {
        case "cod":
            Task { await payDemo(method: "cod") }
        case "card" where !useRazorpayCheckout:
            showCardDialog = true
        case "upi" where !useRazorpayCheckout:
            showUpiDialog = true
        case _ where useRazorpayCheckout && RazorpayCheckoutOptions.isOnlineGatewayMethod(method):
            showPaymentSheet = true
        default:
            Task { await payDemo(method: method) }
        }
    }

    private func payDemo(method: String) async {
        paying = true
        if let order = await env.mainViewModel.placeOrderWithDemoPayment(method: method) {
            paidOrderId = order.id
            path.append(AppRoute.orderConfirmation(order.id))
        }
        paying = false
    }

    private func payRazorpay(method: String) async {
        paying = true
        if let pending = await env.mainViewModel.placeOrderForRazorpay(paymentMethod: method) {
            env.razorpayCoordinator.present(checkout: pending)
        } else if env.mainViewModel.placeOrderMessage != nil, env.mainViewModel.placeOrderError == nil {
            paidOrderId = env.mainViewModel.lastPlacedOrder?.id
        }
        paying = false
    }

    private func successCard(orderId: String) -> some View {
        FMCard {
            VStack(alignment: .leading, spacing: 12) {
                HStack(spacing: 10) {
                    Image(systemName: "checkmark.circle.fill").foregroundStyle(FMTheme.pos)
                    Text(env.mainViewModel.paymentMessage ?? "Order placed")
                        .font(.system(size: 14, weight: .semibold))
                }
                FMButton(title: "View confirmation", icon: "checkmark.circle") {
                    path.append(AppRoute.orderConfirmation(orderId))
                }
            }
        }
    }
}

private func checkoutPaymentOptions(outstanding: String?, useRazorpayCheckout: Bool) -> [PaymentMethodOption] {
    let creditSub = outstanding.map { "\($0) outstanding" } ?? "Pay from FlashMart credit"
    return [
        PaymentMethodOption(id: "upi", label: "UPI", subtitle: "GPay · PhonePe · Paytm", icon: "indianrupeesign.circle", razorpayGateway: true),
        PaymentMethodOption(id: "card", label: "Credit / Debit card", subtitle: "Visa, Mastercard, RuPay", icon: "creditcard", razorpayGateway: true),
        PaymentMethodOption(id: "cod", label: "FlashMart credit", subtitle: creditSub, icon: "wallet.pass", razorpayGateway: false),
    ]
}

struct ShopkeeperWalletView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Wallet & ledger", onBack: { path.removeLast() })

                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 22).fill(FMTheme.heroGradient)
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Outstanding balance").font(.system(size: 13, weight: .semibold)).foregroundStyle(.white.opacity(0.85))
                        Text(FMTheme.inr(outstanding)).font(.system(size: 38, weight: .bold, design: .monospaced)).foregroundStyle(.white)
                        Text("Credit limit \(FMTheme.inr(25_000)) · Available \(FMTheme.inr(max(0, 25_000 - outstanding)))")
                            .font(.system(size: 12.5))
                            .foregroundStyle(.white.opacity(0.82))
                        ProgressView(value: min(outstanding / 25_000, 1))
                            .tint(.white)
                            .padding(.top, 10)
                        FMButton(title: "Pay outstanding", variant: .outline) {
                            if env.mainViewModel.cartCount > 0 { path.append(AppRoute.checkout) }
                        }
                        .padding(.top, 8)
                    }
                    .padding(22)
                }

                FMSectionLabel(title: "Transaction ledger")
                FMCard(padding: 6) {
                    if case .ok(let list) = env.mainViewModel.orders {
                        ForEach(Array(list.prefix(8).enumerated()), id: \.element.id) { idx, order in
                            HStack(spacing: 13) {
                                Image(systemName: "bag")
                                    .frame(width: 38, height: 38)
                                    .background(FMTheme.surface2)
                                    .clipShape(RoundedRectangle(cornerRadius: 11))
                                VStack(alignment: .leading) {
                                    Text("\(order.id.suffix(8).uppercased()) · \(order.items?.count ?? 0) items")
                                        .font(.system(size: 13.5, weight: .semibold))
                                    Text(order.createdAt ?? "—").font(.system(size: 12)).foregroundStyle(FMTheme.ink4)
                                }
                                Spacer()
                                Text(FMTheme.inr(order.finalAmount?.doubleValue ?? 0))
                                    .font(.system(size: 14, weight: .bold, design: .monospaced))
                            }
                            .padding(.horizontal, 8)
                            .padding(.vertical, 12)
                            if idx < min(7, list.count - 1) { Divider() }
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
    }

    private var outstanding: Double {
        guard case .ok(let list) = env.mainViewModel.orders else { return 0 }
        return list
            .filter { ($0.paymentStatus?.uppercased() ?? "") != "PAID" && $0.status.uppercased() != "CANCELLED" }
            .reduce(0) { $0 + ($1.finalAmount?.doubleValue ?? $1.totalAmount?.doubleValue ?? 0) }
    }
}
