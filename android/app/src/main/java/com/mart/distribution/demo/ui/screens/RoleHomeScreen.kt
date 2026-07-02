package com.mart.distribution.demo.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import com.mart.distribution.demo.data.cart.CartLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mart.distribution.demo.data.api.dto.UserRowDto
import com.mart.distribution.demo.data.api.dto.Brand
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.brands.BrandsViewModel
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.finance.FinanceViewModel
import com.mart.distribution.demo.feature.returns.ReturnsViewModel
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.theme.WholesaleTheme
import com.mart.distribution.demo.ui.components.MartElevatedCard
import com.mart.distribution.demo.ui.components.ProductImageStyle
import com.mart.distribution.demo.ui.components.ProductThumbnail
import com.mart.distribution.demo.ui.components.MetricCard
import com.mart.distribution.demo.ui.components.OrderStatusChip
import com.mart.distribution.demo.ui.components.SectionHeader
import com.mart.distribution.demo.ui.flashmart.FmBottomNav
import com.mart.distribution.demo.ui.flashmart.FmNavItem
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperCartScreen
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperCatalogTab
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperFlashmartHome
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperOrdersTab
import com.mart.distribution.demo.ui.shopkeeper.ShopkeeperProfileTab
import com.mart.distribution.demo.ui.dealer.DealerFlashmartHome
import com.mart.distribution.demo.ui.dealer.DealerOrdersTab
import com.mart.distribution.demo.ui.dealer.DealerProfileTab
import com.mart.distribution.demo.ui.dealer.DealerStockTab
import com.mart.distribution.demo.ui.employee.EmployeeFlashmartHome
import com.mart.distribution.demo.ui.employee.EmployeeNetworkTab
import com.mart.distribution.demo.ui.employee.EmployeeProfileTab
import com.mart.distribution.demo.ui.admin.AdminFinanceTab
import com.mart.distribution.demo.ui.admin.AdminFlashmartHome
import com.mart.distribution.demo.ui.admin.AdminOrdersTab
import com.mart.distribution.demo.ui.admin.AdminProfileTab
import com.mart.distribution.demo.ui.admin.AdminUsersTab
import com.mart.distribution.demo.ui.LocalAppContainer
import com.mart.distribution.demo.data.api.dto.cartMath
import com.mart.distribution.demo.ui.util.formatDecimal
import kotlinx.coroutines.launch

