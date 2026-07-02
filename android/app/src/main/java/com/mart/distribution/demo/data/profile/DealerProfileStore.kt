package com.mart.distribution.demo.data.profile

import com.mart.distribution.demo.data.session.SessionUser

/**
 * Dealer profile preferences (client-side; shown on profile & used at checkout labels).
 */
object DealerProfileStore {
    var displayName: String = ""
    var contactPhone: String = ""
    var businessName: String = ""
    var warehouseAddress: String = ""

    var gstin: String = ""
    var gstBusinessName: String = ""

    var accountHolderName: String = ""
    var bankName: String = ""
    var bankAccountNumber: String = ""
    var bankIfsc: String = ""
    var upiId: String = ""

    var orderAlerts: Boolean = true
    var stockAlerts: Boolean = true
    var paymentAlerts: Boolean = true

    fun hydrateFromUser(user: SessionUser) {
        if (displayName.isBlank()) displayName = user.name
        if (contactPhone.isBlank()) contactPhone = user.phone.orEmpty()
        if (businessName.isBlank()) businessName = user.name
        if (gstBusinessName.isBlank()) gstBusinessName = user.name
        if (accountHolderName.isBlank()) accountHolderName = user.name
    }

    fun paymentSummary(): String {
        val upi = upiId.trim()
        if (upi.isNotEmpty()) return upi
        val acct = bankAccountNumber.filter { it.isDigit() }
        if (acct.length >= 4) {
            val bank = bankName.trim().ifBlank { "Bank" }
            return "$bank ···· ${acct.takeLast(4)}"
        }
        return "Add bank or UPI"
    }

    fun addressSummary(): String {
        val addr = warehouseAddress.trim()
        return when {
            addr.length > 42 -> addr.take(42) + "…"
            addr.isNotEmpty() -> addr
            else -> "Add warehouse address"
        }
    }
}
