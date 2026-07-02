import SwiftUI

struct LoginView: View {
    @Environment(AppEnvironment.self) private var env
    let onSignedIn: () -> Void
    var onRegister: (() -> Void)? = nil
    @State private var showReset = false

    var body: some View {
        @Bindable var auth = env.authViewModel
        ZStack {
            LinearGradient(colors: [FMTheme.inkSurface, FMTheme.inkSurface2], startPoint: .topLeading, endPoint: .bottomTrailing)
                .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    HStack(spacing: 12) {
                        RoundedRectangle(cornerRadius: 13, style: .continuous)
                            .fill(FMTheme.brand)
                            .frame(width: 42, height: 42)
                            .overlay(Text("⚡").font(.system(size: 20)))
                        VStack(alignment: .leading, spacing: 2) {
                            Text("FlashMart")
                                .font(.system(size: 22, weight: .bold))
                                .foregroundStyle(.white)
                            Text("Distribution OS")
                                .font(.system(size: 12, weight: .medium))
                                .foregroundStyle(.white.opacity(0.5))
                        }
                    }
                    .padding(.bottom, 36)

                    Text("Welcome back")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundStyle(.white)
                    Text(AppConfig.useLocalDemoAuth ? "Sign in with your demo account" : "Sign in with your mobile or email")
                        .font(.system(size: 14))
                        .foregroundStyle(.white.opacity(0.55))
                        .padding(.top, 6)
                        .padding(.bottom, 28)

                    VStack(alignment: .leading, spacing: 14) {
                        if AppConfig.useLocalDemoAuth {
                            Text("Developer demo · password: \(LocalDemoAuth.demoPassword)")
                                .font(.system(size: 12))
                                .foregroundStyle(.white.opacity(0.55))
                                .lineSpacing(3)
                            demoAccountsCard
                        }

                        FMTextField(
                            label: "Email or phone",
                            text: $auth.identifier,
                            icon: "envelope",
                            placeholder: AppConfig.useLocalDemoAuth ? "shop1@martdemo.com" : "you@company.com",
                            keyboard: .emailAddress
                        )
                        FMTextField(
                            label: "Password",
                            text: $auth.password,
                            icon: "lock",
                            placeholder: "Password",
                            secure: true
                        )

                        if let err = auth.error {
                            Text(err)
                                .font(.system(size: 13))
                                .foregroundStyle(FMTheme.neg)
                        }

                        FMButton(title: auth.loading ? "Signing in…" : "Sign in", enabled: !auth.loading) {
                            Task {
                                if await auth.login() {
                                    env.cartStore.clear()
                                    onSignedIn()
                                }
                            }
                        }

                        Button("Forgot password?") { showReset = true }
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(FMTheme.brand.opacity(0.9))
                            .frame(maxWidth: .infinity)

                        if !AppConfig.useLocalDemoAuth, let onRegister {
                            Button("New user? Create account") { onRegister() }
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundStyle(.white.opacity(0.75))
                                .frame(maxWidth: .infinity)
                                .padding(.top, 4)
                        }
                    }
                    .padding(22)
                    .background(Color.white.opacity(0.06))
                    .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 20).stroke(.white.opacity(0.1)))
                }
                .padding(.horizontal, 28)
                .padding(.vertical, 48)
            }
        }
        .sheet(isPresented: $showReset) {
            ResetPasswordView()
        }
    }

    @ViewBuilder
    private var demoAccountsCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Demo accounts")
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(.white.opacity(0.7))
            ForEach(LocalDemoAuth.demoAccounts, id: \.email) { acct in
                Button {
                    env.authViewModel.identifier = acct.email
                    env.authViewModel.password = LocalDemoAuth.demoPassword
                } label: {
                    HStack {
                        Text(acct.role)
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundStyle(FMTheme.brandTint)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(FMTheme.brand.opacity(0.35))
                            .clipShape(Capsule())
                        Text(acct.email)
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundStyle(.white.opacity(0.85))
                        Spacer()
                        Image(systemName: "arrow.up.left")
                            .font(.system(size: 11))
                            .foregroundStyle(.white.opacity(0.4))
                    }
                }
            }
        }
        .padding(12)
        .background(Color.white.opacity(0.04))
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

