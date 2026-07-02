package com.mart.distribution.demo.ui.shopkeeper

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.toDoubleFromApiOrNull
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.navigation.NavBackHandler
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueDeep
import com.mart.distribution.demo.ui.theme.WholesaleBrandGlow
import com.mart.distribution.demo.ui.theme.WholesaleBrandNavy
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

@Composable
fun ShopkeeperWalletScreen(
    orders: List<OrderDto>,
    creditLimit: Double = 25_000.0,
    onBack: () -> Unit,
    onPayOutstanding: () -> Unit,
) {
    NavBackHandler(onBack)
    val outstanding =
        remember(orders) {
            orders
                .filter {
                    !it.paymentStatus.equals("PAID", true) &&
                        !it.status.equals("CANCELLED", true)
                }
                .sumOf { (it.finalAmount.toDoubleFromApiOrNull() ?: it.totalAmount.toDoubleFromApiOrNull() ?: 0.0) }
        }
    val paidToDate =
        remember(orders) {
            orders
                .filter { it.paymentStatus.equals("PAID", true) }
                .sumOf { (it.finalAmount.toDoubleFromApiOrNull() ?: it.totalAmount.toDoubleFromApiOrNull() ?: 0.0) }
        }
    val monthSpend =
        remember(orders) {
            orders.sumOf { (it.finalAmount.toDoubleFromApiOrNull() ?: it.totalAmount.toDoubleFromApiOrNull() ?: 0.0) } * 0.15
        }
    val available = (creditLimit - outstanding).coerceAtLeast(0.0)
    val usedPct = if (creditLimit > 0) (outstanding / creditLimit).coerceIn(0.0, 1.0) else 0.0
    val txns =
        remember(orders) {
            orders.take(8).map { o ->
                val paid = o.paymentStatus.equals("PAID", true)
                WalletTxn(
                    credit = paid,
                    label = "${o.id.takeLast(8).uppercase()} · ${o.items?.size ?: 0} items",
                    date = o.createdAt?.take(10) ?: "—",
                    amount = (o.finalAmount.toDoubleFromApiOrNull() ?: o.totalAmount.toDoubleFromApiOrNull() ?: 0.0),
                )
            }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = FmSpacing.listH, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            FmAppHeader(title = "Wallet & ledger", subtitle = "Outstanding & transactions", onBack = onBack)
        }
        item {
            val heroShape = RoundedCornerShape(22.dp)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(heroShape)
                        .background(Brush.linearGradient(listOf(WholesaleBlue, WholesaleBlueDeep, WholesaleBrandNavy)))
                        .padding(22.dp),
            ) {
                Column {
                    Text("Outstanding balance", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(0.85f))
                    Text(
                        formatDecimal(outstanding),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Text(
                        "Credit limit ${formatDecimal(creditLimit)} · Available ${formatDecimal(available)}",
                        fontSize = 12.5.sp,
                        color = Color.White.copy(0.82f),
                    )
                    LinearProgressIndicator(
                        progress = { usedPct.toFloat() },
                        modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(6.dp).clip(RoundedCornerShape(99.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(0.25f),
                    )
                    Text(
                        "${(usedPct * 100).toInt()}% of credit limit used",
                        fontSize = 11.5.sp,
                        color = Color.White.copy(0.8f),
                        modifier = Modifier.padding(top = 7.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    FmButton(
                        text = "Pay outstanding",
                        onClick = onPayOutstanding,
                        variant = FmButtonVariant.Outline,
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                StatMini("This month", formatDecimal(monthSpend), Modifier.weight(1f))
                StatMini("Paid to date", formatDecimal(paidToDate), Modifier.weight(1f))
            }
        }
        item { FmSectionLabel(title = "Transaction ledger") }
        item {
            FmCard(padding = androidx.compose.foundation.layout.PaddingValues(6.dp)) {
                if (txns.isEmpty()) {
                    Text("No transactions yet", fontSize = 13.sp, color = WholesaleMuted, modifier = Modifier.padding(16.dp))
                } else {
                    txns.forEachIndexed { i, tx ->
                        WalletTxnRow(tx, last = i == txns.lastIndex)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

private data class WalletTxn(
    val credit: Boolean,
    val label: String,
    val date: String,
    val amount: Double,
)

@Composable
private fun StatMini(label: String, value: String, modifier: Modifier = Modifier) {
    FmCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(15.dp)) {
        Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WholesaleText, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun WalletTxnRow(txn: WalletTxn, last: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (txn.credit) WholesaleGreenTint else WholesaleSurface2),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                Icons.Outlined.ShoppingBag,
                null,
                tint = if (txn.credit) WholesaleGreen else WholesaleMuted,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(txn.label, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText, maxLines = 1)
            Text(txn.date, fontSize = 12.sp, color = WholesaleMuted, modifier = Modifier.padding(top = 1.dp))
        }
        Text(
            (if (txn.credit) "+" else "−") + formatDecimal(txn.amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (txn.credit) WholesaleGreen else WholesaleText,
        )
    }
    if (!last) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder))
    }
}
