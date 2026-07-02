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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.cartMath
import com.mart.distribution.demo.data.cart.CartLine
import com.mart.distribution.demo.ui.components.ProductImageStyle
import com.mart.distribution.demo.ui.components.ProductThumbnail
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmDealerDeliveryFooter
import com.mart.distribution.demo.ui.flashmart.FmEmptyState
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmGoldDiscountBadge
import com.mart.distribution.demo.ui.flashmart.FmMoneyRow
import com.mart.distribution.demo.ui.flashmart.FmQuantityInput
import com.mart.distribution.demo.ui.navigation.NavBackHandler
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal
import kotlin.math.roundToInt

@Composable
fun ShopkeeperCartScreen(
    lines: List<CartLine>,
    buyerRole: String = "SHOPKEEPER",
    maxOrderQuantity: Int = 10000,
    dealerName: String? = null,
    onBack: () -> Unit,
    onQty: (String, Int) -> Unit,
    onCheckout: () -> Unit,
    onProceedToPayment: () -> Unit,
    placeError: String?,
    onBrowse: () -> Unit,
) {
    NavBackHandler(onBack)
    val math = remember(lines, buyerRole) { lines.cartMath(buyerRole) }
    val estimatedLabel =
        remember(lines, math) {
            if (lines.isEmpty()) "—" else formatDecimal(math.total)
        }

    Box(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            FmAppHeader(
                title = "Cart",
                subtitle = if (lines.isEmpty()) "Empty" else "${lines.size} items",
                onBack = onBack,
            )
            if (lines.isEmpty()) {
                FmEmptyState(
                    icon = Icons.Outlined.ShoppingCart,
                    title = "Your cart is empty",
                    message = "Browse the catalog to add products for checkout.",
                    actionLabel = "Browse products",
                    onAction = onBrowse,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = FmSpacing.listH),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        FmCard(
                            padding = androidx.compose.foundation.layout.PaddingValues(6.dp),
                            modifier = Modifier.background(WholesaleSurface2, androidx.compose.foundation.shape.RoundedCornerShape(18.dp)),
                        ) {
                            lines.forEachIndexed { index, line ->
                                CartLineRow(
                                    line = line,
                                    maxOrderQuantity = maxOrderQuantity,
                                    onQty = onQty,
                                    showDivider = index < lines.lastIndex,
                                )
                            }
                        }
                    }
                    item {
                        FmCard {
                            FmMoneyRow("Subtotal", formatDecimal(math.subtotal))
                            FmMoneyRow(
                                if (buyerRole.equals("DEALER", ignoreCase = true)) "Dealer discount" else "Shopkeeper discount",
                                "− " + formatDecimal(math.discount),
                                accent = WholesaleGreen,
                            )
                            FmMoneyRow("GST", "+ " + formatDecimal(math.gst))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder.copy(alpha = 0.6f)))
                            FmMoneyRow("Total payable", estimatedLabel, strong = true)
                        }
                    }
                    item {
                        dealerName?.let { FmDealerDeliveryFooter(dealerName = it) }
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }

        if (lines.isNotEmpty()) {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(WholesaleSurface2)
                        .padding(horizontal = FmSpacing.listH, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                placeError?.let { FmErrorBanner(message = it) }
                FmButton(
                    text = "Checkout · $estimatedLabel",
                    onClick = onProceedToPayment,
                    variant = FmButtonVariant.Primary,
                )
            }
        }
    }
}

@Composable
private fun CartLineRow(
    line: CartLine,
    maxOrderQuantity: Int,
    onQty: (String, Int) -> Unit,
    showDivider: Boolean,
) {
    val unit = line.referenceUnitPrice ?: 0.0
    val discPct = line.discountPercent?.roundToInt() ?: 0
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ProductThumbnail(
            imageUrl = line.imageUrl,
            brandLogoUrl = line.brandLogoUrl,
            productName = line.productName,
            brandName = null,
            style = ProductImageStyle.Grid,
            cornerRadius = 13.dp,
            modifier = Modifier.height(54.dp).fillMaxWidth(0.18f),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(line.productName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WholesaleText, lineHeight = 17.sp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 3.dp),
            ) {
                Text(formatDecimal(unit), fontSize = 12.sp, color = WholesaleMuted)
                if (discPct > 0) {
                    Text("·", fontSize = 12.sp, color = WholesaleMuted)
                    FmGoldDiscountBadge(discPct)
                }
            }
            FmQuantityInput(
                value = line.quantity,
                onValueChange = { v -> onQty(line.productId, v.coerceAtLeast(0)) },
                min = 0,
                max = maxOrderQuantity,
                compact = true,
            )
        }
        Text(
            formatDecimal(unit * line.quantity),
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = WholesaleText,
        )
    }
    if (showDivider) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder).padding(horizontal = 8.dp))
    }
}