private data class TabSpec(
    val label: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleHomeScreen(
    user: SessionUser,
    mainViewModel: MainViewModel,
    brandsViewModel: BrandsViewModel,
    financeViewModel: FinanceViewModel? = null,
    returnsViewModel: ReturnsViewModel? = null,
    navController: NavHostController,
    onLogout: () -> Unit,
) {
    val container = LocalAppContainer.current
    LaunchedEffect(Unit) {
        mainViewModel.refreshForRole()
        brandsViewModel.loadBrands()
    }
    val ui by mainViewModel.uiState.collectAsState()
    val brandsUi by brandsViewModel.uiState.collectAsState()
    val cartLines by mainViewModel.cartLines.collectAsState()
    val cartQtyByProductId =
        remember(cartLines) {
            cartLines.associate { it.productId to it.quantity }
        }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val onOpenPrivacyPolicy = { navController.navigate("legal/privacy") }
    val role = user.role.uppercase()
    val tabs =
        remember(role) {
            when (role) {
                "SHOPKEEPER" ->
                    listOf(
                        TabSpec("Home", Icons.Outlined.Home),
                        TabSpec("Products", Icons.Outlined.GridView),
                        TabSpec("Orders", Icons.Outlined.ShoppingBag),
                        TabSpec("Profile", Icons.Outlined.Person),
                    )
                "DEALER" ->
                    listOf(
                        TabSpec("Home", Icons.Outlined.Home),
                        TabSpec("SKUs", Icons.Outlined.GridView),
                        TabSpec("Orders", Icons.Outlined.ShoppingBag),
                        TabSpec("Stock", Icons.Outlined.Inventory2),
                        TabSpec("Profile", Icons.Outlined.Person),
                    )
                "EMPLOYEE" ->
                    listOf(
                        TabSpec("Home", Icons.Outlined.Home),
                        TabSpec("Network", Icons.Outlined.People),
                        TabSpec("Profile", Icons.Outlined.Person),
                    )
                else ->
                    listOf(
                        TabSpec("Home", Icons.Outlined.Home),
                        TabSpec("Finance", Icons.Outlined.AccountBalance),
                        TabSpec("Orders", Icons.AutoMirrored.Outlined.ReceiptLong),
                        TabSpec("Team", Icons.Outlined.People),
                    )
            }
        }

    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedBrandId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(tabIndex, role) {
        if (role == "SHOPKEEPER" && tabIndex == 1) {
            mainViewModel.ensureCatalogLoaded()
        }
        if (role == "DEALER" && tabIndex == 1) {
            mainViewModel.loadProducts()
        }
        if (role == "DEALER" && tabIndex == 3) {
            mainViewModel.ensureStockLoaded()
        }
    }

    LaunchedEffect(Unit) {
        container.sessionManager.refreshProfileFromServerIfNeeded()
        container.pushTokenRegistrar.registerCurrentToken()
    }

    LaunchedEffect(ui.placedOrder?.id) {
        val id = ui.placedOrder?.id ?: return@LaunchedEffect
        navController.navigate("order-confirmation/$id") {
            popUpTo("main") { inclusive = false }
            launchSingleTop = true
        }
        mainViewModel.clearOrderFeedback()
    }

    val tabTitle = tabs.getOrElse(tabIndex) { tabs[0] }.label
    val isFlashmartShell = true
    val cartCount = cartLines.sumOf { it.quantity }
    val cartTotalLabel =
        remember(cartLines, role) {
            val total = cartLines.cartMath(role).total
            if (cartLines.isEmpty()) "—" else formatDecimal(total)
        }
    val activeOrdersBadge =
        remember(ui.orders, role) {
            when (val o = ui.orders) {
                is LoadState.Ok ->
                    when (role) {
                        "DEALER" ->
                            o.data.count {
                                it.status.equals("PENDING", true) &&
                                    !it.kind.equals("DEALER_RESTOCK", true)
                            }
                        else ->
                            o.data.count {
                                !it.status.equals("DELIVERED", true) && !it.status.equals("CANCELLED", true)
                            }
                    }
                else -> 0
            }
        }
    val lowStockBadge =
        remember(ui.stock) {
            when (val s = ui.stock) {
                is LoadState.Ok -> s.data.count { it.quantity < 5 }
                else -> 0
            }
        }

    val pendingApprovalBadge =
        remember(ui.users, role) {
            if (!role.equals("ADMIN", ignoreCase = true)) return@remember 0
            when (val u = ui.users) {
                is LoadState.Ok ->
                    u.data.count {
                        it.status.equals("PENDING_APPROVAL", true) &&
                            (it.role.equals("DEALER", true) || it.role.equals("SHOPKEEPER", true))
                    }
                else -> 0
            }
        }

    val screen: @Composable () -> Unit = {
    Scaffold(
        containerColor = WholesaleBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {},
        bottomBar = {
            FmBottomNav(
                items =
                    when (role) {
                        "SHOPKEEPER" ->
                            listOf(
                                FmNavItem("home", "Home", Icons.Outlined.Home),
                                FmNavItem("products", "Products", Icons.Outlined.GridView),
                                FmNavItem("orders", "Orders", Icons.Outlined.ShoppingBag, badge = activeOrdersBadge.takeIf { it > 0 }),
                                FmNavItem("profile", "Profile", Icons.Outlined.Person),
                            )
                        "DEALER" ->
                            listOf(
                                FmNavItem("home", "Home", Icons.Outlined.Home),
                                FmNavItem("skus", "SKUs", Icons.Outlined.GridView),
                                FmNavItem("orders", "Orders", Icons.Outlined.ShoppingBag, badge = activeOrdersBadge.takeIf { it > 0 }),
                                FmNavItem("stock", "Stock", Icons.Outlined.Inventory2, badge = lowStockBadge.takeIf { it > 0 }),
                                FmNavItem("profile", "Profile", Icons.Outlined.Person),
                            )
                        "EMPLOYEE" ->
                            listOf(
                                FmNavItem("home", "Home", Icons.Outlined.Home),
                                FmNavItem("network", "Network", Icons.Outlined.People),
                                FmNavItem("profile", "Profile", Icons.Outlined.Person),
                            )
                        else ->
                            listOf(
                                FmNavItem("home", "Home", Icons.Outlined.Home),
                                FmNavItem("finance", "Finance", Icons.Outlined.AccountBalance),
                                FmNavItem("orders", "Orders", Icons.AutoMirrored.Outlined.ReceiptLong),
                                FmNavItem("team", "Team", Icons.Outlined.People, badge = pendingApprovalBadge.takeIf { it > 0 }),
                            )
                    },
                activeId =
                    when (role) {
                        "SHOPKEEPER" ->
                            when (tabIndex) {
                                0 -> "home"; 1 -> "products"; 2 -> "orders"; else -> "profile"
                            }
                        "DEALER" ->
                            when (tabIndex) {
                                0 -> "home"; 1 -> "skus"; 2 -> "orders"; 3 -> "stock"; else -> "profile"
                            }
                        "EMPLOYEE" ->
                            when (tabIndex) {
                                0 -> "home"; 1 -> "network"; else -> "profile"
                            }
                        else ->
                            when (tabIndex) {
                                0 -> "home"; 1 -> "finance"; 2 -> "orders"; else -> "team"
                            }
                    },
                onChange = { id ->
                    tabIndex =
                        when (role) {
                            "SHOPKEEPER" ->
                                when (id) {
                                    "home" -> 0; "products" -> 1; "orders" -> 2; else -> 3
                                }
                            "DEALER" ->
                                when (id) {
                                    "home" -> 0; "skus" -> 1; "orders" -> 2; "stock" -> 3; else -> 4
                                }
                            "EMPLOYEE" ->
                                when (id) {
                                    "home" -> 0; "network" -> 1; else -> 2
                                }
                            else ->
                                when (id) {
                                    "home" -> 0; "finance" -> 1; "orders" -> 2; else -> 3
                                }
                        }
                },
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 0.dp),
        ) {
            when (role) {
                "SHOPKEEPER" ->
                    when (tabIndex) {
                        0 ->
                            ShopkeeperFlashmartHome(
                                ui = ui,
                                user = user,
                                summary = ui.shopkeeperSummary,
                                onOpenCatalog = { tabIndex = 1 },
                                onOpenOrders = { tabIndex = 2 },
                                onOpenOrder = { id -> navController.navigate("order/$id") },
                                onOpenInvoice = { id -> navController.navigate("invoice/$id") },
                                onOpenTrack = { id -> navController.navigate("tracking/$id") },
                                onOpenProfile = { tabIndex = 3 },
                                onOpenWallet = { navController.navigate("wallet") },
                                onOpenSupport = { navController.navigate("profile/help") },
                            )
                        1 ->
                            ShopkeeperCatalogTab(
                                ui = ui,
                                brandsState = brandsUi.brands,
                                shelvesState = ui.shelves,
                                selectedBrandId = selectedBrandId,
                                onSelectBrand = {
                                    selectedBrandId = it
                                    brandsViewModel.selectBrand(it)
                                },
                                cartQuantity = { id -> cartQtyByProductId[id] ?: 0 },
                                cartCount = cartCount,
                                cartTotalLabel = cartTotalLabel,
                                onAddWithQuantity = { product, qty ->
                                    mainViewModel.setCartLineQuantity(product, qty)
                                    scope.launch { snackbarHostState.showSnackbar("Added to cart") }
                                },
                                onSetCartQuantity = { product, qty ->
                                    if (qty <= 0) {
                                        mainViewModel.removeCartLine(product.id)
                                    } else {
                                        mainViewModel.setCartLineQuantity(product, qty)
                                    }
                                },
                                onOpenProduct = { id ->
                                    navController.navigate("product/${Uri.encode(id)}")
                                },
                                onOpenCart = { navController.navigate("cart") },
                            )
                        2 ->
                            Column(modifier = Modifier.fillMaxSize()) {
                                com.mart.distribution.demo.ui.flashmart.FmAppHeader(
                                    title = "My Orders",
                                    subtitle = user.name,
                                )
                                ShopkeeperOrdersTab(
                                    ui = ui,
                                    onOpen = { id -> navController.navigate("order/$id") },
                                    onInvoice = { id -> navController.navigate("invoice/$id") },
                                    onTrack = { id -> navController.navigate("tracking/$id") },
                                    onReorder = { id ->
                                        mainViewModel.reorderFromOrder(id) { success, message ->
                                            scope.launch {
                                                if (success) {
                                                    navController.navigate("cart")
                                                    message?.let { snackbarHostState.showSnackbar(it) }
                                                } else {
                                                    snackbarHostState.showSnackbar(message ?: "Could not reorder")
                                                }
                                            }
                                        }
                                    },
                                    onBrowseCatalog = { tabIndex = 1 },
                                )
                            }
                        else ->
                            Column(modifier = Modifier.fillMaxSize()) {
                                com.mart.distribution.demo.ui.flashmart.FmAppHeader(title = "Profile")
                                ShopkeeperProfileTab(
                                    ui = ui,
                                    user = user,
                                    onLogout = onLogout,
                                    onOpenStoreAddress = { navController.navigate("profile/store-address") },
                                    onOpenPaymentMethods = { navController.navigate("profile/payment-methods") },
                                    onOpenGstDetails = { navController.navigate("profile/gst-details") },
                                    onOpenNotifications = { navController.navigate("profile/notifications") },
                                    onOpenDocuments = { navController.navigate("profile/documents") },
                                    onOpenHelp = { navController.navigate("profile/help") },
                                    onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                                )
                            }
                    }
                "DEALER" ->
                    when (tabIndex) {
                        0 ->
                            DealerFlashmartHome(
                                dealerId = user.id,
                                user = user,
                                ui = ui,
                                summary = ui.dealerSummary,
                                onOpenOrders = { tabIndex = 2 },
                                onOpenStock = { tabIndex = 3 },
                                onManageSkus = { tabIndex = 1 },
                                onOpenOrder = { id -> navController.navigate("order/$id") },
                                onOpenProfile = { tabIndex = 4 },
                                onOpenRevenue = { navController.navigate("dealer/revenue") },
                                onOpenReturns = { navController.navigate("dealer/returns") },
                            )
                        1 ->
                            Column(modifier = Modifier.fillMaxSize()) {
                                com.mart.distribution.demo.ui.flashmart.FmAppHeader(
                                    title = "SKU management",
                                    subtitle = "Products · pricing · discounts",
                                )
                                SkuManagementScreen(
                                    mainViewModel = mainViewModel,
                                    embeddedInTab = true,
                                )
                            }
                        2 ->
                            Column(modifier = Modifier.fillMaxSize()) {
                                com.mart.distribution.demo.ui.flashmart.FmAppHeader(
                                    title = "Orders",
                                    subtitle = "${activeOrdersBadge} awaiting action",
                                )
                                DealerOrdersTab(ui = ui, onOpen = { id -> navController.navigate("order/$id") })
                            }
                        3 ->
                            Column(modifier = Modifier.fillMaxSize()) {
                                com.mart.distribution.demo.ui.flashmart.FmAppHeader(
                                    title = "Stock",
                                    subtitle = "$lowStockBadge low SKUs",
                                )
                                DealerStockTab(
                                    ui = ui,
                                    onEnsureCatalog = { mainViewModel.loadProducts() },
                                    onOpenSkus = { tabIndex = 1 },
                                    onUpdateQuantity = { stockId, qty, cb ->
                                        mainViewModel.updateStockQuantity(stockId, qty, cb)
                                    },
                                    onAddStock = { productId, qty, cb ->
                                        mainViewModel.addStockSku(productId, qty, cb)
                                    },
                                    onShowMessage = { msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    },
                                )
                            }
                        else ->
                            Column(modifier = Modifier.fillMaxSize()) {
                                com.mart.distribution.demo.ui.flashmart.FmAppHeader(title = "Profile")
                                DealerProfileTab(
                                    user = user,
                                    ui = ui,
                                    onLogout = onLogout,
                                    onOpenAccount = { navController.navigate("dealer-profile/account") },
                                    onOpenBusinessAddress = { navController.navigate("dealer-profile/business") },
                                    onOpenPaymentDetails = { navController.navigate("dealer-profile/payment") },
                                    onOpenGstDetails = { navController.navigate("dealer-profile/gst") },
                                    onOpenNotifications = { navController.navigate("dealer-profile/notifications") },
                                    onOpenDocuments = { navController.navigate("dealer-profile/documents") },
                                    onOpenHelp = { navController.navigate("dealer-profile/help") },
                                    onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                                    onManageSkus = { tabIndex = 1 },
                                )
                            }
                    }
                else ->
                    when (role) {
                        "EMPLOYEE" ->
                            when (tabIndex) {
                                0 ->
                                    EmployeeFlashmartHome(
                                        user = user,
                                        ui = ui,
                                        onAddDealer = { navController.navigate("onboard-dealer") },
                                        onAddShopkeeper = { navController.navigate("onboard-shopkeeper") },
                                        onOpenNetwork = { tabIndex = 1 },
                                    )
                                1 ->
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        com.mart.distribution.demo.ui.flashmart.FmAppHeader(title = "My network")
                                        EmployeeNetworkTab(employeeId = user.id, ui = ui)
                                    }
                                else ->
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        com.mart.distribution.demo.ui.flashmart.FmAppHeader(title = "Profile")
                                        EmployeeProfileTab(
                                            user = user,
                                            ui = ui,
                                            onLogout = onLogout,
                                            onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                                        )
                                    }
                            }
                        else ->
                            when (tabIndex) {
                                0 ->
                                    AdminFlashmartHome(
                                        ui = ui,
                                        pendingCount = pendingApprovalBadge,
                                        onAddEmployee = { navController.navigate("onboard-employee") },
                                        onAddShopkeeper = { navController.navigate("onboard-shopkeeper") },
                                        onAddDealer = { navController.navigate("onboard-dealer") },
                                        onManageSkus = { navController.navigate("sku-management") },
                                        onManageBrands = { navController.navigate("brands-management") },
                                        onManageAreas = { navController.navigate("areas-management") },
                                        onOpenOrders = { tabIndex = 2 },
                                        onOpenTeam = { tabIndex = 3 },
                                        onOpenFinance = { tabIndex = 1 },
                                    )
                                1 ->
                                    financeViewModel?.let { fv ->
                                        AdminFinanceTab(
                                            financeViewModel = fv,
                                            returnsViewModel = returnsViewModel,
                                            mainUi = ui,
                                            onOpenSettlement = { id -> navController.navigate("finance/settlement/$id") },
                                            onOpenDealer = { id ->
                                                val name =
                                                    when (val u = ui.users) {
                                                        is LoadState.Ok -> u.data.firstOrNull { it.id == id }?.name ?: "Dealer"
                                                        else -> "Dealer"
                                                    }
                                                navController.navigate("finance/dealer/$id/$name")
                                            },
                                            onOpenRefund = { id -> navController.navigate("admin/refund/$id") },
                                        )
                                    }
                                2 ->
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        com.mart.distribution.demo.ui.flashmart.FmAppHeader(title = "Orders", subtitle = "All network orders")
                                        AdminOrdersTab(ui = ui, onOpen = { id -> navController.navigate("order/$id") })
                                    }
                                else ->
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        com.mart.distribution.demo.ui.flashmart.FmAppHeader(
                                            title = "Network",
                                            subtitle =
                                                if (pendingApprovalBadge > 0) {
                                                    "$pendingApprovalBadge pending approval"
                                                } else {
                                                    "Dealers and shopkeepers"
                                                },
                                        )
                                        AdminUsersTab(
                                            ui = ui,
                                            onAddEmployee = { navController.navigate("onboard-employee") },
                                            onAddShopkeeper = { navController.navigate("onboard-shopkeeper") },
                                            onAddDealer = { navController.navigate("onboard-dealer") },
                                            onReviewUser = { userId ->
                                                navController.navigate("admin-review/$userId")
                                            },
                                            onApprove = { userId ->
                                                mainViewModel.approveUser(userId) { err, msg ->
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(err ?: msg ?: "Approved")
                                                    }
                                                }
                                            },
                                            onReject = { userId, reason ->
                                                mainViewModel.rejectUser(userId, reason) { err ->
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(err ?: "User rejected")
                                                    }
                                                }
                                            },
                                            onDeactivate = { userId, reason ->
                                                mainViewModel.deactivateUser(userId, reason) { err ->
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(err ?: "Login deactivated")
                                                    }
                                                }
                                            },
                                            onReactivate = { userId ->
                                                mainViewModel.reactivateUser(userId) { err ->
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(err ?: "User reactivated")
                                                    }
                                                }
                                            },
                                        )
                                    }
                            }
                    }
            }
        }
    }
    }

    // All roles use the Flashmart-aligned WholesaleTheme (light, indigo brand)
    WholesaleTheme { screen() }
}

