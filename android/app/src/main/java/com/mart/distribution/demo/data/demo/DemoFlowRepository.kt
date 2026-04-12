package com.mart.distribution.demo.data.demo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory demo flags (payment mock and simulated delivery) for investor timeline UX.
 */
class DemoFlowRepository {
    private val _mockPaidOrderIds = MutableStateFlow<Set<String>>(emptySet())
    val mockPaidOrderIds: StateFlow<Set<String>> = _mockPaidOrderIds.asStateFlow()

    private val _demoDeliveredOrderIds = MutableStateFlow<Set<String>>(emptySet())
    val demoDeliveredOrderIds: StateFlow<Set<String>> = _demoDeliveredOrderIds.asStateFlow()

    fun markMockPaid(orderId: String) {
        _mockPaidOrderIds.update { it + orderId }
    }

    fun markDemoDelivered(orderId: String) {
        _demoDeliveredOrderIds.update { it + orderId }
    }
}
