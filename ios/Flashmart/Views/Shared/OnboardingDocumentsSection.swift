import SwiftUI
import UniformTypeIdentifiers

struct OnboardingDocumentsSection: View {
    let slots: [OnboardingDocumentSlot]
    @Binding var attached: [String: PendingOnboardingDocument]

    @State private var pickingLabel: String?
    @State private var showPicker = false

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            FMSectionLabel(
                title: AppConfig.requireOnboardingDocuments
                    ? "Required documents"
                    : "Documents (optional for testing)"
            )
            Text(
                AppConfig.requireOnboardingDocuments
                    ? "Upload KYC and business documents. Admin will review these before approval."
                    : "Attach documents if available. Uploads are mandatory in production builds."
            )
                .font(.system(size: 12))
                .foregroundStyle(FMTheme.ink3)

            ForEach(slots) { slot in
                documentRow(slot)
            }
        }
        .fileImporter(
            isPresented: $showPicker,
            allowedContentTypes: [.pdf, .jpeg, .png, .heic, .data, .item],
            allowsMultipleSelection: false
        ) { result in
            guard let label = pickingLabel else { return }
            pickingLabel = nil
            switch result {
            case .success(let urls):
                guard let url = urls.first else { return }
                let accessed = url.startAccessingSecurityScopedResource()
                defer { if accessed { url.stopAccessingSecurityScopedResource() } }
                guard let staged = OnboardingDocumentStorage.stageDocument(from: url, label: label) else { return }
                attached[label] = staged
            case .failure:
                break
            }
        }
    }

    private func documentRow(_ slot: OnboardingDocumentSlot) -> some View {
        let doc = attached[slot.label]
        return FMCard(padding: 12) {
            HStack(alignment: .center, spacing: 12) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(slot.label + (slot.required ? " *" : ""))
                        .font(.system(size: 14, weight: .semibold))
                    if let doc {
                        Text(doc.displayName)
                            .font(.system(size: 12))
                            .foregroundStyle(FMTheme.brand)
                            .lineLimit(1)
                    } else {
                        Text("Not uploaded")
                            .font(.system(size: 12))
                            .foregroundStyle(FMTheme.ink4)
                    }
                }
                Spacer()
                Button(doc == nil ? "Upload" : "Replace") {
                    pickingLabel = slot.label
                    showPicker = true
                }
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(FMTheme.brand)
            }
        }
    }
}
