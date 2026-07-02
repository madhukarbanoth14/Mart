package com.mart.distribution.demo.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.feature.auth.LoginViewModel
import com.mart.distribution.demo.feature.auth.RegisterViewModel
import com.mart.distribution.demo.feature.brands.BrandsViewModel
import com.mart.distribution.demo.feature.finance.FinanceViewModel
import com.mart.distribution.demo.feature.returns.ReturnsViewModel
import com.mart.distribution.demo.feature.home.MainViewModel
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
                    container.sessionManager,
                    container.martApi,
                    container.localDemoMartStore,
                    container.networkConfigRepository,
                    container.pushTokenRegistrar,
                    container.cartRepository,
                ) as T
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(
                    container.martApi,
                    container.sessionRepository,
                    container.cartRepository,
                    container.localDemoMartStore,
                    container.demoFlowRepository,
                    container.applicationContext,
                    container.pushTokenRegistrar,
                ) as T
            modelClass.isAssignableFrom(BrandsViewModel::class.java) ->
                BrandsViewModel(container.brandsRepository) as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) ->
                RegisterViewModel(container.martApi, container.sessionManager) as T
            modelClass.isAssignableFrom(FinanceViewModel::class.java) ->
                FinanceViewModel(container.martApi) as T
            modelClass.isAssignableFrom(ReturnsViewModel::class.java) ->
                ReturnsViewModel(container.martApi) as T
            else -> throw IllegalArgumentException("Unknown VM: ${modelClass.name}")
        }
    }
}
