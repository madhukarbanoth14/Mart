package com.mart.distribution.demo.util

private val AREA_NAME_ALIASES =
    mapOf(
        "warrangal" to "Warangal",
        "wrangal" to "Warangal",
        "warangal" to "Warangal",
        "warangal urban" to "Warangal Urban",
        "warangal rural" to "Warangal Rural",
    )

fun normalizeAreaName(raw: String): String {
    val trimmed = raw.trim().replace(Regex("\\s+"), " ")
    if (trimmed.isEmpty()) return trimmed
    return AREA_NAME_ALIASES[trimmed.lowercase()] ?: trimmed
}
