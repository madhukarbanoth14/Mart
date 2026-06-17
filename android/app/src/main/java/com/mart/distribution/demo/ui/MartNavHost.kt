package com.mart.distribution.demo.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mart.distribution.demo.feature.auth.LoginViewModel
import com.mart.distribution.demo.feature.brands.BrandsViewModel
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.screens.BrandsManagementScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperCartScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperPaymentScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperGstDetailsScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperHelpSupportScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperNotificationsScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperPaymentMethodsScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperStoreAddressScreen
import com.mart.distribution.demo.ui.screens.InvoiceScreen
import com.mart.distribution.demo.ui.screens.LoginScreen
import com.mart.distribution.demo.ui.screens.ResetPasswordScreen
import com.mart.distribution.demo.ui.screens.OrderDetailScreen
import com.mart.distribution.demo.ui.screens.OrderConfirmationScreen
import com.mart.distribution.demo.ui.screens.ProductDetailScreen
import com.mart.distribution.demo.ui.screens.SkuManagementScreen
import com.mart.distribution.demo.ui.screens.SplashRoute
import com.mart.distribution.demo.ui.navigation.AdminNavGraph
import com.mart.distribution.demo.ui.navigation.DealerNavGraph
import com.mart.distribution.demo.ui.navigation.MartNavRole
import com.mart.distribution.demo.ui.navigation.ShopkeeperNavGraph
import com.mart.distribution.demo.ui.screens.CreateEmployeeScreen
import com.mart.distribution.demo.ui.screens.DealerOnboardScreen
import com.mart.distribution.demo.ui.screens.ShopkeeperOnboardScreen
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
    val brandsVm =
        viewModel<BrandsViewModel>(
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
                    navController.navigate("main") {
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
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate("reset-password") },
            )
        }
        composable("reset-password") {
            ResetPasswordScreen(
                viewModel = loginVm,
                onBack = { navController.popBackStack() },
                onPasswordReset = {
                    navController.popBackStack()
                },
            )
        }
        composable("main") {
            val user by container.sessionManager.sessionUserFlow.collectAsStateWithLifecycle(initialValue = null)
            // Do not key auth redirect on `user` from collectAsState's initial null — that races with DataStore after sign-in.
            LaunchedEffect(Unit) {
                var first = true
                container.sessionManager.sessionUserFlow.collect { u ->
                    if (first) {
                        first = false
                        if (u == null) {
                            navController.navigate("login") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    } else if (u == null) {
                        navController.navigate("login") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            }
            user?.let { u ->
                val onLogout = {
                    mainVm.logout {
                        navController.navigate("login") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
                when (MartNavRole.fromApiRole(u.role)) {
                    MartNavRole.SHOPKEEPER ->
                        ShopkeeperNavGraph(u, mainVm, brandsVm, navController, onLogout)
                    MartNavRole.DEALER ->
                        DealerNavGraph(u, mainVm, brandsVm, navController, onLogout)
                    MartNavRole.ADMIN_OR_EMPLOYEE ->
                        AdminNavGraph(u, mainVm, brandsVm, navController, onLogout)
                }
            }
        }
        composable("cart") {
            val cartLines by mainVm.cartLines.collectAsState()
            val ui by mainVm.uiState.collectAsState()
            ShopkeeperCartScreen(
                lines = cartLines,
                onBack = { navController.popBackStack() },
                onQty = { id, q ->
                    if (q <= 0) mainVm.removeCartLine(id) else mainVm.setCartQuantity(id, q)
                },
                onCheckout = { mainVm.placeOrderFromCart() },
                onProceedToPayment = { navController.navigate("payment") },
                placeError = ui.placeOrderError,
                onBrowse = { navController.popBackStack() },
            )
        }
        composable("payment") {
            var paying by remember { mutableStateOf(false) }
            val cartLines by mainVm.cartLines.collectAsState()
            val ui by mainVm.uiState.collectAsState()
            LaunchedEffect(ui.placedOrder?.id, ui.placeOrderError) {
                if (ui.placedOrder != null || ui.placeOrderError != null) paying = false
            }
            LaunchedEffect(ui.placedOrder?.id) {
                val id = ui.placedOrder?.id ?: return@LaunchedEffect
                navController.navigate("order-confirmation/$id") {
                    popUpTo("cart") { inclusive = false }
                }
            }
            ShopkeeperPaymentScreen(
                lines = cartLines,
                onBack = { navController.popBackStack() },
                onPayOnline = { method ->
                    paying = true
                    mainVm.placeOrderFromCartWithDemoPayment(method)
                },
                onPayLater = {
                    paying = true
                    mainVm.placeOrderFromCart()
                },
                paying = paying,
                placeError = ui.placeOrderError,
            )
        }
        composable("profile/store-address") {
            val user by container.sessionManager.sessionUserFlow.collectAsStateWithLifecycle(initialValue = null)
            user?.let { u ->
                ShopkeeperStoreAddressScreen(
                    user = u,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable("profile/payment-methods") {
            ShopkeeperPaymentMethodsScreen(onBack = { navController.popBackStack() })
        }
        composable("profile/gst-details") {
            ShopkeeperGstDetailsScreen(onBack = { navController.popBackStack() })
        }
        composable("profile/notifications") {
            ShopkeeperNotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable("profile/help") {
            ShopkeeperHelpSupportScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "order/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(id, navController, container)
        }
        composable(
            route = "order-confirmation/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("orderId") ?: return@composable
            val ui by mainVm.uiState.collectAsState()
            OrderConfirmationScreen(
                orderId = id,
                navController = navController,
                prefetchedOrder = ui.placedOrder?.takeIf { it.id == id },
            )
        }
        composable(
            route = "product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(
                productId = id,
                navController = navController,
                onAddToCart = { product, qty ->
                    mainVm.addQuantityToCart(product, qty)
                },
            )
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
        composable("onboard-employee") {
            CreateEmployeeScreen(navController, mainVm)
        }
        composable("onboard-shopkeeper") {
            ShopkeeperOnboardScreen(navController, mainVm)
        }
        composable("onboard-dealer") {
            DealerOnboardScreen(navController, mainVm)
        }
        composable(
            route = "admin-review/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType }),
        ) { entry ->
            val userId = entry.arguments?.getString("userId") ?: return@composable
            com.mart.distribution.demo.ui.admin.AdminOnboardingReviewScreen(
                userId = userId,
                navController = navController,
                mainViewModel = mainVm,
                onApprove = { id ->
                    mainVm.approveUser(id) { _, _ ->
                        navController.popBackStack()
                    }
                },
                onReject = { id, reason ->
                    mainVm.rejectUser(id, reason) { _ ->
                        navController.popBackStack()
                    }
                },
            )
        }
        composable("sku-management") {
            SkuManagementScreen(navController, mainVm)
        }
        composable("brands-management") {
            BrandsManagementScreen(navController, brandsVm)
        }
    }
}
