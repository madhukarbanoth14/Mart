package com.mart.distribution.demo.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mart.distribution.demo.feature.auth.LoginViewModel
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.screens.InvoiceScreen
import com.mart.distribution.demo.ui.screens.LoginScreen
import com.mart.distribution.demo.ui.screens.OrderDetailScreen
import com.mart.distribution.demo.ui.screens.RoleHomeScreen
import com.mart.distribution.demo.ui.screens.SplashRoute
import com.mart.distribution.demo.ui.screens.TrackingScreen

@Composable
fun MartNavHost() {
    val activity = LocalContext.current as ComponentActivity
    val container = LocalAppContainer.current
    val navController = rememberNavController()
    val mainVm =
        viewModel<MainViewModel>(
            viewModelStoreOwner = activity,
            factory = AppViewModelFactory(activity, container),
        )
    val loginVm =
        viewModel<LoginViewModel>(
            viewModelStoreOwner = activity,
            factory = AppViewModelFactory(activity, container),
        )

    NavHost(
        navController = navController,
        startDestination = "splash",
    ) {
        composable("splash") {
            SplashRoute(
                container = container,
                onContinueLoggedIn = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onContinueGuest = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = loginVm,
                onSignedIn = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("home") {
            val user by container.sessionRepository.sessionUserFlow.collectAsStateWithLifecycle(initialValue = null)
            LaunchedEffect(user) {
                if (user == null) {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
            user?.let { u ->
                RoleHomeScreen(
                    user = u,
                    mainViewModel = mainVm,
                    navController = navController,
                    onLogout = {
                        mainVm.logout {
                            navController.navigate("login") {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    },
                )
            }
        }
        composable(
            route = "order/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(id, navController, container)
        }
        composable(
            route = "tracking/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("orderId") ?: return@composable
            TrackingScreen(id, navController, container)
        }
        composable(
            route = "invoice/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("orderId") ?: return@composable
            InvoiceScreen(id, navController, container)
        }
    }
}
