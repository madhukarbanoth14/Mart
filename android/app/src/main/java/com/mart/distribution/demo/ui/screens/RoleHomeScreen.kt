package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.MartElevatedCard
import com.mart.distribution.demo.ui.components.OrderStatusChip
import com.mart.distribution.demo.ui.components.SectionHeader
import com.mart.distribution.demo.ui.util.formatDecimal

private data class TabSpec(
    val label: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleHomeScreen(
    user: SessionUser,
    mainViewModel: MainViewModel,
    navController: NavHostController,
    onLogout: () -> Unit,
) {
    LaunchedEffect(Unit) {
        mainViewModel.refreshForRole()
    }
    val ui by mainViewModel.uiState.collectAsState()
    val cartLines by mainViewModel.cartLines.collectAsState()
    val role = user.role.uppercase()
    val tabs =
        remember(role) {
            when (role) {
                "SHOPKEEPER" ->
                    listOf(
                        TabSpec("Home", Icons.Outlined.Dashboard),
                        TabSpec("Catalog", Icons.Outlined.Inventory2),
                        TabSpec("Cart", Icons.Outlined.ShoppingCart),
                        TabSpec("Orders", Icons.AutoMirrored.Outlined.ReceiptLong),
                    )
                "DEALER" ->
                    listOf(
                        TabSpec("Home", Icons.Outlined.Dashboard),
                        TabSpec("Orders", Icons.AutoMirrored.Outlined.ReceiptLong),
                        TabSpec("Stock", Icons.Outlined.Inventory2),
                    )
                else ->
                    listOf(
                        TabSpec("Home", Icons.Outlined.Dashboard),
                        TabSpec("Orders", Icons.AutoMirrored.Outlined.ReceiptLong),
                        TabSpec("Team", Icons.Outlined.People),
                    )
            }
        }

    var tabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabTitle = tabs.getOrElse(tabIndex) { tabs[0] }.label

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = tabTitle,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { mainViewModel.refreshAll() },
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Sign out")
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                tabs.forEachIndexed { index, spec ->
                    val badgeCount =
                        if (role == "SHOPKEEPER" && spec.label == "Cart") {
                            cartLines.sumOf { it.quantity }
                        } else {
                            0
                        }
                    NavigationBarItem(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        icon = {
                            if (badgeCount > 0) {
                                BadgedBox(badge = { Badge { Text("$badgeCount") } }) {
                                    Icon(spec.icon, contentDescription = spec.label)
                                }
                            } else {
                                Icon(spec.icon, contentDescription = spec.label)
                            }
                        },
                        label = { Text(spec.label) },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            ),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
        ) {
            when (role) {
                "SHOPKEEPER" ->
                    when (tabIndex) {
                        0 ->
                            ShopkeeperDashboard(
                                ui = ui,
                                onBrowse = { tabIndex = 1 },
                                onOrders = { tabIndex = 3 },
                            )
                        1 ->
                            CatalogTab(
                                ui = ui,
                                onAdd = { mainViewModel.addToCart(it) },
                            )
                        2 ->
                            CartTab(
                                lines = cartLines,
                                onQty = { id, q -> mainViewModel.setCartQuantity(id, q) },
                                onRemove = { mainViewModel.removeCartLine(it) },
                                onCheckout = { mainViewModel.placeOrderFromCart() },
                                placeError = ui.placeOrderError,
                                placedOrder = ui.placedOrder,
                                onDismissPlaced = { mainViewModel.clearOrderFeedback() },
                            )
                        else ->
                            OrdersTab(
                                ui = ui,
                                role = role,
                                onOpen = { id -> navController.navigate("order/$id") },
                            )
                    }
                "DEALER" ->
                    when (tabIndex) {
                        0 ->
                            DealerDashboard(
                                ui = ui,
                                onOrders = { tabIndex = 1 },
                            )
                        1 ->
                            OrdersTab(
                                ui = ui,
                                role = role,
                                onOpen = { id -> navController.navigate("order/$id") },
                            )
                        else ->
                            StockTab(ui = ui)
                    }
                else ->
                    when (tabIndex) {
                        0 ->
                            AdminDashboard(ui = ui)
                        1 ->
                            OrdersTab(
                                ui = ui,
                                role = role,
                                onOpen = { id -> navController.navigate("order/$id") },
                            )
                        else ->
                            UsersTab(ui = ui)
                    }
            }
        }
    }
}

