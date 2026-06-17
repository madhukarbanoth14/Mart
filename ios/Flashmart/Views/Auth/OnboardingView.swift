import SwiftUI

private struct OnboardingPage: Identifiable {
    let id = UUID()
    let kicker: String
    let title: String
    let body: String
    let art: OnboardingArtKind
}

private enum OnboardingArtKind {
    case catalog, track, invoice
}

struct OnboardingView: View {
    let onFinish: () -> Void
    @State private var index = 0

    private let pages: [OnboardingPage] = [
        OnboardingPage(
            kicker: "Order",
            title: "Your whole shop,\none tap away",
            body: "Browse 1,200+ FMCG products at dealer prices. Build your cart and reorder favourites in seconds.",
            art: .catalog
        ),
        OnboardingPage(
            kicker: "Deliver",
            title: "Delivered by your\nlocal dealer",
            body: "Orders route straight to your assigned distributor. Track every delivery live, right to your counter.",
            art: .track
        ),
        OnboardingPage(
            kicker: "Bill",
            title: "GST invoices,\nsorted automatically",
            body: "Every order generates a compliant tax invoice. Download, share, and file — no paperwork.",
            art: .invoice
        ),
    ]

    var body: some View {
        let page = pages[index]
        VStack(spacing: 0) {
            HStack {
                Spacer()
                Button("Skip") {
                    OnboardingPreferences.markCompleted()
                    onFinish()
                }
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(FMTheme.ink3)
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)

            Spacer()

            OnboardingArtView(kind: page.art)
                .frame(height: 260)
                .padding(.horizontal, 28)

            VStack(alignment: .leading, spacing: 14) {
                Text(page.kicker.uppercased())
                    .font(.system(size: 13, weight: .bold))
                    .tracking(0.8)
                    .foregroundStyle(FMTheme.brand)
                Text(page.title)
                    .font(.system(size: 30, weight: .bold))
                    .tracking(-0.5)
                    .lineSpacing(2)
                    .foregroundStyle(FMTheme.ink)
                Text(page.body)
                    .font(.system(size: 15.5))
                    .lineSpacing(4)
                    .foregroundStyle(FMTheme.ink3)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 28)
            .padding(.top, 30)

            Spacer()

            VStack(spacing: 22) {
                FMPageDots(count: pages.count, active: index)
                FMButton(
                    title: index == pages.count - 1 ? "Get started" : "Continue",
                    icon: index == pages.count - 1 ? nil : "arrow.right"
                ) {
                    if index < pages.count - 1 {
                        withAnimation(.easeInOut(duration: 0.25)) { index += 1 }
                    } else {
                        OnboardingPreferences.markCompleted()
                        onFinish()
                    }
                }
            }
            .padding(.horizontal, 28)
            .padding(.bottom, 34)
        }
        .background(FMTheme.surface)
    }
}

// MARK: - Art illustrations

private struct OnboardingArtView: View {
    let kind: OnboardingArtKind
    @State private var float = false

