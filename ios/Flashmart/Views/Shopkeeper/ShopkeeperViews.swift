import SwiftUI

// MARK: - Shopkeeper tabs

struct ShopkeeperTabHost: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var selectedTab: String
    @Binding var path: NavigationPath
    let user: SessionUser
    let onLogout: () -> Void

    var body: some View {
        switch selectedTab {
        case "products": CatalogView(path: $path, buyerRole: "SHOPKEEPER", title: "Catalog", subtitle: nil)
        case "orders": ShopkeeperOrdersView(path: $path)
        case "profile": ShopkeeperProfileView(path: $path, user: user, onLogout: onLogout)
        default: ShopkeeperHomeView(selectedTab: $selectedTab, path: $path, user: user)
        }
    }
}

struct ShopkeeperHomeView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var selectedTab: String
    @Binding var path: NavigationPath
    let user: SessionUser

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(
                title: user.name,
                kicker: greeting,
                trailing: AnyView(
                    HStack(spacing: 8) {
                        FMGlyphButton(systemName: "bell", badge: 1)
                        Button { selectedTab = "profile" } label: {
                            FMAvatar(name: user.name)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Open profile")
                    }
                )
            )

            VStack(spacing: 16) {
                heroCard
                quickActions
                recentOrders
            }
            .padding(.horizontal, 16)
        }
    }

    private var greeting: String {
        let hour = Calendar.current.component(.hour, from: Date())
        let part = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening"
        return "\(part), \(user.name.split(separator: " ").first.map(String.init) ?? user.name)"
    }

    private var heroCard: some View {
        ZStack(alignment: .leading) {
            RoundedRectangle(cornerRadius: FMTheme.heroRadius, style: .continuous)
                .fill(FMTheme.heroGradient)
                .shadow(color: FMTheme.brand.opacity(0.25), radius: 12, y: 6)
            Circle()
                .fill(.white.opacity(0.08))
                .frame(width: 160, height: 160)
                .offset(x: 220, y: -40)

            VStack(alignment: .leading, spacing: 16) {
                Text("Ready to restock?")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.85))

                HStack(spacing: 26) {
                    statBlock(value: "\(orderCount)", label: "Total orders")
                    Rectangle().fill(.white.opacity(0.2)).frame(width: 1, height: 44)
                    statBlock(value: "\(inProgress)", label: "In progress")
                }

                FMButton(title: "New order", variant: .outline, icon: "plus") {
                    if env.mainViewModel.cartCount > 0 {
                        path.append(AppRoute.checkout)
                    } else {
                        selectedTab = "products"
                    }
                }
                .background(.white)
                .clipShape(RoundedRectangle(cornerRadius: FMTheme.buttonRadius))
            }
            .padding(20)
            .foregroundStyle(.white)
        }
    }

    private func statBlock(value: String, label: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(value).font(.system(size: 30, weight: .bold, design: .monospaced))
            Text(label).font(.system(size: 12)).opacity(0.8)
        }
    }

    private var quickActions: some View {
        HStack(spacing: 10) {
            quickTile(icon: "square.grid.2x2", label: "Browse") { selectedTab = "products" }
            quickTile(icon: "doc.text", label: "Invoices") {
                if case .ok(let list) = env.mainViewModel.orders, let paid = list.first(where: { $0.paymentStatus?.uppercased() == "PAID" }) {
                    path.append(AppRoute.invoice(paid.id))
                }
            }
            quickTile(icon: "shippingbox", label: "Track") {
                if case .ok(let list) = env.mainViewModel.orders, let active = list.first {
                    path.append(AppRoute.tracking(active.id))
                }
            }
        }
    }

    private func quickTile(icon: String, label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            FMCard(padding: 14) {
                VStack(spacing: 9) {
                    Image(systemName: icon)
                        .font(.system(size: 18))
                        .foregroundStyle(FMTheme.brand)
                        .frame(width: 40, height: 40)
                        .background(FMTheme.brandTint)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    Text(label)
                        .font(.system(size: 12.5, weight: .semibold))
                        .foregroundStyle(FMTheme.ink2)
                }
                .frame(maxWidth: .infinity)
            }
        }
        .buttonStyle(.plain)
    }

    private var recentOrders: some View {
        VStack(alignment: .leading, spacing: 0) {
            FMSectionLabel(title: "Recent orders", actionTitle: "See all") { selectedTab = "orders" }
            FMCard(padding: 4) {
                if case .ok(let list) = env.mainViewModel.orders {
                    ForEach(Array(list.prefix(3).enumerated()), id: \.element.id) { idx, order in
                        Button { path.append(AppRoute.orderDetail(order.id)) } label: {
                            orderRow(order, last: idx == min(2, list.count - 1))
                        }
                        .buttonStyle(.plain)
                    }
                } else {
                    Text("Loading orders…").font(.system(size: 13)).foregroundStyle(FMTheme.ink3).padding()
                }
            }
        }
    }

    private func orderRow(_ order: Order, last: Bool) -> some View {
        FMRow(
            icon: "bag",
            title: order.id,
            subtitle: "\(order.items?.count ?? 0) items",
            trailing: AnyView(
                VStack(alignment: .trailing, spacing: 5) {
                    Text(FMTheme.inr(ProductPricing.orderMath(order: order).total))
                        .font(.system(size: 14, weight: .bold, design: .monospaced))
                    FMBadge(status: order.status)
                }
            ),
            showDivider: !last
        )
    }

    private var orderCount: Int {
        if case .ok(let list) = env.mainViewModel.orders { return list.count }
        return env.mainViewModel.shopkeeperSummary?.openOrders ?? 0
    }

    private var inProgress: Int {
        env.mainViewModel.shopkeeperSummary?.inDelivery ?? 0
    }
}

