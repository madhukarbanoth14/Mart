import UIKit
import UserNotifications

#if canImport(FirebaseMessaging)
import FirebaseMessaging
#endif

@MainActor
final class PushTokenRegistrar {
    private let apiClient: MartAPIClient
    private let sessionStore: SessionStore
    private var tokenObserver: NSObjectProtocol?

    init(apiClient: MartAPIClient, sessionStore: SessionStore) {
        self.apiClient = apiClient
        self.sessionStore = sessionStore
        #if canImport(FirebaseMessaging)
        tokenObserver = NotificationCenter.default.addObserver(
            forName: .fcmTokenRefreshed,
            object: nil,
            queue: .main
        ) { [weak self] note in
            guard let token = note.userInfo?["token"] as? String else { return }
            Task { @MainActor in
                await self?.register(token: token)
            }
        }
        #endif
    }

    deinit {
        if let tokenObserver {
            NotificationCenter.default.removeObserver(tokenObserver)
        }
    }

    var isConfigured: Bool {
        Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil
    }

    func registerCurrentToken() async {
        guard sessionStore.isLoggedIn, !sessionStore.isLocalDemoMode else { return }
        guard isConfigured else { return }
        guard await requestPermission() else { return }

        await UIApplication.shared.registerForRemoteNotifications()

        #if canImport(FirebaseMessaging)
        do {
            let token = try await Messaging.messaging().token()
            await register(token: token)
        } catch {
            // Firebase not configured or token unavailable.
        }
        #endif
    }

    func unregister() async {
        guard sessionStore.isLoggedIn, !sessionStore.isLocalDemoMode else { return }
        try? await apiClient.unregisterFcmToken()
    }

    private func register(token: String) async {
        guard sessionStore.isLoggedIn, !sessionStore.isLocalDemoMode else { return }
        guard !token.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        try? await apiClient.registerFcmToken(token)
    }

    private func requestPermission() async -> Bool {
        let center = UNUserNotificationCenter.current()
        let settings = await center.notificationSettings()
        switch settings.authorizationStatus {
        case .authorized, .provisional, .ephemeral:
            return true
        case .denied:
            return false
        case .notDetermined:
            return (try? await center.requestAuthorization(options: [.alert, .badge, .sound])) ?? false
        @unknown default:
            return false
        }
    }
}