    var body: some View {
        Group {
            switch kind {
            case .catalog: catalogArt
            case .track: trackArt
            case .invoice: invoiceArt
            }
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 3.5).repeatForever(autoreverses: true)) { float = true }
        }
    }

    private var catalogArt: some View {
        ZStack {
            Circle()
                .fill(FMTheme.brandTint)
                .frame(width: 200, height: 200)
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                ForEach(0..<4, id: \.self) { i in
                    VStack(alignment: .leading, spacing: 6) {
                        RoundedRectangle(cornerRadius: 10)
                            .fill(FMTheme.surface2)
                            .frame(height: 42)
                        Text(["Atta", "Oil", "Salt", "Tea"][i])
                            .font(.system(size: 11.5, weight: .bold))
                        Text(["₹420", "₹180", "₹28", "₹95"][i])
                            .font(.system(size: 12, weight: .bold, design: .monospaced))
                            .foregroundStyle(FMTheme.brand)
                    }
                    .padding(12)
                    .background(FMTheme.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .shadow(color: .black.opacity(0.08), radius: 8, y: 4)
                    .offset(y: float ? (i % 2 == 0 ? -7 : 7) : (i % 2 == 0 ? 7 : -7))
                }
            }
            .frame(width: 260)
        }
    }

    private var trackArt: some View {
        ZStack {
            Circle()
                .fill(FMTheme.posTint)
                .frame(width: 200, height: 200)
            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 10) {
                    RoundedRectangle(cornerRadius: 11)
                        .fill(FMTheme.brand)
                        .frame(width: 38, height: 38)
                        .overlay(Image(systemName: "shippingbox.fill").foregroundStyle(.white))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Today, by 6 PM").font(.system(size: 13, weight: .bold))
                        Text("Sharma Distributors").font(.system(size: 11)).foregroundStyle(FMTheme.ink3)
                    }
                }
                ForEach([("Placed", true), ("Accepted", true), ("Out for delivery", false), ("Delivered", false)], id: \.0) { label, done in
                    HStack(spacing: 10) {
                        Circle()
                            .fill(done ? FMTheme.pos : FMTheme.surface3)
                            .frame(width: 18, height: 18)
                            .overlay {
                                if done { Image(systemName: "checkmark").font(.system(size: 9, weight: .bold)).foregroundStyle(.white) }
                            }
                        Text(label)
                            .font(.system(size: 12.5, weight: done ? .semibold : .medium))
                            .foregroundStyle(done ? FMTheme.ink : FMTheme.ink4)
                    }
                }
            }
            .padding(18)
            .frame(width: 250)
            .background(FMTheme.surface)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .shadow(color: .black.opacity(0.08), radius: 10, y: 4)
        }
    }

    private var invoiceArt: some View {
        ZStack {
            Circle()
                .fill(FMTheme.warnTint)
                .frame(width: 200, height: 200)
            VStack(spacing: 0) {
                HStack {
                    Text("Tax Invoice").font(.system(size: 13, weight: .bold)).foregroundStyle(.white)
                    Spacer()
                    Text("INV-1245").font(.system(size: 11, design: .monospaced)).foregroundStyle(.white.opacity(0.6))
                }
                .padding(14)
                .background(FMTheme.inkSurface)
                VStack(spacing: 4) {
                    HStack { Text("Subtotal"); Spacer(); Text("₹4,180").font(.system(size: 12, design: .monospaced)) }
                    HStack { Text("GST"); Spacer(); Text("₹512").font(.system(size: 12, design: .monospaced)) }
                    Divider().padding(.vertical, 6)
                    HStack {
                        Text("Total").font(.system(size: 13, weight: .bold))
                        Spacer()
                        Text("₹4,692").font(.system(size: 16, weight: .bold, design: .monospaced)).foregroundStyle(FMTheme.pos)
                    }
                }
                .font(.system(size: 12))
                .foregroundStyle(FMTheme.ink3)
                .padding(14)
            }
            .frame(width: 210)
            .background(FMTheme.surface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: .black.opacity(0.08), radius: 10, y: 4)
            .rotationEffect(.degrees(-3))

            Image(systemName: "arrow.down.circle.fill")
                .font(.system(size: 36))
                .foregroundStyle(FMTheme.pos)
                .offset(x: 90, y: 70)
                .rotationEffect(.degrees(6))
        }
    }
}

// MARK: - Page dots

struct FMPageDots: View {
    let count: Int
    let active: Int

    var body: some View {
        HStack(spacing: 7) {
            ForEach(0..<count, id: \.self) { i in
                Capsule()
                    .fill(i == active ? FMTheme.brand : FMTheme.line2)
                    .frame(width: i == active ? 22 : 7, height: 7)
                    .animation(.easeInOut(duration: 0.25), value: active)
            }
        }
        .frame(maxWidth: .infinity)
    }
}
