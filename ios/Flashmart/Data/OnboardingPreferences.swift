import Foundation

enum OnboardingPreferences {
    private static let key = "flashmart.hasCompletedOnboarding"

    static var hasCompletedOnboarding: Bool {
        get { UserDefaults.standard.bool(forKey: key) }
        set { UserDefaults.standard.set(newValue, forKey: key) }
    }

    static func markCompleted() {
        hasCompletedOnboarding = true
    }
}
