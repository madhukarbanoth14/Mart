package com.mart.distribution.demo.ui.dealer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.DealerSummaryDto
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.StockRowDto
import com.mart.distribution.demo.data.profile.DealerProfileStore
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.ui.flashmart.FmAvatar
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmEmptyState
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmLoadingState
import com.mart.distribution.demo.ui.flashmart.FmSegmentedControl
import com.mart.distribution.demo.ui.flashmart.FmStatCard
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.flashmart.FmHomeStat
import com.mart.distribution.demo.ui.flashmart.FmHomeStatGrid
import com.mart.distribution.demo.ui.flashmart.FmIconButton
import com.mart.distribution.demo.ui.theme.WholesaleDealerBlue
import com.mart.distribution.demo.ui.theme.WholesaleDealerBlueDeep
import com.mart.distribution.demo.ui.theme.WholesaleDealerBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleGold
import com.mart.distribution.demo.ui.theme.WholesaleGoldInk
import com.mart.distribution.demo.ui.theme.WholesaleGoldTint
import com.mart.distribution.demo.ui.theme.WholesaleSurface3
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrange
import com.mart.distribution.demo.ui.theme.WholesaleOrangeTint
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

@Composable
fun DealerFlashmartHome(
    dealerId: String,
    user: SessionUser,
    ui: MainUiState,
    summary: DealerSummaryDto?,
    onOpenOrders: () -> Unit,
    onOpenStock: () -> Unit,
    onManageSkus: () -> Unit = {},
    onOpenOrder: (String) -> Unit,
    onOpenProfile: () -> Unit = {},
    onOpenRevenue: () -> Unit = {},
    onOpenReturns: () -> Unit = {},
) {
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    val pending = orders.filter { it.status.equals("PENDING", true) }
    val active =
        orders.filter {
            it.status.equals("DEALER_CONFIRMED", true) ||
                it.status.equals("ACCEPTED", true) ||
                it.status.equals("OUT_FOR_DELIVERY", true)
        }
    val todayRev =
        remember(orders) {
            orders.filter { it.paymentStatus.equals("PAID", true) }
                .sumOf { o ->
                    when (val v = o.finalAmount) {
                        is Number -> v.toDouble()
                        is String -> v.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                }
        }
    val shopkeeperCount =
        remember(orders) {
            orders.mapNotNull { it.shopkeeper?.id }.distinct().size
        }
    val inventoryValue =
        remember(ui.stock) {
            when (val s = ui.stock) {
                is LoadState.Ok ->
                    s.data.sumOf { row ->
                        val unit =
                            when (val p = row.product?.basePrice) {
                                is Number -> p.toDouble()
                                is String -> p.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                        unit * row.quantity
                    }
                else -> 0.0
            }
        }
    val lowStock =
        remember(ui.stock) {
            when (val s = ui.stock) {
                is LoadState.Ok -> s.data.count { it.quantity < 5 }
                else -> 0
            }
        }
    val recent = orders.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dealer dashboard", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WholesaleDealerBlue)
                    Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WholesaleText, letterSpacing = (-0.5).sp)
                }
                FmIconButton(
                    icon = Icons.Outlined.Notifications,
                    onClick = onOpenProfile,
                    badge = pending.size.takeIf { it > 0 },
                )
                Box(modifier = Modifier.clickable(onClick = onOpenProfile)) {
                    FmAvatar(user.name, size = 42.dp, tint = WholesaleDealerBlue)
                }
            }
        }

        item {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(WholesaleDealerBlue, WholesaleDealerBlueDeep)))
                        .padding(20.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(150.dp)
                            .clip(RoundedCornerShape(75.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                )
                Column {
                    Text("Today's revenue", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(0.85f))
                    Text(formatDecimal(todayRev), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-0.5).sp)
                    Text(
                        "${orders.count { it.paymentStatus.equals("PAID", true) }} paid orders · ${user.areaName ?: "Your area"}",
                        fontSize = 12.5.sp,
                        color = Color.White.copy(0.82f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item {
            FmHomeStatGrid(
                stats =
                    listOf(
                        FmHomeStat(Icons.Outlined.Schedule, "Pending", "${summary?.pendingOrders ?: pending.size}", WholesaleGoldTint, WholesaleGoldInk),
                        FmHomeStat(Icons.Outlined.LocalShipping, "In transit", "${active.size}", WholesaleDealerBlueTint, WholesaleDealerBlue),
                        FmHomeStat(Icons.Outlined.Inventory2, "Inventory", formatDecimal(inventoryValue), WholesaleSurface3, WholesaleText),
                        FmHomeStat(Icons.Outlined.Groups, "Shopkeepers", "$shopkeeperCount", WholesaleBlueTint, WholesaleBlue),
                    ),
            )
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FmCard(
                    onClick = onOpenRevenue,
                    modifier = Modifier.weight(1f),
                    padding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                ) {
                    Icon(Icons.Outlined.AccountBalance, null, tint = WholesaleDealerBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Revenue", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                    Text("Analytics & reports", fontSize = 11.sp, color = WholesaleMuted)
                }
                FmCard(
                    onClick = onOpenReturns,
                    modifier = Modifier.weight(1f),
                    padding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                ) {
                    Icon(Icons.Outlined.Receipt, null, tint = WholesaleOrange, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Returns", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                    Text("Review & refunds", fontSize = 11.sp, color = WholesaleMuted)
                }
            }
        }

        item {
            FmCard(
                onClick = onManageSkus,
                padding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(WholesaleBlue.copy(0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.GridView, null, tint = WholesaleBlue, modifier = Modifier.size(21.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Manage SKUs & pricing", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                        Text("Add products shopkeepers can order from you", fontSize = 12.sp, color = WholesaleMuted)
                    }
                    Text("›", fontSize = 18.sp, color = WholesaleBlue)
                }
            }
        }

        if (pending.isNotEmpty()) {
            item {
                FmSectionLabel(title = "Awaiting confirmation", action = "View all", onAction = onOpenOrders)
            }
            items(pending.take(2), key = { it.id }) { order ->
                DealerOrderCard(
                    order = order,
                    onClick = { onOpenOrder(order.id) },
                    actionLabel = "Accept order",
                    fresh = true,
                )
            }
        }

        if (lowStock > 0) {
            item {
                FmCard(
                    onClick = onOpenStock,
                    padding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                        Box(
                            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Inventory2, null, tint = WholesaleOrange, modifier = Modifier.size(21.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("$lowStock items low on stock", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WholesaleOrange)
                            Text("Update quantities in Stock tab", fontSize = 12.sp, color = Color(0xFF9A6410))
                        }
                        Text("›", fontSize = 18.sp, color = WholesaleOrange)
                    }
                }
            }
        }

        if (recent.isNotEmpty()) {
            item { FmSectionLabel(title = "Recent orders", action = "See all", onAction = onOpenOrders) }
            item {
                FmCard(padding = androidx.compose.foundation.layout.PaddingValues(4.dp)) {
                    recent.forEachIndexed { i, o ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenOrder(o.id) }.padding(horizontal = 12.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                FmAvatar(orderShopLabel(o), size = 38.dp, tint = WholesaleGreen)
                                Column {
                                    Text(
                                        orderShopLabel(o),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "${o.id.takeLast(8)} · ${o.items?.size ?: 0} items",
                                        fontSize = 12.sp,
                                        color = WholesaleMuted,
                                    )
                                }
                            }
                            FmBadge(o.status)
                        }
                        if (i < recent.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder))
                    }
                }
            }
        }
    }
}

@Composable
fun DealerOrdersTab(
    ui: MainUiState,
    onOpen: (String) -> Unit,
) {
    var segment by rememberSaveable { mutableStateOf("Pending") }
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    val filtered =
        remember(orders, segment) {
            when (segment) {
                "Pending" -> orders.filter { it.status.equals("PENDING", true) }
                "Active" ->
                    orders.filter {
                        it.status.equals("DEALER_CONFIRMED", true) ||
                            it.status.equals("ACCEPTED", true) ||
                            it.status.equals("OUT_FOR_DELIVERY", true)
                    }
                else -> orders.filter { it.status.equals("DELIVERED", true) }
            }
        }

    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        FmSegmentedControl(
            options = listOf("Pending", "Active", "Done"),
            selected = segment,
            onSelect = { segment = it },
        )
        Spacer(Modifier.height(14.dp))
        when (val o = ui.orders) {
            is LoadState.Loading -> FmLoadingState(message = "Loading orders…")
            is LoadState.Ok ->
                if (filtered.isEmpty()) {
                    FmEmptyState(
                        icon = Icons.Outlined.ShoppingBag,
                        title = "No ${segment.lowercase()} orders",
                        message = "Orders from shopkeepers in your area will appear here.",
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        items(filtered, key = { it.id }) { order ->
                            val action =
                                when (segment) {
                                    "Pending" -> "Accept order"
                                    "Active" -> "Mark delivered"
                                    else -> null
                                }
                            DealerOrderCard(
                                order = order,
                                onClick = { onOpen(order.id) },
                                actionLabel = action,
                                fresh = segment == "Pending",
                            )
                        }
                    }
                }
            is LoadState.Err -> FmErrorBanner(message = o.message, modifier = Modifier.padding(vertical = 8.dp))
            else -> {}
        }
    }
}

