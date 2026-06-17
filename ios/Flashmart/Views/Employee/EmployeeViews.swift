import SwiftUI

private func employeeOnboarded(from users: LoadState<[UserRow]>, employeeId: String) -> [UserRow] {
    guard case .ok(let list) = users else { return [] }
    return list.filter { row in
        row.onboardedById == employeeId && (row.role == "DEALER" || row.role == "SHOPKEEPER")
    }
}

struct EmployeeTabHost: View {
    @Environment(AppEnvironment.self) private var env
    let selectedTab: String
    @Binding var path: NavigationPath
    let user: SessionUser
    let onLogout: () -> Void

    var body: some View {
        switch selectedTab {
        case "network": EmployeeNetworkView(user: user)
        case "profile": EmployeeProfileView(path: $path, user: user, onLogout: onLogout)
        default: EmployeeHomeView(path: $path, user: user)
        }
    }
}

struct EmployeeHomeView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let user: SessionUser

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(
                title: "\(user.name.split(separator: " ").first.map(String.init) ?? user.name)'s desk",
                kicker: "Field executive",
                accent: FMTheme.warn,
                trailing: AnyView(
                    HStack(spacing: 8) {
                        FMGlyphButton(systemName: "bell")
                        FMAvatar(name: user.name, tint: FMTheme.warn)
                    }
                )
            )

            VStack(spacing: 16) {
                onboardingHero
                onboardAction(
                    icon: "shippingbox.fill",
                    title: "Add a dealer",
                    subtitle: "Onboard a distributor to an area",
                    tint: FMTheme.posTint,
                    accent: FMTheme.pos
                ) { path.append(AppRoute.onboardDealer) }

                onboardAction(
                    icon: "bag.fill",
                    title: "Add a shopkeeper",
                    subtitle: "Register a retail store",
                    tint: FMTheme.brandTint,
                    accent: FMTheme.brand
                ) { path.append(AppRoute.onboardShopkeeper) }
            }
            .padding(.horizontal, 16)
        }
        .task { await env.mainViewModel.loadUsers() }
    }

    private var myOnboarded: [UserRow] {
        employeeOnboarded(from: env.mainViewModel.users, employeeId: user.id)
    }

    private var onboardingHero: some View {
        let dealers = dealerCount
        let shops = shopkeeperCount
        let progress = min(100, Int(Double(shops) / 120 * 100))
        return ZStack(alignment: .leading) {
            RoundedRectangle(cornerRadius: FMTheme.heroRadius, style: .continuous)
                .fill(FMTheme.employeeHeroGradient)
                .shadow(color: FMTheme.warn.opacity(0.25), radius: 12, y: 6)
            Circle().fill(.white.opacity(0.08)).frame(width: 150).offset(x: 230, y: -30)

            VStack(alignment: .leading, spacing: 14) {
                Text("This month's onboarding")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.85))
                HStack(spacing: 26) {
                    heroStat("\(dealers)", "Dealers")
                    Rectangle().fill(.white.opacity(0.2)).frame(width: 1, height: 40)
                    heroStat("\(shops)", "Shopkeepers")
                }
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule().fill(.white.opacity(0.2))
                        Capsule().fill(.white).frame(width: geo.size.width * CGFloat(progress) / 100)
                    }
                }
                .frame(height: 7)
                Text("\(progress)% of 120 monthly target")
                    .font(.system(size: 11.5))
                    .foregroundStyle(.white.opacity(0.85))
            }
            .padding(20)
            .foregroundStyle(.white)
        }
        .frame(height: 180)
    }

    private func heroStat(_ value: String, _ label: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(value).font(.system(size: 30, weight: .bold, design: .monospaced))
            Text(label).font(.system(size: 12)).opacity(0.8)
        }
    }

    private func onboardAction(icon: String, title: String, subtitle: String, tint: Color, accent: Color, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            FMCard {
                HStack(spacing: 14) {
                    Image(systemName: icon)
                        .font(.system(size: 22))
                        .foregroundStyle(accent)
                        .frame(width: 46, height: 46)
                        .background(tint)
                        .clipShape(RoundedRectangle(cornerRadius: 13))
                    VStack(alignment: .leading, spacing: 3) {
                        Text(title).font(.system(size: 15.5, weight: .bold))
                        Text(subtitle).font(.system(size: 12.5)).foregroundStyle(FMTheme.ink3)
                    }
                    Spacer()
                    Image(systemName: "plus")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(width: 38, height: 38)
                        .background(FMTheme.ink)
                        .clipShape(RoundedRectangle(cornerRadius: 11))
                }
            }
        }
        .buttonStyle(.plain)
    }

    private var dealerCount: Int {
        myOnboarded.filter { $0.role == "DEALER" }.count
    }

    private var shopkeeperCount: Int {
        myOnboarded.filter { $0.role == "SHOPKEEPER" }.count
    }
}

