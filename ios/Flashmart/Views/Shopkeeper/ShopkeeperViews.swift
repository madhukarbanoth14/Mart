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
        case "orders": ShopkeeperOrdersView(selectedTab: $selectedTab, path: $path)
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
                        FMGlyphButton(systemName: "bell", badge: pendingCount > 0 ? pendingCount : nil)
                        Button { selectedTab = "profile" } label: {
                            FMAvatar(name: user.name)
                        }
                        .buttonStyle(.plain)
                    }
                )
            )

            VStack(spacing: 16) {
                outstandingHero
                statGrid
                quickActionsFour
                promoBanner
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

    private var outstandingHero: some View {
        ZStack(alignment: .leading) {
            RoundedRectangle(cornerRadius: FMTheme.heroRadius, style: .continuous)
                .fill(FMTheme.heroGradient)
                .shadow(color: FMTheme.brand.opacity(0.22), radius: 12, y: 6)
            Circle()
                .fill(.white.opacity(0.08))
                .frame(width: 150, height: 150)
                .offset(x: 220, y: -50)

            VStack(alignment: .leading, spacing: 14) {
                Text("Outstanding balance")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.85))
                Text(FMTheme.inr(outstanding))
                    .font(.system(size: 34, weight: .bold, design: .monospaced))
                    .foregroundStyle(.white)
                HStack(spacing: 10) {
                    Button { path.append(AppRoute.wallet) } label: {
                        Text("Pay now")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundStyle(FMTheme.goldInk)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(FMTheme.gold)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)
                    Button { path.append(AppRoute.wallet) } label: {
                        Text("View ledger")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(.white)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(20)
        }
    }

    private var statGrid: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 11) {
            homeStat(icon: "clock", label: "Pending", value: "\(pendingCount)", bg: FMTheme.goldTint, fg: FMTheme.goldInk)
            homeStat(icon: "checkmark.circle", label: "Delivered", value: "\(deliveredCount)", bg: FMTheme.brandTint, fg: FMTheme.brand)
            homeStat(icon: "doc.text", label: "Invoices", value: "\(invoiceCount)", bg: FMTheme.dealerBlueTint, fg: FMTheme.dealerBlue)
            homeStat(icon: "creditcard", label: "This month", value: FMTheme.inr(monthSpend), bg: FMTheme.surface3, fg: FMTheme.ink)
        }
    }

    private func homeStat(icon: String, label: String, value: String, bg: Color, fg: Color) -> some View {
        FMCard(padding: 15) {
            Image(systemName: icon)
                .font(.system(size: 17))
                .foregroundStyle(fg)
                .frame(width: 36, height: 36)
                .background(bg)
                .clipShape(RoundedRectangle(cornerRadius: 10))
            Text(value)
                .font(.system(size: 22, weight: .bold, design: .monospaced))
                .foregroundStyle(FMTheme.ink)
                .padding(.top, 10)
            Text(label)
                .font(.system(size: 12.5, weight: .semibold))
                .foregroundStyle(FMTheme.ink3)
        }
    }

    private var quickActionsFour: some View {
        HStack(spacing: 8) {
            quickIconTile(icon: "square.grid.2x2", label: "Browse") { selectedTab = "products" }
            quickIconTile(icon: "bag", label: "My orders") { selectedTab = "orders" }
            quickIconTile(icon: "doc.text", label: "Invoices") {
                if case .ok(let list) = env.mainViewModel.orders, let paid = list.first(where: { $0.paymentStatus?.uppercased() == "PAID" }) {
                    path.append(AppRoute.invoice(paid.id))
                } else { selectedTab = "orders" }
            }
            quickIconTile(icon: "phone", label: "Support") { path.append(AppRoute.profileHelp) }
        }
    }

    private func quickIconTile(icon: String, label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 22))
                    .foregroundStyle(FMTheme.brand)
                    .frame(width: 56, height: 56)
                    .background(FMTheme.brandTint)
                    .clipShape(RoundedRectangle(cornerRadius: 18))
                Text(label)
                    .font(.system(size: 11.5, weight: .semibold))
                    .foregroundStyle(FMTheme.ink3)
            }
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.plain)
    }

    private var promoBanner: some View {
        HStack {
            VStack(alignment: .leading, spacing: 3) {
                Text("THIS WEEK")
                    .font(.system(size: 11, weight: .heavy))
                    .foregroundStyle(FMTheme.goldInk)
                Text("Extra 8% off staples")
                    .font(.system(size: 16, weight: .heavy))
                    .foregroundStyle(FMTheme.ink)
                Text("On orders above ₹5,000")
                    .font(.system(size: 12.5))
                    .foregroundStyle(FMTheme.goldInk)
            }
            Spacer()
        }
        .padding(16)
        .background(FMTheme.goldTint)
        .clipShape(RoundedRectangle(cornerRadius: 20))
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

    private var pendingCount: Int {
        guard case .ok(let list) = env.mainViewModel.orders else { return 0 }
        return list.filter { $0.status.uppercased() == "PENDING" }.count
    }

    private var deliveredCount: Int {
        guard case .ok(let list) = env.mainViewModel.orders else { return 0 }
        return list.filter { $0.status.uppercased() == "DELIVERED" }.count
    }

    private var invoiceCount: Int {
        if let ready = env.mainViewModel.shopkeeperSummary?.invoicesReady { return ready }
        guard case .ok(let list) = env.mainViewModel.orders else { return 0 }
        return list.filter { $0.paymentStatus?.uppercased() == "PAID" }.count
    }

    private var outstanding: Double {
        guard case .ok(let list) = env.mainViewModel.orders else { return 0 }
        return list
            .filter { ($0.paymentStatus?.uppercased() ?? "") != "PAID" && $0.status.uppercased() != "CANCELLED" }
            .reduce(0) { $0 + ($1.finalAmount?.doubleValue ?? $1.totalAmount?.doubleValue ?? 0) }
    }

    private var monthSpend: Double {
        guard case .ok(let list) = env.mainViewModel.orders else { return 0 }
        return list.reduce(0) { $0 + ($1.finalAmount?.doubleValue ?? $1.totalAmount?.doubleValue ?? 0) } * 0.15
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
    @State private var quantityPickerProduct: Product?
    @State private var showQuantityPicker = false

    var body: some View {
        ZStack(alignment: .bottom) {
            FMScreen(showNav: true) {
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text(buyerRole == "SHOPKEEPER" ? "Products" : title)
                            .font(.system(size: 26, weight: .heavy))
                            .foregroundStyle(FMTheme.ink)
                            .tracking(-0.5)
                        Spacer()
                        FMGlyphButton(systemName: "line.3.horizontal.decrease.circle", accent: FMTheme.brand) {}
                        FMGlyphButton(systemName: "cart", badge: env.mainViewModel.cartCount) {
                            path.append(AppRoute.cart)
                        }
                    }
                    .padding(.horizontal, 16)

                    HStack(spacing: 10) {
                        Image(systemName: "magnifyingglass").foregroundStyle(FMTheme.ink4)
                        TextField("Search products or brands", text: $search)
                            .onChange(of: search) { _, v in
                                env.mainViewModel.searchText = v
                                Task { await env.mainViewModel.loadProducts() }
                            }
                        Image(systemName: "square.grid.2x2")
                            .foregroundStyle(FMTheme.ink4)
                    }
                    .padding(.horizontal, 16)
                    .frame(height: 52)
                    .background(FMTheme.surface3)
                    .clipShape(Capsule())
                    .padding(.horizontal, 16)

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
                                        .background(selected ? FMTheme.brandTint : FMTheme.surface)
                                        .foregroundStyle(selected ? FMTheme.brandInk : FMTheme.ink2)
                                        .clipShape(Capsule())
                                        .overlay(Capsule().stroke(selected ? FMTheme.brand.opacity(0.35) : FMTheme.line2))
                                }
                            }
                        }
                        .padding(.horizontal, 16)
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
        .sheet(isPresented: $showQuantityPicker) {
            if let product = quantityPickerProduct {
                FMQuantityPickerSheet(
                    productName: product.name,
                    max: env.mainViewModel.maxOrderQuantity,
                    quickChips: env.mainViewModel.quickQuantityChips,
                    onConfirm: { qty in
                        env.mainViewModel.addToCart(product, qty: qty)
                        quantityPickerProduct = nil
                    }
                )
            }
        }
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
                            .foregroundStyle(FMTheme.goldInk)
                            .padding(.horizontal, 7)
                            .padding(.vertical, 2)
                            .background(FMTheme.goldTint)
                            .clipShape(RoundedRectangle(cornerRadius: 6))
                    }
                }
                Spacer()
                if qty > 0 {
                    FMQuantityInput(
                        value: qty,
                        onChange: { env.mainViewModel.setCartQty(productId: product.id, qty: $0) },
                        min: 0,
                        max: env.mainViewModel.maxOrderQuantity,
                        compact: true
                    )
                } else {
                    Button {
                        quantityPickerProduct = product
                        showQuantityPicker = true
                    } label: {
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
    @Binding var selectedTab: String
    @Binding var path: NavigationPath
    @State private var reorderAlert: String?
    @State private var segment = "All"

    private var filteredOrders: [Order] {
        guard case .ok(let list) = env.mainViewModel.orders else { return [] }
        switch segment {
        case "Pending":
            return list.filter {
                let s = $0.status.uppercased()
                return s == "PENDING" || s == "PLACED" || s == "DEALER_CONFIRMED" || s == "OUT_FOR_DELIVERY"
            }
        case "Delivered":
            return list.filter { $0.status.uppercased() == "DELIVERED" }
        case "Cancelled":
            return list.filter { $0.status.uppercased() == "CANCELLED" }
        default:
            return list
        }
    }

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "My orders", subtitle: env.sessionStore.user?.name)
            FMSegmented(options: ["All", "Pending", "Delivered", "Cancelled"], selection: $segment)
                .padding(.horizontal, 16)
                .padding(.bottom, 12)

            switch env.mainViewModel.orders {
            case .loading:
                FMSkeletonListScreen()
                    .padding(.horizontal, 16)
            case .err(let msg):
                FMErrorScreen(
                    title: "Couldn't load orders",
                    message: msg,
                    onPrimaryAction: { Task { await env.mainViewModel.loadMyOrders() } }
                )
            case .ok:
                if filteredOrders.isEmpty {
                    FMEmptyStateHero(
                        icon: "bag",
                        title: segment == "All" ? "No orders yet" : "No \(segment.lowercased()) orders",
                        message: segment == "All"
                            ? "When you place your first order, it'll show up here with live delivery tracking."
                            : "Try another filter or browse the catalog to place an order.",
                        actionTitle: segment == "All" ? "Browse catalog" : nil,
                        onAction: segment == "All" ? { selectedTab = "products" } : nil
                    )
                } else {
                    shopkeeperOrdersList(filteredOrders, path: $path) { orderId in
                        Task {
                            let result = await env.mainViewModel.reorderFromOrder(orderId: orderId)
                            if result.success {
                                path.append(AppRoute.cart)
                                if let msg = result.message { reorderAlert = msg }
                            } else {
                                reorderAlert = result.message ?? "Could not reorder"
                            }
                        }
                    }
                }
            case .idle:
                EmptyView()
            }
        }
        .task { await env.mainViewModel.loadMyOrders() }
        .alert("Reorder", isPresented: Binding(
            get: { reorderAlert != nil },
            set: { if !$0 { reorderAlert = nil } }
        )) {
            Button("OK", role: .cancel) { reorderAlert = nil }
        } message: {
            Text(reorderAlert ?? "")
        }
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

