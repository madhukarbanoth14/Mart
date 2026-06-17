package com.mart.distribution.demo.data.payment

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class RazorpayResultEvent(
    val success: Boolean,
    val paymentId: String? = null,
    val orderId: String? = null,
    val signature: String? = null,
    val error: String? = null,
)

object RazorpayBridge {
    private val _events = MutableSharedFlow<RazorpayResultEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    fun emit(event: RazorpayResultEvent) {
        _events.tryEmit(event)
    }
}
