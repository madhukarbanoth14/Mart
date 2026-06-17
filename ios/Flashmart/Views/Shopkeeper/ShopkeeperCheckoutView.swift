import SwiftUI

/// Checkout screen — card / UPI / pay-later demo + Razorpay (matches Android ShopkeeperPaymentScreen).
struct ShopkeeperCheckoutView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath

    @State private var method = "card"
    @State private var paying = false
    @State private var paidOrderId: String?
    @State private var showCardDialog = false
    @State private var showUpiDialog = false
    @State private var cardNumber = "4111 1111 1111 1111"
    @State private var cardExpiry = "12/28"
    @State private var cardCvv = "123"
    @State private var cardName = "Demo Shopkeeper"

    private var role: String { env.sessionStore.user?.role ?? "SHOPKEEPER" }
    private var math: OrderMath {
        ProductPricing.cartMath(lines: env.mainViewModel.cartLines, role: role)
    }

    private var isDealer: Bool { role.uppercased() == "DEALER" }

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    FMTopBar(
                        title: isDealer ? "Restock checkout" : "Payment",
                        subtitle: isDealer ? "Pay for warehouse restock" : "Secure checkout",
                        onBack: { path.removeLast() }
                    )

                    FMCard(padding: 22) {
                        VStack(spacing: 6) {
                            Text("Amount payable")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundStyle(FMTheme.ink3)
                            Text(FMTheme.inr(math.total))
                                .font(.system(size: 42, weight: .bold, design: .monospaced))
                                .foregroundStyle(FMTheme.ink)
                            Text("Incl. GST · \(env.mainViewModel.cartCount) items")
                                .font(.system(size: 12))
                                .foregroundStyle(FMTheme.ink3)
                        }
                        .frame(maxWidth: .infinity)
                    }

                    FMSectionLabel(title: "Payment method")
                    paymentMethodCard(id: "card", label: "Card", sub: "Demo credit / debit card", icon: "creditcard")
                    paymentMethodCard(id: "upi", label: "UPI", sub: "GPay · PhonePe · Paytm", icon: "indianrupeesign.circle")
                    paymentMethodCard(
                        id: "wallet",
                        label: isDealer ? "Pay on delivery" : "Pay on delivery",
                        sub: isDealer ? "Pay when stock is delivered" : "Cash / UPI to dealer",
                        icon: "banknote"
                    )

                    if !AppConfig.useLocalDemoAuth && !env.sessionStore.isLocalDemoMode {
                        FMSectionLabel(title: "Live payment")
                        paymentMethodCard(id: "razorpay", label: "Razorpay", sub: "Card · UPI · Netbanking", icon: "bolt.fill")
                    }

                    HStack(spacing: 8) {
                        Text("⚡")
                        Text(AppConfig.useLocalDemoAuth ? "Demo mode · no real charges" : "Test card: 4111 1111 1111 1111")
                            .font(.system(size: 12))
                            .foregroundStyle(FMTheme.ink3)
                    }
                    .frame(maxWidth: .infinity)

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

            if paidOrderId == nil {
                VStack(spacing: 0) {
                    LinearGradient(colors: [FMTheme.bg.opacity(0), FMTheme.bg], startPoint: .top, endPoint: .bottom)
                        .frame(height: 16)
                    FMButton(
                        title: paying ? "Processing…" : payButtonTitle,
                        variant: .dark,
                        enabled: !paying && !env.mainViewModel.cartLines.isEmpty
                    ) { startPay() }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 30)
                    .background(FMTheme.bg)
                }
            }
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .onChange(of: env.razorpayCoordinator.lastResult) { _, result in
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
                paying = false
            }
        }
        .alert("Enter card details", isPresented: $showCardDialog) {
            Button("Pay \(FMTheme.inr(math.total))") {
                Task { await payDemo(method: "card") }
            }
            Button("Cancel", role: .cancel) { }
        } message: {
            Text("Demo card \(cardNumber)\nExpiry \(cardExpiry) · CVV \(cardCvv)\nNo real payment is processed.")
        }
        .alert("Pay with UPI", isPresented: $showUpiDialog) {
            Button("Pay now") { Task { await payDemo(method: "upi") } }
            Button("Cancel", role: .cancel) { }
        } message: {
            Text("Confirm demo UPI payment of \(FMTheme.inr(math.total))?")
        }
    }

    private var payButtonTitle: String {
        switch method {
        case "wallet":
            return isDealer ? "Place restock order · pay later" : "Place order · pay later"
        case "razorpay":
            return isDealer ? "Place restock & pay with Razorpay" : "Place & pay with Razorpay"
        default:
            return isDealer ? "Pay \(FMTheme.inr(math.total)) & place restock" : "Pay \(FMTheme.inr(math.total))"
        }
    }

    private func paymentMethodCard(id: String, label: String, sub: String, icon: String) -> some View {
        let selected = method == id
        return Button { method = id } label: {
            FMCard(padding: 14) {
                HStack(spacing: 13) {
                    Image(systemName: icon)
                        .font(.system(size: 18))
                        .foregroundStyle(selected ? .white : FMTheme.ink3)
                        .frame(width: 42, height: 42)
                        .background(selected ? FMTheme.brand : FMTheme.surface2)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    VStack(alignment: .leading, spacing: 2) {
                        Text(label).font(.system(size: 15, weight: .bold)).foregroundStyle(FMTheme.ink)
                        Text(sub).font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                    }
                    Spacer()
                    Circle()
                        .strokeBorder(selected ? FMTheme.brand : FMTheme.line2, lineWidth: selected ? 7 : 2)
                        .frame(width: 22, height: 22)
                        .background(Circle().fill(selected ? FMTheme.brand : .clear))
                }
            }
            .overlay(
                RoundedRectangle(cornerRadius: FMTheme.cardRadius)
                    .stroke(selected ? FMTheme.brand : .clear, lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }

    private func startPay() {
        switch method {
        case "card": showCardDialog = true
        case "upi": showUpiDialog = true
        case "razorpay": Task { await payRazorpay() }
        default: Task { await payDemo(method: method) }
        }
    }

    private func payDemo(method: String) async {
        paying = true
        if let order = await env.mainViewModel.placeOrderWithDemoPayment(method: method) {
            paidOrderId = order.id
        }
        paying = false
    }

    private func payRazorpay() async {
        paying = true
        if let pending = await env.mainViewModel.placeOrderForRazorpay() {
            env.razorpayCoordinator.present(checkout: pending)
        } else if let msg = env.mainViewModel.placeOrderMessage, env.mainViewModel.placeOrderError == nil {
            // Demo mode fell back to mock pay
            paidOrderId = env.mainViewModel.lastPlacedOrder?.id
        }
        paying = false
    }

    private func successCard(orderId: String) -> some View {
        FMCard {
            VStack(alignment: .leading, spacing: 12) {
                HStack(spacing: 10) {
                    Image(systemName: "checkmark.circle.fill").foregroundStyle(FMTheme.pos)
                    Text(env.mainViewModel.paymentMessage ?? env.mainViewModel.placeOrderMessage ?? "Payment successful")
                        .font(.system(size: 14, weight: .semibold))
                }
                if isDealer {
                    FMButton(title: "View restock order", icon: "shippingbox") {
                        path.append(AppRoute.orderDetail(orderId))
                    }
                } else {
                    FMButton(title: "View invoice", icon: "doc.text") {
                        path.append(AppRoute.invoice(orderId))
                    }
                    FMButton(title: "Track delivery", variant: .soft, icon: "shippingbox") {
                        path.append(AppRoute.tracking(orderId))
                    }
                }
            }
        }
    }
}
