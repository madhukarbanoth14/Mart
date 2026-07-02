import SwiftUI

// MARK: - Card

struct FMCard<Content: View>: View {
    var padding: CGFloat = 16
    var onTap: (() -> Void)?
    @ViewBuilder var content: () -> Content

    var body: some View {
        let card = VStack(alignment: .leading, spacing: 0) {
            content()
        }
        .padding(padding)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(FMTheme.surface)
        .clipShape(RoundedRectangle(cornerRadius: FMTheme.cardRadius, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: FMTheme.cardRadius, style: .continuous)
                .stroke(FMTheme.line, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.04), radius: 4, y: 2)

        if let onTap {
            Button(action: onTap) { card }.buttonStyle(.plain)
        } else {
            card
        }
    }
}

// MARK: - Buttons

enum FMButtonVariant {
    case primary, dark, soft, outline, ghost, pos
}

struct FMButton: View {
    let title: String
    var variant: FMButtonVariant = .primary
    var icon: String? = nil
    var fullWidth: Bool = true
    var enabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if let icon { Image(systemName: icon).font(.system(size: 15, weight: .semibold)) }
                Text(title)
                    .font(.system(size: 15, weight: .semibold))
            }
            .frame(maxWidth: fullWidth ? .infinity : nil)
            .frame(height: 52)
            .foregroundStyle(foreground)
            .background(background)
            .clipShape(RoundedRectangle(cornerRadius: FMTheme.buttonRadius, style: .continuous))
            .overlay {
                if variant == .outline {
                    RoundedRectangle(cornerRadius: FMTheme.buttonRadius, style: .continuous)
                        .stroke(FMTheme.line2, lineWidth: 1)
                }
            }
        }
        .disabled(!enabled)
        .opacity(enabled ? 1 : 0.5)
    }

    private var background: Color {
        switch variant {
        case .primary: FMTheme.brand
        case .dark: FMTheme.ink
        case .soft: FMTheme.brandTint
        case .outline, .ghost: .clear
        case .pos: FMTheme.pos
        }
    }

    private var foreground: Color {
        switch variant {
        case .primary, .dark, .pos: .white
        case .soft: FMTheme.brandInk
        case .outline: FMTheme.ink
        case .ghost: FMTheme.ink2
        }
    }
}

// MARK: - Badge

struct FMBadge: View {
    let status: String
    var label: String? = nil

    var body: some View {
        Text(displayLabel)
            .font(.system(size: 12, weight: .semibold))
            .foregroundStyle(fg)
            .padding(.horizontal, 11)
            .padding(.vertical, 6)
            .background(bg)
            .clipShape(Capsule())
    }

    private var displayLabel: String {
        label ?? Self.defaultLabel(for: status)
    }

    private var bg: Color {
        switch status.uppercased() {
        case "PENDING", "PLACED", "PENDING_APPROVAL": FMTheme.warnTint
        case "DEALER_CONFIRMED", "ACCEPTED": FMTheme.brandTint
        case "OUT_FOR_DELIVERY", "OUT": FMTheme.warnTint
        case "DELIVERED", "PAID", "ACTIVE": FMTheme.posTint
        case "UNPAID", "REJECTED", "DEACTIVATED", "CANCELLED": FMTheme.negTint
        case "RETURN_REQUESTED": FMTheme.warnTint
        case "RETURNED", "REFUNDED": FMTheme.brandTint
        default: FMTheme.surface3
        }
    }

    private var fg: Color {
        switch status.uppercased() {
        case "PENDING", "PLACED", "PENDING_APPROVAL", "OUT_FOR_DELIVERY", "OUT": FMTheme.warn
        case "DEALER_CONFIRMED", "ACCEPTED": FMTheme.brandInk
        case "DELIVERED", "PAID", "ACTIVE": FMTheme.pos
        case "UNPAID", "REJECTED", "DEACTIVATED", "CANCELLED": FMTheme.neg
        case "RETURN_REQUESTED": FMTheme.warn
        case "RETURNED", "REFUNDED": FMTheme.brandInk
        default: FMTheme.ink2
        }
    }

