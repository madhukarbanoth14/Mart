import Foundation
import Observation

@Observable
final class SessionStore {
    private enum Keys {
        static let token = "mart.access_token"
        static let user = "mart.user_json"
        static let localDemo = "mart.local_demo_mode"
    }

    private(set) var user: SessionUser?
    private(set) var token: String?
    private(set) var isLocalDemoMode = false

    var isLoggedIn: Bool { user != nil }

    func hydrate() {
        let defaults = UserDefaults.standard
        token = defaults.string(forKey: Keys.token)
        isLocalDemoMode = defaults.bool(forKey: Keys.localDemo)
        if let data = defaults.data(forKey: Keys.user),
           let decoded = try? JSONDecoder().decode(SessionUser.self, from: data) {
            user = decoded
        }
    }

    func saveSession(token: String, user: SessionUser) {
        self.token = token
        self.user = user
        self.isLocalDemoMode = false
        persist()
    }

    func saveLocalDemoSession(user: SessionUser) {
        token = LocalDemoAuth.localDemoBearerToken
        self.user = user
        isLocalDemoMode = true
        persist()
    }

    func patchUser(_ user: SessionUser) {
        self.user = user
        persist()
    }

    func clear() {
        token = nil
        user = nil
        isLocalDemoMode = false
        let defaults = UserDefaults.standard
        defaults.removeObject(forKey: Keys.token)
        defaults.removeObject(forKey: Keys.user)
        defaults.removeObject(forKey: Keys.localDemo)
    }

    private func persist() {
        let defaults = UserDefaults.standard
        defaults.set(token, forKey: Keys.token)
        defaults.set(isLocalDemoMode, forKey: Keys.localDemo)
        if let user, let data = try? JSONEncoder().encode(user) {
            defaults.set(data, forKey: Keys.user)
        }
    }
}
