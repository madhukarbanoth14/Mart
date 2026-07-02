import Foundation
import Observation

@Observable
final class AuthViewModel {
    var identifier = ""
    var password = ""
    var loading = false
    var error: String?
    var resetToken: String?

    private let sessionStore: SessionStore
    private let apiClient: MartAPIClient
    private let localDemoStore: LocalDemoStore

    init(sessionStore: SessionStore, apiClient: MartAPIClient, localDemoStore: LocalDemoStore) {
        self.sessionStore = sessionStore
        self.apiClient = apiClient
        self.localDemoStore = localDemoStore
    }

    @MainActor
    func login() async -> Bool {
        guard !identifier.trimmingCharacters(in: .whitespaces).isEmpty,
              !password.isEmpty else {
            error = "Enter email and password"
            return false
        }
        loading = true
        error = nil
        defer { loading = false }

        let id = identifier.trimmingCharacters(in: .whitespaces)

        if AppConfig.useLocalDemoAuth {
            return await loginLocalDemo(identifier: id)
        }

        sessionStore.clear()

        do {
            let res = try await apiClient.login(identifier: id, password: password)
            let user = SessionUser(
                id: res.user.id, name: res.user.name, email: res.user.email,
                role: res.user.role, companyId: res.user.companyId
            )
            sessionStore.saveSession(token: res.accessToken, user: user)
            return true
        } catch let loginError {
            if AppConfig.useLocalDemoAuth,
               let demo = LocalDemoAuth.resolveDemoUser(identifier: id, password: password)
                ?? localDemoStore.tryResolveOnboardedSession(email: id, password: password) {
                localDemoStore.resetForNewSession()
                sessionStore.saveLocalDemoSession(user: demo)
                return true
            }
            error = (loginError as? LocalizedError)?.errorDescription ?? loginError.localizedDescription
            return false
        }
    }

    @MainActor
    private func loginLocalDemo(identifier: String) async -> Bool {
        if let demo = LocalDemoAuth.resolveDemoUser(identifier: identifier, password: password)
            ?? localDemoStore.tryResolveOnboardedSession(email: identifier, password: password) {
            localDemoStore.resetForNewSession()
            sessionStore.saveLocalDemoSession(user: demo)
            return true
        }
        error = localDemoStore.loginStatusMessage(email: identifier)
            ?? "Invalid email or password. Demo password is \(LocalDemoAuth.demoPassword)."
        return false
    }

    @MainActor
    func requestPasswordReset(email: String) async -> ForgotPasswordResponse? {
        guard !email.isEmpty else {
            error = "Enter email to request reset link"
            return nil
        }
        loading = true
        error = nil
        defer { loading = false }
        do {
            let resp = try await apiClient.forgotPassword(email: email.trimmingCharacters(in: .whitespaces))
            resetToken = resp.resetPasswordToken
            return resp
        } catch {
            self.error = (error as? LocalizedError)?.errorDescription ?? error.localizedDescription
            return nil
        }
    }

    @MainActor
    func resetPassword(token: String, newPassword: String) async -> Bool {
        guard !token.isEmpty, newPassword.count >= 8 else {
            error = "Token and password (min 8 chars) are required"
            return false
        }
        loading = true
        error = nil
        defer { loading = false }
        do {
            _ = try await apiClient.resetPassword(token: token, newPassword: newPassword)
            password = ""
            return true
        } catch {
            self.error = (error as? LocalizedError)?.errorDescription ?? error.localizedDescription
            return false
        }
    }

    func logout() {
        sessionStore.clear()
    }
}

// MARK: - Self-registration

enum RegisterAuthMethod: String, Equatable {
    case mobile
    case email
}

struct RegisterUiState: Equatable {
    var step = 0
    var authMethod: RegisterAuthMethod = .mobile
    var role = "SHOPKEEPER"
    var phone = ""
    var otp = ""
    var verificationToken: String?
    var name = ""
    var email = ""
    var password = ""
    var shopName = ""
    var state = ""
    var district = ""
    var areaId = ""
    var address = ""
    var latitude: Double?
    var longitude: Double?
    var referralCode = ""
    var geo: [RegistrationState] = []
    var areas: [RegistrationArea] = []
    var loading = false
    var error: String?
    var otpSent = false
    var devOtp: String?

    var authComplete: Bool {
        switch authMethod {
        case .mobile:
            return verificationToken?.isEmpty == false
        case .email:
            return step >= 2 && email.contains("@") && password.count >= 8
        }
    }
}

@Observable
final class RegisterViewModel {
    var ui = RegisterUiState()

    private let apiClient: MartAPIClient
    private let sessionStore: SessionStore

    init(apiClient: MartAPIClient, sessionStore: SessionStore) {
        self.apiClient = apiClient
        self.sessionStore = sessionStore
    }

    func setRole(_ role: String) {
        ui.role = role
    }