// MARK: - Self registration

struct SelfRegistrationView: View {
    @Environment(AppEnvironment.self) private var env
    let onBack: () -> Void
    let onRegistered: () -> Void

    var body: some View {
        @Bindable var reg = env.registerViewModel
        ScrollView {
            VStack(alignment: .leading, spacing: FMSpacing.itemGap) {
                FMTopBar(
                    title: "Create account",
                    subtitle: subtitle(for: reg.ui),
                    onBack: handleBack(reg: reg)
                )

                if let err = reg.ui.error {
                    FMErrorBanner(text: err)
                }

                if reg.ui.step == 0 {
                    roleOption("Shopkeeper", selected: reg.ui.role == "SHOPKEEPER") { reg.setRole("SHOPKEEPER") }
                    roleOption("Dealer", selected: reg.ui.role == "DEALER") { reg.setRole("DEALER") }
                    FMButton(title: "Continue") { reg.ui.step = 1 }
                } else if !reg.ui.authComplete {
                    authMethodStep
                } else {
                    profileStep
                }
            }
            .padding(.horizontal, FMSpacing.screenH)
            .padding(.bottom, 30)
        }
        .background(FMTheme.bg)
        .task { await reg.loadGeo() }
    }

    private func subtitle(for ui: RegisterUiState) -> String {
        if ui.step == 0 { return "Choose account type" }
        if !ui.authComplete { return ui.authMethod == .mobile ? "Verify mobile" : "Sign up with email" }
        return "Complete profile"
    }

    private func handleBack(reg: RegisterViewModel) -> (() -> Void)? {
        if reg.ui.step == 0 { return onBack }
        if !reg.ui.authComplete {
            if reg.ui.authMethod == .mobile && reg.ui.otpSent {
                return {
                    reg.ui.otpSent = false
                    reg.ui.otp = ""
                }
            }
            return { reg.ui.step = 0 }
        }
        return onBack
    }

    @ViewBuilder
    private var authMethodStep: some View {
        @Bindable var reg = env.registerViewModel
        Picker("Sign up with", selection: Binding(
            get: { reg.ui.authMethod },
            set: { reg.setAuthMethod($0) }
        )) {
            Text("Mobile").tag(RegisterAuthMethod.mobile)
            Text("Email").tag(RegisterAuthMethod.email)
        }
        .pickerStyle(.segmented)

        switch reg.ui.authMethod {
        case .mobile:
            FMTextField(label: "Mobile number", text: $reg.ui.phone, icon: "phone", placeholder: "10-digit mobile", keyboard: .phonePad)
            if reg.ui.otpSent {
                if let devOtp = reg.ui.devOtp {
                    FMInfoBanner(text: "Dev OTP: \(devOtp)")
                }
                FMTextField(label: "6-digit OTP", text: $reg.ui.otp, icon: "number", placeholder: "123456", keyboard: .numberPad)
                FMButton(title: reg.ui.loading ? "Verifying…" : "Verify & continue", enabled: !reg.ui.loading && reg.ui.otp.count == 6) {
                    Task { _ = await reg.verifyOtp() }
                }
            } else {
                let phoneValid = reg.ui.phone.filter(\.isNumber).count == 10
                FMButton(title: reg.ui.loading ? "Sending…" : "Send OTP", enabled: !reg.ui.loading && phoneValid) {
                    Task { await reg.sendOtp() }
                }
            }
        case .email:
            FMTextField(label: "Email address", text: $reg.ui.email, icon: "envelope", placeholder: "you@shop.com", keyboard: .emailAddress)
            if reg.ui.email.contains("@") {
                FMTextField(label: "Password", text: $reg.ui.password, icon: "lock", placeholder: "Min 8 characters", secure: true)
                FMButton(title: "Continue", enabled: reg.ui.password.count >= 8) {
                    reg.continueWithEmailAuth()
                }
            }
        }
    }

