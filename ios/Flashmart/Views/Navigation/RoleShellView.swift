import SwiftUI

struct RoleShellView: View {
    @Environment(AppEnvironment.self) private var env
    let user: SessionUser
    let onLogout: () -> Void

    @State private var selectedTab: String = "home"
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            ZStack(alignment: .bottom) {
                tabContent
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

                FMBottomNav(items: navItems, selection: $selectedTab)
            }
            .background(FMTheme.bg)
            .navigationDestination(for: AppRoute.self) { route in
                sharedDestination(route)
            }
        }
        .task {
            await env.mainViewModel.refreshForRole()
            await env.pushTokenRegistrar.registerCurrentToken()
        }
    }

    @ViewBuilder
    private var tabContent: some View {
        switch user.userRole {
        case .shopkeeper:
            ShopkeeperTabHost(selectedTab: $selectedTab, path: $path, user: user, onLogout: onLogout)
        case .dealer:
            DealerTabHost(selectedTab: $selectedTab, path: $path, user: user, onLogout: onLogout)
        case .admin:
            AdminTabHost(selectedTab: $selectedTab, path: $path, user: user, onLogout: onLogout)
        case .employee:
            EmployeeTabHost(selectedTab: $selectedTab, path: $path, user: user, onLogout: onLogout)
        }
    }

    private var navItems: [FMNavItem] {
        switch user.userRole {
        case .shopkeeper:
            let pending = pendingOrderBadge
            return [
                FMNavItem(id: "home", icon: "house.fill", label: "Home"),
                FMNavItem(id: "products", icon: "square.grid.2x2.fill", label: "Products"),
                FMNavItem(id: "orders", icon: "bag.fill", label: "Orders", badge: pending),
                FMNavItem(id: "profile", icon: "person.fill", label: "Profile"),
            ]
        case .dealer:
            return [
                FMNavItem(id: "home", icon: "house.fill", label: "Home"),
                FMNavItem(id: "products", icon: "cube.box.fill", label: "SKUs"),
                FMNavItem(id: "orders", icon: "bag.fill", label: "Orders", badge: pendingOrderBadge),
                FMNavItem(id: "stock", icon: "shippingbox.fill", label: "Stock"),
                FMNavItem(id: "profile", icon: "person.fill", label: "Profile"),
            ]
        case .admin:
            return [
                FMNavItem(id: "home", icon: "house.fill", label: "Home"),
                FMNavItem(id: "finance", icon: "indianrupeesign.circle.fill", label: "Finance"),
                FMNavItem(id: "orders", icon: "doc.text.fill", label: "Orders"),
                FMNavItem(id: "team", icon: "person.3.fill", label: "Team", badge: env.mainViewModel.pendingCount),
                FMNavItem(id: "profile", icon: "person.fill", label: "Profile"),
            ]
        case .employee:
            return [
                FMNavItem(id: "home", icon: "house.fill", label: "Home"),
                FMNavItem(id: "network", icon: "point.3.connected.trianglepath.dotted", label: "Network"),
                FMNavItem(id: "profile", icon: "person.fill", label: "Profile"),
            ]
        }
    }

    private var pendingOrderBadge: Int? {
        guard case .ok(let list) = env.mainViewModel.orders else { return nil }
        let n = list.filter { $0.status.uppercased() == "PENDING" }.count
        return n > 0 ? n : nil
    }

    @ViewBuilder
    private func sharedDestination(_ route: AppRoute) -> some View {
        switch route {
        case .productDetail(let id):
            ProductDetailView(path: $path, productId: id)
        case .orderDetail(let id):
            OrderDetailView(path: $path, orderId: id)
        case .cart:
            CartView(path: $path)
        case .checkout:
            ShopkeeperCheckoutView(path: $path)
        case .wallet:
            ShopkeeperWalletView(path: $path)
        case .payment(let orderId):
            PaymentView(path: $path, orderId: orderId)
        case .invoice(let orderId):
            InvoiceView(orderId: orderId)
        case .orderConfirmation(let orderId):
            OrderConfirmationView(path: $path, orderId: orderId)
        case .tracking(let orderId):
            TrackingView(orderId: orderId)
        case .resetPassword(let token):
            ResetPasswordView()
        case .onboardShopkeeper:
            OnboardShopkeeperView(path: $path)
        case .onboardDealer:
            OnboardDealerView(path: $path)
        case .createEmployee:
            CreateEmployeeView(path: $path)
        case .skuManagement:
            SkuManagementView(path: $path)
        case .brandsManagement:
            BrandsManagementView(path: $path)
        case .areasManagement:
            AdminAreasView(path: $path)
        case .profileStoreAddress:
            ProfileStoreAddressView(path: $path)
        case .profilePaymentMethods:
            ProfilePaymentMethodsView(path: $path)
        case .profileGstDetails:
            ProfileGstDetailsView(path: $path)
        case .profileNotifications:
            ProfileNotificationsView(path: $path)
        case .profileHelp:
            ProfileHelpView(path: $path)
        case .profileDocuments:
            ProfileDocumentsView(path: $path)
        case .privacyPolicy:
            PrivacyPolicyView(path: $path)
        case .adminReview(let userId):
            AdminOnboardingReviewView(path: $path, userId: userId)
        case .financeSettlement(let settlementId):
            AdminSettlementDetailView(path: $path, settlementId: settlementId)
        case .financeDealer(let dealerId, let dealerName):
            AdminDealerPerformanceView(path: $path, dealerId: dealerId, dealerName: dealerName)
        case .dealerRevenue:
            DealerRevenueView(path: $path)
        case .dealerShopkeepers:
            DealerShopkeepersRevenueView()
        case .dealerReturns:
            DealerReturnsListView(path: $path)
        case .dealerReturnDetail(let id):
            DealerReturnDetailView(returnId: id)
        case .adminRefundDetail(let id):
            AdminRefundDetailView(refundId: id)
        }
    }
}
