import SwiftUI

struct DealerTabHost: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var selectedTab: String
    @Binding var path: NavigationPath
    let user: SessionUser
    let onLogout: () -> Void

    var body: some View {
        switch selectedTab {
        case "products":
            CatalogView(
                path: $path,
                buyerRole: "DEALER",
                title: "Browse products",
                subtitle: "10% dealer discount · order stock from KNSR"
            )
        case "orders": DealerOrdersView(path: $path)
        case "stock": DealerStockView(selectedTab: $selectedTab)
        case "profile": DealerProfileView(user: user, onLogout: onLogout)
        default: DealerHomeView(selectedTab: $selectedTab, path: $path, user: user)
        }
    }
}

struct DealerHomeView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var selectedTab: String
    @Binding var path: NavigationPath
    let user: SessionUser

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(
                title: user.name,
                kicker: "Dealer dashboard",
                accent: FMTheme.pos,
                trailing: AnyView(
                    HStack(spacing: 8) {
                        if lowStockCount > 0 {
                            FMGlyphButton(systemName: "exclamationmark.triangle", badge: lowStockCount) {
                                selectedTab = "stock"
                            }
                        }
                        Button { selectedTab = "profile" } label: {
                            FMAvatar(name: user.name, tint: FMTheme.pos)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Open profile")
                    }
                )
            )

            VStack(spacing: 16) {
                revenueHero

                Button { selectedTab = "products" } label: {
                    FMCard {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Browse & order stock")
                                    .font(.system(size: 15, weight: .bold))
                                Text("Place restock orders for your warehouse")
                                    .font(.system(size: 12))
                                    .foregroundStyle(FMTheme.ink3)
                            }
                            Spacer()
                            Image(systemName: "cart.badge.plus")
                                .foregroundStyle(FMTheme.brand)
                        }
                    }
                }
                .buttonStyle(.plain)

                HStack(spacing: 11) {
                    FMStatTile(
                        label: "Pending orders",
                        value: "\(env.mainViewModel.dealerSummary?.pendingOrders ?? 0)",
                        icon: "clock",
                        accent: FMTheme.warn,
                        tint: FMTheme.warnTint,
                        subtitle: "Need your action"
                    )
                    FMStatTile(
                        label: "In transit",
                        value: "\(env.mainViewModel.dealerSummary?.todaysDeliveries ?? 0)",
                        icon: "shippingbox",
                        accent: FMTheme.brand,
                        tint: FMTheme.brandTint,
                        subtitle: "Accepted / out"
                    )
                }

                if let pending = pendingShopkeeperOrder {
                    FMSectionLabel(title: "Awaiting confirmation", actionTitle: "View all") { selectedTab = "orders" }
                    dealerOrderCard(pending, path: $path, action: "Accept order", variant: .primary, fresh: true)
                }
            }
            .padding(.horizontal, 16)
        }
        .task { await env.mainViewModel.loadStock() }
    }

    private var revenueHero: some View {
        ZStack(alignment: .leading) {
            RoundedRectangle(cornerRadius: FMTheme.heroRadius, style: .continuous)
                .fill(FMTheme.dealerHeroGradient)
                .shadow(color: FMTheme.pos.opacity(0.25), radius: 12, y: 6)
            Circle().fill(.white.opacity(0.08)).frame(width: 150).offset(x: 230, y: -30)

            VStack(alignment: .leading, spacing: 6) {
                Text("Today's revenue")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.85))
                Text(FMTheme.inr(env.mainViewModel.dealerSummary?.weeklyRevenue ?? 0))
                    .font(.system(size: 38, weight: .bold, design: .monospaced))
                    .foregroundStyle(.white)
                Text("Paid orders · Central Zone")
                    .font(.system(size: 12.5))
                    .foregroundStyle(.white.opacity(0.82))
            }
            .padding(20)
        }
    }

    private var pendingShopkeeperOrder: Order? {
        guard case .ok(let list) = env.mainViewModel.orders else { return nil }
        return list.first {
            $0.status.uppercased() == "PENDING" &&
                $0.kind?.uppercased() != "DEALER_RESTOCK"
        }
    }

    private var lowStockCount: Int {
        guard case .ok(let rows) = env.mainViewModel.stock else { return 0 }
        return rows.filter { $0.quantity < 5 }.count
    }
}

