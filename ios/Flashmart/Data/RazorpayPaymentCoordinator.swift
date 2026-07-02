import Foundation
import UIKit
import Observation

#if canImport(Razorpay)
import Razorpay
#endif

struct RazorpayPaymentResult: Equatable {
    let success: Bool
    let paymentId: String?
    let orderId: String?
    let signature: String?
    let error: String?
}

@Observable
@MainActor
final class RazorpayPaymentCoordinator: NSObject {
    var pendingCheckout: PendingRazorpayCheckout?
    var lastResult: RazorpayPaymentResult?

    #if canImport(Razorpay)
    private var checkout: RazorpayCheckout?
    #endif

    var isSDKAvailable: Bool {
        #if canImport(Razorpay)
        return true
        #else
        return false
        #endif
    }

    func present(checkout config: PendingRazorpayCheckout) {
        pendingCheckout = config
        openCheckout(config)
    }

    func clearPending() {
        pendingCheckout = nil
    }

    func clearLastResult() {
        lastResult = nil
    }

    private func openCheckout(_ config: PendingRazorpayCheckout) {
        #if canImport(Razorpay)
        checkout = RazorpayCheckout.initWithKey(config.keyId, andDelegateWithData: self)
        let options = RazorpayCheckoutOptions.buildOptions(config: config)
        guard let controller = topViewController() else {
            lastResult = RazorpayPaymentResult(
                success: false,
                paymentId: nil,
                orderId: config.gatewayOrderId,
                signature: nil,
                error: "Unable to present Razorpay checkout"
            )
            return
        }
        checkout?.open(options, displayController: controller)
        #else
        lastResult = RazorpayPaymentResult(
            success: false,
            paymentId: nil,
            orderId: config.gatewayOrderId,
            signature: nil,
            error: "Razorpay SDK not linked. Run `pod install` in Mart/ios and open Flashmart.xcworkspace."
        )
        #endif
    }

    private func topViewController() -> UIViewController? {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let root = scene.windows.first(where: { $0.isKeyWindow })?.rootViewController else {
            return nil
        }
        var top = root
        while let presented = top.presentedViewController { top = presented }
        return top
    }
}

#if canImport(Razorpay)
extension RazorpayPaymentCoordinator: RazorpayPaymentCompletionProtocolWithData {
    nonisolated func onPaymentSuccess(_ paymentId: String, andData response: [AnyHashable: Any]?) {
        let orderId = response?["razorpay_order_id"] as? String
        let signature = response?["razorpay_signature"] as? String
        Task { @MainActor in
            lastResult = RazorpayPaymentResult(success: true, paymentId: paymentId, orderId: orderId, signature: signature, error: nil)
            pendingCheckout = nil
        }
    }

    nonisolated func onPaymentError(_ code: Int32, description str: String, andData response: [AnyHashable: Any]?) {
        Task { @MainActor in
            lastResult = RazorpayPaymentResult(
                success: false,
                paymentId: response?["razorpay_payment_id"] as? String,
                orderId: response?["razorpay_order_id"] as? String,
                signature: nil,
                error: "Razorpay error \(code): \(str)"
            )
            pendingCheckout = nil
        }
    }
}
#endif
