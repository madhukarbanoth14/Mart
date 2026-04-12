package com.mart.distribution.demo.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mart.distribution.demo.AppContainer

class OrderDetailViewModelFactory(
    private val container: AppContainer,
    private val orderId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderDetailViewModel::class.java)) {
            return OrderDetailViewModel(
                container.martApi,
                container.demoFlowRepository,
                orderId,
            ) as T
        }
        throw IllegalArgumentException("Unknown VM: ${modelClass.name}")
    }
}