struct CatalogView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let buyerRole: String
    let title: String
    let subtitle: String?
    @State private var search = ""

    var body: some View {
        ZStack(alignment: .bottom) {
            FMScreen(showNav: true) {
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(title)
                                .font(.system(size: 25, weight: .bold))
                                .foregroundStyle(FMTheme.ink)
                            if let subtitle {
                                Text(subtitle)
                                    .font(.system(size: 13))
                                    .foregroundStyle(FMTheme.ink3)
                            }
                        }
                        Spacer()
                        FMGlyphButton(systemName: "cart", badge: env.mainViewModel.cartCount) {
                            path.append(AppRoute.cart)
                        }
                    }
                    .padding(.horizontal, 20)

                    HStack(spacing: 10) {
                        Image(systemName: "magnifyingglass").foregroundStyle(FMTheme.ink4)
                        TextField("Search products or brands", text: $search)
                            .onSubmit { Task { env.mainViewModel.searchText = search; await env.mainViewModel.loadProducts() } }
                    }
                    .padding(.horizontal, 14)
                    .frame(height: 46)
                    .background(FMTheme.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 13))
                    .overlay(RoundedRectangle(cornerRadius: 13).stroke(FMTheme.line))
                    .padding(.horizontal, 20)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(ProductShelf.allCases, id: \.rawValue) { shelf in
                                let selected = env.mainViewModel.selectedShelf == shelf.rawValue
                                Button {
                                    env.mainViewModel.selectedShelf = shelf.rawValue
                                    Task { await env.mainViewModel.loadProducts() }
                                } label: {
                                    Text(shelf.label)
                                        .font(.system(size: 13, weight: .semibold))
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 8)
                                        .background(selected ? FMTheme.ink : FMTheme.surface)
                                        .foregroundStyle(selected ? .white : FMTheme.ink2)
                                        .clipShape(RoundedRectangle(cornerRadius: 11))
                                        .overlay(RoundedRectangle(cornerRadius: 11).stroke(selected ? .clear : FMTheme.line2))
                                }
                            }
                        }
                        .padding(.horizontal, 20)
                    }

                    productList
                        .padding(.bottom, env.mainViewModel.cartCount > 0 ? 88 : 0)
                }
                .padding(.top, 8)
            }

            if env.mainViewModel.cartCount > 0 {
                FMFloatingCartBar(
                    itemCount: env.mainViewModel.cartCount,
                    totalLabel: FMTheme.inr(env.mainViewModel.cartTotal(forRole: buyerRole))
                ) {
                    path.append(AppRoute.cart)
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 72)
            }
        }
        .task { await env.mainViewModel.loadProducts() }
    }

    @ViewBuilder
    private var productList: some View {
        VStack(spacing: 10) {
            if case .ok(let list) = env.mainViewModel.products {
                ForEach(list) { product in
                    productCard(product)
                }
            } else if case .loading = env.mainViewModel.products {
                ProgressView().padding()
            } else if case .err(let msg) = env.mainViewModel.products {
                Text(msg).foregroundStyle(FMTheme.neg).padding()
            }
        }
        .padding(.horizontal, 16)
    }

    private func productCard(_ product: Product) -> some View {
        let qty = env.mainViewModel.cartLines.first(where: { $0.product.id == product.id })?.quantity ?? 0
        return FMCard(padding: 12) {
            HStack(spacing: 13) {
                Button { path.append(AppRoute.productDetail(product.id)) } label: {
                    FMProductThumb(product: product, size: 62)
                }
                .buttonStyle(.plain)

                VStack(alignment: .leading, spacing: 4) {
                    Text(product.brand?.name.uppercased() ?? "BRAND")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(FMTheme.brand)
                    Text(product.name)
                        .font(.system(size: 14.5, weight: .semibold))
                        .lineLimit(2)
                    HStack(spacing: 7) {
                        Text(FMTheme.inr(product.catalogUnitPrice(forRole: buyerRole)))
                            .font(.system(size: 15, weight: .bold, design: .monospaced))
                        Text("\(Int(product.discountPercent(forRole: buyerRole)))% off")
                            .font(.system(size: 10.5, weight: .bold))
                            .foregroundStyle(FMTheme.pos)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(FMTheme.posTint)
                            .clipShape(RoundedRectangle(cornerRadius: 6))
                    }
                }
                Spacer()
                if qty > 0 {
                    FMStepper(value: qty) { env.mainViewModel.setCartQty(productId: product.id, qty: $0) }
                } else {
                    Button { env.mainViewModel.addToCart(product) } label: {
                        Image(systemName: "plus")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(.white)
                            .frame(width: 40, height: 40)
                            .background(FMTheme.brand)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
            }
        }
    }
}

struct ShopkeeperCatalogView: View {
    @Binding var path: NavigationPath
    var body: some View {
        CatalogView(path: $path, buyerRole: "SHOPKEEPER", title: "Catalog", subtitle: nil)
    }
}

struct ShopkeeperOrdersView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "Orders", subtitle: "Your order history")
            ordersList(env.mainViewModel.orders, path: $path)
        }
        .task { await env.mainViewModel.loadMyOrders() }
    }
}