struct DealerOrdersView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    @State private var segment = "Pending"

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "Orders", subtitle: "Shopkeeper orders & your restocks")
            FMSegmented(options: ["Pending", "Active", "Done"], selection: $segment)
                .padding(.horizontal, 16)
                .padding(.bottom, 14)

            VStack(spacing: 11) {
                ForEach(filteredOrders) { order in
                    dealerOrderCard(order, path: $path, action: actionLabel(for: order), variant: actionVariant(for: order))
                }
            }
            .padding(.horizontal, 16)
        }
        .task { await env.mainViewModel.loadDealerOrders() }
    }

    private var filteredOrders: [Order] {
        env.mainViewModel.filteredOrders(segment: segment)
    }

    private func actionLabel(for order: Order) -> String? {
        if order.kind?.uppercased() == "DEALER_RESTOCK" { return nil }
        switch order.status.uppercased() {
        case "PENDING": return "Accept order"
        case "DEALER_CONFIRMED", "ACCEPTED": return "Mark out for delivery"
        case "OUT_FOR_DELIVERY": return "Mark delivered"
        default: return nil
        }
    }

    private func actionVariant(for order: Order) -> FMButtonVariant {
        order.status.uppercased() == "OUT_FOR_DELIVERY" ? .pos : .primary
    }
}

struct DealerStockView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var selectedTab: String

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "Stock", subtitle: stockSubtitle)
            VStack(spacing: 10) {
                if case .ok(let rows) = env.mainViewModel.stock {
                    if rows.isEmpty {
                        emptyStockState
                    } else {
                        ForEach(rows) { row in
                            stockRow(row)
                        }
                    }
                } else if case .loading = env.mainViewModel.stock {
                    ProgressView().padding()
                } else if case .err(let msg) = env.mainViewModel.stock {
                    Text(msg).foregroundStyle(FMTheme.neg).font(.system(size: 13)).padding()
                }
            }
            .padding(.horizontal, 16)
        }
        .task { await env.mainViewModel.loadStock() }
    }

    private var emptyStockState: some View {
        FMCard {
            VStack(spacing: 12) {
                Text("No stock yet")
                    .font(.system(size: 16, weight: .bold))
                Text("Browse products and place a restock order. Stock appears here after admin confirms delivery.")
                    .font(.system(size: 13))
                    .foregroundStyle(FMTheme.ink3)
                    .multilineTextAlignment(.center)
                FMButton(title: "Browse products", variant: .soft, icon: "square.grid.2x2") {
                    selectedTab = "products"
                }
            }
            .padding(.vertical, 8)
        }
    }

    private var stockSubtitle: String {
        guard case .ok(let rows) = env.mainViewModel.stock else { return "Loading…" }
        let low = rows.filter { $0.quantity < 5 }.count
        return "\(rows.count) SKUs · \(low) low"
    }

    private func stockRow(_ row: StockRow) -> some View {
        let product = row.product
        let low = row.quantity < 5
        let pct = min(100, Double(row.quantity) / 180 * 100)
        return FMCard(padding: 13) {
            HStack(spacing: 13) {
                if let p = product { FMProductThumb(product: p, size: 48) }
                VStack(alignment: .leading, spacing: 6) {
                    Text(product?.name ?? "SKU")
                        .font(.system(size: 14, weight: .semibold))
                        .lineLimit(2)
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            Capsule().fill(FMTheme.surface3)
                            Capsule()
                                .fill(low ? FMTheme.neg : FMTheme.pos)
                                .frame(width: geo.size.width * pct / 100)
                        }
                    }
                    .frame(height: 6)
                    Text(low ? "Low stock" : "In stock")
                        .font(.system(size: 11.5))
                        .foregroundStyle(low ? FMTheme.neg : FMTheme.pos)
                }
                Text("\(row.quantity)")
                    .font(.system(size: 12, weight: .bold, design: .monospaced))
                    .foregroundStyle(low ? FMTheme.neg : FMTheme.ink2)
                if low {
                    FMButton(title: "Reorder", variant: .soft, icon: "arrow.clockwise", fullWidth: false) {
                        selectedTab = "products"
                    }
                    .frame(width: 100)
                }
            }
        }
    }
}

