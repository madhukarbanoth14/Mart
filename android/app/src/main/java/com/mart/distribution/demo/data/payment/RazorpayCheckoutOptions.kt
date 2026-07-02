package com.mart.distribution.demo.data.payment

import org.json.JSONObject

/**
 * Builds Razorpay Standard Checkout options.
 * Card / UPI / netbanking / wallet / EMI / pay-later fields are collected inside the Razorpay SDK UI.
 */
object RazorpayCheckoutOptions {
    fun toRazorpayMethod(method: String?): String? =
        when (method?.lowercase()) {
            "card" -> "card"
            "upi" -> "upi"
            "netbanking" -> "netbanking"
            "wallet" -> "wallet"
            "emi" -> "emi"
            "paylater", "pay_later" -> "paylater"
            else -> null
        }

    fun isRazorpayOnlineMethod(method: String): Boolean = toRazorpayMethod(method) != null

    fun build(
        appOrderId: String,
        gatewayOrderId: String,
        amountPaise: Int,
        currency: String,
        userEmail: String?,
        userPhone: String?,
        paymentMethod: String?,
    ): JSONObject {
        val options =
            JSONObject().apply {
                put("name", "FlashMart")
                put("description", "Order payment")
                put("currency", currency)
                put("amount", amountPaise)
                put("order_id", gatewayOrderId)
                put("theme.color", "#2F48D4")
                put(
                    "retry",
                    JSONObject().apply {
                        put("enabled", true)
                        put("max_count", 4)
                    },
                )
                put("notes", JSONObject().apply { put("appOrderId", appOrderId) })
            }

        val prefill = JSONObject()
        userEmail?.trim()?.takeIf { it.isNotEmpty() }?.let { prefill.put("email", it) }
        userPhone
            ?.filter(Char::isDigit)
            ?.takeIf { it.length >= 10 }
            ?.let { prefill.put("contact", it.takeLast(10)) }
        if (prefill.length() > 0) {
            options.put("prefill", prefill)
        }

        toRazorpayMethod(paymentMethod)?.let { gatewayMethod ->
            // Pre-select tab inside Razorpay (requires email/contact prefill per Razorpay docs).
            if (prefill.length() > 0) {
                options.put("method", JSONObject().put(gatewayMethod, true))
            }
        }

        return options
    }
}