@Composable
private fun DealerDashboard(
    dealerId: String,
    ui: com.mart.distribution.demo.feature.home.MainUiState,
    onOrders: () -> Unit,
    onOpenStock: () -> Unit,
    summary: com.mart.distribution.demo.data.api.dto.DealerSummaryDto?,
) {
    val orders =
        when (val o = ui.orders) {
            is LoadState.Ok -> o.data
            else -> emptyList()
        }
    val pending = summary?.pendingOrders ?: orders.count { it.status.equals("PENDING", true) }
    val lowStock =
        when (val s = ui.stock) {
            is LoadState.Ok -> s.data.count { it.quantity < 5 }
            else -> 0
        }
    val revenueSum =
        summary?.weeklyRevenue ?: remember(orders) {
            orders.sumOf { ord ->
                when (val v = ord.finalAmount) {
                    is Number -> v.toDouble()
                    is String -> v.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
            }
        }
    val revenueLabel = remember(revenueSum) { String.format("%.2f", revenueSum) }
    val assignedAreaIds =
        remember(ui.areas, dealerId) {
            when (val a = ui.areas) {
                is LoadState.Ok -> a.data.filter { it.dealerId == dealerId }.map { it.id }.toSet()
                else -> emptySet()
            }
        }
    val assignedShopkeepers =
        remember(ui.users, assignedAreaIds) {
            when (val u = ui.users) {
                is LoadState.Ok ->
                    u.data.count {
                        it.role.equals("SHOPKEEPER", ignoreCase = true) &&
                            it.area?.id != null &&
                            assignedAreaIds.contains(it.area.id)
                    }
                else -> 0
            }
        }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionHeader(
                title = "Dealer operations",
                subtitle = "Maintain stock, receive and fulfill orders, track assigned shopkeepers, and monitor sales performance.",
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    label = "Pending orders",
                    value = "$pending",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Today's deliveries",
                    value = "${summary?.todaysDeliveries ?: 0}",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    label = "Low stock SKUs",
                    value = "$lowStock",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "This week revenue",
                    value = if (orders.isEmpty()) "—" else revenueLabel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            GradientGoldButton(text = "Open order queue", onClick = onOrders)
        }
        item {
            OutlinedButton(
                onClick = onOpenStock,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Maintain stock")
            }
        }
        item {
            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text("Assign delivery boy", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Use the order queue to open an order and assign delivery in the delivery-tracking step.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AdminDashboard(
    ui: com.mart.distribution.demo.feature.home.MainUiState,
    onAddShopkeeper: () -> Unit,
    onAddDealer: () -> Unit,
    onManageSkus: () -> Unit,
    onManageBrands: () -> Unit,
) {
    val orders =
        when (val o = ui.orders) {
            is LoadState.Ok -> o.data
            else -> emptyList()
        }
    val users =
        when (val u = ui.users) {
            is LoadState.Ok -> u.data
            else -> emptyList()
        }
    val products =
        when (val p = ui.products) {
            is LoadState.Ok -> p.data
            else -> emptyList()
        }
    val areas =
        when (val a = ui.areas) {
            is LoadState.Ok -> a.data
            else -> emptyList()
        }
    val dealers = users.count { it.role.equals("DEALER", ignoreCase = true) }
    val shopkeepers = users.count { it.role.equals("SHOPKEEPER", ignoreCase = true) }
    val revenueSum =
        remember(orders) {
            orders.sumOf { ord ->
                when (val v = ord.finalAmount) {
                    is Number -> v.toDouble()
                    is String -> v.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
            }
        }
    val revenueLabel = remember(revenueSum) { String.format("%.2f", revenueSum) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionHeader(
                title = "Super admin control center",
                subtitle = "Full system control across sales monitoring, SKU management, area management, and dealer/shopkeeper oversight.",
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    label = "Sales orders",
                    value = "${orders.size}",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Revenue (Σ finalAmount)",
                    value = if (orders.isEmpty()) "—" else revenueLabel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    label = "SKUs",
                    value = "${products.size}",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Areas",
                    value = "${areas.size}",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    label = "Dealers",
                    value = "$dealers",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Shopkeepers",
                    value = "$shopkeepers",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            GradientGoldButton(text = "Onboard shopkeeper", onClick = onAddShopkeeper)
        }
        item {
            OutlinedButton(
                onClick = onAddDealer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Onboard dealer")
            }
        }
        item {
            OutlinedButton(
                onClick = onManageSkus,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Manage SKUs")
            }
        }
        item {
            OutlinedButton(
                onClick = onManageBrands,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Manage brands")
            }
        }
    }
}

@Composable
private fun BrandSelectorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun EmployeeOnboardingHub(
    user: SessionUser,
    ui: com.mart.distribution.demo.feature.home.MainUiState,
    navController: NavHostController,
    onOpenMyOnboardedList: () -> Unit,
) {
    val usersState = ui.users
    val mine =
        remember(usersState, user.id) {
            when (usersState) {
                is LoadState.Ok ->
                    usersState.data.filter {
                        it.onboardedById == user.id &&
                            (it.role.equals("SHOPKEEPER", ignoreCase = true) ||
                                it.role.equals("DEALER", ignoreCase = true))
                    }
                else -> emptyList()
            }
        }
    val shopkeeperCount = remember(mine) { mine.count { it.role.equals("SHOPKEEPER", ignoreCase = true) } }
    val dealerCount = remember(mine) { mine.count { it.role.equals("DEALER", ignoreCase = true) } }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            SectionHeader(
                title = "Field employee — onboarding",
                subtitle = "Onboard dealers and shopkeepers, assign sign-in credentials, and capture notes or document names. Order management is not available here.",
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    label = "Shopkeepers I onboarded",
                    value = "$shopkeeperCount",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Dealers I onboarded",
                    value = "$dealerCount",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Performance: counts above include only partners you registered (tracked per your account).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            GradientGoldButton(
                text = "Onboard shopkeeper",
                onClick = { navController.navigate("onboard-shopkeeper") },
            )
        }
        item {
            GradientGoldButton(
                text = "Onboard dealer",
                onClick = { navController.navigate("onboard-dealer") },
            )
        }
        item {
            OutlinedButton(
                onClick = onOpenMyOnboardedList,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("View my onboarded list")
            }
        }
        when (usersState) {
            is LoadState.Loading ->
                item { Text("Loading roster…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            is LoadState.Err ->
                item { Text(usersState.message, color = MaterialTheme.colorScheme.error) }
            else -> {}
        }
    }
}

@Composable
private fun MyOnboardedTab(
    employeeId: String,
    ui: com.mart.distribution.demo.feature.home.MainUiState,
) {
    val usersState = ui.users
    val rows =
        remember(usersState, employeeId) {
            when (usersState) {
                is LoadState.Ok ->
                    usersState.data
                        .filter {
                            it.onboardedById == employeeId &&
                                (it.role.equals("SHOPKEEPER", ignoreCase = true) ||
                                    it.role.equals("DEALER", ignoreCase = true))
                        }
                        .sortedByDescending { it.createdAt ?: "" }
                else -> emptyList()
            }
        }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Partners you onboarded",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        when (usersState) {
            is LoadState.Loading -> Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            is LoadState.Err -> Text(usersState.message, color = MaterialTheme.colorScheme.error)
            is LoadState.Ok ->
                if (rows.isEmpty()) {
                    Text(
                        "You have not onboarded any shopkeepers or dealers yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(rows, key = { it.id }) { row ->
                            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Text(row.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    row.role,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    row.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                row.phone?.let { p ->
                                    Text(
                                        "Phone · $p",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                val areaPart = row.area?.name ?: row.area?.id
                                if (areaPart != null) {
                                    Text(
                                        "Area · $areaPart",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                row.createdAt?.let { ca ->
                                    Text(
                                        "Added · ${ca.take(10)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                row.onboardingNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            else -> {}
        }
    }
}

@Composable
private fun CartTab(
    lines: List<CartLine>,
    buyerRole: String,
    onQty: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit,
    onCheckoutRazorpay: () -> Unit,
    onPayNow: () -> Unit,
    placeError: String?,
    placedOrder: com.mart.distribution.demo.data.api.dto.OrderDto?,
    paymentMessage: String?,
) {
    val linesWithRef = lines.count { it.referenceUnitPrice != null }
    val math = remember(lines, buyerRole) { lines.cartMath(buyerRole) }
    val estimatedLabel =
        remember(lines, math, linesWithRef) {
            when {
                lines.isEmpty() -> "—"
                linesWithRef == lines.size -> formatDecimal(math.total)
                linesWithRef > 0 -> "${formatDecimal(math.total)} (partial)"
                else -> "—"
            }
        }

    Column(modifier = Modifier.fillMaxSize()) {
        if (lines.isEmpty()) {
            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text("Cart is empty", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Go to the Catalog tab, tap products to add them here, then come back to review quantities and place your order.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(lines, key = { it.productId }) { line ->
                    MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ProductThumbnail(
                                imageUrl = line.imageUrl,
                                brandLogoUrl = line.brandLogoUrl,
                                productName = line.productName,
                                brandName = null,
                                style = ProductImageStyle.Grid,
                                cornerRadius = 10.dp,
                                modifier = Modifier.size(80.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    line.productName,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                val lineEst = line.referenceUnitPrice?.let { it * line.quantity }
                                Text(
                                    text =
                                        if (lineEst != null) {
                                            "Est. line (base × qty): ${formatDecimal(lineEst)}"
                                        } else {
                                            "Est. line: — (no catalog base on this line)"
                                        },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    IconButtonThin("-") {
                                        onQty(line.productId, line.quantity - 1)
                                    }
                                    Text(
                                        "${line.quantity}",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    IconButtonThin("+") {
                                        onQty(line.productId, line.quantity + 1)
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        "Remove",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier =
                                            Modifier.clickable {
                                                onRemove(line.productId)
                                            },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Estimated total (catalog base × qty): $estimatedLabel",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                "Checkout uses server pricing, discounts, and GST. After placing an order, totals in the dialog are authoritative.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        placeError?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        paymentMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
        GradientGoldButton(
            text = "Place order",
            onClick = onCheckout,
            enabled = lines.isNotEmpty(),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onCheckoutRazorpay,
            modifier = Modifier.fillMaxWidth(),
            enabled = lines.isNotEmpty(),
        ) {
            Text("Place & pay with Razorpay")
        }
        if (placedOrder != null && !placedOrder.paymentStatus.equals("PAID", true)) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onPayNow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Pay now (Razorpay)")
            }
        }
    }

}

@Composable
private fun IconButtonThin(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun OrdersTab(
    ui: com.mart.distribution.demo.feature.home.MainUiState,
    role: String,
    onOpen: (String) -> Unit,
) {
    var dealerStatusTab by rememberSaveable { mutableStateOf("NEW") }
    var dealerQuery by rememberSaveable { mutableStateOf("") }
    var shopkeeperQuery by rememberSaveable { mutableStateOf("") }
    var dateQuery by rememberSaveable { mutableStateOf("") }
    val label =
        when (role) {
            "DEALER" -> "Assigned to you"
            "SHOPKEEPER" -> "Your orders"
            else -> "All orders"
        }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        if (role == "DEALER") {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BrandSelectorChip("New", dealerStatusTab == "NEW") { dealerStatusTab = "NEW" }
                BrandSelectorChip("Confirmed", dealerStatusTab == "CONFIRMED") { dealerStatusTab = "CONFIRMED" }
                BrandSelectorChip("Out for delivery", dealerStatusTab == "DISPATCHED") { dealerStatusTab = "DISPATCHED" }
                BrandSelectorChip("Delivered", dealerStatusTab == "DELIVERED") { dealerStatusTab = "DELIVERED" }
            }
            Spacer(Modifier.height(10.dp))
        }
        if (role == "ADMIN") {
            androidx.compose.material3.OutlinedTextField(
                value = dealerQuery,
                onValueChange = { dealerQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Filter by dealer") },
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.OutlinedTextField(
                value = shopkeeperQuery,
                onValueChange = { shopkeeperQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Filter by shopkeeper") },
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.OutlinedTextField(
                value = dateQuery,
                onValueChange = { dateQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Filter by date (YYYY-MM-DD)") },
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            Spacer(Modifier.height(10.dp))
        }
        when (val o = ui.orders) {
            is LoadState.Loading -> Text("Loading…")
            is LoadState.Err -> Text(o.message, color = MaterialTheme.colorScheme.error)
            is LoadState.Ok ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    val filtered =
                        o.data.filter { ord ->
                            val roleFiltered =
                                when (role) {
                                    "DEALER" ->
                                        when (dealerStatusTab) {
                                            "NEW" -> ord.status.equals("PENDING", true)
                                            "CONFIRMED" -> ord.status.equals("DEALER_CONFIRMED", true) || ord.status.equals("ACCEPTED", true)
                                            "DISPATCHED" -> ord.status.equals("OUT_FOR_DELIVERY", true)
                                            "DELIVERED" -> ord.status.equals("DELIVERED", true)
                                            else -> true
                                        }
                                    "ADMIN" -> true
                                    else -> true
                                }
                            if (role != "ADMIN") {
                                roleFiltered
                            } else {
                                val dealerText = (ord.dealer?.name ?: ord.dealer?.email ?: "").lowercase()
                                val shopText = (ord.shopkeeper?.name ?: ord.shopkeeper?.email ?: "").lowercase()
                                val createdDate = ord.createdAt?.take(10) ?: ""
                                roleFiltered &&
                                    dealerText.contains(dealerQuery.trim().lowercase()) &&
                                    shopText.contains(shopkeeperQuery.trim().lowercase()) &&
                                    createdDate.contains(dateQuery.trim())
                            }
                        }
                    items(filtered, key = { it.id }) { ord ->
                        MartElevatedCard(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpen(ord.id) },
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(formatDecimal(ord.finalAmount), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        ord.shopkeeper?.name ?: ord.dealer?.name ?: ord.id.take(8),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (role == "ADMIN") {
                                        Text(
                                            "Dealer: ${ord.dealer?.name ?: "—"} · Date: ${ord.createdAt?.take(10) ?: "—"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                OrderStatusChip(ord.status)
                            }
                        }
                    }
                }
            else -> {}
        }
    }
}

@Composable
private fun StockTab(ui: com.mart.distribution.demo.feature.home.MainUiState) {
    when (val s = ui.stock) {
        is LoadState.Loading -> Text("Loading stock…")
        is LoadState.Err -> Text(s.message, color = MaterialTheme.colorScheme.error)
        is LoadState.Ok ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(s.data, key = { it.id }) { row ->
                    MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(row.product?.name ?: "Product", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Qty ${row.quantity}",
                            color =
                                if (row.quantity < 5) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
            }
        else -> Text("Stock will load for dealers.")
    }
}

@Composable
private fun UsersTab(
    ui: com.mart.distribution.demo.feature.home.MainUiState,
    onAddShopkeeper: () -> Unit,
    onAddDealer: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GradientGoldButton(text = "Onboard shopkeeper", onClick = onAddShopkeeper)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onAddDealer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Onboard dealer")
        }
        Spacer(Modifier.height(12.dp))
        when (val u = ui.users) {
            is LoadState.Loading -> Text("Loading team…")
            is LoadState.Err -> Text(u.message, color = MaterialTheme.colorScheme.error)
            is LoadState.Ok ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(u.data, key = { it.id }) { row ->
                        MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Text(row.name, style = MaterialTheme.typography.titleMedium)
                            Text(row.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            row.phone?.let { phone ->
                                Text("Phone · $phone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val areaPart = row.area?.name ?: row.area?.id
                            if (areaPart != null) {
                                Text(
                                    "Area · $areaPart",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(row.role, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            else -> {}
        }
    }
}
