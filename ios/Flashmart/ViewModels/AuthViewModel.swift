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
