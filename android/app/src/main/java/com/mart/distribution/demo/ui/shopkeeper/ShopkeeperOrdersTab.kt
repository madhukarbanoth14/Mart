package com.mart.distribution.demo.ui.shopkeeper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmEmptyStateHero
import com.mart.distribution.demo.ui.flashmart.FmErrorScreen
import com.mart.distribution.demo.ui.flashmart.FmSegmentedControl
import com.mart.distribution.demo.ui.flashmart.FmSkeletonListScreen
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.theme.WholesaleSurface3
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

@Composable
fun ShopkeeperOrdersTab(
    ui: MainUiState,
    onOpen: (String) -> Unit,
    onInvoice: (String) -> Unit = {},
    onTrack: (String) -> Unit = {},
    onReorder: (String) -> Unit = {},
    onBrowseCatalog: () -> Unit = {},
) {
    var segment by rememberSaveable { mutableStateOf("All") }
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    val filtered =
        remember(orders, segment) {
            when (segment) {
                "Pending" ->
                    orders.filter {
                        it.status.equals("PENDING", true) ||
                            it.status.equals("PLACED", true) ||
                            it.status.equals("DEALER_CONFIRMED", true) ||
                            it.status.equals("OUT_FOR_DELIVERY", true)
                    }
                "Delivered" -> orders.filter { it.status.equals("DELIVERED", true) }
                "Cancelled" -> orders.filter { it.status.equals("CANCELLED", true) }
                else -> orders
            }
        }

    Column(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        FmSegmentedControl(
            options = listOf("All", "Pending", "Delivered", "Cancelled"),
            selected = segment,
            onSelect = { segment = it },
        )
        Spacer(Modifier.height(14.dp))
        when (val o = ui.orders) {
            is LoadState.Loading -> FmSkeletonListScreen(modifier = Modifier.weight(1f))
            is LoadState.Err ->
                FmErrorScreen(
                    title = "Couldn't load orders",
                    message = o.message,
                    onPrimaryAction = { /* parent reloads via tab */ },
                    modifier = Modifier.weight(1f),
                )
            is LoadState.Ok ->
                if (filtered.isEmpty()) {
                    FmEmptyStateHero(
                        icon = Icons.Outlined.ShoppingBag,
                        title = if (segment == "All") "No orders yet" else "No ${segment.lowercase()} orders",
                        message =
                            if (segment == "All") {
                                "When you place your first order, it'll show up here with live delivery tracking."
                            } else {
                                "Try another filter or browse the catalog to place an order."
                            },
                        actionLabel = if (segment == "All") "Browse catalog" else null,
                        onAction = if (segment == "All") onBrowseCatalog else null,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        itemsIndexed(
                            filtered,
                            key = { index, order -> "${order.id}-$index" },
                        ) { _, order ->
                            ShopkeeperOrderCard(
                                order = order,
                                onView = { onOpen(order.id) },
                                onInvoice = { onInvoice(order.id) },
                                onTrack = { onTrack(order.id) },
                                onReorder = { onReorder(order.id) },
                            )
                        }
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            else -> {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShopkeeperOrderCard(
    order: OrderDto,
    onView: () -> Unit,
    onInvoice: () -> Unit,
    onTrack: () -> Unit,
    onReorder: () -> Unit,
) {
    val items = order.items.orEmpty()
    val delivered = order.status.equals("DELIVERED", true)
    FmCard(onClick = onView) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WholesaleSurface3),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = WholesaleMuted, modifier = Modifier.size(19.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    order.id.takeLast(8).uppercase(),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WholesaleText,
                )
                Text(
                    "${order.createdAt?.take(10) ?: "—"} · ${items.size} items",
                    fontSize = 12.sp,
                    color = WholesaleMuted,
                )
            }
            FmBadge(order.status)
        }

        if (items.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.take(3).forEach { line ->
                    val name = line.product?.name?.split(" ")?.take(2)?.joinToString(" ") ?: "Item"
                    Text(
                        "${line.quantity}× $name",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WholesaleMuted,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(WholesaleSurface3)
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .padding(bottom = 4.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(com.mart.distribution.demo.ui.theme.WholesaleBorder),
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatDecimal(order.finalAmount),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = WholesaleText,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (delivered) {
                    FmButton(
                        text = "Invoice",
                        onClick = onInvoice,
                        variant = FmButtonVariant.Outline,
                        fullWidth = false,
                    )
                    FmButton(
                        text = "Reorder",
                        onClick = onReorder,
                        variant = FmButtonVariant.Soft,
                        fullWidth = false,
                    )
                } else {
                    FmButton(
                        text = "Track",
                        onClick = onTrack,
                        variant = FmButtonVariant.Primary,
                        fullWidth = false,
                    )
                }
            }
        }
    }
}