struct ShopkeeperProfileView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let user: SessionUser
    let onLogout: () -> Void

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "Profile")
            profileContent(user: user, path: $path, onLogout: onLogout, stats: profileStats)
        }
    }

    private var profileStats: [(String, String)] {
        let count: Int = {
            if case .ok(let list) = env.mainViewModel.orders { return list.count }
            return 0
        }()
        let delivered: Int = {
            if case .ok(let list) = env.mainViewModel.orders {
                return list.filter { $0.status.uppercased() == "DELIVERED" }.count
            }
            return 0
        }()
        let active = max(0, count - delivered)
        return [("Orders", "\(count)"), ("Delivered", "\(delivered)"), ("Active", "\(active)")]
    }
}

func ordersList(_ state: LoadState<[Order]>, path: Binding<NavigationPath>) -> some View {
    VStack(spacing: 10) {
        switch state {
        case .ok(let list):
            if list.isEmpty {
                Text("No orders yet").foregroundStyle(FMTheme.ink3).padding()
            } else {
                ForEach(list) { order in
                    Button { path.wrappedValue.append(AppRoute.orderDetail(order.id)) } label: {
                        orderCard(order)
                    }
                    .buttonStyle(.plain)
                }
            }
        case .loading:
            ProgressView().padding()
        case .err(let msg):
            Text(msg).foregroundStyle(FMTheme.neg).padding()
        default:
            EmptyView()
        }
    }
    .padding(.horizontal, 16)
}

