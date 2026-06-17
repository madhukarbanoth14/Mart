package com.mart.distribution.demo.data.api.dto

fun ProductDto.discountPercentForRole(role: String): Double {
    val isDealer = role.equals("DEALER", ignoreCase = true)
    val fromProduct =
        if (isDealer) {
            dealerDiscount.toDoubleFromApiOrNull()
        } else {
            shopkeeperDiscount.toDoubleFromApiOrNull()
        }
    return fromProduct ?: if (isDealer) 10.0 else 5.0
}

fun ProductDto.catalogUnitPrice(role: String): Double {
    val base = dealerPrice.toDoubleFromApiOrNull() ?: basePrice.toDoubleFromApiOrNull() ?: 0.0
    val disc = discountPercentForRole(role) / 100.0
    return base * (1.0 - disc)
}