    static func defaultLabel(for status: String) -> String {
        switch status.uppercased() {
        case "PENDING": "Placed"
        case "PENDING_APPROVAL": "Pending"
        case "DEALER_CONFIRMED", "ACCEPTED": "Accepted"
        case "OUT_FOR_DELIVERY": "Out for delivery"
        case "DELIVERED": "Delivered"
        case "PAID": "Paid"
        case "UNPAID": "Unpaid"
        case "ACTIVE": "Active"
        case "CANCELLED": "Cancelled"
        case "RETURN_REQUESTED": "Return requested"
        case "RETURNED": "Returned"
        case "REFUNDED": "Refunded"
        default: status.replacingOccurrences(of: "_", with: " ").capitalized
        }
    }
}

// MARK: - Top bar

struct FMTopBar: View {
    let title: String
    var subtitle: String? = nil
    var kicker: String? = nil
    var accent: Color = FMTheme.brand
    var onBack: (() -> Void)? = nil
    var trailing: AnyView? = nil

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            if let onBack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(FMTheme.ink)
                        .frame(width: 40, height: 40)
                        .background(FMTheme.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(FMTheme.line))
                        .shadow(color: .black.opacity(0.04), radius: 2, y: 1)
                }
            }

            VStack(alignment: .leading, spacing: 4) {
                if let kicker {
                    Text(kicker)
                        .font(.system(size: 12.5, weight: .semibold))
                        .foregroundStyle(accent)
                }
                Text(title)
                    .font(.system(size: 26, weight: .bold))
                    .tracking(-0.5)
                    .foregroundStyle(FMTheme.ink)
                if let subtitle {
                    Text(subtitle)
                        .font(.system(size: 13.5))
                        .foregroundStyle(FMTheme.ink3)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            if let trailing { trailing }
        }
        .padding(.horizontal, 20)
        .padding(.top, 6)
        .padding(.bottom, 14)
    }
}

// MARK: - Glyph button

struct FMGlyphButton: View {
    let systemName: String
    var badge: Int? = nil
    var accent: Color = FMTheme.ink2
    var action: (() -> Void)? = nil

    var body: some View {
        Button {
            action?()
        } label: {
            ZStack(alignment: .topTrailing) {
                Image(systemName: systemName)
                    .font(.system(size: 18, weight: .medium))
                    .foregroundStyle(accent)
                    .frame(width: 42, height: 42)
                    .background(FMTheme.surface)
                    .clipShape(RoundedRectangle(cornerRadius: FMTheme.glyphRadius, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: FMTheme.glyphRadius).stroke(FMTheme.line))
                    .shadow(color: .black.opacity(0.04), radius: 2, y: 1)

                if let badge, badge > 0 {
                    Text("\(badge)")
                        .font(.system(size: 10.5, weight: .bold, design: .monospaced))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 5)
                        .frame(minWidth: 18, minHeight: 18)
                        .background(FMTheme.neg)
                        .clipShape(Capsule())
                        .overlay(Capsule().stroke(FMTheme.surface, lineWidth: 2))
                        .offset(x: 6, y: -6)
                }
            }
        }
        .buttonStyle(.plain)
        .disabled(action == nil)
    }
}

// MARK: - Avatar

struct FMAvatar: View {
    let name: String
    var size: CGFloat = 42
    var tint: Color = FMTheme.brand

    var body: some View {
        Text(initials)
            .font(.system(size: size * 0.34, weight: .bold))
            .foregroundStyle(.white)
            .frame(width: size, height: size)
            .background(tint)
            .clipShape(RoundedRectangle(cornerRadius: size * 0.28, style: .continuous))
    }

    private var initials: String {
        let parts = name.split(separator: " ")
        let letters = parts.prefix(2).compactMap { $0.first }
        return String(letters).uppercased()
    }
}

// MARK: - Stepper

struct FMStepper: View {
    let value: Int
    let onChange: (Int) -> Void
    var min: Int = 0

