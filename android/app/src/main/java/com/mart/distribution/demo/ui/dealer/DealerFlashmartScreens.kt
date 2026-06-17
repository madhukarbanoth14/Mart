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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.mart.distribution.demo.data.api.dto.StockRowDto
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.ui.flashmart.FmAvatar
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmSegmentedControl
import com.mart.distribution.demo.ui.flashmart.FmStatCard
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBorder
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
    onOpenProducts: () -> Unit = {},
    onOpenOrder: (String) -> Unit,
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
    val lowStock =
        when (val s = ui.stock) {
            is LoadState.Ok -> s.data.count { it.quantity < 5 }
            else -> 0
        }
    val recent = orders.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Dealer dashboard", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WholesaleGreen)
                    Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                }
                FmAvatar(user.name, size = 42.dp, tint = WholesaleGreen)
            }
        }

        item {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(WholesaleGreen, Color(0xFF086B4B))))
                        .padding(20.dp),
            ) {
                Column {
                    Text("Today's revenue", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(0.85f))
                    Text(formatDecimal(todayRev), fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "${orders.count { it.paymentStatus.equals("PAID", true) }} paid orders",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.82f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                FmStatCard(
                    label = "Pending orders",
                    value = "${summary?.pendingOrders ?: pending.size}",
                    sub = "Need your action",
                    modifier = Modifier.weight(1f),
                )
                FmStatCard(
                    label = "In transit",
                    value = "${active.size}",
                    sub = "Accepted / out",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            FmCard(
                onClick = onOpenProducts,
                padding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(WholesaleBlue.copy(0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.LocalShipping, null, tint = WholesaleBlue, modifier = Modifier.size(21.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Browse & order stock", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                        Text("10% dealer discount from KNSR catalog", fontSize = 12.sp, color = WholesaleMuted)
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
                DealerOrderCard(order = order, onClick = { onOpenOrder(order.id) })
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
                            Text("Reorder to avoid stockouts", fontSize = 12.sp, color = Color(0xFF9A6410))
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
                                FmAvatar(o.shopkeeper?.name ?: "Shop", size = 38.dp, tint = WholesaleGreen)
                                Column {
                                    Text(
                                        o.shopkeeper?.name ?: "Shopkeeper",
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
            is LoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WholesaleGreen)
            }
            is LoadState.Ok ->
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No ${segment.lowercase()} orders", color = WholesaleInk4, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        items(filtered, key = { it.id }) { order ->
                            DealerOrderCard(order = order, onClick = { onOpen(order.id) })
                        }
                    }
                }
            is LoadState.Err -> Text(o.message, color = WholesaleRed)
            else -> {}
        }
    }
}

@Composable
private fun DealerOrderCard(
    order: OrderDto,
    onClick: () -> Unit,
) {
    FmCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FmAvatar(order.shopkeeper?.name ?: "Shop", size = 42.dp, tint = WholesaleGreen)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    order.shopkeeper?.name ?: "Shopkeeper",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text("${order.id.takeLast(8)} · ${order.items?.size ?: 0} items", fontSize = 12.sp, color = WholesaleMuted)
            }
            Text(formatDecimal(order.finalAmount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
        }
        Spacer(Modifier.height(10.dp))
        FmBadge(order.status)
    }
}

@Composable
fun DealerStockTab(ui: MainUiState) {
    when (val s = ui.stock) {
        is LoadState.Loading ->
            Box(Modifier.fillMaxSize().background(WholesaleBg), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WholesaleGreen)
            }
        is LoadState.Err -> Text(s.message, color = WholesaleRed, modifier = Modifier.padding(16.dp))
        is LoadState.Ok ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text("${s.data.size} SKUs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted)
                    Spacer(Modifier.height(8.dp))
                }
                items(s.data, key = { it.id }) { row -> DealerStockRow(row) }
            }
        else -> Text("Stock loads for dealers.", modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun DealerStockRow(row: StockRowDto) {
    val low = row.quantity < 5
    FmCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(row.product?.name ?: "Product", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                Text("SKU ${row.product?.sku ?: "—"}", fontSize = 12.sp, color = WholesaleMuted)
            }
            if (low) {
                FmBadge("low", label = "Low stock")
            } else {
                Text("Qty ${row.quantity}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
            }
        }
    }
}

@Composable
fun DealerProfileTab(
    user: SessionUser,
    ui: MainUiState,
    onLogout: () -> Unit,
) {
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            FmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FmAvatar(user.name, size = 56.dp, tint = WholesaleGreen)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                        Text(user.email, fontSize = 13.sp, color = WholesaleMuted)
                    }
                    FmBadge("ACTIVE")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FmStatCard("Orders", "${orders.size}", modifier = Modifier.weight(1f))
                FmStatCard(
                    "Delivered",
                    "${orders.count { it.status.equals("DELIVERED", true) }}",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            FmCard(onClick = onLogout) {
                Text("Sign out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleRed)
            }
        }
    }
}
