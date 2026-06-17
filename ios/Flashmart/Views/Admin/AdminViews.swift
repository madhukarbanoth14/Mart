import SwiftUI

private enum AdminTeamFilter: String, CaseIterable {
    case pending = "Pending"
    case employees = "Employees"
    case all = "All"
    case dealers = "Dealers"
    case shopkeepers = "Shopkeepers"
}

private enum AdminUserConfirmAction: Identifiable {
    case approve(UserRow)
    case reject(UserRow)
    case deactivate(UserRow)
    case reactivate(UserRow)

    var id: String {
        switch self {
        case .approve(let u): return "approve-\(u.id)"
        case .reject(let u): return "reject-\(u.id)"
        case .deactivate(let u): return "deactivate-\(u.id)"
        case .reactivate(let u): return "reactivate-\(u.id)"
        }
    }
}

struct AdminTabHost: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var selectedTab: String
    @Binding var path: NavigationPath
    let user: SessionUser
    let onLogout: () -> Void

    var body: some View {
        switch selectedTab {
        case "orders": AdminOrdersView(path: $path)
        case "team": AdminTeamView(path: $path)
        case "profile": AdminProfileView(user: user, onLogout: onLogout)
        default: AdminHomeView(selectedTab: $selectedTab, path: $path, user: user, onLogout: onLogout)
        }
    }
}

struct AdminHomeView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var selectedTab: String
    @Binding var path: NavigationPath
    let user: SessionUser
    let onLogout: () -> Void

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(
                title: "Admin console",
                kicker: "Company oversight",
                accent: FMTheme.brand,
                trailing: AnyView(
                    Button { selectedTab = "profile" } label: {
                        FMAvatar(name: user.name)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("Open profile")
                )
            )

            VStack(spacing: 16) {
                if pendingCount > 0 {
                    Button { selectedTab = "team" } label: {
                        FMCard {
                            HStack {
                                Image(systemName: "person.badge.clock")
                                    .foregroundStyle(FMTheme.warn)
                                    .frame(width: 40, height: 40)
                                    .background(FMTheme.warnTint)
                                    .clipShape(RoundedRectangle(cornerRadius: 12))
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("\(pendingCount) pending approval\(pendingCount == 1 ? "" : "s")")
                                        .font(.system(size: 14.5, weight: .bold))
                                    Text("Review dealer and shopkeeper onboarding")
                                        .font(.system(size: 12))
                                        .foregroundStyle(FMTheme.ink3)
                                }
                                Spacer()
                                FMBadge(status: "PENDING_APPROVAL")
                            }
                        }
                    }
                    .buttonStyle(.plain)
                }

                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: FMTheme.heroRadius, style: .continuous)
                        .fill(LinearGradient(colors: [FMTheme.inkSurface, FMTheme.inkSurface2], startPoint: .topLeading, endPoint: .bottomTrailing))
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Overview")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(.white.opacity(0.8))
                        Text(FMTheme.inr(revenue))
                            .font(.system(size: 30, weight: .bold, design: .monospaced))
                            .foregroundStyle(.white)
                        Text("\(orderCount) orders · \(userCount) network users")
                            .font(.system(size: 12))
                            .foregroundStyle(.white.opacity(0.7))
                    }
                    .padding(20)
                }

                HStack(spacing: 11) {
                    FMStatTile(label: "Orders", value: "\(orderCount)", icon: "bag", accent: FMTheme.brand, tint: FMTheme.brandTint)
                    FMStatTile(label: "SKUs", value: "\(skuCount)", icon: "cube.box", accent: FMTheme.pos, tint: FMTheme.posTint)
                }

                HStack(spacing: 11) {
                    FMStatTile(label: "Dealers", value: "\(dealerCount)", icon: "shippingbox", accent: FMTheme.warn, tint: FMTheme.warnTint)
                    FMStatTile(label: "Shopkeepers", value: "\(shopkeeperCount)", icon: "storefront", accent: FMTheme.brand, tint: FMTheme.brandTint)
                }

                HStack(spacing: 10) {
                    adminQuickTile(icon: "doc.text.fill", label: "Orders") { selectedTab = "orders" }
                    adminQuickTile(icon: "cube.box", label: "SKUs") { path.append(AppRoute.skuManagement) }
                    adminQuickTile(icon: "person.3.fill", label: "Team") { selectedTab = "team" }
                }

                FMButton(title: "Add employee", variant: .dark, icon: "person.badge.plus") {
                    path.append(AppRoute.createEmployee)
                }
                FMButton(title: "Onboard shopkeeper", variant: .soft, icon: "storefront") {
                    path.append(AppRoute.onboardShopkeeper)
                }
                FMButton(title: "Onboard dealer", variant: .outline, icon: "shippingbox") {
                    path.append(AppRoute.onboardDealer)
                }

                FMSectionLabel(title: "Catalog admin")
                HStack(spacing: 10) {
                    adminActionTile(icon: "cube.box", label: "Manage SKUs") {
                        path.append(AppRoute.skuManagement)
                    }
                    adminActionTile(icon: "tag", label: "Manage brands") {
                        path.append(AppRoute.brandsManagement)
                    }
                }
            }
            .padding(.horizontal, 16)
        }
        .task {
            await env.mainViewModel.loadProducts()
            await env.mainViewModel.loadPendingCount()
        }
    }

    private var pendingCount: Int { env.mainViewModel.pendingCount }
    private var orderCount: Int {
        if case .ok(let list) = env.mainViewModel.orders { return list.count }
        return 0
    }
    private var userCount: Int {
        if case .ok(let list) = env.mainViewModel.users { return list.count }
        return 0
    }
    private var skuCount: Int {
        if case .ok(let list) = env.mainViewModel.products { return list.count }
        return 0
    }
    private var dealerCount: Int { roleCount("DEALER") }
    private var shopkeeperCount: Int { roleCount("SHOPKEEPER") }
    private var revenue: Double {
        guard case .ok(let list) = env.mainViewModel.orders else { return 0 }
        return list.reduce(0) { $0 + ($1.finalAmount?.doubleValue ?? 0) }
    }

    private func roleCount(_ role: String) -> Int {
        guard case .ok(let list) = env.mainViewModel.users else { return 0 }
        return list.filter { $0.role == role }.count
    }

    private func adminQuickTile(icon: String, label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            FMCard(padding: 14) {
                VStack(spacing: 8) {
                    Image(systemName: icon)
                        .font(.system(size: 18))
                        .foregroundStyle(FMTheme.brand)
                        .frame(width: 40, height: 40)
                        .background(FMTheme.brandTint)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    Text(label)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(FMTheme.ink3)
                }
                .frame(maxWidth: .infinity)
            }
        }
        .buttonStyle(.plain)
    }

    private func adminActionTile(icon: String, label: String, action: @escaping () -> Void) -> some View {
        adminQuickTile(icon: icon, label: label, action: action)
    }
}