@Composable
private fun DealerOrderCard(
    order: OrderDto,
    onClick: () -> Unit,
    actionLabel: String? = null,
    fresh: Boolean = false,
) {
    val address = order.shopkeeper?.address?.takeIf { it.isNotBlank() }
    val borderModifier =
        if (fresh) {
            Modifier.fillMaxWidth().padding(1.dp).clip(RoundedCornerShape(17.dp)).background(WholesaleDealerBlue.copy(0.35f))
        } else {
            Modifier
        }
    Box(modifier = borderModifier) {
        FmCard(onClick = onClick) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FmAvatar(orderShopLabel(order), size = 42.dp, tint = WholesaleDealerBlue)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        orderShopLabel(order),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text("${order.id.takeLast(8)} · ${order.items?.size ?: 0} items", fontSize = 12.sp, color = WholesaleMuted)
                    if (address != null) {
                        Text(
                            "📍 $address",
                            fontSize = 11.sp,
                            color = WholesaleMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatDecimal(order.finalAmount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                    FmBadge(order.status)
                }
            }
            order.items?.take(3)?.let { items ->
                if (items.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items.forEach { item ->
                            Text(
                                "${item.quantity}× ${item.product?.name?.take(14) ?: "Item"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WholesaleDealerBlue,
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WholesaleDealerBlueTint)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            actionLabel?.let { label ->
                Spacer(Modifier.height(12.dp))
                FmButton(
                    text = label,
                    onClick = onClick,
                    variant = if (fresh) FmButtonVariant.Primary else FmButtonVariant.Soft,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Dealer-facing label for a shopkeeper order. The backend redacts the shopkeeper's
 * personal name for dealers, so we show the shop/business name (or the order id).
 */
private fun orderShopLabel(order: OrderDto): String =
    order.shopkeeper?.shopName?.takeIf { it.isNotBlank() }
        ?: order.shopkeeper?.name?.takeIf { it.isNotBlank() }
        ?: "Order ${order.id.takeLast(6).uppercase()}"

@Composable
fun DealerStockTab(
    ui: MainUiState,
    onEnsureCatalog: () -> Unit = {},
    onOpenSkus: () -> Unit = {},
    onUpdateQuantity: (stockId: String, quantity: Int, onFinished: (String?) -> Unit) -> Unit = { _, _, cb -> cb(null) },
    onAddStock: (productId: String, quantity: Int, onFinished: (String?) -> Unit) -> Unit = { _, _, cb -> cb(null) },
    onShowMessage: (String) -> Unit = {},
) {
    LaunchedEffect(Unit) { onEnsureCatalog() }

    var editingRow by remember { mutableStateOf<StockRowDto?>(null) }
    var addDialogOpen by rememberSaveable { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    when (val s = ui.stock) {
        is LoadState.Loading ->
            FmLoadingState(message = "Loading stock…", modifier = Modifier.fillMaxSize())
        is LoadState.Err -> Text(s.message, color = WholesaleRed, modifier = Modifier.padding(16.dp))
        is LoadState.Ok -> {
            val rows = s.data
            val lowCount = rows.count { it.quantity < 5 }
            val catalogProducts =
                when (val p = ui.products) {
                    is LoadState.Ok -> p.data
                    else -> emptyList()
                }
            val stockedProductIds = remember(rows) { rows.mapNotNull { it.product?.id }.toSet() }
            val addableProducts =
                remember(catalogProducts, stockedProductIds) {
                    catalogProducts.filter { it.id !in stockedProductIds }
                }

            if (rows.isEmpty()) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(WholesaleBg)
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FmCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "No stock yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = WholesaleText,
                            )
                            Text(
                                "Add SKUs in SKU management, then set quantities here. " +
                                    "Shopkeepers in your area order from this stock.",
                                fontSize = 13.sp,
                                color = WholesaleMuted,
                            )
                            Spacer(Modifier.height(4.dp))
                            FmButton(
                                text = "Manage SKUs",
                                onClick = onOpenSkus,
                                variant = FmButtonVariant.Primary,
                            )
                        }
                    }
                }
            } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 24.dp),
            ) {
                item {
                    FmCard {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = WholesaleBlue,
                                modifier = Modifier.size(20.dp),
                            )
                            Column {
                                Text(
                                    "Manage quantities for the SKUs you sell to shopkeepers.",
                                    fontSize = 13.sp,
                                    color = WholesaleText,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Tap + to add stock for a product, or tap a row to edit quantity.",
                                    fontSize = 12.sp,
                                    color = WholesaleMuted,
                                )
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${rows.size} SKUs · $lowCount low",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WholesaleMuted,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { addDialogOpen = true },
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White),
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "Add stock", tint = WholesaleGreen)
                        }
                    }
                }
                actionError?.let { err ->
                    item {
                        Text(err, color = WholesaleRed, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                items(rows, key = { it.id }) { row ->
                    DealerStockRow(
                        row = row,
                        selectionMode = false,
                        selected = false,
                        onToggleSelect = {},
                        onEdit = { editingRow = row },
                    )
                }
            }

            editingRow?.let { row ->
                StockQuantityDialog(
                    title = row.product?.name ?: "Edit stock",
                    initialQty = row.quantity.toString(),
                    busy = busy,
                    onDismiss = { if (!busy) editingRow = null },
                    onConfirm = { qtyText ->
                        val qty = qtyText.toIntOrNull()
                        if (qty == null || qty < 0) {
                            actionError = "Enter a valid quantity (0 or more)"
                            return@StockQuantityDialog
                        }
                        busy = true
                        actionError = null
                        onUpdateQuantity(row.id, qty) { err ->
                            busy = false
                            if (err != null) {
                                actionError = err
                            } else {
                                editingRow = null
                                onShowMessage("Stock updated")
                            }
                        }
                    },
                )
            }

            if (addDialogOpen) {
                AddStockDialog(
                    products = addableProducts,
                    catalogLoading = ui.products is LoadState.Loading,
                    busy = busy,
                    onDismiss = { if (!busy) addDialogOpen = false },
                    onConfirm = { productId, qty ->
                        busy = true
                        actionError = null
                        onAddStock(productId, qty) { err ->
                            busy = false
                            if (err != null) {
                                actionError = err
                            } else {
                                addDialogOpen = false
                                onShowMessage("Stock added")
                            }
                        }
                    },
                )
            }
            }
        }
        else -> Text("Stock loads for dealers.", modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun DealerStockRow(
    row: StockRowDto,
    selectionMode: Boolean,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onEdit: () -> Unit,
) {
    val low = row.quantity < 5
    val capacity = (row.product?.caseQty ?: 50).coerceAtLeast(1)
    val fill = (row.quantity.toFloat() / capacity).coerceIn(0f, 1f)
    FmCard(onClick = if (!selectionMode) onEdit else null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = { onToggleSelect() })
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .then(if (selectionMode) Modifier.clickable(onClick = onToggleSelect) else Modifier),
            ) {
                Text(
                    row.product?.name ?: "Product",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WholesaleText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text("SKU ${row.product?.sku ?: "—"}", fontSize = 12.sp, color = WholesaleMuted)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(WholesaleBorder),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(fill)
                                .height(6.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(if (low) WholesaleOrange else WholesaleDealerBlue),
                    )
                }
                Text(
                    "${row.quantity} / $capacity units",
                    fontSize = 11.sp,
                    color = if (low) WholesaleOrange else WholesaleMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (low) {
                    FmBadge("low", label = "Low")
                }
                if (!selectionMode) {
                    if (low) {
                        FmButton(
                            text = "Reorder",
                            onClick = onEdit,
                            variant = FmButtonVariant.Soft,
                        )
                    } else {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit quantity", tint = WholesaleMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockQuantityDialog(
    title: String,
    initialQty: String,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var qtyText by rememberSaveable(title, initialQty) { mutableStateOf(initialQty) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit stock", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, fontSize = 13.sp, color = WholesaleMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = MartFieldDefaults.outlinedColors(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(qtyText) }, enabled = !busy) {
                Text(if (busy) "Saving…" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        },
    )
}

@Composable
private fun AddStockDialog(
    products: List<ProductDto>,
    catalogLoading: Boolean,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (productId: String, quantity: Int) -> Unit,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedProductId by rememberSaveable { mutableStateOf<String?>(null) }
    var qtyText by rememberSaveable { mutableStateOf("50") }
    val filtered =
        remember(products, search) {
            val q = search.trim().lowercase()
            if (q.isEmpty()) products.take(40)
            else products.filter { it.name.lowercase().contains(q) || (it.sku?.lowercase()?.contains(q) == true) }.take(40)
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add stock SKU", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search catalog") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = MartFieldDefaults.outlinedColors(),
                )
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Starting quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = MartFieldDefaults.outlinedColors(),
                )
                when {
                    catalogLoading -> Text("Loading catalog…", fontSize = 13.sp, color = WholesaleMuted)
                    products.isEmpty() ->
                        Text(
                            "All catalog SKUs already have stock rows. Edit quantity on an existing row instead.",
                            fontSize = 13.sp,
                            color = WholesaleMuted,
                        )
                    filtered.isEmpty() -> Text("No products match your search.", fontSize = 13.sp, color = WholesaleMuted)
                    else ->
                        filtered.forEach { p ->
                            val picked = selectedProductId == p.id
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (picked) WholesaleBlueTint else Color.Transparent)
                                        .clickable { selectedProductId = p.id }
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        p.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (picked) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text("SKU ${p.sku ?: "—"}", fontSize = 11.sp, color = WholesaleMuted)
                                }
                            }
                        }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val pid = selectedProductId ?: return@TextButton
                    val qty = qtyText.toIntOrNull() ?: return@TextButton
                    onConfirm(pid, qty.coerceAtLeast(0))
                },
                enabled = !busy && selectedProductId != null && qtyText.isNotBlank(),
            ) {
                Text(if (busy) "Adding…" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        },
    )
}

@Composable
private fun BulkRestockDialog(
    rows: List<StockRowDto>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (List<Pair<String, Int>>) -> Unit,
) {
    val qtyByProductId = remember(rows) { mutableStateMapOf<String, String>() }
    LaunchedEffect(rows) {
        rows.forEach { row ->
            val pid = row.product?.id ?: return@forEach
            if (pid !in qtyByProductId) {
                val defaultQty = (row.product?.caseQty ?: 50).coerceAtLeast(1)
                qtyByProductId[pid] = defaultQty.toString()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk reorder", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Text(
                    "Review quantities, then continue to payment to place your restock order.",
                    fontSize = 13.sp,
                    color = WholesaleMuted,
                )
                rows.forEach { row ->
                    val pid = row.product?.id ?: return@forEach
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                row.product?.name ?: "Product",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text("On hand: ${row.quantity}", fontSize = 11.sp, color = WholesaleMuted)
                        }
                        OutlinedTextField(
                            value = qtyByProductId[pid].orEmpty(),
                            onValueChange = { qtyByProductId[pid] = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Order qty") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.35f),
                            colors = MartFieldDefaults.outlinedColors(),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val items =
                        rows.mapNotNull { row ->
                            val pid = row.product?.id ?: return@mapNotNull null
                            val qty = qtyByProductId[pid]?.toIntOrNull() ?: return@mapNotNull null
                            if (qty <= 0) return@mapNotNull null
                            pid to qty
                        }
                    if (items.isNotEmpty()) onConfirm(items)
                },
                enabled = !busy && rows.isNotEmpty(),
            ) {
                Text(if (busy) "Loading…" else "Continue to payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        },
    )
}

@Composable
fun DealerProfileTab(
    user: SessionUser,
    ui: MainUiState,
    onLogout: () -> Unit,
    onOpenAccount: () -> Unit = {},
    onOpenBusinessAddress: () -> Unit = {},
    onOpenPaymentDetails: () -> Unit = {},
    onOpenGstDetails: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenDocuments: () -> Unit = {},
    onOpenHelp: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onManageSkus: () -> Unit = {},
) {
    LaunchedEffect(user.id) {
        DealerProfileStore.hydrateFromUser(user)
    }
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    val delivered = orders.count { it.status.equals("DELIVERED", true) }
    val active = orders.count { !it.status.equals("DELIVERED", true) && !it.status.equals("CANCELLED", true) }
    val displayName = DealerProfileStore.displayName.ifBlank { user.name }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        item {
            FmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FmAvatar(displayName, size = 56.dp, tint = WholesaleGreen)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                        Text(
                            "${user.email} · Dealer",
                            fontSize = 13.sp,
                            color = WholesaleMuted,
                        )
                        DealerProfileStore.contactPhone.takeIf { it.isNotBlank() }?.let { phone ->
                            Text(phone, fontSize = 12.sp, color = WholesaleInk4, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    FmBadge("ACTIVE")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FmStatCard("Orders", "${orders.size}", modifier = Modifier.weight(1f))
                FmStatCard("Delivered", "$delivered", modifier = Modifier.weight(1f))
                FmStatCard("Active", "$active", modifier = Modifier.weight(1f))
            }
        }
        item {
            FmCard(padding = androidx.compose.foundation.layout.PaddingValues(6.dp)) {
                DealerProfileRow(
                    Icons.Outlined.GridView,
                    "SKU management",
                    "Products & pricing",
                    onClick = onManageSkus,
                )
                DealerProfileRow(
                    Icons.Outlined.Edit,
                    "Account",
                    displayName,
                    onClick = onOpenAccount,
                )
                DealerProfileRow(
                    Icons.Outlined.LocalShipping,
                    "Business address",
                    DealerProfileStore.addressSummary(),
                    onClick = onOpenBusinessAddress,
                )
                DealerProfileRow(
                    Icons.Outlined.AccountBalance,
                    "Payment details",
                    DealerProfileStore.paymentSummary(),
                    onClick = onOpenPaymentDetails,
                )
                DealerProfileRow(
                    Icons.Outlined.Receipt,
                    "GST details",
                    DealerProfileStore.gstin.ifBlank { "Add GSTIN" },
                    onClick = onOpenGstDetails,
                )
                DealerProfileRow(
                    Icons.Outlined.Notifications,
                    "Notifications",
                    when {
                        DealerProfileStore.orderAlerts && DealerProfileStore.stockAlerts ->
                            "Orders & stock alerts on"
                        DealerProfileStore.orderAlerts -> "Order alerts on"
                        else -> "Alerts off"
                    },
                    onClick = onOpenNotifications,
                )
                DealerProfileRow(
                    Icons.Outlined.Description,
                    "Documents",
                    user.documentStatus.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                    onClick = onOpenDocuments,
                )
                DealerProfileRow(
                    Icons.Outlined.Info,
                    "Help & support",
                    "Dealer desk & FAQs",
                    onClick = onOpenHelp,
                    last = true,
                )
            }
        }
        item {
            FmCard(onClick = onLogout) {
                Text("Sign out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleRed)
            }
        }
        item {
            com.mart.distribution.demo.ui.components.MartAppFooter(onPrivacyPolicy = onOpenPrivacyPolicy)
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun DealerProfileRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sub: String,
    onClick: () -> Unit,
    last: Boolean = false,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(WholesaleBlueTint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = WholesaleGreen, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText)
            Text(sub, fontSize = 12.sp, color = WholesaleMuted, modifier = Modifier.padding(top = 2.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("›", fontSize = 18.sp, color = WholesaleMuted)
    }
    if (!last) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder))
    }
}
