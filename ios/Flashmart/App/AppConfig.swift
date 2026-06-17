import Foundation

enum AppConfig {
    private static func plistString(_ key: String, fallback: String) -> String {
        Bundle.main.object(forInfoDictionaryKey: key) as? String ?? fallback
    }

    private static func plistBool(_ key: String, fallback: Bool) -> Bool {
        guard let raw = Bundle.main.object(forInfoDictionaryKey: key) as? String else {
            return fallback
        }
        return raw == "YES" || raw == "true" || raw == "1"
    }

    static var apiBaseURL: URL {
        let raw = plistString("MART_API_BASE_URL", fallback: "http://127.0.0.1:3005")
        let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines).trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        return URL(string: trimmed + "/") ?? URL(string: "http://127.0.0.1:3005/")!
    }

    static var useLocalDemoAuth: Bool {
        plistBool("MART_USE_LOCAL_DEMO_AUTH", fallback: false)
    }

    static var demoMode: Bool {
        plistBool("MART_DEMO_MODE", fallback: false)
    }

    /// Debug builds skip required-document validation; Release builds enforce it.
    static var requireOnboardingDocuments: Bool {
        plistBool("MART_REQUIRE_ONBOARDING_DOCUMENTS", fallback: false)
    }

    /// Razorpay publishable key — set in Config.xcconfig or use server keyId from API.
    static var razorpayKeyId: String {
        plistString("MART_RAZORPAY_KEY_ID", fallback: "rzp_test_YOUR_KEY_HERE")
    }

    static let demoPassword = LocalDemoAuth.demoPassword
}
