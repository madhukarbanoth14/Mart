package com.mart.distribution.demo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.brands.BrandsViewModel
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.feature.returns.ReturnsViewModel
import com.mart.distribution.demo.ui.screens.RoleHomeScreen

@Composable
fun DealerNavGraph(
    user: SessionUser,
    mainViewModel: MainViewModel,
    brandsViewModel: BrandsViewModel,
    returnsViewModel: ReturnsViewModel,
    navController: NavHostController,
    onLogout: () -> Unit,
) {
    RoleHomeScreen(
        user = user,
        mainViewModel = mainViewModel,
        brandsViewModel = brandsViewModel,
        returnsViewModel = returnsViewModel,
        navController = navController,
        onLogout = onLogout,
    )
}
