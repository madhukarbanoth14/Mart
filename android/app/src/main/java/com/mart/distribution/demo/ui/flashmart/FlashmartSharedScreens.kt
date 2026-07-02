package com.mart.distribution.demo.ui.flashmart

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.mart.distribution.demo.data.api.dto.OnboardingDocumentDto
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.StockRowDto
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGold
import com.mart.distribution.demo.ui.theme.WholesaleGoldInk
import com.mart.distribution.demo.ui.theme.WholesaleGoldTint
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrange
import com.mart.distribution.demo.ui.theme.WholesaleOrangeTint
import com.mart.distribution.demo.ui.theme.WholesaleText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class FmNotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val icon: ImageVector,
    val tint: Color,
    val tintBg: Color,
    val unread: Boolean,
    val sortKey: String,
)

data class FmNotificationGroup(
    val day: String,
    val items: List<FmNotificationItem>,
)

@Composable
fun FmNotificationsInbox(
    groups: List<FmNotificationGroup>,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit = {},
    emptyMessage: String = "You're all caught up. Order and document updates will appear here.",
) {
    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        FmAppHeader(
            title = "Notifications",
            onBack = onBack,
            right = {
                if (groups.any { g -> g.items.any { it.unread } }) {
                    IconButton(onClick = onMarkAllRead) {
                        Icon(Icons.Outlined.Check, contentDescription = "Mark all read", tint = WholesaleGreen)
                    }
                }
            },
        )
        if (groups.isEmpty()) {
            FmEmptyState(
                icon = Icons.Outlined.Notifications,
                title = "No notifications",
                message = emptyMessage,
                modifier = Modifier.weight(1f),
            )
        } else {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                groups.forEach { group ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            group.day,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = WholesaleMuted,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                        FmCard(padding = androidx.compose.foundation.layout.PaddingValues(6.dp)) {
                            group.items.forEachIndexed { i, item ->
                                NotificationRow(item)
                                if (i < group.items.lastIndex) {
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NotificationRow(item: FmNotificationItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.tintBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icon, contentDescription = null, tint = item.tint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
            Text(item.body, fontSize = 13.sp, color = WholesaleMuted, lineHeight = 18.sp, modifier = Modifier.padding(top = 2.dp))
        }
        if (item.unread) {
            Box(
                modifier =
                    Modifier
                        .padding(top = 6.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(WholesaleGreen),
            )
        }
    }
}

@Composable
fun FmVerificationProgressCard(
    verifiedCount: Int,
    totalSlots: Int,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val pct = if (totalSlots == 0) 0f else verifiedCount.toFloat() / totalSlots.toFloat()
    FmCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 6.dp.toPx()
                    drawCircle(color = androidx.compose.ui.graphics.Color(0xFFE8EAED), style = Stroke(width = stroke))
                    drawArc(
                        color = androidx.compose.ui.graphics.Color(0xFF15924B),
                        startAngle = -90f,
                        sweepAngle = 360f * pct,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
                Text(
                    "${(pct * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WholesaleText,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Verification status", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                Text(subtitle, fontSize = 13.sp, color = WholesaleMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
fun FmDocumentCenterRow(
    doc: OnboardingDocumentDto,
    modifier: Modifier = Modifier,
) {
    val verified = doc.verificationStatus.equals("VERIFIED", true)
    val statusLabel =
        when {
            verified -> "Verified"
            doc.verificationStatus.equals("REJECTED", true) -> "Rejected"
            else -> "Pending"
        }
    val icon = documentIcon(doc.documentType ?: doc.label)
    val (bg, fg) =
        if (verified) WholesaleGreenTint to WholesaleGreen
        else WholesaleGoldTint to WholesaleGoldInk
    val dateText = formatDocDate(doc.verifiedAt ?: doc.uploadedAt, verified)

    FmCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(bg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(doc.label, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                Text(dateText, fontSize = 12.sp, color = WholesaleMuted, modifier = Modifier.padding(top = 1.dp))
            }
            FmBadge(doc.verificationStatus, label = statusLabel)
        }
    }
}

fun documentIcon(typeOrLabel: String): ImageVector {
    val key = typeOrLabel.uppercase(Locale.getDefault())
    return when {
        key.contains("AADHAAR") || key.contains("DOC") -> Icons.Outlined.Description
        key.contains("PAN") || key.contains("CARD") -> Icons.Outlined.CreditCard
        key.contains("GST") || key.contains("RECEIPT") -> Icons.Outlined.Receipt
        key.contains("TRADE") || key.contains("LICENSE") || key.contains("LAYER") -> Icons.Outlined.Layers
        else -> Icons.Outlined.Description
    }
}

fun formatDocDate(iso: String?, verified: Boolean): String {
    if (iso.isNullOrBlank()) return if (verified) "Verified" else "Uploaded recently"
    val datePart = iso.take(10)
    return try {
        val date = LocalDate.parse(datePart)
        val formatted = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
        if (verified) formatted else "Uploaded $formatted"
    } catch (_: Exception) {
        if (verified) "Verified $datePart" else "Uploaded $datePart"
    }
}

fun groupNotifications(items: List<FmNotificationItem>): List<FmNotificationGroup> {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val buckets = linkedMapOf<String, MutableList<FmNotificationItem>>()
    items.sortedByDescending { it.sortKey }.forEach { item ->
        val day =
            runCatching { LocalDate.parse(item.sortKey.take(10)) }.getOrNull()?.let { d ->
                when {
                    d == today -> "Today"
                    d == yesterday -> "Yesterday"
                    ChronoUnit.DAYS.between(d, today) < 7 -> d.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() }
                    else -> d.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
                }
            } ?: "Earlier"
        buckets.getOrPut(day) { mutableListOf() }.add(item)
    }
    return buckets.map { FmNotificationGroup(it.key, it.value) }
}

fun buildShopkeeperNotifications(
    orders: List<OrderDto>,
    docs: List<OnboardingDocumentDto>,
    readIds: Set<String>,
): List<FmNotificationGroup> {
    val items = mutableListOf<FmNotificationItem>()
    orders.forEach { order ->
        val shortId = order.id.takeLast(6).uppercase()
        val date = order.createdAt ?: ""
        when (order.status.uppercase(Locale.getDefault())) {
            "OUT_FOR_DELIVERY" ->
                items +=
                    FmNotificationItem(
                        id = "ord-ofd-${order.id}",
                        title = "Out for delivery",
                        body = "ORD-$shortId is on the way to your store.",
                        icon = Icons.Outlined.LocalShipping,
                        tint = WholesaleGreen,
                        tintBg = WholesaleGreenTint,
                        unread = "ord-ofd-${order.id}" !in readIds,
                        sortKey = date,
                    )
            "DELIVERED" ->
                items +=
                    FmNotificationItem(
                        id = "ord-del-${order.id}",
                        title = "Order delivered",
                        body = "ORD-$shortId delivered. Tap to view invoice.",
                        icon = Icons.Outlined.CheckCircle,
                        tint = WholesaleGreen,
                        tintBg = WholesaleGreenTint,
                        unread = "ord-del-${order.id}" !in readIds,
                        sortKey = date,
                    )
            "PENDING", "PLACED" ->
                items +=
                    FmNotificationItem(
                        id = "ord-pend-${order.id}",
                        title = "Order placed",
                        body = "ORD-$shortId is awaiting dealer confirmation.",
                        icon = Icons.Outlined.Schedule,
                        tint = WholesaleGoldInk,
                        tintBg = WholesaleGoldTint,
                        unread = "ord-pend-${order.id}" !in readIds,
                        sortKey = date,
                    )
        }
    }
    docs.forEach { doc ->
        if (doc.verificationStatus.equals("VERIFIED", true)) {
            items +=
                FmNotificationItem(
                    id = "doc-${doc.id}",
                    title = "Document verified",
                    body = "Your ${doc.label} was approved.",
                    icon = Icons.Outlined.Description,
                    tint = WholesaleBlue,
                    tintBg = WholesaleBlueTint,
                    unread = "doc-${doc.id}" !in readIds,
                    sortKey = doc.verifiedAt ?: doc.uploadedAt ?: "",
                )
        }
    }
    if (items.isEmpty()) {
        items +=
            FmNotificationItem(
                id = "promo-demo",
                title = "Offer just for you",
                body = "Extra discounts on staples this week from your dealer.",
                icon = Icons.Outlined.LocalOffer,
                tint = WholesaleGoldInk,
                tintBg = WholesaleGoldTint,
                unread = "promo-demo" !in readIds,
                sortKey = LocalDate.now().toString(),
            )
    }
    return groupNotifications(items)
}

fun buildDealerNotifications(
    orders: List<OrderDto>,
    stock: List<StockRowDto>,
    docs: List<OnboardingDocumentDto>,
    readIds: Set<String>,
): List<FmNotificationGroup> {
    val items = mutableListOf<FmNotificationItem>()
    orders.filter { it.kind?.uppercase() != "DEALER_RESTOCK" }.forEach { order ->
        val shortId = order.id.takeLast(6).uppercase()
        val shop = order.shopkeeper?.shopName ?: order.shopkeeper?.name ?: "Shopkeeper"
        val date = order.createdAt ?: ""
        when (order.status.uppercase(Locale.getDefault())) {
            "PENDING" ->
                items +=
                    FmNotificationItem(
                        id = "ord-new-${order.id}",
                        title = "New shopkeeper order",
                        body = "$shop placed ORD-$shortId. Tap to accept.",
                        icon = Icons.Outlined.ShoppingBag,
                        tint = WholesaleGoldInk,
                        tintBg = WholesaleGoldTint,
                        unread = "ord-new-${order.id}" !in readIds,
                        sortKey = date,
                    )
            "OUT_FOR_DELIVERY" ->
                items +=
                    FmNotificationItem(
                        id = "ord-ofd-${order.id}",
                        title = "Out for delivery",
                        body = "ORD-$shortId marked out for delivery.",
                        icon = Icons.Outlined.LocalShipping,
                        tint = WholesaleBlue,
                        tintBg = WholesaleBlueTint,
                        unread = "ord-ofd-${order.id}" !in readIds,
                        sortKey = date,
                    )
        }
        if (order.paymentStatus.equals("PAID", true)) {
            items +=
                FmNotificationItem(
                    id = "pay-${order.id}",
                    title = "Payment received",
                    body = "ORD-$shortId was paid by $shop.",
                    icon = Icons.Outlined.Receipt,
                    tint = WholesaleGreen,
                    tintBg = WholesaleGreenTint,
                    unread = "pay-${order.id}" !in readIds,
                    sortKey = date,
                )
        }
    }
    stock.filter { it.quantity < 5 }.take(2).forEach { row ->
        items +=
            FmNotificationItem(
                id = "stock-${row.id}",
                title = "Low stock alert",
                body = "${row.product?.name ?: "SKU"} is running low (${row.quantity} left).",
                icon = Icons.Outlined.Schedule,
                tint = WholesaleOrange,
                tintBg = WholesaleOrangeTint,
                unread = "stock-${row.id}" !in readIds,
                sortKey = LocalDate.now().toString(),
            )
    }
    docs.filter { it.verificationStatus.equals("VERIFIED", true) }.forEach { doc ->
        items +=
            FmNotificationItem(
                id = "doc-${doc.id}",
                title = "Document verified",
                body = "Your ${doc.label} was approved.",
                icon = Icons.Outlined.Description,
                tint = WholesaleBlue,
                tintBg = WholesaleBlueTint,
                unread = "doc-${doc.id}" !in readIds,
                sortKey = doc.verifiedAt ?: doc.uploadedAt ?: "",
            )
    }
    return groupNotifications(items)
}

@Composable
fun FmDocumentCenterLoading() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = WholesaleGreen, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
    }
}
