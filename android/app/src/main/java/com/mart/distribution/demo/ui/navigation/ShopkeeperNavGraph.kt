package com.mart.distribution.demo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.brands.BrandsViewModel
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.screens.RoleHomeScreen

/**
 * Shopkeeper-only shell; tab content and deep links stay in [RoleHomeScreen] until further split.
 */
@Composable
fun ShopkeeperNavGraph(
    user: SessionUser,
    mainViewModel: MainViewModel,
    brandsViewModel: BrandsViewModel,
    navController: NavHostController,
    onLogout: () -> Unit,
) {
    RoleHomeScreen(
        user = user,
        mainViewModel = mainViewModel,
        brandsViewModel = brandsViewModel,
        navController = navController,
        onLogout = onLogout,
    )
}
