package com.mart.distribution.demo.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.feature.auth.LoginViewModel
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.feature.orders.OrderDetailViewModel

class AppViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val container: AppContainer,
    defaultArgs: android.os.Bundle? = null,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(
                    container.sessionRepository,
                    container.martApi,
                ) as T
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(
                    container.martApi,
                    container.sessionRepository,
                    container.cartRepository,
                ) as T
            modelClass.isAssignableFrom(OrderDetailViewModel::class.java) ->
                OrderDetailViewModel(
                    container.martApi,
                    container.cartRepository,
                    handle.get<String>(OrderDetailViewModel.ARG_ORDER_ID)!!,
                ) as T
            else -> throw IllegalArgumentException("Unknown VM: ${modelClass.name}")
        }
    }
}