    var body: some View {
        HStack(spacing: 10) {
            stepButton("minus", enabled: value > min) { onChange(max(min, value - 1)) }
            Text("\(value)")
                .font(.system(size: 15, weight: .bold))
                .frame(minWidth: 24)
            stepButton("plus", enabled: true) { onChange(value + 1) }
        }
        .padding(4)
        .background(FMTheme.surface2)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(FMTheme.line))
    }

    private func stepButton(_ icon: String, enabled: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(enabled ? FMTheme.ink : FMTheme.ink4)
                .frame(width: 32, height: 32)
                .background(enabled ? FMTheme.surface : FMTheme.surface3)
                .clipShape(RoundedRectangle(cornerRadius: 9, style: .continuous))
        }
        .disabled(!enabled)
    }
}

// MARK: - Quantity selector (bulk orders)

enum FMQuantityDefaults {
    static let minQuantity = 1
    static let maxQuantity = 10000
    static let quickChips = [10, 25, 50, 100, 250, 500, 1000]
}

func fmCoerceWholeQuantity(_ raw: String, min minQty: Int = FMQuantityDefaults.minQuantity, max maxQty: Int = FMQuantityDefaults.maxQuantity) -> Int? {
    let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
    if trimmed.isEmpty { return nil }
    if trimmed.contains(".") || trimmed.contains(",") { return nil }
    guard let value = Int(trimmed), value >= minQty else { return nil }
    return Swift.min(value, maxQty)
}

struct FMQuantityInput: View {
    let value: Int
    let onChange: (Int) -> Void
    var min: Int = FMQuantityDefaults.minQuantity
    var max: Int = FMQuantityDefaults.maxQuantity
    var compact: Bool = false

    @State private var text: String = ""
    @State private var error: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 8) {
                qtyButton("minus", enabled: value > min) { onChange(Swift.max(min, value - 1)) }
                TextField("", text: $text)
                    .keyboardType(.numberPad)
                    .multilineTextAlignment(.center)
                    .font(.system(size: compact ? 14 : 16, weight: .bold, design: .monospaced))
                    .frame(minWidth: compact ? 44 : 56, maxWidth: 72)
                    .padding(.vertical, 8)
                    .background(FMTheme.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(FMTheme.line))
                    .onChange(of: text) { _, newValue in
                        let filtered = newValue.filter(\.isNumber)
                        if filtered != newValue { text = filtered }
                        error = nil
                    }
                    .onSubmit { commit() }
                qtyButton("plus", enabled: value < max) { onChange(Swift.min(max, value + 1)) }
            }
            if let error {
                Text(error).font(.system(size: 11)).foregroundStyle(FMTheme.neg)
            }
        }
        .onAppear { text = "\(value)" }
        .onChange(of: value) { _, newValue in
            if text != "\(newValue)" { text = "\(newValue)" }
        }
    }

    private func commit() {
        guard let parsed = fmCoerceWholeQuantity(text, min: min, max: max) else {
            error = "Enter a whole number between \(min) and \(max)"
            text = "\(value)"
            return
        }
        error = nil
        text = "\(parsed)"
        if parsed != value { onChange(parsed) }
    }

    private func qtyButton(_ icon: String, enabled: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(enabled ? FMTheme.ink : FMTheme.ink4)
                .frame(width: 32, height: 32)
                .background(enabled ? FMTheme.surface2 : FMTheme.surface3)
                .clipShape(RoundedRectangle(cornerRadius: 9))
        }
        .disabled(!enabled)
    }
}

struct FMQuantityPickerSheet: View {
    let productName: String
    var initialQuantity: Int = 1
    var min: Int = FMQuantityDefaults.minQuantity
    var max: Int = FMQuantityDefaults.maxQuantity
    var quickChips: [Int] = FMQuantityDefaults.quickChips
    let onConfirm: (Int) -> Void
    @Environment(\.dismiss) private var dismiss

    @State private var quantityText = "1"
    @State private var error: String?

    private var maxQuantity: Int { max }

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Text(productName)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(FMTheme.ink2)
                    .lineLimit(2)