struct AdminProfileView: View {
    @Environment(AppEnvironment.self) private var env
    let user: SessionUser
    let onLogout: () -> Void

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "Profile")
            VStack(spacing: 14) {
                FMCard {
                    HStack(spacing: 14) {
                        FMAvatar(name: user.name, size: 56)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(user.name).font(.system(size: 18, weight: .bold))
                            Text(user.email)
                                .font(.system(size: 13))
                                .foregroundStyle(FMTheme.ink3)
                            Text("Administrator")
                                .font(.system(size: 12))
                                .foregroundStyle(FMTheme.ink4)
                        }
                        Spacer()
                        FMBadge(status: "ACTIVE")
                    }
                }

                HStack(spacing: 10) {
                    adminProfileStat("Pending", "\(pendingCount)")
                    adminProfileStat("Dealers", "\(dealerCount)")
                    adminProfileStat("Shopkeepers", "\(shopkeeperCount)")
                }

                FMButton(title: "Sign out", variant: .outline, icon: "rectangle.portrait.and.arrow.right", action: onLogout)
            }
            .padding(.horizontal, 16)
        }
        .task {
            await env.mainViewModel.loadUsers()
            await env.mainViewModel.loadPendingCount()
        }
    }

    private var pendingCount: Int { env.mainViewModel.pendingCount }

    private var dealerCount: Int { roleCount("DEALER") }
    private var shopkeeperCount: Int { roleCount("SHOPKEEPER") }

    private func roleCount(_ role: String) -> Int {
        guard case .ok(let list) = env.mainViewModel.users else { return 0 }
        return list.filter { $0.role == role }.count
    }

    private func adminProfileStat(_ label: String, _ value: String) -> some View {
        FMCard(padding: 14) {
            VStack(spacing: 3) {
                Text(value).font(.system(size: 18, weight: .bold, design: .monospaced)).lineLimit(1).minimumScaleFactor(0.7)
                Text(label).font(.system(size: 11.5, weight: .semibold)).foregroundStyle(FMTheme.ink4)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

struct AdminOrdersView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "Orders", subtitle: "All company orders")
            ordersList(env.mainViewModel.orders, path: $path)
        }
        .task { await env.mainViewModel.loadAllOrders() }
    }
}

