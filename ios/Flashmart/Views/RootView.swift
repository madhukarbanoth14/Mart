import SwiftUI

struct RootView: View {
    @Environment(AppEnvironment.self) private var env
    @State private var phase: AppPhase = .splash

    enum AppPhase {
        case splash, onboarding, login, register, main
    }

    var body: some View {
        Group {
            switch phase {
            case .splash:
                SplashView {
                    withAnimation(.easeInOut(duration: 0.35)) {
                        if env.sessionStore.isLoggedIn {
                            phase = .main
                        } else if OnboardingPreferences.hasCompletedOnboarding {
                            phase = .login
                        } else {
                            phase = .onboarding
                        }
                    }
                }
            case .onboarding:
                OnboardingView {
                    withAnimation(.easeInOut(duration: 0.35)) { phase = .login }
                }
            case .login:
                LoginView(onSignedIn: {
                    withAnimation { phase = .main }
                }, onRegister: AppConfig.useLocalDemoAuth ? nil : {
                    withAnimation { phase = .register }
                })
            case .register:
                SelfRegistrationView(
                    onBack: { withAnimation { phase = .login } },
                    onRegistered: { withAnimation { phase = .main } }
                )
            case .main:
                if let user = env.sessionStore.user {
                    RoleShellView(user: user, onLogout: {
                        Task { await env.pushTokenRegistrar.unregister() }
                        env.authViewModel.logout()
                        env.cartStore.clear()
                        withAnimation { phase = .login }
                    })
                } else {
                    LoginView(onSignedIn: { phase = .main })
                }
            }
        }
        .preferredColorScheme(.light)
    }
}