                TextField("Quantity", text: $quantityText)
                    .keyboardType(.numberPad)
                    .font(.system(size: 22, weight: .bold, design: .monospaced))
                    .padding(14)
                    .background(FMTheme.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(FMTheme.line))
                    .onChange(of: quantityText) { _, newValue in
                        quantityText = newValue.filter(\.isNumber)
                        error = nil
                    }

                Text("Quick select")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(FMTheme.ink4)

                LazyVGrid(columns: [GridItem(.adaptive(minimum: 72), spacing: 8)], spacing: 8) {
                    ForEach(quickChips, id: \.self) { chip in
                        let selected = quantityText == "\(chip)"
                        Button {
                            quantityText = "\(Swift.min(chip, maxQuantity))"
                            error = nil
                        } label: {
                            Text("\(chip)")
                                .font(.system(size: 14, weight: .semibold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(selected ? FMTheme.brand : FMTheme.surface3)
                                .foregroundStyle(selected ? .white : FMTheme.ink2)
                                .clipShape(RoundedRectangle(cornerRadius: 20))
                        }
                        .buttonStyle(.plain)
                    }
                }

                if let error {
                    Text(error).font(.system(size: 12)).foregroundStyle(FMTheme.neg)
                }

                Spacer()
            }
            .padding(20)
            .navigationTitle("Enter quantity")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add to cart") {
                        guard let qty = fmCoerceWholeQuantity(quantityText, min: min, max: maxQuantity) else {
                            error = "Enter a whole number between \(min) and \(maxQuantity)"
                            return
                        }
                        onConfirm(qty)
                        dismiss()
                    }
                    .fontWeight(.semibold)
                }
            }
        }
        .onAppear { quantityText = "\(Swift.min(Swift.max(initialQuantity, min), maxQuantity))" }
    }
}

// MARK: - Section label

struct FMSectionLabel: View {
    let title: String
    var actionTitle: String? = nil
    var onAction: (() -> Void)? = nil

    var body: some View {
        HStack {
            Text(title)
                .font(.system(size: 15, weight: .bold))
                .foregroundStyle(FMTheme.ink)
            Spacer()
            if let actionTitle, let onAction {
                Button(actionTitle, action: onAction)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(FMTheme.brand)
            }
        }
        .padding(.bottom, 10)
    }
}

// MARK: - Row

struct FMRow: View {
    var icon: String? = nil
    let title: String
    var subtitle: String? = nil
    var trailing: AnyView? = nil
    var showDivider: Bool = true
    var action: (() -> Void)? = nil

    var body: some View {
        let content = rowContent
        if let action {
            Button(action: action) { content }
                .buttonStyle(.plain)
        } else {
            content
        }
    }

    private var rowContent: some View {
        VStack(spacing: 0) {
            HStack(spacing: 12) {
                if let icon {
                    Image(systemName: icon)
                        .font(.system(size: 17))
                        .foregroundStyle(FMTheme.ink2)
                        .frame(width: 38, height: 38)
                        .background(FMTheme.surface2)
                        .clipShape(RoundedRectangle(cornerRadius: 11, style: .continuous))
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 14.5, weight: .semibold))
                        .foregroundStyle(FMTheme.ink)
                    if let subtitle {
                        Text(subtitle)
                            .font(.system(size: 12.5))
                            .foregroundStyle(FMTheme.ink3)
                    }
                }
                Spacer()
                if action != nil {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(FMTheme.ink4)
                }
                trailing
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 8)
            if showDivider {
                Divider().padding(.leading, icon != nil ? 58 : 8)
            }
        }
    }
}

// MARK: - Money row

struct FMMoneyRow: View {
    let label: String
    let value: String
    var accent: Color = FMTheme.ink
    var strong: Bool = false

    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: strong ? 14 : 13.5, weight: strong ? .bold : .medium))
                .foregroundStyle(strong ? FMTheme.ink : FMTheme.ink3)
            Spacer()
            Text(value)
                .font(.system(size: strong ? 16 : 14, weight: .bold, design: .monospaced))
                .foregroundStyle(accent)
        }
        .padding(.vertical, 6)
    }
}

// MARK: - Segmented control

struct FMSegmented: View {
    let options: [String]
    @Binding var selection: String

