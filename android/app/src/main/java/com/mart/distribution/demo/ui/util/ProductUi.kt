package com.mart.distribution.demo.ui.util

import com.mart.distribution.demo.data.api.dto.ProductDto

fun shelfLabel(raw: String?): String {
    if (raw.isNullOrBlank()) return "Catalog"
    return raw.replace('_', ' ').lowercase().split(' ')
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

fun bulkShippingCaption(product: ProductDto): String? {
    if (product.bulkShippingFee == null) return null
    val min = product.bulkShippingMinQty?.takeIf { it > 0 } ?: 10
    return "Bulk ship ${formatDecimal(product.bulkShippingFee)} · min $min units"
}
