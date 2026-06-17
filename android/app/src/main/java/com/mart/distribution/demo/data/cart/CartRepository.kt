package com.mart.distribution.demo.data.cart

import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.catalogUnitPrice
import com.mart.distribution.demo.data.api.dto.toDoubleFromApiOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartLine(
    val productId: String,
    val productName: String,
    val quantity: Int,
    /** Catalog base price when the line was added — UI estimate only; checkout uses server pricing. */
    val referenceUnitPrice: Double? = null,
    val imageUrl: String? = null,
    val brandLogoUrl: String? = null,
)

class CartRepository {
    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    fun addOne(product: ProductDto, role: String = "SHOPKEEPER") {
        val unit = product.catalogUnitPrice(role)
        _lines.update { current ->
            val i = current.indexOfFirst { it.productId == product.id }
            if (i < 0) {
                current +
                    CartLine(
                        productId = product.id,
                        productName = product.name,
                        quantity = 1,
                        referenceUnitPrice = unit,
                        imageUrl = product.imageUrl,
                        brandLogoUrl = product.brand?.logoUrl,
                    )
            } else {
                current.mapIndexed { idx, line ->
                    if (idx == i) line.copy(quantity = line.quantity + 1) else line
                }
            }
        }
    }

    fun setQuantity(
        productId: String,
        quantity: Int,
    ) {
        if (quantity <= 0) {
            remove(productId)
            return
        }
        _lines.update { current ->
            current.map {
                if (it.productId == productId) it.copy(quantity = quantity) else it
            }
        }
    }

    /** Sets total quantity for a SKU, adding the line if missing. */
    fun setLineQuantity(
        product: ProductDto,
        quantity: Int,
        role: String = "SHOPKEEPER",
    ) {
        if (quantity <= 0) {
            remove(product.id)
            return
        }
        val unit = product.catalogUnitPrice(role)
        _lines.update { current ->
            val i = current.indexOfFirst { it.productId == product.id }
            if (i < 0) {
                current +
                    CartLine(
                        productId = product.id,
                        productName = product.name,
                        quantity = quantity,
                        referenceUnitPrice = unit,
                        imageUrl = product.imageUrl,
                        brandLogoUrl = product.brand?.logoUrl,
                    )
            } else {
                current.mapIndexed { idx, line ->
                    if (idx == i) line.copy(quantity = quantity) else line
                }
            }
        }
    }

    fun remove(productId: String) {
        _lines.update { it.filter { line -> line.productId != productId } }
    }

    fun clear() {
        _lines.value = emptyList()
    }
}