    var body: some View {
        HStack(spacing: 0) {
            ForEach(options, id: \.self) { opt in
                Button {
                    selection = opt
                } label: {
                    Text(opt)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(selection == opt ? FMTheme.ink : FMTheme.ink3)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(selection == opt ? FMTheme.surface : .clear)
                        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                }
            }
        }
        .padding(4)
        .background(FMTheme.surface2)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(FMTheme.line))
    }
}

// MARK: - Product thumb

struct FMProductThumb: View {
    let product: Product
    var size: CGFloat = 52

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .fill(
                    LinearGradient(
                        colors: [FMTheme.surface2, FMTheme.surface3],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
            Text(product.brand?.name.prefix(1).uppercased() ?? "P")
                .font(.system(size: size * 0.32, weight: .bold))
                .foregroundStyle(FMTheme.brand)
        }
        .frame(width: size, height: size)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(FMTheme.line))
    }
}

// MARK: - Bottom nav

struct FMNavItem: Identifiable {
    let id: String
    let icon: String
    let label: String
    var badge: Int? = nil
}

struct FMBottomNav: View {
    let items: [FMNavItem]
    @Binding var selection: String

    var body: some View {
        VStack(spacing: 0) {
            LinearGradient(
                colors: [FMTheme.bg.opacity(0), FMTheme.surface.opacity(0.72), FMTheme.surface],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(height: 12)

            HStack(spacing: 0) {
                ForEach(items) { item in
                    let selected = selection == item.id
                    Button {
                        selection = item.id
                    } label: {
                        VStack(spacing: 3) {
                            ZStack(alignment: .topTrailing) {
                                Image(systemName: item.icon)
                                    .font(.system(size: 23, weight: selected ? .semibold : .regular))
                                if let badge = item.badge, badge > 0 {
                                    Text("\(badge)")
                                        .font(.system(size: 10, weight: .bold, design: .monospaced))
                                        .foregroundStyle(.white)
                                        .padding(.horizontal, 4)
                                        .frame(minWidth: 16, minHeight: 16)
                                        .background(FMTheme.neg)
                                        .clipShape(Capsule())
                                        .overlay(Capsule().stroke(FMTheme.surface, lineWidth: 2))
                                        .offset(x: 8, y: -6)
                                }
                            }
                            Text(item.label)
                                .font(.system(size: 11, weight: selected ? .bold : .semibold))
                                .tracking(-0.1)
                        }
                        .foregroundStyle(selected ? FMTheme.brand : FMTheme.ink4)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 6)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 6)
            .frame(height: 62)
            .background(FMTheme.surface)
            .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .stroke(FMTheme.line, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.10), radius: 16, y: 8)
            .padding(.horizontal, 14)
            .padding(.bottom, 26)
        }
        .background(Color.clear)
    }
}

// MARK: - Stat tile

struct FMStatTile: View {
    let label: String
    let value: String
    var icon: String? = nil
    var accent: Color = FMTheme.brand
    var tint: Color = FMTheme.brandTint
    var subtitle: String? = nil

    var body: some View {
        FMCard {
            if let icon {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundStyle(accent)
                    .frame(width: 36, height: 36)
                    .background(tint)
                    .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                    .padding(.bottom, 8)
            }
            Text(value)
                .font(.system(size: 22, weight: .bold, design: .monospaced))
                .foregroundStyle(FMTheme.ink)
            Text(label)
                .font(.system(size: 12.5, weight: .semibold))
                .foregroundStyle(FMTheme.ink3)
            if let subtitle {
                Text(subtitle)
                    .font(.system(size: 11))
                    .foregroundStyle(FMTheme.ink4)
                    .padding(.top, 2)
            }
        }
    }
}

// MARK: - Mini bar chart

struct FMMiniBarChart: View {
    let data: [CGFloat]
    var barColor: Color = FMTheme.brand
    var height: CGFloat = 52

