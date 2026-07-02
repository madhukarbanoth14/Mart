import QuickLook
import SwiftUI

struct AdminOnboardingReviewView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let userId: String

    @State private var previewURL: URL?
    @State private var showPreview = false
    @State private var docBusyId: String?
    @State private var docError: String?
    @State private var confirmAction: ReviewConfirmAction?
    @State private var showConfirm = false

    private var user: UserRow? {
        guard case .ok(let list) = env.mainViewModel.users else { return nil }
        return list.first { $0.id == userId }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Review onboarding", subtitle: user?.name, onBack: { path.removeLast() })

                if let user {
                    FMCard {
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(user.name).font(.system(size: 18, weight: .bold))
                                    Text(user.email).font(.system(size: 13)).foregroundStyle(FMTheme.ink3)
                                }
                                Spacer()
                                VStack(alignment: .trailing, spacing: 6) {
                                    FMBadge(status: user.status)
                                    FMBadge(status: user.role)
                                }
                            }
                            detailLine("Phone", user.phone ?? "—")
                            detailLine("Area", user.area?.name ?? "—")
                            detailLine("Onboarded by", user.onboardedBy?.name ?? "—")
                            if let email = user.onboardedBy?.email {
                                detailLine("Employee email", email)
                            }
                            detailLine("Submitted", String((user.createdAt ?? "—").prefix(19)).replacingOccurrences(of: "T", with: " "))
                            if let notes = user.onboardingNotes, !notes.isEmpty {
                                detailLine("Notes", notes)
                            }
                            if let reason = user.statusReason, !reason.isEmpty {
                                detailLine("Status reason", reason)
                            }
                        }
                    }

                    FMCard {
                        FMSectionLabel(title: "Uploaded documents")
                        if let docs = user.onboardingDocuments, !docs.isEmpty {
                            VStack(spacing: 10) {
                                ForEach(docs) { doc in
                                    HStack {
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(doc.label).font(.system(size: 14, weight: .semibold))
                                            Text(doc.fileName).font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                                        }
                                        Spacer()
                                        FMButton(
                                            title: docBusyId == doc.id ? "Opening…" : "View",
                                            variant: .outline,
                                            fullWidth: false,
                                            enabled: docBusyId != doc.id
                                        ) {
                                            Task { await openDocument(doc) }
                                        }
                                        .frame(width: 88)
                                    }
                                }
                            }
                        } else {
                            Text("No documents attached.")
                                .font(.system(size: 13))
                                .foregroundStyle(FMTheme.ink3)
                        }
                    }

                    if let docError {
                        FMErrorBanner(text: docError)
                    }

                    if user.status == "PENDING_APPROVAL" && user.role != "EMPLOYEE" {
                        Text("Verify details and documents, then approve. A confirmation email is sent only after approval.")
                            .font(.system(size: 12))
                            .foregroundStyle(FMTheme.ink3)
                        HStack(spacing: 10) {
                            FMButton(title: "Approve", variant: .pos, fullWidth: true) {
                                confirmAction = .approve(user)
                                showConfirm = true
                            }
                            FMButton(title: "Reject", variant: .outline, fullWidth: true) {
                                confirmAction = .reject(user)
                                showConfirm = true
                            }
                        }
                    }
                } else {
                    FMLoadingState(message: "Loading user…")
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 24)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task {
            if case .ok = env.mainViewModel.users {} else {
                await env.mainViewModel.loadUsers()
            }
        }
        .sheet(isPresented: $showPreview) {
            if let previewURL {
                DocumentPreview(url: previewURL)
            }
        }
        .alert(confirmTitle, isPresented: $showConfirm, presenting: confirmAction) { action in
            Button(confirmButtonTitle, role: confirmDestructive ? .destructive : nil) {
                Task { await performConfirm(action) }
            }
            Button("Cancel", role: .cancel) {}
        } message: { action in
            Text(confirmMessage(action))
        }
    }

    private func detailLine(_ label: String, _ value: String) -> some View {
        HStack(alignment: .top) {
            Text(label)
                .font(.system(size: 12, weight: .semibold))
                .foregroundStyle(FMTheme.ink3)
                .frame(width: 110, alignment: .leading)
            Text(value)
                .font(.system(size: 13))
                .foregroundStyle(FMTheme.ink)
        }
    }

    private func openDocument(_ doc: OnboardingDocument) async {
        docBusyId = doc.id
        docError = nil
        defer { docBusyId = nil }
        do {
            let url = try await env.apiClient.openOnboardingDocument(userId: userId, document: doc)
            previewURL = url
            showPreview = true
        } catch {
            docError = (error as? LocalizedError)?.errorDescription ?? error.localizedDescription
        }
    }

    private var confirmTitle: String {
        switch confirmAction {
        case .approve: "Approve user?"
        case .reject: "Reject onboarding?"
        case .none: ""
        }
    }

    private var confirmButtonTitle: String {
        switch confirmAction {
        case .approve: "Approve"
        case .reject: "Reject"
        case .none: ""
        }
    }

    private var confirmDestructive: Bool {
        switch confirmAction {
        case .reject: true
        default: false
        }
    }

    private func confirmMessage(_ action: ReviewConfirmAction) -> String {
        switch action {
        case .approve(let u): "\(u.name) will be activated."
        case .reject(let u): "\(u.name) will be rejected and cannot sign in."
        }
    }

    private func performConfirm(_ action: ReviewConfirmAction) async {
        switch action {
        case .approve(let user):
            if let msg = await env.mainViewModel.approveUser(user.id) {
                _ = msg
            }
            path.removeLast()
        case .reject(let user):
            await env.mainViewModel.rejectUser(user.id)
            path.removeLast()
        }
    }
}

private enum ReviewConfirmAction: Identifiable {
    case approve(UserRow)
    case reject(UserRow)

    var id: String {
        switch self {
        case .approve(let u): return "approve-\(u.id)"
        case .reject(let u): return "reject-\(u.id)"
        }
    }
}

private struct DocumentPreview: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> QLPreviewController {
        let controller = QLPreviewController()
        controller.dataSource = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: QLPreviewController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(url: url) }

    final class Coordinator: NSObject, QLPreviewControllerDataSource {
        let url: URL
        init(url: URL) { self.url = url }

        func numberOfPreviewItems(in controller: QLPreviewController) -> Int { 1 }

        func previewController(_ controller: QLPreviewController, previewItemAt index: Int) -> QLPreviewItem {
            url as NSURL
        }
    }
}
