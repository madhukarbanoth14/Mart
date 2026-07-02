import SwiftUI

struct FMNotificationItem: Identifiable {
    let id: String
    let title: String
    let body: String
    let icon: String
    let tint: Color
    let tintBg: Color
    let unread: Bool
    let sortKey: String
}

struct FMNotificationGroup: Identifiable {
    let id: String
    let day: String
    let items: [FMNotificationItem]
}

struct FMNotificationsInbox: View {
    let groups: [FMNotificationGroup]
    var emptyMessage: String = "You're all caught up. Order and document updates will appear here."
    var onMarkAllRead: () -> Void = {}
    @Binding var path: NavigationPath

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                FMTopBar(
                    title: "Notifications",
                    onBack: { path.removeLast() },
                    trailing: AnyView(
                        Group {
                            if groups.contains(where: { $0.items.contains(where: \.unread) }) {
                                Button(action: onMarkAllRead) {
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 17, weight: .semibold))
                                        .foregroundStyle(FMTheme.pos)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    )
                )

                if groups.isEmpty {
                    FMEmptyState(icon: "bell", title: "No notifications", message: emptyMessage)
                } else {
                    ForEach(groups) { group in
                        VStack(alignment: .leading, spacing: 8) {
                            Text(group.day)
                                .font(.system(size: 12.5, weight: .bold))
                                .foregroundStyle(FMTheme.ink4)
                                .padding(.horizontal, 4)
                            FMCard(padding: 6) {
                                ForEach(Array(group.items.enumerated()), id: \.element.id) { index, item in
                                    notificationRow(item)
                                    if index < group.items.count - 1 { Divider().padding(.leading, 62) }
                                }
                            }
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 30)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
    }

    private func notificationRow(_ item: FMNotificationItem) -> some View {
        HStack(alignment: .top, spacing: 13) {
            Image(systemName: item.icon)
                .font(.system(size: 18))
                .foregroundStyle(item.tint)
                .frame(width: 42, height: 42)
                .background(item.tintBg)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            VStack(alignment: .leading, spacing: 2) {
                Text(item.title).font(.system(size: 14.5, weight: .bold))
                Text(item.body)
                    .font(.system(size: 13))
                    .foregroundStyle(FMTheme.ink3)
                    .fixedSize(horizontal: false, vertical: true)
            }
            if item.unread {
                Circle()
                    .fill(FMTheme.brand)
                    .frame(width: 9, height: 9)
                    .padding(.top, 6)
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 13)
    }
}

struct FMVerificationProgressCard: View {
    let verifiedCount: Int
    let totalSlots: Int
    let subtitle: String

    var body: some View {
        let pct = totalSlots == 0 ? 0.0 : Double(verifiedCount) / Double(totalSlots)
        FMCard {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .stroke(FMTheme.surface3, lineWidth: 6)
                    Circle()
                        .trim(from: 0, to: pct)
                        .stroke(FMTheme.pos, style: StrokeStyle(lineWidth: 6, lineCap: .round))
                        .rotationEffect(.degrees(-90))
                    Text("\(Int(pct * 100))%")
                        .font(.system(size: 13, weight: .bold, design: .monospaced))
                }
                .frame(width: 52, height: 52)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Verification status").font(.system(size: 15, weight: .bold))
                    Text(subtitle).font(.system(size: 13)).foregroundStyle(FMTheme.ink3)
                }
            }
        }
    }
}

enum FlashmartNotificationBuilder {
    static func group(_ items: [FMNotificationItem]) -> [FMNotificationGroup] {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let display = DateFormatter()
        display.dateFormat = "MMM d"
        let today = Calendar.current.startOfDay(for: Date())
        let yesterday = Calendar.current.date(byAdding: .day, value: -1, to: today)!

        var buckets: [(String, [FMNotificationItem])] = []
        let sorted = items.sorted { $0.sortKey > $1.sortKey }
        for item in sorted {
            let day: String
            if let date = formatter.date(from: String(item.sortKey.prefix(10))) {
                if Calendar.current.isDate(date, inSameDayAs: today) {
                    day = "Today"
                } else if Calendar.current.isDate(date, inSameDayAs: yesterday) {
                    day = "Yesterday"
                } else {
                    day = display.string(from: date)
                }
            } else {
                day = "Earlier"
            }
            if let idx = buckets.firstIndex(where: { $0.0 == day }) {
                buckets[idx].1.append(item)
            } else {
                buckets.append((day, [item]))
            }
        }
        return buckets.map { FMNotificationGroup(id: $0.0, day: $0.0, items: $0.1) }
    }

