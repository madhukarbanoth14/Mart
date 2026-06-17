package com.mart.distribution.demo.ui.shopkeeper

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.mart.distribution.demo.ui.components.ProductImageStyle
import com.mart.distribution.demo.ui.components.ProductThumbnail
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmSegmentedControl
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

@Composable
fun ShopkeeperOrdersTab(
    ui: MainUiState,
    onOpen: (String) -> Unit,
) {
    var segment by rememberSaveable { mutableStateOf("All") }
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    val filtered =
        remember(orders, segment) {
            when (segment) {
                "Active" -> orders.filter { !it.status.equals("DELIVERED", true) && !it.status.equals("CANCELLED", true) }
                "Delivered" -> orders.filter { it.status.equals("DELIVERED", true) }
                else -> orders
            }
        }

    Column(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        FmSegmentedControl(
            options = listOf("All", "Active", "Delivered"),
            selected = segment,
            onSelect = { segment = it },
        )
        Spacer(Modifier.height(14.dp))
        when (val o = ui.orders) {
            is LoadState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WholesaleBlue)
                }
            is LoadState.Err -> Text(o.message, color = com.mart.distribution.demo.ui.theme.WholesaleRed)
            is LoadState.Ok ->
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No ${segment.lowercase()} orders",
                            fontSize = 14.sp,
                            color = WholesaleInk4,
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        items(filtered, key = { it.id }) { order ->
                            ShopkeeperOrderCard(order = order, onClick = { onOpen(order.id) })
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            else -> {}
        }
    }
}

@Composable
private fun ShopkeeperOrderCard(
    order: OrderDto,
    onClick: () -> Unit,
) {
    val items = order.items.orEmpty()
    FmCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(
                    order.id.takeLast(8).uppercase(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WholesaleText,
                )
            }
            FmBadge(order.status)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row {
                items.take(4).forEachIndexed { i, line ->
                    Box(
                        modifier =
                            Modifier
                                .padding(start = if (i > 0) (-10).dp else 0.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(com.mart.distribution.demo.ui.theme.WholesaleSurface),
                    ) {
                        ProductThumbnail(
                            imageUrl = line.product?.imageUrl,
                            brandLogoUrl = line.product?.brand?.logoUrl,
                            productName = line.product?.name ?: "Item",
                            brandName = line.product?.brand?.name,
                            style = ProductImageStyle.Grid,
                            cornerRadius = 9.dp,
                            modifier = Modifier.size(38.dp),
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    "${order.createdAt?.take(10) ?: "—"} · ${items.size} items",
                    fontSize = 12.sp,
                    color = WholesaleMuted,
                )
            }
            Text(
                formatDecimal(order.finalAmount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WholesaleText,
            )
        }
    }
}
