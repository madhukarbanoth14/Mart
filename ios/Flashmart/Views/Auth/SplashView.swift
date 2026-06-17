import SwiftUI

struct SplashView: View {
    let onFinish: () -> Void
    @State private var float = false
    @State private var spin = false

    var body: some View {
        ZStack {
            FMTheme.splashGradient.ignoresSafeArea()

            Circle()
                .fill(RadialGradient(colors: [.white.opacity(0.22), .clear], center: .center, startRadius: 0, endRadius: 160))
                .frame(width: 320, height: 320)
                .offset(y: -80)

            VStack(spacing: 22) {
                ZStack {
                    RoundedRectangle(cornerRadius: 28, style: .continuous)
                        .fill(.white)
                        .frame(width: 96, height: 96)
                        .shadow(color: .black.opacity(0.3), radius: 25, y: 12)
                        .offset(y: float ? -7 : 0)
                    Image(systemName: "bolt.fill")
                        .font(.system(size: 44, weight: .bold))
                        .foregroundStyle(FMTheme.brand)
                }

                VStack(spacing: 4) {
                    Text("Flashmart")
                        .font(.system(size: 38, weight: .heavy))
                        .tracking(-1)
                        .foregroundStyle(.white)
                    Text("Distribution, delivered.")
                        .font(.system(size: 14.5, weight: .medium))
                        .foregroundStyle(.white.opacity(0.7))
                }
            }

            VStack(spacing: 16) {
                Circle()
                    .trim(from: 0, to: 0.72)
                    .stroke(.white.opacity(0.25), lineWidth: 3)
                    .frame(width: 26, height: 26)
                    .rotationEffect(.degrees(spin ? 360 : 0))
                Text("v2.4 · Made in India 🇮🇳")
                    .font(.system(size: 11.5, weight: .semibold))
                    .tracking(0.8)
                    .foregroundStyle(.white.opacity(0.5))
            }
            .frame(maxHeight: .infinity, alignment: .bottom)
            .padding(.bottom, 70)
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 3.5).repeatForever(autoreverses: true)) { float = true }
            withAnimation(.linear(duration: 0.8).repeatForever(autoreverses: false)) { spin = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.2) { onFinish() }
        }
    }
}
