package com.mart.distribution.demo.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.cart.CartRepository
import com.mart.distribution.demo.feature.home.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderDetailViewModel(
    private val martApi: MartApi,
    private val sessionRepository: SessionRepository,
    private val cartRepository: CartRepository,
    private val orderId: String,
) : ViewModel() {
    companion object {
        const val ARG_ORDER_ID = "orderId"
    }

    private val _order = MutableStateFlow<LoadState<OrderDto>>(LoadState.Loading)
    val orderState: StateFlow<LoadState<OrderDto>> = _order.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _order.value = LoadState.Loading
            try {
                val list = martApi.orders()
                val found = list.find { it.id == orderId }
                _order.value =
                    if (found != null) {
                        LoadState.Ok(found)
                    } else {
                        LoadState.Err("Order not found")
                    }
            } catch (e: Exception) {
                _order.value = LoadState.Err(e.message ?: "Load failed")
            }
        }
    }

    fun clearMessages() {
        _actionMessage.value = null
        _actionError.value = null
    }

    fun mockPay() {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                val res = martApi.mockPayment(orderId)
                _actionMessage.value = res.message ?: "Payment ${res.status ?: "OK"}"
                load()
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Payment failed"
            }
        }
    }

    fun confirm() {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                martApi.confirmOrder(orderId)
                _actionMessage.value = "Order confirmed; stock updated"
                load()
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Confirm failed"
            }
        }
    }

    fun currentUserRole(): String? = sessionRepository.sessionUserFlow.value?.role

    // snapshot without collecting flow in VM init — use passed role from UI instead
}