func shopkeeperOrdersList(
    _ orders: [Order],
    path: Binding<NavigationPath>,
    onReorder: @escaping (String) -> Void
) -> some View {
    VStack(spacing: 10) {
        if orders.isEmpty {
            Text("No orders yet").foregroundStyle(FMTheme.ink3).padding()
        } else {
            ForEach(orders) { order in
                shopkeeperOrderCard(order, path: path, onReorder: { onReorder(order.id) })
            }
        }
    }
    .padding(.horizontal, 16)
}

func shopkeeperOrderCard(_ order: Order, path: Binding<NavigationPath>, onReorder: @escaping () -> Void) -> some View {
    let items = order.items ?? []
    let delivered = order.status.uppercased() == "DELIVERED"
    return FMCard {
        Button { path.wrappedValue.append(AppRoute.orderDetail(order.id)) } label: {
            HStack(spacing: 11) {
                ZStack {
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .fill(FMTheme.surface3)
                        .frame(width: 40, height: 40)
                    Image(systemName: "bag")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(FMTheme.ink3)
                }
                VStack(alignment: .leading, spacing: 3) {
                    Text(order.id.suffix(8).uppercased())
                        .font(.system(size: 14.5, weight: .bold, design: .monospaced))
                    Text("\(order.createdAt?.prefix(10).description ?? "—") · \(items.count) items")
                        .font(.system(size: 12, design: .monospaced))
                        .foregroundStyle(FMTheme.ink3)
                }
                Spacer()
                FMBadge(status: order.status)
            }
        }
        .buttonStyle(.plain)

        if !items.isEmpty {
            FlowLayout(spacing: 6) {
                ForEach(items.prefix(3), id: \.productId) { item in
                    let name = item.product?.name.split(separator: " ").prefix(2).joined(separator: " ") ?? "Item"
                    Text("\(item.quantity)× \(name)")
                        .font(.system(size: 11, weight: .semibold, design: .monospaced))
                        .foregroundStyle(FMTheme.ink3)
                        .padding(.horizontal, 9)
                        .padding(.vertical, 4)
                        .background(FMTheme.surface3)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
            }
            .padding(.top, 4)
        }

        HStack {
            Text(FMTheme.inr(ProductPricing.orderMath(order: order).total))
                .font(.system(size: 15, weight: .bold, design: .monospaced))
            Spacer()
            HStack(spacing: 8) {
                if delivered {
                    FMButton(title: "Invoice", variant: .outline, fullWidth: false) {
                        path.wrappedValue.append(AppRoute.invoice(order.id))
                    }
                    FMButton(title: "Reorder", variant: .soft, fullWidth: false, action: onReorder)
                } else {
                    FMButton(title: "Track", variant: .primary, fullWidth: false) {
                        path.wrappedValue.append(AppRoute.tracking(order.id))
                    }
                }
            }
        }
        .padding(.top, 12)
        .overlay(alignment: .top) {
            Rectangle().fill(FMTheme.line).frame(height: 1)
        }
    }
}

/// Simple horizontal flow for item chips (iOS 16+).
private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = arrange(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = arrange(proposal: proposal, subviews: subviews)
        for (index, frame) in result.frames.enumerated() {
            subviews[index].place(at: CGPoint(x: bounds.minX + frame.minX, y: bounds.minY + frame.minY), proposal: .unspecified)
        }
    }

    private func arrange(proposal: ProposedViewSize, subviews: Subviews) -> (size: CGSize, frames: [CGRect]) {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var frames: [CGRect] = []
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth, x > 0 {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            frames.append(CGRect(origin: CGPoint(x: x, y: y), size: size))
            rowHeight = max(rowHeight, size.height)
            x += size.width + spacing
        }
        return (CGSize(width: maxWidth, height: y + rowHeight), frames)
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
            FMRow(icon: "doc.badge.plus", title: "Documents", subtitle: user.documentStatus.replacingOccurrences(of: "_", with: " ").capitalized, showDivider: true) {
                path.wrappedValue.append(AppRoute.profileDocuments)
            }
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
