package com.mart.distribution.demo.data.api.dto

/** Parse Prisma Decimal JSON (number or string) for rough UI estimates only. */
fun Any?.toDoubleFromApiOrNull(): Double? =
    when (this) {
        is Number -> this.toDouble()
        is String -> this.toDoubleOrNull()
        else -> null
    }