struct DealerProfileView: View {
    @Environment(AppEnvironment.self) private var env
    let user: SessionUser
    let onLogout: () -> Void

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "Profile")
            VStack(spacing: 14) {
                FMCard {
                    HStack(spacing: 14) {
                        FMAvatar(name: user.name, size: 56, tint: FMTheme.pos)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(user.name).font(.system(size: 18, weight: .bold))
                            Text("\(user.email) · Dealer")
                                .font(.system(size: 13))
                                .foregroundStyle(FMTheme.ink3)
                        }
                        Spacer()
                        FMBadge(status: "ACTIVE")
                    }
                }

                HStack(spacing: 10) {
                    statCard("Orders", "\(orderCount)")
                    statCard("Revenue", FMTheme.inr(env.mainViewModel.dealerSummary?.weeklyRevenue ?? 0))
                    statCard("Pending", "\(env.mainViewModel.dealerSummary?.pendingOrders ?? 0)")
                }

                FMButton(title: "Sign out", variant: .outline, icon: "rectangle.portrait.and.arrow.right", action: onLogout)
            }
            .padding(.horizontal, 16)
        }
    }

    private func statCard(_ label: String, _ value: String) -> some View {
        FMCard(padding: 14) {
            VStack(spacing: 3) {
                Text(value).font(.system(size: 18, weight: .bold, design: .monospaced)).lineLimit(1).minimumScaleFactor(0.7)
                Text(label).font(.system(size: 11.5, weight: .semibold)).foregroundStyle(FMTheme.ink4)
            }
            .frame(maxWidth: .infinity)
        }
    }

    private var orderCount: Int {
        if case .ok(let list) = env.mainViewModel.orders { return list.count }
        return 0
    }
}

func dealerOrderCard(_ order: Order, path: Binding<NavigationPath>, action: String?, variant: FMButtonVariant = .primary, fresh: Bool = false) -> some View {
    DealerOrderCardView(order: order, path: path, action: action, variant: variant, fresh: fresh)
}

private struct DealerOrderCardView: View {
    @Environment(AppEnvironment.self) private var env
    let order: Order
    @Binding var path: NavigationPath
    let action: String?
    let variant: FMButtonVariant
    let fresh: Bool

    var body: some View {
        FMCard {
            HStack(spacing: 12) {
                FMAvatar(name: orderTitle, tint: FMTheme.pos)
                VStack(alignment: .leading, spacing: 2) {
                    Text(orderTitle)
                        .font(.system(size: 14.5, weight: .bold))
                        .lineLimit(1)
                    Text(orderSubtitle)
                        .font(.system(size: 12.5, design: .monospaced))
                        .foregroundStyle(FMTheme.ink3)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 4) {
                    Text(FMTheme.inr(ProductPricing.orderMath(order: order).total))
                        .font(.system(size: 15, weight: .bold, design: .monospaced))
                    if order.kind?.uppercased() == "DEALER_RESTOCK" {
                        FMBadge(status: "RESTOCK")
                    } else {
                        FMBadge(status: order.status)
                    }
                }
            }
            .padding(.bottom, 12)

            if let items = order.items?.prefix(3) {
                HStack(spacing: 6) {
                    ForEach(Array(items), id: \.productId) { item in
                        Text("\(item.quantity)× \(item.product?.name.split(separator: " ").prefix(2).joined(separator: " ") ?? "Item")")
                            .font(.system(size: 11.5, weight: .semibold, design: .monospaced))
                            .padding(.horizontal, 9)
                            .padding(.vertical, 4)
                            .background(FMTheme.surface2)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                }
                .padding(.bottom, action != nil ? 12 : 0)
            }

            if let action {
                FMButton(title: action, variant: variant, fullWidth: true) {
                    Task { await performAction(action) }
                }
            }
        }
        .overlay {
            if fresh {
                RoundedRectangle(cornerRadius: FMTheme.cardRadius)
                    .stroke(FMTheme.pos, lineWidth: 2)
            }
        }
        .onTapGesture { path.append(AppRoute.orderDetail(order.id)) }
    }

    private var orderTitle: String {
        if order.kind?.uppercased() == "DEALER_RESTOCK" { return "Restock order" }
        return order.shopkeeper?.name ?? "Shopkeeper"
    }

    private var orderSubtitle: String {
        if order.kind?.uppercased() == "DEALER_RESTOCK" {
            return "\(order.id) · Awaiting admin"
        }
        return "\(order.id) · Central Zone"
    }

    private func performAction(_ label: String) async {
        switch label {
        case "Accept order": await env.mainViewModel.confirmOrder(order.id)
        case "Mark out for delivery": await env.mainViewModel.dispatchOrder(order.id)
        case "Mark delivered": await env.mainViewModel.deliverOrder(order.id)
        default: break
        }
    }
}
