import Foundation

struct OnboardingDocumentSlot: Equatable, Identifiable {
    var id: String { label }
    let label: String
    let required: Bool
}

struct PendingOnboardingDocument: Equatable, Identifiable {
    var id: String { label }
    let label: String
    let displayName: String
    let localPath: URL
    let mimeType: String?
    let fileSize: Int64

    var localURL: URL { localPath }
}

enum OnboardingDocumentStorage {
    static func dealerSlots() -> [OnboardingDocumentSlot] {
        withDocumentPolicy(dealerDocumentSlots())
    }

    static func shopkeeperSlots() -> [OnboardingDocumentSlot] {
        withDocumentPolicy(shopkeeperDocumentSlots())
    }

    static func validateRequired(
        slots: [OnboardingDocumentSlot],
        documents: [PendingOnboardingDocument]
    ) -> String? {
        guard AppConfig.requireOnboardingDocuments else { return nil }
        return validateRequiredDocuments(slots: slots, documents: documents)
    }

    private static func withDocumentPolicy(_ slots: [OnboardingDocumentSlot]) -> [OnboardingDocumentSlot] {
        guard AppConfig.requireOnboardingDocuments else {
            return slots.map { OnboardingDocumentSlot(label: $0.label, required: false) }
        }
        return slots
    }

    static func dealerDocumentSlots() -> [OnboardingDocumentSlot] {
        [
            OnboardingDocumentSlot(label: "ID proof (Aadhaar / PAN)", required: true),
            OnboardingDocumentSlot(label: "GST certificate", required: true),
            OnboardingDocumentSlot(label: "Business registration / trade license", required: true),
            OnboardingDocumentSlot(label: "Bank proof / cancelled cheque", required: false),
        ]
    }

    static func shopkeeperDocumentSlots() -> [OnboardingDocumentSlot] {
        [
            OnboardingDocumentSlot(label: "ID proof (Aadhaar / PAN)", required: true),
            OnboardingDocumentSlot(label: "Shop license / rent agreement", required: true),
            OnboardingDocumentSlot(label: "GST certificate", required: false),
        ]
    }

    static func validateRequiredDocuments(
        slots: [OnboardingDocumentSlot],
        documents: [PendingOnboardingDocument]
    ) -> String? {
        let labels = Set(documents.map(\.label))
        let missing = slots.filter { $0.required && !labels.contains($0.label) }.map(\.label)
        guard !missing.isEmpty else { return nil }
        return "Please upload: \(missing.joined(separator: ", "))"
    }

    static func stageDocument(from sourceURL: URL, label: String) -> PendingOnboardingDocument? {
        let displayName = sourceURL.lastPathComponent.isEmpty ? "document" : sourceURL.lastPathComponent
        let stagingDir = onboardingRoot.appendingPathComponent("staging", isDirectory: true)
        try? FileManager.default.createDirectory(at: stagingDir, withIntermediateDirectories: true)
        let ext = sourceURL.pathExtension.isEmpty ? "bin" : sourceURL.pathExtension
        let target = stagingDir.appendingPathComponent("\(UUID().uuidString).\(ext)")
        do {
            if FileManager.default.fileExists(atPath: target.path) {
                try FileManager.default.removeItem(at: target)
            }
            try FileManager.default.copyItem(at: sourceURL, to: target)
            let attrs = try FileManager.default.attributesOfItem(atPath: target.path)
            let size = (attrs[.size] as? NSNumber)?.int64Value ?? 0
            return PendingOnboardingDocument(
                label: label,
                displayName: displayName,
                localPath: target,
                mimeType: mimeType(for: ext),
                fileSize: size
            )
        } catch {
            return nil
        }
    }

    static func persistForUser(userId: String, pending: [PendingOnboardingDocument]) -> [OnboardingDocument] {
        let userDir = onboardingRoot.appendingPathComponent(userId, isDirectory: true)
        try? FileManager.default.createDirectory(at: userDir, withIntermediateDirectories: true)
        return pending.map { doc in
            let safeName = doc.displayName.replacingOccurrences(of: #"[^\w.\-]+"#, with: "_", options: .regularExpression)
            let target = userDir.appendingPathComponent(safeName)
            try? FileManager.default.removeItem(at: target)
            try? FileManager.default.copyItem(at: doc.localPath, to: target)
            try? FileManager.default.removeItem(at: doc.localPath)
            return OnboardingDocument(
                id: "doc-\(UUID().uuidString)",
                label: doc.label,
                fileName: doc.displayName,
                mimeType: doc.mimeType,
                fileSize: doc.fileSize,
                uploadedAt: ISO8601DateFormatter().string(from: Date())
            )
        }
    }

    private static var onboardingRoot: URL {
        let base = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
        return base.appendingPathComponent("onboarding", isDirectory: true)
    }

    private static func mimeType(for ext: String) -> String {
        switch ext.lowercased() {
        case "pdf": return "application/pdf"
        case "jpg", "jpeg": return "image/jpeg"
        case "png": return "image/png"
        default: return "application/octet-stream"
        }
    }
}
