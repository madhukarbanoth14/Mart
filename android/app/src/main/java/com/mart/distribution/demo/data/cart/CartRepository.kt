package com.mart.distribution.demo.data.cart

import com.mart.distribution.demo.data.api.dto.ProductDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartLine(
    val productId: String,
    val productName: String,
    val quantity: Int,
)

class CartRepository {
    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    fun addOne(product: ProductDto) {
        _lines.update { current ->
            val i = current.indexOfFirst { it.productId == product.id }
            if (i < 0) {
                current + CartLine(product.id, product.name, 1)
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

    fun remove(productId: String) {
        _lines.update { it.filter { line -> line.productId != productId } }
    }

    fun clear() {
        _lines.value = emptyList()
    }
}
