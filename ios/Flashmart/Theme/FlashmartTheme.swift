import SwiftUI

enum FMTheme {
    // Brand
    static let brand = Color(hex: 0x2F48D4)
    static let brand600 = Color(hex: 0x2839B8)
    static let brand700 = Color(hex: 0x1F2C93)
    static let brandTint = Color(hex: 0xEEF0FD)
    static let brandInk = Color(hex: 0x1A2470)

    // Surfaces
    static let bg = Color(hex: 0xEEF0F4)
    static let surface = Color.white
    static let surface2 = Color(hex: 0xF6F7F9)
    static let surface3 = Color(hex: 0xECEEF2)

    // Ink
    static let ink = Color(hex: 0x131722)
    static let ink2 = Color(hex: 0x3D4452)
    static let ink3 = Color(hex: 0x6B7280)
    static let ink4 = Color(hex: 0x9AA1AD)
    static let line = Color(hex: 0xE4E7EC)
    static let line2 = Color(hex: 0xD4D8E0)

    // Semantic
    static let pos = Color(hex: 0x0E9E6E)
    static let posTint = Color(hex: 0xE4F6EF)
    static let warn = Color(hex: 0xC97A16)
    static let warnTint = Color(hex: 0xFBF0DF)
    static let neg = Color(hex: 0xD6453F)
    static let negTint = Color(hex: 0xFBE9E8)

    static let inkSurface = Color(hex: 0x11152A)
    static let inkSurface2 = Color(hex: 0x1B2140)

    static let heroGradient = LinearGradient(
        colors: [brand, brand700],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let dealerHeroGradient = LinearGradient(
        colors: [pos, Color(hex: 0x086B4B)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let employeeHeroGradient = LinearGradient(
        colors: [warn, Color(hex: 0x92560A)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let splashGradient = LinearGradient(
        colors: [brand, brand700, Color(hex: 0x141A4D)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let cardRadius: CGFloat = 16
    static let heroRadius: CGFloat = 22
    static let buttonRadius: CGFloat = 14
    static let glyphRadius: CGFloat = 13

    static func inr(_ value: Double) -> String {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.currencyCode = "INR"
        f.currencySymbol = "₹"
        f.maximumFractionDigits = value.truncatingRemainder(dividingBy: 1) == 0 ? 0 : 2
        return f.string(from: NSNumber(value: value)) ?? "₹\(String(format: "%.2f", value))"
    }
}

extension Color {
    init(hex: UInt, alpha: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: alpha
        )
    }
}

enum UserRole: String, CaseIterable {
    case shopkeeper = "SHOPKEEPER"
    case dealer = "DEALER"
    case admin = "ADMIN"
    case employee = "EMPLOYEE"

    init(apiRole: String) {
        switch apiRole.uppercased() {
        case "SHOPKEEPER": self = .shopkeeper
        case "DEALER": self = .dealer
        case "EMPLOYEE": self = .employee
        default: self = .admin
        }
    }
}
