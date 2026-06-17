package com.mart.distribution.demo.feature.home

import com.mart.distribution.demo.data.api.dto.ProductDto

fun filterCatalogProducts(
    all: List<ProductDto>,
    search: String?,
    brandId: String?,
    shelf: String?,
): List<ProductDto> {
    var list = all
    search?.trim()?.takeIf { it.isNotEmpty() }?.let { q ->
        val ql = q.lowercase()
        list =
            list.filter {
                it.name.lowercase().contains(ql) ||
                    it.brand?.name?.lowercase()?.contains(ql) == true ||
                    it.sku?.lowercase()?.contains(ql) == true
            }
    }
    brandId?.trim()?.takeIf { it.isNotEmpty() }?.let { bid ->
        list = list.filter { it.brandId == bid || it.brand?.id == bid }
    }
    shelf?.trim()?.takeIf { it.isNotEmpty() }?.let { sh ->
        list = list.filter { it.shelf.equals(sh, ignoreCase = true) }
    }
    return list
}