struct AdminTeamView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    @State private var filter: AdminTeamFilter = .pending
    @State private var confirmAction: AdminUserConfirmAction?
    @State private var successMessage: String?

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "Team", subtitle: "Users & onboarding")

            VStack(spacing: 12) {
                VStack(spacing: 8) {
                    FMButton(title: "Add employee", variant: .dark, icon: "person.badge.plus") {
                        path.append(AppRoute.createEmployee)
                    }
                    FMButton(title: "Onboard shopkeeper", variant: .outline, icon: "storefront") {
                        path.append(AppRoute.onboardShopkeeper)
                    }
                    FMButton(title: "Onboard dealer", variant: .soft, icon: "shippingbox") {
                        path.append(AppRoute.onboardDealer)
                    }
                }
                .padding(.horizontal, 16)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(AdminTeamFilter.allCases, id: \.self) { option in
                            adminFilterChip(option.rawValue, selected: filter == option) {
                                filter = option
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }

                FMSectionLabel(title: "Network · \(filteredUsers.count)")
                    .padding(.horizontal, 16)

                if filteredUsers.isEmpty {
                    Text("No users in this view")
                        .font(.system(size: 13))
                        .foregroundStyle(FMTheme.ink3)
                        .padding(.vertical, 24)
                } else {
                    VStack(spacing: 10) {
                        ForEach(filteredUsers) { user in
                            userCard(user)
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
            .padding(.bottom, 14)
        }
        .task {
            await env.mainViewModel.loadUsers()
            await env.mainViewModel.loadPendingCount()
        }
        .confirmationDialog(
            confirmTitle,
            isPresented: Binding(
                get: { confirmAction != nil },
                set: { if !$0 { confirmAction = nil } }
            ),
            titleVisibility: .visible
        ) {
            Button(confirmPrimaryLabel, role: confirmDestructive ? .destructive : nil) {
                guard let action = confirmAction else { return }
                Task { await performConfirm(action) }
                confirmAction = nil
            }
            Button("Cancel", role: .cancel) { confirmAction = nil }
        } message: {
            Text(confirmMessage)
        }
        .alert("Approval complete", isPresented: Binding(
            get: { successMessage != nil },
            set: { if !$0 { successMessage = nil } }
        )) {
            Button("OK", role: .cancel) { successMessage = nil }
        } message: {
            Text(successMessage ?? "")
        }
    }

    private var filteredUsers: [UserRow] {
        guard case .ok(let list) = env.mainViewModel.users else { return [] }
        switch filter {
        case .pending:
            return list.filter {
                $0.status == "PENDING_APPROVAL" &&
                    ($0.role == "DEALER" || $0.role == "SHOPKEEPER")
            }
        case .employees:
            return list.filter { $0.role == "EMPLOYEE" }
        case .dealers:
            return list.filter { $0.role == "DEALER" }
        case .shopkeepers:
            return list.filter { $0.role == "SHOPKEEPER" }
        case .all:
            return list.filter {
                $0.role == "EMPLOYEE" || $0.role == "DEALER" || $0.role == "SHOPKEEPER"
            }
        }
    }

    private var confirmTitle: String {
        switch confirmAction {
        case .approve: "Approve user?"
        case .reject: "Reject onboarding?"
        case .deactivate: "Deactivate account?"
        case .reactivate: "Reactivate account?"
        case .none: ""
        }
    }

    private var confirmPrimaryLabel: String {
        switch confirmAction {
        case .approve: "Approve"
        case .reject: "Reject"
        case .deactivate: "Deactivate"
        case .reactivate: "Reactivate"
        case .none: "OK"
        }
    }

    private var confirmDestructive: Bool {
        switch confirmAction {
        case .reject, .deactivate: true
        default: false
        }
    }

    private var confirmMessage: String {
        guard let action = confirmAction else { return "" }
        let name: String
        switch action {
        case .approve(let u), .reject(let u), .deactivate(let u), .reactivate(let u):
            name = u.name
        }
        switch action {
        case .approve: return "\(name) will be activated. A login confirmation email will be sent."
        case .reject: return "\(name) will be rejected and cannot sign in."
        case .deactivate: return "\(name) will lose access until reactivated."
        case .reactivate: return "\(name) will be able to sign in again."
        }
    }

    private func performConfirm(_ action: AdminUserConfirmAction) async {
        switch action {
        case .approve(let user):
            if let msg = await env.mainViewModel.approveUser(user.id) {
                successMessage = msg
            }
        case .reject(let user):
            await env.mainViewModel.rejectUser(user.id)
        case .deactivate(let user):
            await env.mainViewModel.deactivateUser(user.id)
        case .reactivate(let user):
            await env.mainViewModel.reactivateUser(user.id)
        }
    }

    private func userCard(_ user: UserRow) -> some View {
        FMCard(padding: 13) {
            HStack(spacing: 13) {
                FMAvatar(
                    name: user.name,
                    tint: user.role == "DEALER" ? FMTheme.pos : (user.role == "EMPLOYEE" ? FMTheme.warn : FMTheme.brand)
                )
                VStack(alignment: .leading, spacing: 2) {
                    Text(user.name).font(.system(size: 14.5, weight: .bold))
                    Text("\(user.email) · \(user.role)")
                        .font(.system(size: 12.5, design: .monospaced))
                        .foregroundStyle(FMTheme.ink3)
                    if let area = user.area?.name {
                        Text(area).font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                    }
                    if let phone = user.phone, !phone.isEmpty {
                        Text(phone).font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                    }
                }
                Spacer()
                FMBadge(status: user.status)
            }
            userActions(user)
        }
    }

    @ViewBuilder
    private func userActions(_ user: UserRow) -> some View {
        if user.status == "PENDING_APPROVAL" && user.role != "EMPLOYEE" {
            HStack(spacing: 10) {
                FMButton(title: "Approve", variant: .pos, fullWidth: true) {
                    confirmAction = .approve(user)
                }
                FMButton(title: "Reject", variant: .outline, fullWidth: true) {
                    confirmAction = .reject(user)
                }
            }
            .padding(.top, 12)
        } else if user.status == "ACTIVE" && (user.role == "DEALER" || user.role == "SHOPKEEPER") {
            FMButton(title: "Deactivate", variant: .outline, fullWidth: true) {
                confirmAction = .deactivate(user)
            }
            .padding(.top, 12)
        } else if user.status == "DEACTIVATED" && (user.role == "DEALER" || user.role == "SHOPKEEPER") {
            FMButton(title: "Reactivate", variant: .soft, fullWidth: true) {
                confirmAction = .reactivate(user)
            }
            .padding(.top, 12)
        }
    }

    private func adminFilterChip(_ label: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(selected ? FMTheme.ink : FMTheme.ink3)
                .padding(.horizontal, 14)
                .padding(.vertical, 9)
                .background(selected ? FMTheme.surface : FMTheme.surface2)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(selected ? FMTheme.brand.opacity(0.35) : FMTheme.line))
        }
        .buttonStyle(.plain)
    }
}