    var body: some View {
        let maxVal = max(data.max() ?? 1, 1)
        HStack(alignment: .bottom, spacing: 4) {
            ForEach(Array(data.enumerated()), id: \.offset) { index, value in
                RoundedRectangle(cornerRadius: 4, style: .continuous)
                    .fill(index == data.count - 1 ? barColor : barColor.opacity(0.35))
                    .frame(maxWidth: .infinity)
                    .frame(height: max(height * (value / maxVal), height * 0.08))
            }
        }
        .frame(height: height)
    }
}

// MARK: - Text field

struct FMTextField: View {
    let label: String
    @Binding var text: String
    var icon: String = "textformat"
    var placeholder: String = ""
    var secure: Bool = false
    var keyboard: UIKeyboardType = .default
    var filter: ((String) -> String)? = nil
    var maxPhoneDigits: Int = 10

    var body: some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(FMTheme.ink2)
            HStack(spacing: 10) {
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundStyle(FMTheme.ink4)
                if secure {
                    SecureField(
                        "",
                        text: binding,
                        prompt: Text(placeholder).foregroundStyle(FMTheme.ink4)
                    )
                        .keyboardType(effectiveKeyboard)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled(false)
                } else {
                    TextField(
                        "",
                        text: binding,
                        prompt: Text(placeholder).foregroundStyle(FMTheme.ink4)
                    )
                        .keyboardType(effectiveKeyboard)
                        .textInputAutocapitalization(textAutocapitalization)
                        .autocorrectionDisabled(false)
                }
            }
            .font(.system(size: 15))
            .foregroundStyle(FMTheme.ink)
            .padding(.horizontal, 14)
            .frame(height: 50)
            .background(FMTheme.surface)
            .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 13).stroke(FMTheme.line2, lineWidth: 1.5))
        }
        .onChange(of: text) { _, newValue in
            guard let filter = resolvedFilter else { return }
            let filtered = filter(newValue)
            if filtered != newValue {
                text = filtered
            }
        }
    }

    private var effectiveKeyboard: UIKeyboardType {
        // ASCII keyboard keeps suggestions in English (avoids Hindi/other script autocorrect).
        keyboard == .default ? .asciiCapable : keyboard
    }

    private var textAutocapitalization: TextInputAutocapitalization {
        switch keyboard {
        case .emailAddress, .phonePad, .decimalPad, .numberPad, .asciiCapable:
            return .never
        default:
            return .sentences
        }
    }

    private var resolvedFilter: ((String) -> String)? {
        if let filter { return filter }
        switch keyboard {
        case .phonePad:
            return { FieldValidators.digitsOnly($0, maxLength: maxPhoneDigits) }
        case .decimalPad:
            return FieldValidators.decimalOnly
        default:
            return nil
        }
    }

    private var binding: Binding<String> {
        guard let filter = resolvedFilter else { return $text }
        return Binding(
            get: { text },
            set: { text = filter($0) }
        )
    }
}

// MARK: - Floating cart bar

struct FMFloatingCartBar: View {
    let itemCount: Int
    let totalLabel: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                HStack(spacing: 10) {
                    Text("\(itemCount)")
                        .font(.system(size: 14, weight: .bold, design: .monospaced))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 9)
                        .padding(.vertical, 3)
                        .background(.white.opacity(0.16))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    Text("View cart")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(.white)
                }
                Spacer()
                HStack(spacing: 8) {
                    Text(totalLabel)
                        .font(.system(size: 14, weight: .bold, design: .monospaced))
                        .foregroundStyle(.white)
                    Text("→")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(.white)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(FMTheme.brand)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal, 18)
            .frame(height: 56)
            .background(FMTheme.ink)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Order timeline

struct FMOrderTimeline: View {
    let steps: [TimelineStep]

    struct TimelineStep: Identifiable {
        let id = UUID()
        let label: String
        let time: String
        let state: StepState
    }

