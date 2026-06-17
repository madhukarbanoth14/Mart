import SwiftUI

struct LoginView: View {
    @Environment(AppEnvironment.self) private var env
    let onSignedIn: () -> Void
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
                                if await auth.login() { onSignedIn() }
                            }
                        }

                        Button("Forgot password?") { showReset = true }
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(FMTheme.brand.opacity(0.9))
                            .frame(maxWidth: .infinity)
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