struct EmployeeNetworkView: View {
    @Environment(AppEnvironment.self) private var env
    let user: SessionUser
    @State private var segment = "Dealers"

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "My network", subtitle: networkSubtitle)
            FMSegmented(options: ["Dealers", "Shopkeepers"], selection: $segment)
                .padding(.horizontal, 16)
                .padding(.bottom, 14)

            VStack(spacing: 10) {
                ForEach(filteredNetwork) { user in
                    networkCard(user)
                }
            }
            .padding(.horizontal, 16)
        }
        .task { await env.mainViewModel.loadUsers() }
    }

    private var myOnboarded: [UserRow] {
        employeeOnboarded(from: env.mainViewModel.users, employeeId: user.id)
    }

    private var networkSubtitle: String {
        guard case .ok = env.mainViewModel.users else { return "Loading…" }
        let dealers = myOnboarded.filter { $0.role == "DEALER" }.count
        let shops = myOnboarded.filter { $0.role == "SHOPKEEPER" }.count
        return "\(dealers) dealers · \(shops) shopkeepers"
    }

    private var filteredNetwork: [UserRow] {
        myOnboarded.filter { segment == "Dealers" ? $0.role == "DEALER" : $0.role == "SHOPKEEPER" }
    }

    private func networkCard(_ user: UserRow) -> some View {
        FMCard(padding: 13) {
            HStack(spacing: 13) {
                FMAvatar(name: user.name, tint: user.role == "DEALER" ? FMTheme.pos : FMTheme.brand)
                VStack(alignment: .leading, spacing: 2) {
                    Text(user.name).font(.system(size: 14.5, weight: .bold)).lineLimit(1)
                    Text("\(user.email) · \(user.area?.name ?? "—")")
                        .font(.system(size: 12.5, design: .monospaced))
                        .foregroundStyle(FMTheme.ink3)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 5) {
                    FMBadge(status: user.status)
                }
            }
        }
    }
}

struct EmployeeProfileView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let user: SessionUser
    let onLogout: () -> Void

    var body: some View {
        FMScreen(showNav: true) {
            FMTopBar(title: "Profile")
            profileContent(user: user, path: $path, onLogout: onLogout, stats: [
                ("Dealers", "\(count(role: "DEALER"))"),
                ("Shops", "\(count(role: "SHOPKEEPER"))"),
                ("Active", "\(count(status: "ACTIVE"))"),
            ])
        }
        .task { await env.mainViewModel.loadUsers() }
    }

    private var myOnboarded: [UserRow] {
        employeeOnboarded(from: env.mainViewModel.users, employeeId: user.id)
    }

    private func count(role: String? = nil, status: String? = nil) -> Int {
        myOnboarded.filter { u in
            (role == nil || u.role == role) && (status == nil || u.status == status)
        }.count
    }
}

struct OnboardShopkeeperView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    @State private var storeName = ""
    @State private var ownerName = ""
    @State private var phone = ""
    @State private var email = ""
    @State private var password = ""
    @State private var notes = ""
    @State private var selectedAreaId = ""
    @State private var saving = false
    @State private var attachedDocuments: [String: PendingOnboardingDocument] = [:]

    private var canSave: Bool {
        guard !saving else { return false }
        guard !password.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return false }
        if case .ok(let list) = env.mainViewModel.areas {
            return !list.isEmpty && !selectedAreaId.isEmpty
        }
        return false
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Add shopkeeper", subtitle: "Field onboarding", onBack: { path.removeLast() })
                FMCard {
                    Text("Set a sign-in password below. After admin approval, login details will be emailed to the shopkeeper.")
                        .font(.system(size: 12))
                        .foregroundStyle(FMTheme.ink3)
                }
                FMTextField(label: "Store name", text: $storeName, icon: "storefront", placeholder: "Sharma General Store")
                FMTextField(label: "Owner name", text: $ownerName, icon: "person", placeholder: "Ravi Sharma")
                FMTextField(label: "Phone number", text: $phone, icon: "phone", placeholder: "98213 44567", keyboard: .phonePad)
                FMTextField(label: "Email", text: $email, icon: "envelope", placeholder: "shop@example.com", keyboard: .emailAddress)
                FMTextField(label: "Sign-in password", text: $password, icon: "lock", placeholder: "Min 8 characters", secure: true)
                FMTextField(label: "Notes (optional)", text: $notes, icon: "note.text", placeholder: "Onboarding notes")
                OnboardingDocumentsSection(
                    slots: OnboardingDocumentStorage.shopkeeperSlots(),
                    attached: $attachedDocuments
                )
                OnboardingAreaSection(
                    areasState: env.mainViewModel.areas,
                    selectedAreaId: $selectedAreaId
                )
                if let err = env.mainViewModel.placeOrderError {
                    Text(err).foregroundStyle(FMTheme.neg).font(.system(size: 13))
                }
                FMButton(title: saving ? "Saving…" : "Save shopkeeper", variant: .dark, icon: "checkmark", enabled: canSave) {
                    Task {
                        saving = true
                        let ok = await env.mainViewModel.onboardShopkeeper(
                            name: ownerName.isEmpty ? storeName : ownerName,
                            email: email,
                            phone: phone,
                            password: password,
                            areaId: selectedAreaId,
                            notes: notes,
                            documents: Array(attachedDocuments.values)
                        )
                        saving = false
                        if ok { path.removeLast() }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 30)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task {
            env.mainViewModel.placeOrderError = nil
            await env.mainViewModel.loadAreas()
            if case .ok(let list) = env.mainViewModel.areas,
               selectedAreaId.isEmpty,
               let first = list.first {
                selectedAreaId = first.id
            }
        }
    }
}