func orderCard(_ order: Order, highlight: Bool = false) -> some View {
    FMCard {
        HStack {
            FMAvatar(name: order.shopkeeper?.name ?? order.dealer?.name ?? "Order", tint: FMTheme.pos)
            VStack(alignment: .leading, spacing: 2) {
                Text(order.shopkeeper?.name ?? order.id)
                    .font(.system(size: 14.5, weight: .bold))
                    .lineLimit(1)
                Text("\(order.id) · \(order.items?.count ?? 0) items")
                    .font(.system(size: 12.5, design: .monospaced))
                    .foregroundStyle(FMTheme.ink3)
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 5) {
                Text(FMTheme.inr(ProductPricing.orderMath(order: order).total))
                    .font(.system(size: 15, weight: .bold, design: .monospaced))
                FMBadge(status: order.status)
            }
        }
    }
    .overlay {
        if highlight {
            RoundedRectangle(cornerRadius: FMTheme.cardRadius)
                .stroke(FMTheme.pos, lineWidth: 2)
        }
    }
}

func profileContent(user: SessionUser, path: Binding<NavigationPath>, onLogout: @escaping () -> Void, stats: [(String, String)]) -> some View {
    VStack(spacing: 14) {
        FMCard {
            HStack(spacing: 14) {
                FMAvatar(name: user.name, size: 56)
                VStack(alignment: .leading, spacing: 4) {
                    Text(user.name).font(.system(size: 18, weight: .bold))
                    Text("\(user.email) · \(user.role.capitalized)")
                        .font(.system(size: 13))
                        .foregroundStyle(FMTheme.ink3)
                }
                Spacer()
                FMBadge(status: "ACTIVE")
            }
        }

        HStack(spacing: 10) {
            ForEach(stats, id: \.0) { label, value in
                FMCard(padding: 14) {
                    VStack(spacing: 3) {
                        Text(value).font(.system(size: 22, weight: .bold, design: .monospaced))
                        Text(label).font(.system(size: 11.5, weight: .semibold)).foregroundStyle(FMTheme.ink4)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }

        FMCard(padding: 6) {
            FMRow(icon: "mappin.and.ellipse", title: "Store address", subtitle: ShopkeeperProfileStore.storeAddress, showDivider: true) {
                path.wrappedValue.append(AppRoute.profileStoreAddress)
            }
            FMRow(icon: "creditcard", title: "Payment methods", subtitle: ShopkeeperProfileStore.dummyCardMasked, showDivider: true) {
                path.wrappedValue.append(AppRoute.profilePaymentMethods)
            }
            FMRow(icon: "doc.text", title: "GST details", subtitle: ShopkeeperProfileStore.gstin, showDivider: true) {
                path.wrappedValue.append(AppRoute.profileGstDetails)
            }
            FMRow(icon: "bell", title: "Notifications", subtitle: "Order & delivery alerts", showDivider: true) {
                path.wrappedValue.append(AppRoute.profileNotifications)
            }
            FMRow(icon: "questionmark.circle", title: "Help & support", subtitle: "Chat, call, FAQs", showDivider: false) {
                path.wrappedValue.append(AppRoute.profileHelp)
            }
        }

        FMButton(title: "Sign out", variant: .outline, icon: "rectangle.portrait.and.arrow.right", action: onLogout)
    }
    .padding(.horizontal, 16)
}
