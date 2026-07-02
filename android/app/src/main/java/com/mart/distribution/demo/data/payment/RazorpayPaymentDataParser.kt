package com.mart.distribution.demo.data.payment

import com.razorpay.PaymentData
import org.json.JSONObject

data class ParsedRazorpayPayment(
    val paymentId: String?,
    val orderId: String?,
    val signature: String?,
)

/**
 * Razorpay's Android SDK sometimes returns null order/signature on [PaymentData]
 * even when checkout used a server-generated order_id. Parse every known field.
 */
object RazorpayPaymentDataParser {
    fun fromSuccess(
        paymentIdArg: String?,
        paymentData: PaymentData?,
    ): ParsedRazorpayPayment {
        var paymentId = paymentIdArg?.trim()?.takeIf { it.isNotEmpty() }
        var orderId: String? = null
        var signature: String? = null

        if (paymentData != null) {
            paymentId = paymentId ?: paymentData.paymentId?.trim()?.takeIf { it.isNotEmpty() }
            orderId = paymentData.orderId?.trim()?.takeIf { it.isNotEmpty() }
            signature = paymentData.signature?.trim()?.takeIf { it.isNotEmpty() }
            mergeJsonPayload(paymentData)?.let { json ->
                orderId = orderId ?: json.optString("razorpay_order_id").takeIf { it.isNotEmpty() }
                paymentId = paymentId ?: json.optString("razorpay_payment_id").takeIf { it.isNotEmpty() }
                signature = signature ?: json.optString("razorpay_signature").takeIf { it.isNotEmpty() }
            }
        }

        return ParsedRazorpayPayment(
            paymentId = paymentId,
            orderId = orderId,
            signature = signature,
        )
    }

    fun fromError(paymentData: PaymentData?): ParsedRazorpayPayment {
        if (paymentData == null) {
            return ParsedRazorpayPayment(null, null, null)
        }
        var paymentId = paymentData.paymentId?.trim()?.takeIf { it.isNotEmpty() }
        var orderId = paymentData.orderId?.trim()?.takeIf { it.isNotEmpty() }
        var signature = paymentData.signature?.trim()?.takeIf { it.isNotEmpty() }
        mergeJsonPayload(paymentData)?.let { json ->
            orderId = orderId ?: json.optString("razorpay_order_id").takeIf { it.isNotEmpty() }
            paymentId = paymentId ?: json.optString("razorpay_payment_id").takeIf { it.isNotEmpty() }
            signature = signature ?: json.optString("razorpay_signature").takeIf { it.isNotEmpty() }
        }
        return ParsedRazorpayPayment(paymentId, orderId, signature)
    }

    private fun mergeJsonPayload(paymentData: PaymentData): JSONObject? {
        val raw =
            runCatching {
                paymentData.javaClass.getMethod("getData").invoke(paymentData) as? String
            }.getOrNull()
        if (raw.isNullOrBlank()) return null
        return runCatching { JSONObject(raw) }.getOrNull()
    }
}