struct OnboardDealerView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    @State private var name = ""
    @State private var email = ""
    @State private var phone = ""
    @State private var password = ""
    @State private var notes = ""
    @State private var selectedAreaId = ""
    @State private var saving = false
    @State private var attachedDocuments: [String: PendingOnboardingDocument] = [:]

    private var canSave: Bool {
        guard !saving else { return false }
        guard !password.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return false }
        if case .ok(let list) = env.mainViewModel.areas {
            return !list.isEmpty && !selectedAreaId.isEmpty
        }
        return false
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Add dealer", subtitle: "Field onboarding", onBack: { path.removeLast() })
                FMCard {
                    Text("Set a sign-in password below. After admin approval, login details will be emailed to the dealer.")
                        .font(.system(size: 12))
                        .foregroundStyle(FMTheme.ink3)
                }
                FMTextField(label: "Distributor name", text: $name, icon: "shippingbox", placeholder: "Shree Balaji Distributors")
                FMTextField(label: "Email", text: $email, icon: "envelope", placeholder: "dealer@example.com", keyboard: .emailAddress)
                FMTextField(label: "Phone number", text: $phone, icon: "phone", placeholder: "98100 11223", keyboard: .phonePad)
                FMTextField(label: "Sign-in password", text: $password, icon: "lock", placeholder: "Min 8 characters", secure: true)
                FMTextField(label: "Notes (optional)", text: $notes, icon: "note.text", placeholder: "Area assignment notes")
                OnboardingDocumentsSection(
                    slots: OnboardingDocumentStorage.dealerSlots(),
                    attached: $attachedDocuments
                )
                OnboardingAreaSection(
                    areasState: env.mainViewModel.areas,
                    selectedAreaId: $selectedAreaId
                )
                if let err = env.mainViewModel.placeOrderError {
                    Text(err).foregroundStyle(FMTheme.neg).font(.system(size: 13))
                }
                FMButton(title: saving ? "Saving…" : "Save dealer", variant: .dark, icon: "checkmark", enabled: canSave) {
                    Task {
                        saving = true
                        let ok = await env.mainViewModel.onboardDealer(
                            name: name,
                            email: email,
                            phone: phone,
                            password: password,
                            areaId: selectedAreaId,
                            notes: notes,
                            documents: Array(attachedDocuments.values)
                        )
                        saving = false
                        if ok { path.removeLast() }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 30)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task {
            env.mainViewModel.placeOrderError = nil
            await env.mainViewModel.loadAreas()
            if case .ok(let list) = env.mainViewModel.areas,
               selectedAreaId.isEmpty,
               let first = list.first {
                selectedAreaId = first.id
            }
        }
    }
}

private struct OnboardingAreaSection: View {
    let areasState: LoadState<[Area]>
    @Binding var selectedAreaId: String

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            FMSectionLabel(title: "Assigned area")
            switch areasState {
            case .idle, .loading:
                Text("Loading areas…")
                    .font(.system(size: 13))
                    .foregroundStyle(FMTheme.ink3)
            case .err(let message):
                Text(message)
                    .font(.system(size: 13))
                    .foregroundStyle(FMTheme.neg)
            case .ok(let areas):
                if areas.isEmpty {
                    Text("No areas available. Ask admin to create territories first.")
                        .font(.system(size: 13))
                        .foregroundStyle(FMTheme.neg)
                } else {
                    ForEach(areas) { area in
                        areaRow(area)
                    }
                }
            }
        }
    }

    private func areaRow(_ area: Area) -> some View {
        let selected = selectedAreaId == area.id
        return Button {
            selectedAreaId = area.id
        } label: {
            FMCard(padding: 12) {
                HStack(spacing: 12) {
                    Image(systemName: selected ? "largecircle.fill.circle" : "circle")
                        .font(.system(size: 18))
                        .foregroundStyle(selected ? FMTheme.brand : FMTheme.ink3)
                    VStack(alignment: .leading, spacing: 4) {
                        Text(area.name)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(FMTheme.ink)
                        if let dealerName = area.dealer?.name {
                            Text("Dealer · \(dealerName)")
                                .font(.system(size: 12))
                                .foregroundStyle(FMTheme.ink3)
                        }
                    }
                    Spacer(minLength: 0)
                }
            }
        }
        .buttonStyle(.plain)
    }
}
