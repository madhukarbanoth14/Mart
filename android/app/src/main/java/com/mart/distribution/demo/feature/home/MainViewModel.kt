package com.mart.distribution.demo.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.CreateOrderItemDto
import com.mart.distribution.demo.data.api.dto.CreateOrderRequest
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.StockRowDto
import com.mart.distribution.demo.data.api.dto.UserRowDto
import com.mart.distribution.demo.data.cart.CartRepository
import com.mart.distribution.demo.data.session.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val stock: LoadState<List<StockRowDto>> = LoadState.Idle,
    val users: LoadState<List<UserRowDto>> = LoadState.Idle,
    val placeOrderResult: String? = null,
    val placeOrderError: String? = null,
    val placedOrder: OrderDto? = null,
)

class MainViewModel(
    private val martApi: MartApi,
    private val sessionRepository: SessionRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _ui.asStateFlow()

    val cartLines = cartRepository.lines

    fun refreshForRole() {
        viewModelScope.launch {
            val user = sessionRepository.sessionUserFlow.first() ?: return@launch
            loadProducts()
            loadOrders()
            when (user.role.uppercase()) {
                "DEALER" -> loadStock()
                "ADMIN", "EMPLOYEE" -> loadUsers()
                else -> {
                    _ui.update {
                        it.copy(
                            stock = LoadState.Idle,
                            users = LoadState.Idle,
                        )
                    }
                }
            }
        }
    }

    fun refreshAll() {
        refreshForRole()
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

    fun loadStock() {
        viewModelScope.launch {
            _ui.update { it.copy(stock = LoadState.Loading) }
            try {
                val list = martApi.stock()
                _ui.update { it.copy(stock = LoadState.Ok(list)) }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(stock = LoadState.Err(e.message ?: "Stock unavailable"))
                }
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _ui.update { it.copy(users = LoadState.Loading) }
            try {
                val list = martApi.users()
                _ui.update { it.copy(users = LoadState.Ok(list)) }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(users = LoadState.Err(e.message ?: "Users unavailable"))
                }
            }
        }
    }

    fun clearOrderFeedback() {
        _ui.update {
            it.copy(
                placeOrderResult = null,
                placeOrderError = null,
                placedOrder = null,
            )
        }
    }

    fun placeOrderFromCart() {
        val lines = cartRepository.lines.value
        if (lines.isEmpty()) {
            _ui.update { it.copy(placeOrderError = "Cart is empty") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(placeOrderError = null, placeOrderResult = null, placedOrder = null) }
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
                val created = martApi.createOrder(body)
                cartRepository.clear()
                _ui.update {
                    it.copy(placedOrder = created)
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

    fun addToCart(product: ProductDto) {
        cartRepository.addOne(product)
    }

    fun setCartQuantity(
        productId: String,
        quantity: Int,
    ) {
        cartRepository.setQuantity(productId, quantity)
    }

    fun removeCartLine(productId: String) {
        cartRepository.remove(productId)
    }
}