struct CreateEmployeeView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    @State private var name = ""
    @State private var email = ""
    @State private var phone = ""
    @State private var password = ""
    @State private var saving = false
    @State private var successMessage: String?
    @State private var showSuccessAlert = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Add employee", subtitle: "Field onboarding staff", onBack: { path.removeLast() })
                FMCard {
                    Text("Employees onboard dealers and shopkeepers. Login credentials are emailed when SMTP is configured.")
                        .font(.system(size: 13))
                        .foregroundStyle(FMTheme.ink3)
                }
                FMTextField(label: "Full name", text: $name, icon: "person", placeholder: "Field Employee")
                FMTextField(label: "Work email", text: $email, icon: "envelope", placeholder: "employee@company.com", keyboard: .emailAddress)
                FMTextField(label: "Phone (optional)", text: $phone, icon: "phone", placeholder: "98765 43210", keyboard: .phonePad)
                if !AppConfig.useLocalDemoAuth {
                    FMTextField(label: "Password (optional)", text: $password, icon: "lock", placeholder: "Auto-generated if blank", secure: true)
                }
                if let err = env.mainViewModel.placeOrderError {
                    Text(err).foregroundStyle(FMTheme.neg).font(.system(size: 13))
                }
                FMButton(title: saving ? "Creating…" : "Create employee", variant: .dark, icon: "checkmark", enabled: !saving) {
                    Task {
                        saving = true
                        if let msg = await env.mainViewModel.createEmployee(
                            name: name,
                            email: email,
                            phone: FieldValidators.digitsOnly(phone),
                            password: password.isEmpty ? nil : password
                        ) {
                            successMessage = msg
                            showSuccessAlert = true
                        }
                        saving = false
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 30)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .alert("Employee created", isPresented: $showSuccessAlert) {
            Button("OK") { path.removeLast() }
        } message: {
            Text(successMessage ?? "")
        }
    }
}
