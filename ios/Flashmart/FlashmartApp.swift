import SwiftUI

@main
struct FlashmartApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @State private var environment = AppEnvironment()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(environment)
                .environment(\.locale, Locale(identifier: "en"))
                .tint(FMTheme.brand)
        }
    }
}
