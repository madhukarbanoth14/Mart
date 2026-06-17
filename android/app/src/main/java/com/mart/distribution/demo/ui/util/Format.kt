package com.mart.distribution.demo.ui.util

fun formatDecimal(value: Any?): String =
    when (value) {
        null -> "—"
        is String -> value.toDoubleOrNull()?.let { "₹" + "%.2f".format(it) } ?: value
        is Number -> "₹" + "%.2f".format(value.toDouble())
        else -> value.toString()
    }
