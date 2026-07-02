import Foundation

enum RazorpayCheckoutOptions {
    static func gatewayMethod(for method: String?) -> String? {
        switch method?.lowercased() {
        case "card": return "card"
        case "upi": return "upi"
        case "netbanking": return "netbanking"
        case "wallet": return "wallet"
        case "emi": return "emi"
        case "paylater", "pay_later": return "paylater"
        default: return nil
        }
    }

    static func isOnlineGatewayMethod(_ method: String) -> Bool {
        gatewayMethod(for: method) != nil
    }

    static func buildOptions(config: PendingRazorpayCheckout) -> [String: Any] {
        var prefill: [String: String] = [:]
        let email = config.userEmail.trimmingCharacters(in: .whitespacesAndNewlines)
        if !email.isEmpty { prefill["email"] = email }
        if let phone = config.userPhone?.filter({ $0.isNumber }), phone.count >= 10 {
            prefill["contact"] = String(phone.suffix(10))
        }

        var options: [String: Any] = [
            "amount": config.amountPaise,
            "currency": config.currency,
            "order_id": config.gatewayOrderId,
            "name": "Flashmart",
            "description": "Order payment",
            "notes": ["appOrderId": config.appOrderId],
            "theme": ["color": "#2F48D4"],
            "retry": ["enabled": true, "max_count": 4],
        ]
        if !prefill.isEmpty {
            options["prefill"] = prefill
        }
        if let gateway = gatewayMethod(for: config.paymentMethod), !prefill.isEmpty {
            options["method"] = [gateway: true]
        }
        return options
    }
}

struct PaymentMethodOption: Identifiable {
    let id: String
    let label: String
    let subtitle: String
    let icon: String
    let razorpayGateway: Bool

    func isEnabled(useRazorpayCheckout: Bool) -> Bool {
        switch id {
        case "cod": return true
        default:
            if useRazorpayCheckout && razorpayGateway { return true }
            if !useRazorpayCheckout && (id == "card" || id == "upi") { return true }
            return false
        }
    }
}

func paymentMethodOptions(useRazorpayCheckout: Bool) -> [PaymentMethodOption] {
    [
        PaymentMethodOption(
            id: "card",
            label: "Credit / Debit Card",
            subtitle: useRazorpayCheckout
                ? "Enter card number, expiry & CVV in Razorpay"
                : "Demo card form",
            icon: "creditcard",
            razorpayGateway: true
        ),
        PaymentMethodOption(
            id: "upi",
            label: "UPI",
            subtitle: useRazorpayCheckout
                ? "GPay, PhonePe, Paytm & UPI ID in Razorpay"
                : "Demo UPI confirm",
            icon: "indianrupeesign.circle",
            razorpayGateway: true
        ),
        PaymentMethodOption(
            id: "netbanking",
            label: "Net Banking",
            subtitle: "Select your bank in Razorpay checkout",
            icon: "building.columns",
            razorpayGateway: true
        ),
        PaymentMethodOption(
            id: "wallet",
            label: "Wallets",
            subtitle: "Paytm, PhonePe wallet, Mobikwik & more",
            icon: "wallet.pass",
            razorpayGateway: true
        ),
        PaymentMethodOption(
            id: "emi",
            label: "EMI",
            subtitle: "Card EMI & cardless EMI via Razorpay",
            icon: "calendar",
            razorpayGateway: true
        ),
        PaymentMethodOption(
            id: "paylater",
            label: "Pay Later",
            subtitle: "LazyPay, Simpl & other pay-later options",
            icon: "clock",
            razorpayGateway: true
        ),
        PaymentMethodOption(
            id: "cod",
            label: "Pay on delivery",
            subtitle: "Cash or UPI to dealer when order arrives",
            icon: "shippingbox",
            razorpayGateway: false
        ),
    ]
}
