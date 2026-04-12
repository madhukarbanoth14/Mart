package com.mart.distribution.demo.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.CreateOrderItemDto
import com.mart.distribution.demo.data.api.dto.CreateOrderRequest
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.cart.CartRepository
import com.mart.distribution.demo.data.session.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LoadState<out T> {
    data object Idle : LoadState<Nothing>()

    data object Loading : LoadState<Nothing>()

    data class Ok<T>(
        val data: T,
    ) : LoadState<T>()

    data class Err(
        val message: String,
    ) : LoadState<Nothing>()
}

data class MainUiState(
    val products: LoadState<List<ProductDto>> = LoadState.Idle,
    val orders: LoadState<List<OrderDto>> = LoadState.Idle,
    val placeOrderResult: String? = null,
    val placeOrderError: String? = null,
)

class MainViewModel(
    private val martApi: MartApi,
    private val sessionRepository: SessionRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _ui.asStateFlow()

    val cartLines = cartRepository.lines

    init {
        refreshAll()
    }

    fun refreshAll() {
        loadProducts()
        loadOrders()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _ui.update { it.copy(products = LoadState.Loading) }
            try {
                val list = martApi.products()
                _ui.update { it.copy(products = LoadState.Ok(list)) }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(products = LoadState.Err(e.message ?: "Failed to load products"))
                }
            }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _ui.update { it.copy(orders = LoadState.Loading) }
            try {
                val list = martApi.orders()
                _ui.update { it.copy(orders = LoadState.Ok(list)) }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(orders = LoadState.Err(e.message ?: "Failed to load orders"))
                }
            }
        }
    }

    fun clearOrderFeedback() {
        _ui.update { it.copy(placeOrderResult = null, placeOrderError = null) }
    }

    fun placeOrderFromCart() {
        val lines = cartRepository.lines.value
        if (lines.isEmpty()) {
            _ui.update { it.copy(placeOrderError = "Cart is empty") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(placeOrderError = null, placeOrderResult = null) }
            try {
                val body =
                    CreateOrderRequest(
                        items =
                            lines.map {
                                CreateOrderItemDto(
                                    productId = it.productId,
                                    quantity = it.quantity,
                                )
                            },
                    )
                martApi.createOrder(body)
                cartRepository.clear()
                _ui.update {
                    it.copy(placeOrderResult = "Order placed successfully")
                }
                loadOrders()
            } catch (e: Exception) {
                _ui.update {
                    it.copy(placeOrderError = e.message ?: "Order failed")
                }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.clear()
            onDone()
        }
    }
}
