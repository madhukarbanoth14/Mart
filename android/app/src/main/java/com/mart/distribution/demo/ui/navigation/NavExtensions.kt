package com.mart.distribution.demo.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/** Pop the back stack, or return to [fallbackRoute] when there is nothing to pop. */
fun NavController.safePopBack(fallbackRoute: String = "main") {
    if (!popBackStack()) {
        navigate(fallbackRoute) {
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun NavBackHandler(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
}