    @ViewBuilder
    private var profileStep: some View {
        @Bindable var reg = env.registerViewModel
        FMTextField(label: "Full name", text: $reg.ui.name, icon: "person", placeholder: "Your name")
        FMTextField(
            label: reg.ui.role == "DEALER" ? "Business name" : "Shop name",
            text: $reg.ui.shopName,
            icon: "storefront",
            placeholder: "Store / business name"
        )
        if reg.ui.authMethod == .mobile {
            profileReadOnlyField(label: "Verified mobile", value: reg.ui.phone, icon: "phone")
            FMTextField(label: "Email (optional)", text: $reg.ui.email, icon: "envelope", placeholder: "you@shop.com", keyboard: .emailAddress)
        } else {
            profileReadOnlyField(label: "Email", value: reg.ui.email, icon: "envelope")
        }
        FMTextField(label: "Referral code (optional)", text: $reg.ui.referralCode, icon: "gift", placeholder: "REF123")
        geoSelectors
        FMTextField(label: "Address", text: $reg.ui.address, icon: "mappin", placeholder: "Street, locality")
        FMButton(title: reg.ui.loading ? "Creating account…" : "Create account", enabled: !reg.ui.loading && !reg.ui.name.isEmpty && !reg.ui.shopName.isEmpty && !reg.ui.areaId.isEmpty && !reg.ui.address.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty) {
            Task {
                if await reg.submitRegistration() {
                    env.cartStore.clear()
                    onRegistered()
                }
            }
        }
    }

    private func roleOption(_ title: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            FMCard {
                HStack {
                    Image(systemName: selected ? "largecircle.fill.circle" : "circle")
                        .foregroundStyle(selected ? FMTheme.brand : FMTheme.ink4)
                    Text(title).font(.system(size: 15, weight: .semibold))
                    Spacer()
                }
            }
        }
        .buttonStyle(.plain)
    }

    private func profileReadOnlyField(label: String, value: String, icon: String) -> some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(FMTheme.ink2)
            HStack(spacing: 10) {
                Image(systemName: icon)
                    .foregroundStyle(FMTheme.ink3)
                Text(value)
                    .font(.system(size: 15))
                    .foregroundStyle(FMTheme.ink)
                Spacer()
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 14)
            .background(FMTheme.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        }
    }

    @ViewBuilder
    private var geoSelectors: some View {
        @Bindable var reg = env.registerViewModel
        if !reg.ui.geo.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                Text("State").font(.system(size: 13, weight: .semibold)).foregroundStyle(FMTheme.ink2)
                Picker("State", selection: Binding(
                    get: { reg.ui.state },
                    set: { newState in
                        reg.ui.state = newState
                        reg.ui.district = reg.ui.geo.first { $0.name == newState }?.districts.first ?? ""
                        Task { await reg.loadAreas() }
                    }
                )) {
                    ForEach(reg.ui.geo, id: \.name) { st in
                        Text(st.name).tag(st.name)
                    }
                }
                .pickerStyle(.menu)
            }
            let districts = reg.ui.geo.first { $0.name == reg.ui.state }?.districts ?? []
            if !districts.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    Text("District").font(.system(size: 13, weight: .semibold)).foregroundStyle(FMTheme.ink2)
                    Picker("District", selection: Binding(
                        get: { reg.ui.district },
                        set: { newDistrict in
                            reg.ui.district = newDistrict
                            Task { await reg.loadAreas() }
                        }
                    )) {
                        ForEach(districts, id: \.self) { d in Text(d).tag(d) }
                    }
                    .pickerStyle(.menu)
                }
            }
        }
        if !reg.ui.areas.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                Text("Service area").font(.system(size: 13, weight: .semibold)).foregroundStyle(FMTheme.ink2)
                Picker("Area", selection: Binding(
                    get: { reg.ui.areaId },
                    set: { reg.ui.areaId = $0 }
                )) {
                    ForEach(reg.ui.areas) { area in
                        Text(area.name).tag(area.id)
                    }
                }
                .pickerStyle(.menu)
            }
        } else {
            FMInfoBanner(text: "No service areas found for this district. Pick another district or contact support.")
        }
    }
}
