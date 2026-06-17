import SwiftUI

struct ResetPasswordView: View {
    @Environment(AppEnvironment.self) private var env
    @Environment(\.dismiss) private var dismiss
    @State private var email = ""
    @State private var token = ""
    @State private var newPassword = ""
    @State private var step = 0
    @State private var message: String?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    if step == 0 {
                        Text("Enter your email to receive a reset token.")
                            .font(.system(size: 14))
                            .foregroundStyle(FMTheme.ink3)
                        FMTextField(label: "Email", text: $email, icon: "envelope", placeholder: "shop1@martdemo.com", keyboard: .emailAddress)
                        FMButton(title: "Request reset link") {
                            Task {
                                if let resp = await env.authViewModel.requestPasswordReset(email: email) {
                                    message = resp.message
                                    token = resp.resetPasswordToken ?? "demo-reset-token"
                                    step = 1
                                }
                            }
                        }
                    } else {
                        if let message {
                            Text(message)
                                .font(.system(size: 13))
                                .foregroundStyle(FMTheme.pos)
                                .padding(12)
                                .background(FMTheme.posTint)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        FMTextField(label: "Reset token", text: $token, icon: "key", placeholder: "demo-reset-token")
                        FMTextField(label: "New password", text: $newPassword, icon: "lock", placeholder: "Min 8 characters", secure: true)
                        FMButton(title: "Update password") {
                            Task {
                                if await env.authViewModel.resetPassword(token: token, newPassword: newPassword) {
                                    dismiss()
                                }
                            }
                        }
                    }
                    if let err = env.authViewModel.error {
                        Text(err).font(.system(size: 13)).foregroundStyle(FMTheme.neg)
                    }
                }
                .padding(24)
            }
            .background(FMTheme.bg)
            .navigationTitle("Reset password")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
        }
    }
}
