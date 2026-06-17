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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.ShopkeeperSummaryDto
import com.mart.distribution.demo.data.profile.ShopkeeperProfileStore
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.ui.flashmart.FmAvatar
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmHeroCard
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleText
import java.util.Calendar

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
fun ShopkeeperFlashmartHome(
    ui: MainUiState,
    user: SessionUser,
    summary: ShopkeeperSummaryDto?,
    onOpenCatalog: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenOrder: (String) -> Unit,
) {
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    val inProgress =
        summary?.openOrders ?: orders.count {
            !it.status.equals("DELIVERED", true) && !it.status.equals("CANCELLED", true)
        }
    val recent = orders.take(4)
    val firstName = user.name.split(" ").firstOrNull() ?: user.name

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${greeting()}, $firstName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = com.mart.distribution.demo.ui.theme.WholesaleBlue,
                    )
                    Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                }
                FmAvatar(user.name, size = 42.dp)
            }
        }

        item {
            FmHeroCard(
                totalOrders = orders.size,
                inProgress = inProgress,
                onNewOrder = onOpenCatalog,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickAction("Browse", Icons.Outlined.LocalShipping, onOpenCatalog, Modifier.weight(1f))
                QuickAction("Invoices", Icons.Outlined.Receipt, onOpenOrders, Modifier.weight(1f))
                QuickAction("Track", Icons.Outlined.LocalShipping, onOpenOrders, Modifier.weight(1f))
            }
        }

        if (recent.isNotEmpty()) {
            item {
                FmSectionLabel(
                    title = "Recent orders",
                    action = "See all",
                    onAction = onOpenOrders,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                FmCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    padding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                ) {
                    recent.forEachIndexed { i, o ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenOrder(o.id) }
                                    .padding(horizontal = 12.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(WholesaleSurface2),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Outlined.Receipt, null, tint = WholesaleMuted, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        o.id.takeLast(8).uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WholesaleText,
                                    )
                                    Text(
                                        "${o.createdAt?.take(10) ?: "—"} · ${o.items?.size ?: 0} items",
                                        fontSize = 12.sp,
                                        color = WholesaleMuted,
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    remember(o.finalAmount) {
                                        com.mart.distribution.demo.ui.util.formatDecimal(o.finalAmount)
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WholesaleText,
                                )
                                Spacer(Modifier.height(5.dp))
                                FmBadge(o.status)
                            }
                        }
                        if (i < recent.lastIndex) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder))
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FmCard(
        modifier = modifier,
        padding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        onClick = onClick,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(com.mart.distribution.demo.ui.theme.WholesaleBlueTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = com.mart.distribution.demo.ui.theme.WholesaleBlue, modifier = Modifier.size(20.dp))
            }
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted)
        }
    }
}

@Composable
fun ShopkeeperProfileTab(
    ui: MainUiState,
    user: SessionUser,
    onLogout: () -> Unit,
    onOpenStoreAddress: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
    onOpenGstDetails: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenHelp: () -> Unit,
) {
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    val delivered = orders.count { it.status.equals("DELIVERED", true) }
    val active = orders.count { !it.status.equals("DELIVERED", true) && !it.status.equals("CANCELLED", true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        item {
            FmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FmAvatar(user.name, size = 56.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                        Text("${user.email} · ${user.role.replace('_', ' ')}", fontSize = 13.sp, color = WholesaleMuted)
                    }
                    FmBadge("ACTIVE")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Orders", "${orders.size}", Modifier.weight(1f))
                StatTile("Delivered", "$delivered", Modifier.weight(1f))
                StatTile("Active", "$active", Modifier.weight(1f))
            }
        }
        item {
            FmCard(padding = androidx.compose.foundation.layout.PaddingValues(6.dp)) {
                ProfileRow(
                    Icons.Outlined.LocationOn,
                    "Store address",
                    ShopkeeperProfileStore.storeAddress.take(40).let { if (ShopkeeperProfileStore.storeAddress.length > 40) "$it…" else it },
                    onClick = onOpenStoreAddress,
                )
                ProfileRow(
                    Icons.Outlined.Payments,
                    "Payment methods",
                    ShopkeeperProfileStore.DUMMY_CARD_MASKED,
                    onClick = onOpenPaymentMethods,
                )
                ProfileRow(
                    Icons.Outlined.Receipt,
                    "GST details",
                    ShopkeeperProfileStore.gstin,
                    onClick = onOpenGstDetails,
                )
                ProfileRow(
                    Icons.Outlined.Notifications,
                    "Notifications",
                    if (ShopkeeperProfileStore.orderAlerts) "Order & delivery alerts on" else "Alerts off",
                    onClick = onOpenNotifications,
                )
                ProfileRow(
                    Icons.Outlined.Settings,
                    "Help & support",
                    "Chat, call, FAQs",
                    onClick = onOpenHelp,
                    last = true,
                )
            }
        }
        item {
            FmCard(onClick = onLogout) {
                Text("Sign out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = com.mart.distribution.demo.ui.theme.WholesaleRed)
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    FmCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
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
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(WholesaleSurface2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = WholesaleMuted, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText)
            Text(sub, fontSize = 12.sp, color = WholesaleMuted, modifier = Modifier.padding(top = 2.dp))
        }
        Text("›", fontSize = 18.sp, color = WholesaleMuted)
    }
    if (!last) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder))
    }
}
