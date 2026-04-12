package com.mart.distribution.demo.ui.util

fun formatDecimal(value: Any?): String =
    when (value) {
        null -> "—"
        is String -> value
        is Number -> value.toString()
        else -> value.toString()
    }
