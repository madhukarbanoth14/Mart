import SwiftUI
import UniformTypeIdentifiers

struct ProfileDocumentsView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    @State private var uploadError: String?
    @State private var uploadingType: String?
    @State private var pendingType: String?
    @State private var showPicker = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(
                    title: "Document center",
                    subtitle: "\(verifiedCount) verified · \(pendingCount) pending",
                    onBack: { path.removeLast() }
                )

                FMVerificationProgressCard(
                    verifiedCount: verifiedCount,
                    totalSlots: BusinessDocumentTypes.all.count,
                    subtitle: progressSubtitle
                )

                if let uploadError {
                    FMErrorBanner(text: uploadError)
                }

                switch env.mainViewModel.myDocuments {
                case .loading:
                    FMLoadingState(message: "Loading documents…")
                case .err(let msg):
                    FMErrorBanner(text: msg)
                case .ok(let docs):
                    if docs.isEmpty {
                        FMEmptyState(
                            icon: "doc.text",
                            title: "No documents yet",
                            message: "Upload a business document to enable checkout."
                        )
                    } else {
                        ForEach(docs) { doc in
                            fmDocumentCenterRow(doc)
                        }
                    }
                case .idle:
                    EmptyView()
                }

                if !missingSlots.isEmpty {
                    FMSectionLabel(title: "Recommended uploads")
                    ForEach(missingSlots) { slot in
                        FMCard {
                            HStack(spacing: 14) {
                                Image(systemName: documentIcon(for: slot.type))
                                    .font(.system(size: 18))
                                    .foregroundStyle(FMTheme.goldInk)
                                    .frame(width: 44, height: 44)
                                    .background(FMTheme.goldTint)
                                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(slot.label).font(.system(size: 14.5, weight: .bold))
                                    Text("Not uploaded yet").font(.system(size: 12)).foregroundStyle(FMTheme.ink4)
                                }
                                Spacer()
                            }
                        }
                    }
                }

                FMButton(
                    title: uploadingType != nil ? "Uploading…" : "Upload document",
                    variant: .primary,
                    icon: "arrow.up.doc",
                    enabled: uploadingType == nil
                ) {
                    pendingType = missingSlots.first?.type ?? BusinessDocumentTypes.all.first?.type
                    showPicker = true
                }
            }
            .padding(FMSpacing.screenH)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .fileImporter(
            isPresented: $showPicker,
            allowedContentTypes: [.pdf, .image],
            allowsMultipleSelection: false
        ) { result in
            handlePickerResult(result)
        }
        .task {
            await env.mainViewModel.loadMyDocuments()
            await env.mainViewModel.refreshAuthProfile()
        }
    }

    private var docs: [OnboardingDocument] {
        if case .ok(let list) = env.mainViewModel.myDocuments { return list }
        return []
    }

    private var verifiedCount: Int {
        docs.filter { $0.verificationStatus?.uppercased() == "VERIFIED" }.count
    }

    private var pendingCount: Int {
        docs.filter { $0.verificationStatus?.uppercased() != "VERIFIED" }.count
    }

    private var progressSubtitle: String {
        let total = BusinessDocumentTypes.all.count
        if verifiedCount >= total { return "All required documents verified." }
        if pendingCount > 0 { return "\(pendingCount) document\(pendingCount == 1 ? "" : "s") awaiting verification." }
        if docs.isEmpty { return "Upload at least one document to unlock full verification." }
        let remaining = total - verifiedCount
        return "\(remaining) more document\(remaining == 1 ? "" : "s") recommended."
    }

    private var missingSlots: [BusinessDocumentTypes.Slot] {
        let uploaded = Set(docs.compactMap { $0.documentType?.uppercased() })
        return BusinessDocumentTypes.all.filter { !uploaded.contains($0.type.uppercased()) }
    }

    private func handlePickerResult(_ result: Result<[URL], Error>) {
        guard let type = pendingType else { return }
        pendingType = nil
        switch result {
        case .failure:
            uploadError = "Could not read selected file"
        case .success(let urls):
            guard let url = urls.first else { return }
            guard url.startAccessingSecurityScopedResource() else {
                uploadError = "Could not access selected file"
                return
            }
            defer { url.stopAccessingSecurityScopedResource() }
            let staged = OnboardingDocumentStorage.stageDocument(from: url, label: BusinessDocumentTypes.label(for: type))
            guard let staged else {
                uploadError = "Could not read selected file"
                return
            }
            uploadingType = type
            uploadError = nil
            Task {
                let err = await env.mainViewModel.uploadMyDocument(
                    documentType: type,
                    fileURL: staged.localURL,
                    fileName: staged.displayName,
                    mimeType: staged.mimeType ?? "application/octet-stream"
                )
                uploadingType = nil
                uploadError = err
            }
        }
    }
}