    static func shopkeeper(orders: [Order], docs: [OnboardingDocument], readIds: Set<String>) -> [FMNotificationGroup] {
        var items: [FMNotificationItem] = []
        for order in orders {
            let shortId = String(order.id.suffix(6)).uppercased()
            let date = order.createdAt ?? ""
            switch order.status.uppercased() {
            case "OUT_FOR_DELIVERY":
                items.append(FMNotificationItem(id: "ord-ofd-\(order.id)", title: "Out for delivery", body: "ORD-\(shortId) is on the way to your store.", icon: "shippingbox", tint: FMTheme.brand, tintBg: FMTheme.brandTint, unread: !readIds.contains("ord-ofd-\(order.id)"), sortKey: date))
            case "DELIVERED":
                items.append(FMNotificationItem(id: "ord-del-\(order.id)", title: "Order delivered", body: "ORD-\(shortId) delivered. Tap to view invoice.", icon: "checkmark.circle", tint: FMTheme.pos, tintBg: FMTheme.posTint, unread: !readIds.contains("ord-del-\(order.id)"), sortKey: date))
            case "PENDING", "PLACED":
                items.append(FMNotificationItem(id: "ord-pend-\(order.id)", title: "Order placed", body: "ORD-\(shortId) is awaiting dealer confirmation.", icon: "clock", tint: FMTheme.goldInk, tintBg: FMTheme.goldTint, unread: !readIds.contains("ord-pend-\(order.id)"), sortKey: date))
            default: break
            }
        }
        for doc in docs where doc.verificationStatus?.uppercased() == "VERIFIED" {
            items.append(FMNotificationItem(id: "doc-\(doc.id)", title: "Document verified", body: "Your \(doc.label) was approved.", icon: "doc.text", tint: FMTheme.dealerBlue, tintBg: FMTheme.dealerBlueTint, unread: !readIds.contains("doc-\(doc.id)"), sortKey: doc.verifiedAt ?? doc.uploadedAt ?? ""))
        }
        if items.isEmpty {
            items.append(FMNotificationItem(id: "promo-demo", title: "Offer just for you", body: "Extra discounts on staples this week from your dealer.", icon: "tag", tint: FMTheme.goldInk, tintBg: FMTheme.goldTint, unread: !readIds.contains("promo-demo"), sortKey: ISO8601DateFormatter().string(from: Date())))
        }
        return group(items)
    }

    static func dealer(orders: [Order], stock: [StockRow], docs: [OnboardingDocument], readIds: Set<String>) -> [FMNotificationGroup] {
        var items: [FMNotificationItem] = []
        for order in orders where order.kind?.uppercased() != "DEALER_RESTOCK" {
            let shortId = String(order.id.suffix(6)).uppercased()
            let shop = order.shopkeeper?.name ?? "Shopkeeper"
            let date = order.createdAt ?? ""
            if order.status.uppercased() == "PENDING" {
                items.append(FMNotificationItem(id: "ord-new-\(order.id)", title: "New shopkeeper order", body: "\(shop) placed ORD-\(shortId). Tap to accept.", icon: "bag", tint: FMTheme.goldInk, tintBg: FMTheme.goldTint, unread: !readIds.contains("ord-new-\(order.id)"), sortKey: date))
            }
            if order.paymentStatus?.uppercased() == "PAID" {
                items.append(FMNotificationItem(id: "pay-\(order.id)", title: "Payment received", body: "ORD-\(shortId) was paid by \(shop).", icon: "doc.text", tint: FMTheme.pos, tintBg: FMTheme.posTint, unread: !readIds.contains("pay-\(order.id)"), sortKey: date))
            }
        }
        for row in stock.filter({ $0.quantity < 5 }).prefix(2) {
            items.append(FMNotificationItem(id: "stock-\(row.id)", title: "Low stock alert", body: "\(row.product?.name ?? "SKU") is running low (\(row.quantity) left).", icon: "exclamationmark.triangle", tint: FMTheme.warn, tintBg: FMTheme.warnTint, unread: !readIds.contains("stock-\(row.id)"), sortKey: ISO8601DateFormatter().string(from: Date())))
        }
        return group(items)
    }
}

func documentIcon(for typeOrLabel: String) -> String {
    let key = typeOrLabel.uppercased()
    if key.contains("AADHAAR") { return "doc.text" }
    if key.contains("PAN") { return "creditcard" }
    if key.contains("GST") { return "doc.plaintext" }
    if key.contains("TRADE") || key.contains("LICENSE") { return "square.stack.3d.up" }
    return "doc.text"
}

private func formatDocDate(_ iso: String?, verified: Bool) -> String {
    guard let iso, !iso.isEmpty else { return verified ? "Verified" : "Uploaded recently" }
    let part = String(iso.prefix(10))
    let inFmt = DateFormatter(); inFmt.dateFormat = "yyyy-MM-dd"
    let outFmt = DateFormatter(); outFmt.dateFormat = "MMM d, yyyy"
    if let date = inFmt.date(from: part) {
        let text = outFmt.string(from: date)
        return verified ? text : "Uploaded \(text)"
    }
    return verified ? "Verified \(part)" : "Uploaded \(part)"
}

func fmDocumentCenterRow(_ doc: OnboardingDocument) -> some View {
    let verified = doc.verificationStatus?.uppercased() == "VERIFIED"
    let statusLabel = verified ? "Verified" : (doc.verificationStatus?.uppercased() == "REJECTED" ? "Rejected" : "Pending")
    return FMCard {
        HStack(spacing: 14) {
            Image(systemName: documentIcon(for: doc.documentType ?? doc.label))
                .font(.system(size: 18))
                .foregroundStyle(verified ? FMTheme.pos : FMTheme.goldInk)
                .frame(width: 44, height: 44)
                .background(verified ? FMTheme.posTint : FMTheme.goldTint)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            VStack(alignment: .leading, spacing: 2) {
                Text(doc.label).font(.system(size: 14.5, weight: .bold))
                Text(formatDocDate(doc.verifiedAt ?? doc.uploadedAt, verified: verified))
                    .font(.system(size: 12))
                    .foregroundStyle(FMTheme.ink4)
            }
            Spacer()
            FMBadge(status: doc.verificationStatus ?? "PENDING", label: statusLabel)
        }
    }
}
