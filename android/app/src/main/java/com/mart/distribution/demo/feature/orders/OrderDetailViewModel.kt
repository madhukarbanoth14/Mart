package com.mart.distribution.demo.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.demo.DemoFlowRepository
import com.mart.distribution.demo.data.demo.LocalDemoMartStore
import com.mart.distribution.demo.data.session.SessionRepository
import com.mart.distribution.demo.feature.home.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

class OrderDetailViewModel(
    private val martApi: MartApi,
    private val demoFlowRepository: DemoFlowRepository,
    private val sessionRepository: SessionRepository,
    private val localDemoMartStore: LocalDemoMartStore,
    private val orderId: String,
) : ViewModel() {
    private fun actionErrorMessage(e: Exception): String {
        if (e is HttpException) {
            return when (e.code()) {
                400, 422 -> e.response()?.errorBody()?.string()?.ifBlank { "Invalid action." } ?: "Invalid action."
                401 -> "Session expired. Please login again."
                500 -> "Something went wrong. Please try again."
                else -> e.message()
            }
        }
        return e.message ?: "Action failed"
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
                val found =
                    if (sessionRepository.isLocalDemoMode()) {
                        val u =
                            sessionRepository.sessionUserFlow.first()
                                ?: run {
                                    _order.value = LoadState.Err("Not signed in")
                                    return@launch
                                }
                        localDemoMartStore.orderById(orderId)
                            ?: localDemoMartStore.ordersForActor(u.id, u.role).find { it.id == orderId }
                    } else {
                        martApi.orderById(orderId)
                    }
                _order.value =
                    if (found != null) {
                        LoadState.Ok(found)
                    } else {
                        LoadState.Err("Order not found")
                    }
            } catch (e: Exception) {
                _order.value = LoadState.Err(actionErrorMessage(e))
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
                val res =
                    if (sessionRepository.isLocalDemoMode()) {
                        localDemoMartStore.mockPayment(orderId)
                    } else {
                        martApi.mockPayment(orderId)
                    }
                demoFlowRepository.markMockPaid(orderId)
                _actionMessage.value = res.message ?: "Payment ${res.status ?: "OK"}"
                load()
            } catch (e: Exception) {
                _actionError.value = actionErrorMessage(e)
            }
        }
    }

    fun confirm() {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.confirmOrder(orderId)
                } else {
                    martApi.confirmOrder(orderId)
                }
                _actionMessage.value = "Order confirmed; stock updated"
                load()
            } catch (e: Exception) {
                _actionError.value = actionErrorMessage(e)
            }
        }
    }

    fun markOutForDelivery() {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.markOutForDelivery(orderId)
                } else {
                    martApi.markOutForDelivery(orderId)
                }
                _actionMessage.value = "Order marked out for delivery"
                load()
            } catch (e: Exception) {
                _actionError.value = actionErrorMessage(e)
            }
        }
    }

    fun markDelivered() {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.markDelivered(orderId)
                } else {
                    martApi.markDelivered(orderId)
                }
                _actionMessage.value = "Order marked delivered"
                load()
            } catch (e: Exception) {
                _actionError.value = actionErrorMessage(e)
            }
        }
    }

    fun cancelOrder() {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.cancelOrder(orderId)
                } else {
                    martApi.cancelOrder(orderId)
                }
                _actionMessage.value = "Order cancelled"
                load()
            } catch (e: Exception) {
                _actionError.value = actionErrorMessage(e)
            }
        }
    }

    fun requestReturn(reason: String) {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.requestReturn(orderId, reason)
                } else {
                    martApi.requestOrderReturn(orderId, com.mart.distribution.demo.data.api.dto.OrderReturnRequest(reason))
                }
                _actionMessage.value = "Return request submitted"
                load()
            } catch (e: Exception) {
                _actionError.value = actionErrorMessage(e)
            }
        }
    }

    fun requestReturnDetailed(
        reason: String,
        reasonText: String?,
        comments: String?,
        items: List<com.mart.distribution.demo.data.api.dto.CreateReturnItemRequest>,
    ) {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.requestReturn(orderId, reasonText ?: reason)
                } else {
                    martApi.createReturn(
                        orderId,
                        com.mart.distribution.demo.data.api.dto.CreateReturnRequest(
                            reason = reason,
                            reasonText = reasonText,
                            comments = comments,
                            items = items,
                        ),
                    )
                }
                _actionMessage.value = "Return request submitted"
                load()
            } catch (e: Exception) {
                _actionError.value = actionErrorMessage(e)
            }
        }
    }

    fun approveReturn() {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.approveReturn(orderId)
                } else {
                    martApi.approveOrderReturn(orderId)
                }
                _actionMessage.value = "Return approved — raise refund request when ready"
                load()
            } catch (e: Exception) {
                _actionError.value = actionErrorMessage(e)
            }
        }
    }

    fun rejectReturn(note: String? = null) {
        viewModelScope.launch {
            _actionError.value = null
            _actionMessage.value = null
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.rejectReturn(orderId, note)
                } else {
                    martApi.rejectOrderReturn(
                        orderId,
                        com.mart.distribution.demo.data.api.dto.OrderReturnRejectRequest(note),
                    )
                }
                _actionMessage.value = "Return request rejected"
                load()
            } catch (e: Exception) {
                _actionError.value = actionErrorMessage(e)
            }
        }
    }

}
