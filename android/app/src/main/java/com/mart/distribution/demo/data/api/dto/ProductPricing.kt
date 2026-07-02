package com.mart.distribution.demo.data.api.dto

import com.mart.distribution.demo.data.cart.CartLine
import kotlin.math.roundToInt

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

data class OrderMath(
    val subtotal: Double,
    val discount: Double,
    val gst: Double,
    val total: Double,
)

fun ProductDto.lineMath(
    quantity: Int,
    role: String,
): OrderMath {
    val unit = catalogUnitPrice(role)
    val discPct = discountPercentForRole(role) / 100.0
    val lineSub = if (discPct < 1.0) unit / (1.0 - discPct) * quantity else unit * quantity
    val lineDisc = lineSub * discPct
    val taxable = lineSub - lineDisc
    val gstRate = (gstPercentage.toDoubleFromApiOrNull() ?: gstRate.toDoubleFromApiOrNull() ?: 18.0) / 100.0
    val lineGst = taxable * gstRate
    return OrderMath(
        subtotal = round2(lineSub),
        discount = round2(lineDisc),
        gst = round2(lineGst),
        total = round2(taxable + lineGst),
    )
}

fun List<CartLine>.cartMath(role: String): OrderMath {
    var sub = 0.0
    var disc = 0.0
    var gst = 0.0
    for (line in this) {
        val unit = line.referenceUnitPrice ?: continue
        val discPct = (line.discountPercent ?: if (role.equals("DEALER", ignoreCase = true)) 10.0 else 5.0) / 100.0
        val gstRate = (line.gstPercentage ?: 18.0) / 100.0
        val taxable = unit * line.quantity
        val lineSub = if (discPct < 1.0) taxable / (1.0 - discPct) else taxable
        val lineDisc = lineSub - taxable
        val lineGst = taxable * gstRate
        sub += lineSub
        disc += lineDisc
        gst += lineGst
    }
    return OrderMath(
        subtotal = round2(sub),
        discount = round2(disc),
        gst = round2(gst),
        total = round2(sub - disc + gst),
    )
}

private fun round2(v: Double): Double = (v * 100).roundToInt() / 100.0
