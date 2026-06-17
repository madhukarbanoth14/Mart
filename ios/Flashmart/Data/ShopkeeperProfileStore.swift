import Foundation

enum ShopkeeperProfileStore {
    static var storeName = "Shopkeeper One Store"
    static var storeAddress = "12, Main Market Road, Central Zone, Hyderabad — 500001"
    static var storePhone = "9000000004"
    static var gstin = "36AABCU9603R1Z5"
    static var gstBusinessName = "Shopkeeper One Trading Co."
    static var orderAlerts = true
    static var deliveryAlerts = true
    static var promoAlerts = false
    static let dummyCardMasked = "Visa ···· 4242"
    static let dummyUpiId = "shop1@upi"
}

enum FieldValidators {
    static func digitsOnly(_ input: String, maxLength: Int? = nil) -> String {
        var filtered = input.filter(\.isNumber)
        if let maxLength { filtered = String(filtered.prefix(maxLength)) }
        return filtered
    }

    static func decimalOnly(_ input: String) -> String {
        var result = ""
        var seenDot = false
        for ch in input {
            if ch.isNumber {
                result.append(ch)
            } else if ch == "." && !seenDot {
                seenDot = true
                result.append(ch)
            }
        }
        return result
    }

    static func phone(_ value: String) -> String? {
        let digits = digitsOnly(value)
        guard digits.count == 10 else { return "Enter a valid 10-digit mobile number" }
        guard let first = digits.first, "6789".contains(first) else {
            return "Mobile number must start with 6, 7, 8, or 9"
        }
        return nil
    }

    static func phoneOptional(_ value: String) -> String? {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return nil }
        return phone(trimmed)
    }

    static func normalizedPhone(_ value: String) -> String {
        digitsOnly(value, maxLength: 10)
    }

    static func email(_ value: String) -> String? {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        guard trimmed.contains("@"), trimmed.contains(".") else { return "Enter a valid email address" }
        return nil
    }

    static func required(_ value: String, label: String) -> String? {
        value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Enter \(label)" : nil
    }

    static func password(_ value: String, minLen: Int = 8) -> String? {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        guard trimmed.count >= minLen else { return "Password must be at least \(minLen) characters" }
        return nil
    }
}