    enum StepState { case done, active, todo }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            ForEach(Array(steps.enumerated()), id: \.element.id) { idx, step in
                HStack(alignment: .top, spacing: 12) {
                    VStack(spacing: 0) {
                        circle(for: step.state)
                        if idx < steps.count - 1 {
                            Rectangle()
                                .fill(step.state == .done ? FMTheme.pos.opacity(0.4) : FMTheme.line2)
                                .frame(width: 2, height: 28)
                        }
                    }
                    VStack(alignment: .leading, spacing: 2) {
                        Text(step.label)
                            .font(.system(size: 13.5, weight: step.state == .todo ? .medium : .semibold))
                            .foregroundStyle(step.state == .todo ? FMTheme.ink4 : FMTheme.ink)
                        Text(step.time)
                            .font(.system(size: 12))
                            .foregroundStyle(FMTheme.ink3)
                    }
                    .padding(.bottom, idx < steps.count - 1 ? 12 : 0)
                }
            }
        }
    }

    @ViewBuilder
    private func circle(for state: StepState) -> some View {
        ZStack {
            Circle()
                .fill(state == .done ? FMTheme.pos : state == .active ? FMTheme.brand : FMTheme.surface3)
                .frame(width: 20, height: 20)
            if state == .done {
                Image(systemName: "checkmark")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundStyle(.white)
            }
        }
        .shadow(color: state == .active ? FMTheme.brand.opacity(0.25) : .clear, radius: 6)
    }
}

// MARK: - Screen scaffold

struct FMScreen<Content: View>: View {
    var showNav: Bool = false
    @ViewBuilder var content: () -> Content

    var body: some View {
        ScrollView {
            content()
                .padding(.bottom, showNav ? 100 : 24)
        }
        .background(FMTheme.bg)
    }
}

// MARK: - Spacing

enum FMSpacing {
    static let screenH: CGFloat = 16
    static let itemGap: CGFloat = 14
    static let fieldGap: CGFloat = 12
}

// MARK: - Banners

struct FMInfoBanner: View {
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "info.circle.fill")
                .font(.system(size: 16))
                .foregroundStyle(FMTheme.brand)
            Text(text)
                .font(.system(size: 13))
                .foregroundStyle(FMTheme.ink2)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(FMTheme.brandTint)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(FMTheme.brand.opacity(0.2)))
    }
}

struct FMErrorBanner: View {
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 16))
                .foregroundStyle(FMTheme.neg)
            Text(text)
                .font(.system(size: 13))
                .foregroundStyle(FMTheme.neg)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(FMTheme.negTint)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

// MARK: - Empty / loading

struct FMEmptyState: View {
    var icon: String = "tray"
    let title: String
    var message: String? = nil
    var actionTitle: String? = nil
    var onAction: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 36))
                .foregroundStyle(FMTheme.ink4)
            Text(title)
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(FMTheme.ink)
            if let message {
                Text(message)
                    .font(.system(size: 13))
                    .foregroundStyle(FMTheme.ink3)
                    .multilineTextAlignment(.center)
            }
            if let actionTitle, let onAction {
                FMButton(title: actionTitle, variant: .soft, fullWidth: false, action: onAction)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 28)
    }
}

struct FMEmptyStateHero: View {
    let icon: String
    let title: String
    let message: String
    var actionTitle: String? = nil
    var onAction: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 8) {
            ZStack(alignment: .bottomTrailing) {
                Image(systemName: icon)
                    .font(.system(size: 46))
                    .foregroundStyle(FMTheme.brand)
                    .frame(width: 110, height: 110)
                    .background(FMTheme.brandTint)
                    .clipShape(RoundedRectangle(cornerRadius: 32, style: .continuous))
                Image(systemName: "plus")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(width: 42, height: 42)
                    .background(FMTheme.pos)
                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(FMTheme.bg, lineWidth: 3))
                    .offset(x: 8, y: 8)
            }
            Text(title)
                .font(.system(size: 19, weight: .heavy))
                .foregroundStyle(FMTheme.ink)
                .padding(.top, 16)
            Text(message)
                .font(.system(size: 14.5))
                .foregroundStyle(FMTheme.ink3)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 8)
            if let actionTitle, let onAction {
                FMButton(title: actionTitle, variant: .primary, fullWidth: false, action: onAction)
                    .padding(.top, 14)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 36)
        .padding(.horizontal, 28)
    }
}

