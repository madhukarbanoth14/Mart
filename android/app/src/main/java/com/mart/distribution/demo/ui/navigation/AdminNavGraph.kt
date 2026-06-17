package com.mart.distribution.demo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.brands.BrandsViewModel
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.screens.RoleHomeScreen

/** ADMIN: company dashboard, orders, team. EMPLOYEE: onboarding hub and my-onboarded list only (no orders in UI). */
@Composable
fun AdminNavGraph(
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
