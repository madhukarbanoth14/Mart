package com.mart.distribution.demo.data.onboarding

object BusinessDocumentTypes {
    data class Slot(
        val type: String,
        val label: String,
    )

    val all: List<Slot> =
        listOf(
            Slot("AADHAAR", "Aadhaar Card"),
            Slot("PAN", "PAN Card"),
            Slot("GST", "GST Certificate"),
            Slot("TRADE_LICENSE", "Trade License"),
        )

    fun labelFor(type: String): String =
        all.firstOrNull { it.type == type }?.label ?: type
}