struct FMSkeletonBlock: View {
    var width: CGFloat? = nil
    var height: CGFloat = 14
    @State private var pulse = false

    var body: some View {
        RoundedRectangle(cornerRadius: 8, style: .continuous)
            .fill(FMTheme.surface3.opacity(pulse ? 0.85 : 0.45))
            .frame(width: width, height: height)
            .frame(maxWidth: width == nil ? .infinity : nil, alignment: .leading)
            .onAppear {
                withAnimation(.easeInOut(duration: 0.9).repeatForever(autoreverses: true)) {
                    pulse = true
                }
            }
    }
}

struct FMSkeletonListScreen: View {
    var body: some View {
        VStack(spacing: 16) {
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 8) {
                    FMSkeletonBlock(width: 130, height: 13)
                    FMSkeletonBlock(width: 170, height: 22)
                }
                Spacer()
                FMSkeletonBlock(width: 42, height: 42)
                FMSkeletonBlock(width: 42, height: 42)
            }
            FMSkeletonBlock(height: 130)
            HStack(spacing: 11) {
                FMSkeletonBlock(height: 92)
                FMSkeletonBlock(height: 92)
            }
            FMCard(padding: 6) {
                ForEach(0..<3, id: \.self) { i in
                    HStack(spacing: 14) {
                        FMSkeletonBlock(width: 40, height: 40)
                        VStack(alignment: .leading, spacing: 7) {
                            FMSkeletonBlock(width: 140, height: 14)
                            FMSkeletonBlock(width: 90, height: 11)
                        }
                        Spacer()
                        FMSkeletonBlock(width: 56, height: 20)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 13)
                    if i < 2 { Divider() }
                }
            }
        }
    }
}

struct FMErrorScreen: View {
    let title: String
    let message: String
    var primaryAction: String = "Try again"
    var onPrimaryAction: () -> Void
    var secondaryAction: String? = nil
    var onSecondaryAction: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 0) {
            Image(systemName: "wifi.slash")
                .font(.system(size: 50))
                .foregroundStyle(FMTheme.neg)
                .frame(width: 110, height: 110)
                .background(FMTheme.neg.opacity(0.12))
                .clipShape(RoundedRectangle(cornerRadius: 32, style: .continuous))
            Text(title)
                .font(.system(size: 23, weight: .heavy))
                .foregroundStyle(FMTheme.ink)
                .padding(.top, 26)
            Text(message)
                .font(.system(size: 15))
                .foregroundStyle(FMTheme.ink3)
                .multilineTextAlignment(.center)
                .padding(.top, 10)
            VStack(spacing: 11) {
                FMButton(title: primaryAction, action: onPrimaryAction)
                if let secondaryAction, let onSecondaryAction {
                    FMButton(title: secondaryAction, variant: .outline, action: onSecondaryAction)
                }
            }
            .padding(.top, 30)
        }
        .padding(.horizontal, 36)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct FMLoadingState: View {
    var message: String = "Loading…"

    var body: some View {
        VStack(spacing: 12) {
            ProgressView()
            Text(message)
                .font(.system(size: 13))
                .foregroundStyle(FMTheme.ink3)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 24)
    }
}

// MARK: - Dialog

struct FMDialog<Content: View>: View {
    let title: String
    let confirmLabel: String
    let dismissLabel: String
    var confirmBusy: Bool = false
    let onConfirm: () -> Void
    let onDismiss: () -> Void
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(spacing: 0) {
            Text(title)
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(FMTheme.ink)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.bottom, 12)

            content()
                .frame(maxWidth: .infinity, alignment: .leading)

            HStack(spacing: 12) {
                Button(dismissLabel, action: onDismiss)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(FMTheme.ink3)
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .disabled(confirmBusy)

                Button(confirmBusy ? "Saving…" : confirmLabel, action: onConfirm)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .background(FMTheme.brand)
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    .disabled(confirmBusy)
            }
            .padding(.top, 20)
        }
        .padding(22)
        .background(FMTheme.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .shadow(color: .black.opacity(0.15), radius: 24, y: 8)
        .padding(.horizontal, 28)
    }
}