@Composable
private fun ShopkeeperDashboard(
    ui: com.mart.distribution.demo.feature.home.MainUiState,
    onBrowse: () -> Unit,
    onOrders: () -> Unit,
) {
    val orders =
        when (val o = ui.orders) {
            is LoadState.Ok -> o.data
            else -> emptyList()
        }
    val open = orders.count { it.status.equals("PENDING", true) || it.status.equals("ACCEPTED", true) }
    val last = orders.maxByOrNull { it.id }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            SectionHeader(
                title = "Your workspace",
                subtitle = "Server-priced orders, mock payment, and PDF invoices.",
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MartElevatedCard(modifier = Modifier.weight(1f)) {
                    Text("Open orders", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text("$open", style = MaterialTheme.typography.headlineMedium)
                }
                MartElevatedCard(modifier = Modifier.weight(1f)) {
                    Text("Last total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text(formatDecimal(last?.finalAmount), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        item {
            GradientGoldButton(text = "Browse catalog", onClick = onBrowse)
        }
        item {
            androidx.compose.material3.OutlinedButton(
                onClick = onOrders,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("View my orders")
            }
        }
    }
}

@Composable
private fun DealerDashboard(
    ui: com.mart.distribution.demo.feature.home.MainUiState,
    onOrders: () -> Unit,
) {
    val orders =
        when (val o = ui.orders) {
            is LoadState.Ok -> o.data
            else -> emptyList()
        }
    val pending = orders.count { it.status.equals("PENDING", true) }
    val lowStock =
        when (val s = ui.stock) {
            is LoadState.Ok -> s.data.count { it.quantity < 5 }
            else -> 0
        }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionHeader(
                title = "Fulfillment",
                subtitle = "Confirm orders to generate invoices and decrement stock.",
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MartElevatedCard(modifier = Modifier.weight(1f)) {
                    Text("Awaiting confirm", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text("$pending", style = MaterialTheme.typography.headlineMedium)
                }
                MartElevatedCard(modifier = Modifier.weight(1f)) {
                    Text("Low stock SKUs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text("$lowStock", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        item {
            GradientGoldButton(text = "Open order queue", onClick = onOrders)
        }
    }
}

@Composable
private fun AdminDashboard(ui: com.mart.distribution.demo.feature.home.MainUiState) {
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
    var demoSum by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(orders) {
        var total = 0.0
        for (ord in orders) {
            val v = ord.finalAmount
            when (v) {
                is Number -> total += v.toDouble()
                is String -> v.toDoubleOrNull()?.let { total += it }
                else -> {}
            }
        }
        demoSum = String.format("%.2f", total)
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionHeader(
                title = "Company overview",
                subtitle = "Demo totals are computed on-device for the pitch.",
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MartElevatedCard(modifier = Modifier.weight(1f)) {
                    Text("Users", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text("${users.size}", style = MaterialTheme.typography.headlineMedium)
                }
                MartElevatedCard(modifier = Modifier.weight(1f)) {
                    Text("Orders", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text("${orders.size}", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        item {
            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text("Demo totals (Σ finalAmount)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(demoSum ?: "—", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
private fun CatalogTab(
    ui: com.mart.distribution.demo.feature.home.MainUiState,
    onAdd: (com.mart.distribution.demo.data.api.dto.ProductDto) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val products =
        when (val p = ui.products) {
            is LoadState.Ok ->
                p.data.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            else -> emptyList()
        }

    Column(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material3.OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search products") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        when (val p = ui.products) {
            is LoadState.Loading -> Text("Loading catalog…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            is LoadState.Err -> Text(p.message, color = MaterialTheme.colorScheme.error)
            is LoadState.Ok ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(products, key = { it.id }) { prod ->
                        MartElevatedCard(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onAdd(prod) },
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${prod.brandType} · Base ${formatDecimal(prod.basePrice)} · GST ${formatDecimal(prod.gstPercentage)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text("+", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
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
    lines: List<com.mart.distribution.demo.data.cart.CartLine>,
    onQty: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit,
    placeError: String?,
    placedOrder: com.mart.distribution.demo.data.api.dto.OrderDto?,
    onDismissPlaced: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (lines.isEmpty()) {
            Text(
                "Your cart is empty. Add items from the catalog.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(lines, key = { it.productId }) { line ->
                    MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(line.productName, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            IconButtonThin("-") { onQty(line.productId, line.quantity - 1) }
                            Text("${line.quantity}", style = MaterialTheme.typography.titleMedium)
                            IconButtonThin("+") { onQty(line.productId, line.quantity + 1) }
                            Spacer(Modifier.weight(1f))
                            Text("Remove", color = MaterialTheme.colorScheme.error, modifier = Modifier.clickable { onRemove(line.productId) })
                        }
                    }
                }
            }
        }
        placeError?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        GradientGoldButton(
            text = "Place order (server totals)",
            onClick = onCheckout,
            enabled = lines.isNotEmpty(),
        )
    }

    placedOrder?.let { ord ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissPlaced,
            title = { Text("Order placed") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Totals from NestJS (source of truth)", style = MaterialTheme.typography.labelMedium)
                    Text("Subtotal: ${formatDecimal(ord.totalAmount)}")
                    Text("Discount: ${formatDecimal(ord.discountAmount)}")
                    Text("GST: ${formatDecimal(ord.gstAmount)}")
                    Text("Payable: ${formatDecimal(ord.finalAmount)}", style = MaterialTheme.typography.titleMedium)
                    Text("Status: ${ord.status}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = onDismissPlaced) { Text("Done") }
            },
        )
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
    val label =
        when (role) {
            "DEALER" -> "Assigned to you"
            "SHOPKEEPER" -> "Your orders"
            else -> "All orders"
        }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        when (val o = ui.orders) {
            is LoadState.Loading -> Text("Loading…")
            is LoadState.Err -> Text(o.message, color = MaterialTheme.colorScheme.error)
            is LoadState.Ok ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(o.data, key = { it.id }) { ord ->
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
private fun UsersTab(ui: com.mart.distribution.demo.feature.home.MainUiState) {
    when (val u = ui.users) {
        is LoadState.Loading -> Text("Loading team…")
        is LoadState.Err -> Text(u.message, color = MaterialTheme.colorScheme.error)
        is LoadState.Ok ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(u.data, key = { it.id }) { row ->
                    MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(row.name, style = MaterialTheme.typography.titleMedium)
                        Text(row.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(row.role, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        else -> {}
    }
}
