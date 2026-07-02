package com.mart.distribution.demo.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.payment.RazorpayBridge
import com.mart.distribution.demo.data.payment.RazorpayCheckoutOptions
import com.mart.distribution.demo.data.api.dto.toDoubleFromApiOrNull
import com.mart.distribution.demo.ui.screens.SelfRegistrationScreen
import com.mart.distribution.demo.ui.flashmart.FmDialog
import com.mart.distribution.demo.feature.auth.LoginViewModel
import com.mart.distribution.demo.feature.auth.RegisterViewModel
import com.mart.distribution.demo.feature.brands.BrandsViewModel
import com.mart.distribution.demo.feature.finance.FinanceViewModel
import com.mart.distribution.demo.feature.returns.ReturnsViewModel
import com.mart.distribution.demo.feature.home.MainViewModel
import com.razorpay.Checkout
import com.mart.distribution.demo.ui.screens.BrandsManagementScreen
import com.mart.distribution.demo.ui.dealer.DealerAccountScreen
import com.mart.distribution.demo.ui.dealer.DealerBusinessAddressScreen
import com.mart.distribution.demo.ui.dealer.DealerGstDetailsScreen
import com.mart.distribution.demo.ui.dealer.DealerHelpSupportScreen
import com.mart.distribution.demo.ui.dealer.DealerNotificationsScreen
import com.mart.distribution.demo.ui.dealer.DealerPaymentDetailsScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperCartScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperCheckoutScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperPaymentScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperWalletScreen
import com.mart.distribution.demo.data.profile.ShopkeeperProfileStore
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperGstDetailsScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperHelpSupportScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperNotificationsScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperPaymentMethodsScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperStoreAddressScreen
import com.mart.distribution.demo.ui.screens.InvoiceScreen
import com.mart.distribution.demo.ui.screens.LoginScreen
import com.mart.distribution.demo.ui.screens.OnboardingScreen
import com.mart.distribution.demo.ui.screens.ResetPasswordScreen
import com.mart.distribution.demo.ui.screens.OrderDetailScreen
import com.mart.distribution.demo.ui.screens.OrderConfirmationScreen
import com.mart.distribution.demo.ui.screens.ProfileDocumentsScreen
import com.mart.distribution.demo.ui.screens.PrivacyPolicyScreen
import com.mart.distribution.demo.ui.screens.ProductDetailScreen
import com.mart.distribution.demo.ui.screens.SkuManagementScreen
import com.mart.distribution.demo.ui.screens.SplashRoute
import com.mart.distribution.demo.ui.screens.UserTypeScreen
import com.mart.distribution.demo.ui.screens.WelcomeScreen
import com.mart.distribution.demo.ui.navigation.AdminNavGraph
import com.mart.distribution.demo.ui.navigation.DealerNavGraph
import com.mart.distribution.demo.ui.navigation.MartNavRole
import com.mart.distribution.demo.ui.navigation.ShopkeeperNavGraph
import com.mart.distribution.demo.ui.navigation.safePopBack
import com.mart.distribution.demo.ui.screens.CreateEmployeeScreen
import com.mart.distribution.demo.ui.screens.DealerOnboardScreen
import com.mart.distribution.demo.ui.screens.ShopkeeperOnboardScreen
import com.mart.distribution.demo.ui.screens.TrackingScreen
import kotlinx.coroutines.launch

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
    val registerVm =
        viewModel<RegisterViewModel>(
            viewModelStoreOwner = activity,
            factory = AppViewModelFactory(activity, container),
        )
    val financeVm =
        viewModel<FinanceViewModel>(
            viewModelStoreOwner = activity,
            factory = AppViewModelFactory(activity, container),
        )
    val returnsVm =
        viewModel<ReturnsViewModel>(
            viewModelStoreOwner = activity,
            factory = AppViewModelFactory(activity, container),
        )
    val mainUi by mainVm.uiState.collectAsState()
    val sessionUser by container.sessionManager.sessionUserFlow.collectAsStateWithLifecycle(
        initialValue = container.sessionManager.getUserSnapshot(),
    )

    LaunchedEffect(Unit) {
        RazorpayBridge.events.collect { event ->
            mainVm.onRazorpayResult(
                success = event.success,
                razorpayOrderId = event.orderId,
                razorpayPaymentId = event.paymentId,
                razorpaySignature = event.signature,
                error = event.error,
            )
        }
    }

    LaunchedEffect(
        mainUi.pendingRazorpayOrderId,
        mainUi.pendingRazorpayGatewayOrderId,
        mainUi.pendingRazorpayKeyId,
        mainUi.pendingRazorpayAmountPaise,
        mainUi.pendingRazorpayCurrency,
        mainUi.pendingRazorpayMethod,
    ) {
        val appOrderId = mainUi.pendingRazorpayOrderId ?: return@LaunchedEffect
        val gatewayOrderId = mainUi.pendingRazorpayGatewayOrderId ?: return@LaunchedEffect
        val keyId = mainUi.pendingRazorpayKeyId ?: return@LaunchedEffect
        val amountPaise = mainUi.pendingRazorpayAmountPaise ?: return@LaunchedEffect
        val currency = mainUi.pendingRazorpayCurrency ?: "INR"
        val paymentMethod = mainUi.pendingRazorpayMethod

        val checkout = Checkout().apply { setKeyID(keyId) }
        val options =
            RazorpayCheckoutOptions.build(
                appOrderId = appOrderId,
                gatewayOrderId = gatewayOrderId,
                amountPaise = amountPaise,
                currency = currency,
                userEmail = sessionUser?.email,
                userPhone = sessionUser?.phone,
                paymentMethod = paymentMethod,
            )
        checkout.open(activity, options)
        mainVm.clearPendingRazorpayLaunch()
    }

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
                onContinueOnboarding = {
                    navController.navigate("welcome") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
            )
        }
        composable("onboarding") {
            val scope = rememberCoroutineScope()
            OnboardingScreen(
                onFinish = {
                    scope.launch { container.onboardingPreferences.markCompleted() }
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
            )
        }
        composable("welcome") {
            val scope = rememberCoroutineScope()
            WelcomeScreen(
                onCreateAccount = {
                    scope.launch { container.onboardingPreferences.markCompleted() }
                    navController.navigate("user-type")
                },
                onLogin = {
                    scope.launch { container.onboardingPreferences.markCompleted() }
                    navController.navigate("login") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
            )
        }
        composable("user-type") {
            UserTypeScreen(
                onBack = { navController.safePopBack() },
                onContinue = { role ->
                    registerVm.setRole(role)
                    registerVm.update { it.copy(step = 1) }
                    navController.navigate("register")
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
                onRegister = { navController.navigate("user-type") },
                onOpenPrivacyPolicy = { navController.navigate("legal/privacy") },
            )
        }
        composable("register") {
            SelfRegistrationScreen(
                viewModel = registerVm,
                onBack = { navController.safePopBack() },
                onRegistered = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("reset-password") {
            ResetPasswordScreen(
                viewModel = loginVm,
                onBack = { navController.safePopBack() },
                onPasswordReset = {
                    navController.popBackStack()
                },
            )
        }
        composable("main") {
            val sessionManager = container.sessionManager
            val user by sessionManager.sessionUserFlow.collectAsStateWithLifecycle(
                initialValue = sessionManager.getUserSnapshot(),
            )
            // Redirect only on explicit logout (user was set, then cleared) — not on first null before DataStore emits.
            LaunchedEffect(Unit) {
                var previousUser = sessionManager.getUserSnapshot()
                sessionManager.sessionUserFlow.collect { current ->
                    if (previousUser != null && current == null) {
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    previousUser = current
                }
            }
            user?.let { u ->
                val onLogout = {
                    mainVm.logout {
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                when (MartNavRole.fromApiRole(u.role)) {
                    MartNavRole.SHOPKEEPER ->
                        ShopkeeperNavGraph(u, mainVm, brandsVm, navController, onLogout)
                    MartNavRole.DEALER ->
                        DealerNavGraph(u, mainVm, brandsVm, returnsVm, navController, onLogout)
                    MartNavRole.ADMIN_OR_EMPLOYEE ->
                        AdminNavGraph(u, mainVm, brandsVm, financeVm, returnsVm, navController, onLogout)
                }
            }
        }
        composable("cart") {
            val cartLines by mainVm.cartLines.collectAsState()
            val ui by mainVm.uiState.collectAsState()
            val buyerRole =
                container.sessionManager.getUserSnapshot()?.role?.uppercase() ?: "SHOPKEEPER"
            // Clear any stale order error left over from a previous failed checkout
            // so it doesn't stick around until the app is killed.
            LaunchedEffect(Unit) { mainVm.clearPlaceOrderError() }
            if (ui.documentCheckoutBlocked) {
                FmDialog(
                    title = "Document required",
                    onDismiss = { mainVm.dismissDocumentCheckoutBlock() },
                    confirmLabel = "Upload documents",
                    onConfirm = {
                        mainVm.dismissDocumentCheckoutBlock()
                        navController.navigate(
                            if (buyerRole == "DEALER") "dealer-profile/documents" else "profile/documents",
                        )
                    },
                    dismissLabel = "Not now",
                ) {
                    Text(
                        "To place orders, please upload at least one valid business document for verification.",
                        color = com.mart.distribution.demo.ui.theme.WholesaleMuted,
                    )
                }
            }
            val dealerName = container.sessionManager.getUserSnapshot()?.assignedDealer?.name
            ShopkeeperCartScreen(
                lines = cartLines,
                buyerRole = buyerRole,
                maxOrderQuantity = ui.maxOrderQuantity,
                dealerName = dealerName,
                onBack = { navController.safePopBack() },
                onQty = { id, q ->
                    if (q <= 0) mainVm.removeCartLine(id) else mainVm.setCartQuantity(id, q)
                },
                onCheckout = { mainVm.placeOrderFromCart() },
                onProceedToPayment = {
                    if (container.sessionManager.getUserSnapshot()?.canPlaceOrders == false) {
                        mainVm.showDocumentCheckoutBlock()
                    } else {
                        navController.navigate("checkout")
                    }
                },
                placeError = ui.placeOrderError,
                onBrowse = { navController.safePopBack() },
            )
        }
        composable("checkout") {
            var placing by remember { mutableStateOf(false) }
            val cartLines by mainVm.cartLines.collectAsState()
            val ui by mainVm.uiState.collectAsState()
            val buyerRole =
                container.sessionManager.getUserSnapshot()?.role?.uppercase() ?: "SHOPKEEPER"
            val user = container.sessionManager.getUserSnapshot()
            val orders = when (val o = ui.orders) {
                is com.mart.distribution.demo.feature.home.LoadState.Ok -> o.data
                else -> emptyList()
            }
            val outstanding =
                orders
                    .filter {
                        !it.paymentStatus.equals("PAID", true) &&
                            !it.status.equals("CANCELLED", true)
                    }
                    .sumOf { (it.finalAmount.toDoubleFromApiOrNull() ?: it.totalAmount.toDoubleFromApiOrNull() ?: 0.0) }
            val outstandingLabel =
                if (outstanding > 0) {
                    com.mart.distribution.demo.ui.util.formatDecimal(outstanding)
                } else {
                    null
                }
            val useRazorpayCheckout = !BuildConfig.USE_LOCAL_DEMO_AUTH
            LaunchedEffect(ui.placedOrder?.id, ui.placeOrderError) {
                if (ui.placedOrder != null || ui.placeOrderError != null) placing = false
            }
            LaunchedEffect(ui.placedOrder?.id) {
                val order = ui.placedOrder ?: return@LaunchedEffect
                navController.navigate("order-confirmation/${order.id}") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }
            }
            ShopkeeperCheckoutScreen(
                lines = cartLines,
                buyerRole = buyerRole,
                storeName = ShopkeeperProfileStore.storeName.ifBlank { user?.name ?: "Your store" },
                storeAddress = ShopkeeperProfileStore.storeAddress.ifBlank { "Add your store address in profile" },
                dealerName = user?.assignedDealer?.name,
                dealerArea = user?.areaName,
                outstandingLabel = outstandingLabel,
                useRazorpayCheckout = useRazorpayCheckout,
                onBack = { navController.safePopBack() },
                onChangeAddress = { navController.navigate("profile/store-address") },
                onPlaceOrder = { method ->
                    if (container.sessionManager.getUserSnapshot()?.canPlaceOrders == false) {
                        mainVm.showDocumentCheckoutBlock()
                        return@ShopkeeperCheckoutScreen
                    }
                    placing = true
                    when (method) {
                        "cod" -> mainVm.placeOrderFromCart()
                        else -> {
                            placing = false
                            navController.navigate("payment")
                        }
                    }
                },
                placing = placing,
                placeError = ui.placeOrderError,
            )
        }
        composable("payment") {
            var paying by remember { mutableStateOf(false) }
            val cartLines by mainVm.cartLines.collectAsState()
            val ui by mainVm.uiState.collectAsState()
            val buyerRole =
                container.sessionManager.getUserSnapshot()?.role?.uppercase() ?: "SHOPKEEPER"
            LaunchedEffect(ui.placedOrder?.id, ui.placeOrderError, ui.paymentMessage) {
                if (ui.placedOrder != null || ui.placeOrderError != null || ui.paymentMessage != null) {
                    paying = false
                }
            }
            LaunchedEffect(ui.placedOrder?.id, ui.placedOrder?.paymentStatus) {
                val order = ui.placedOrder ?: return@LaunchedEffect
                if (!order.paymentStatus.equals("PAID", ignoreCase = true)) return@LaunchedEffect
                navController.navigate("order-confirmation/${order.id}") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }
            }
            val useRazorpayCheckout = !BuildConfig.USE_LOCAL_DEMO_AUTH
            ShopkeeperPaymentScreen(
                lines = cartLines,
                buyerRole = buyerRole,
                useRazorpayCheckout = useRazorpayCheckout,
                onBack = { navController.safePopBack() },
                onPayOnline = { method ->
                    paying = true
                    if (useRazorpayCheckout) {
                        mainVm.placeOrderFromCartRazorpay(method)
                    } else {
                        mainVm.placeOrderFromCartWithDemoPayment(method)
                    }
                },
                onPayLater = {
                    paying = true
                    mainVm.placeOrderFromCart()
                },
                paying = paying,
                placeError = ui.placeOrderError,
                paymentMessage = ui.paymentMessage,
            )
        }
        composable("profile/store-address") {
            val user by container.sessionManager.sessionUserFlow.collectAsStateWithLifecycle(initialValue = null)
            user?.let { u ->
                ShopkeeperStoreAddressScreen(
                    user = u,
                    onBack = { navController.safePopBack() },
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
            ShopkeeperNotificationsScreen(mainViewModel = mainVm, onBack = { navController.popBackStack() })
        }
        composable("profile/help") {
            ShopkeeperHelpSupportScreen(onBack = { navController.popBackStack() })
        }
        composable("profile/documents") {
            ProfileDocumentsScreen(mainViewModel = mainVm, onBack = { navController.safePopBack() })
        }
        composable("dealer-profile/account") {
            val user by container.sessionManager.sessionUserFlow.collectAsStateWithLifecycle(initialValue = null)
            user?.let { u ->
                DealerAccountScreen(user = u, onBack = { navController.safePopBack() })
            }
        }
        composable("dealer-profile/business") {
            val user by container.sessionManager.sessionUserFlow.collectAsStateWithLifecycle(initialValue = null)
            user?.let { u ->
                DealerBusinessAddressScreen(user = u, onBack = { navController.safePopBack() })
            }
        }
        composable("dealer-profile/payment") {
            DealerPaymentDetailsScreen(onBack = { navController.safePopBack() })
        }
        composable("dealer-profile/gst") {
            DealerGstDetailsScreen(onBack = { navController.safePopBack() })
        }
        composable("dealer-profile/notifications") {
            DealerNotificationsScreen(mainViewModel = mainVm, onBack = { navController.safePopBack() })
        }
        composable("dealer-profile/help") {
            DealerHelpSupportScreen(onBack = { navController.safePopBack() })
        }
        composable("dealer-profile/documents") {
            ProfileDocumentsScreen(mainViewModel = mainVm, onBack = { navController.safePopBack() })
        }
        composable("legal/privacy") {
            PrivacyPolicyScreen(onBack = { navController.safePopBack() })
        }
        composable(
            route = "order/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(id, navController, container, mainVm)
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
                maxOrderQuantity = mainVm.uiState.value.maxOrderQuantity,
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
        composable("wallet") {
            val ui by mainVm.uiState.collectAsState()
            val orders = when (val o = ui.orders) {
                is com.mart.distribution.demo.feature.home.LoadState.Ok -> o.data
                else -> emptyList()
            }
            ShopkeeperWalletScreen(
                orders = orders,
                onBack = { navController.safePopBack() },
                onPayOutstanding = { navController.navigate("checkout") },
            )
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
        composable("areas-management") {
            com.mart.distribution.demo.ui.admin.AdminAreasScreen(navController, mainVm)
        }
        composable(
            route = "finance/settlement/{settlementId}",
            arguments = listOf(navArgument("settlementId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("settlementId").orEmpty()
            com.mart.distribution.demo.ui.admin.AdminSettlementDetailScreen(
                financeViewModel = financeVm,
                settlementId = id,
                onBack = { navController.safePopBack() },
            )
        }
        composable(
            route = "finance/dealer/{dealerId}/{dealerName}",
            arguments = listOf(
                navArgument("dealerId") { type = NavType.StringType },
                navArgument("dealerName") { type = NavType.StringType },
            ),
        ) { entry ->
            val id = entry.arguments?.getString("dealerId").orEmpty()
            val name = entry.arguments?.getString("dealerName").orEmpty()
            com.mart.distribution.demo.ui.admin.AdminDealerPerformanceScreen(
                financeViewModel = financeVm,
                dealerId = id,
                dealerName = name,
                onBack = { navController.safePopBack() },
            )
        }
        composable("dealer/revenue") {
            com.mart.distribution.demo.ui.dealer.DealerRevenueScreen(
                returnsViewModel = returnsVm,
                onBack = { navController.safePopBack() },
                onOpenShopkeepers = { navController.navigate("dealer/shopkeepers") },
            )
        }
        composable("dealer/shopkeepers") {
            com.mart.distribution.demo.ui.dealer.DealerShopkeeperRevenueScreen(
                returnsViewModel = returnsVm,
                onBack = { navController.safePopBack() },
            )
        }
        composable("dealer/returns") {
            com.mart.distribution.demo.ui.dealer.DealerReturnsScreen(
                returnsViewModel = returnsVm,
                onOpenReturn = { id -> navController.navigate("dealer/return/$id") },
                onBack = { navController.safePopBack() },
            )
        }
        composable(
            route = "dealer/return/{returnId}",
            arguments = listOf(navArgument("returnId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("returnId").orEmpty()
            com.mart.distribution.demo.ui.dealer.DealerReturnDetailScreen(
                returnId = id,
                returnsViewModel = returnsVm,
                isDealer = true,
                onBack = { navController.safePopBack() },
            )
        }
        composable(
            route = "admin/refund/{refundId}",
            arguments = listOf(navArgument("refundId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("refundId").orEmpty()
            com.mart.distribution.demo.ui.dealer.AdminRefundDetailScreen(
                refundId = id,
                returnsViewModel = returnsVm,
                onBack = { navController.safePopBack() },
            )
        }
    }
}