    func setAuthMethod(_ method: RegisterAuthMethod) {
        ui.authMethod = method
        ui.otp = ""
        ui.otpSent = false
        ui.devOtp = nil
        ui.verificationToken = nil
        ui.error = nil
        if method == .email {
            ui.phone = ""
        } else {
            ui.email = ""
            ui.password = ""
        }
    }

    @MainActor
    func continueWithEmailAuth() {
        let email = ui.email.trimmingCharacters(in: .whitespacesAndNewlines)
        if !email.contains("@") {
            ui.error = "Enter a valid email address"
            return
        }
        if ui.password.count < 8 {
            ui.error = "Password must be at least 8 characters"
            return
        }
        ui.step = 2
        ui.error = nil
    }

    @MainActor
    func loadGeo() async {
        do {
            let geo = try await apiClient.registrationGeo()
            ui.geo = geo.states
            ui.state = geo.states.first?.name ?? ui.state
            ui.district = geo.states.first?.districts.first ?? ui.district
            await loadAreas()
        } catch {
            ui.error = error.localizedDescription
        }
    }

    @MainActor
    func loadAreas() async {
        do {
            let areas = try await apiClient.registrationAreas(state: ui.state.nilIfBlank, district: ui.district.nilIfBlank)
            ui.areas = areas
            ui.areaId = areas.first?.id ?? ""
        } catch {
            ui.error = error.localizedDescription
        }
    }

    @MainActor
    func sendOtp() async {
        let phone = ui.phone.trimmingCharacters(in: .whitespacesAndNewlines)
        guard phone.count >= 10 else {
            ui.error = "Enter a valid mobile number"
            return
        }
        ui.loading = true
        ui.error = nil
        defer { ui.loading = false }
        do {
            let res = try await apiClient.sendOtp(phone: phone)
            ui.otpSent = true
            ui.devOtp = res.devOtp
        } catch {
            ui.error = error.localizedDescription
        }
    }

    @MainActor
    func verifyOtp() async -> Bool {
        ui.loading = true
        ui.error = nil
        defer { ui.loading = false }
        do {
            let res = try await apiClient.verifyOtp(phone: ui.phone.trimmingCharacters(in: .whitespacesAndNewlines), code: ui.otp.trimmingCharacters(in: .whitespacesAndNewlines))
            ui.verificationToken = res.verificationToken
            ui.step = 2
            return true
        } catch {
            ui.error = error.localizedDescription
            return false
        }
    }

    @MainActor
    func submitRegistration() async -> Bool {
        let isMobile = ui.authMethod == .mobile
        if isMobile {
            guard let token = ui.verificationToken, !token.isEmpty else {
                ui.error = "Verify mobile OTP first"
                return false
            }
        } else if !ui.email.contains("@") || ui.password.count < 8 {
            ui.error = "Enter email and password (min 8 characters)"
            return false
        }
        if let validationError = validateRegistrationFields() {
            ui.error = validationError
            return false
        }
        ui.loading = true
        ui.error = nil
        defer { ui.loading = false }
        let body = SelfRegisterRequest(
            verificationToken: isMobile ? ui.verificationToken : nil,
            phone: isMobile ? ui.phone.trimmingCharacters(in: .whitespacesAndNewlines) : nil,
            name: ui.name.trimmingCharacters(in: .whitespacesAndNewlines),
            email: isMobile ? ui.email.trimmingCharacters(in: .whitespacesAndNewlines).nilIfBlank : ui.email.trimmingCharacters(in: .whitespacesAndNewlines),
            password: isMobile ? (ui.password.isEmpty ? nil : ui.password) : ui.password,
            areaId: ui.areaId,
            state: ui.state.trimmingCharacters(in: .whitespacesAndNewlines),
            district: ui.district.trimmingCharacters(in: .whitespacesAndNewlines),
            address: ui.address.trimmingCharacters(in: .whitespacesAndNewlines),
            shopName: ui.shopName.trimmingCharacters(in: .whitespacesAndNewlines),
            latitude: ui.latitude,
            longitude: ui.longitude,
            referralCode: ui.referralCode.trimmingCharacters(in: .whitespacesAndNewlines).nilIfBlank
        )
        do {
            let res = ui.role.uppercased() == "DEALER"
                ? try await apiClient.registerDealer(body)
                : try await apiClient.registerShopkeeper(body)
            let user = SessionUser(
                id: res.user.id,
                name: res.user.name,
                email: res.user.email,
                role: res.user.role,
                companyId: res.user.companyId,
                canPlaceOrders: false,
                documentStatus: "NOT_UPLOADED"
            )
            sessionStore.saveSession(token: res.accessToken, user: user)
            return true
        } catch {
            ui.error = error.localizedDescription
            return false
        }
    }

    private func validateRegistrationFields() -> String? {
        if ui.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
            ui.shopName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return "Complete business details"
        }
        if ui.areaId.isEmpty {
            return "Select a service area / route"
        }
        if ui.address.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return "Enter your shop address"
        }
        return nil
    }
}

private extension String {
    var nilIfBlank: String? {
        let t = trimmingCharacters(in: .whitespacesAndNewlines)
        return t.isEmpty ? nil : t
    }
}
