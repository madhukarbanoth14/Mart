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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.cart.CartLine
import com.mart.distribution.demo.ui.components.ProductImageStyle
import com.mart.distribution.demo.ui.components.ProductThumbnail
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmMoneyRow
import com.mart.distribution.demo.ui.flashmart.FmStepper
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

@Composable
fun ShopkeeperCartScreen(
    lines: List<CartLine>,
    onBack: () -> Unit,
    onQty: (String, Int) -> Unit,
    onCheckout: () -> Unit,
    onProceedToPayment: () -> Unit,
    placeError: String?,
    onBrowse: () -> Unit,
) {
    val pricedTotal =
        remember(lines) {
            lines.sumOf { line -> line.referenceUnitPrice?.let { it * line.quantity } ?: 0.0 }
        }
    val estimatedLabel =
        remember(lines, pricedTotal) {
            if (lines.isEmpty()) "—" else formatDecimal(pricedTotal)
        }

    Box(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            FmAppHeader(
                title = "Cart",
                subtitle = if (lines.isEmpty()) "Empty" else "${lines.size} items",
                onBack = onBack,
            )
            if (lines.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .height(72.dp)
                                .fillMaxWidth(0.35f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(WholesaleSurface2),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, null, tint = WholesaleInk4, modifier = Modifier.height(32.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Your cart is empty", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                    Spacer(Modifier.height(6.dp))
                    Text("Browse the catalog to add products.", fontSize = 13.sp, color = WholesaleMuted)
                    Spacer(Modifier.height(18.dp))
                    FmButton("Browse products", onClick = onBrowse, variant = FmButtonVariant.Soft, fullWidth = false)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(lines, key = { it.productId }) { line ->
                        FmCard(padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ProductThumbnail(
                                    imageUrl = line.imageUrl,
                                    brandLogoUrl = line.brandLogoUrl,
                                    productName = line.productName,
                                    brandName = null,
                                    style = ProductImageStyle.Grid,
                                    cornerRadius = 13.dp,
                                    modifier = Modifier.height(52.dp).fillMaxWidth(0.18f),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(line.productName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                                    val unit = line.referenceUnitPrice
                                    Text(
                                        buildString {
                                            if (unit != null) append(formatDecimal(unit))
                                            append(" · est. line")
                                        },
                                        fontSize = 12.sp,
                                        color = WholesaleInk4,
                                        modifier = Modifier.padding(vertical = 3.dp),
                                    )
                                    FmStepper(
                                        value = line.quantity,
                                        onChange = { v -> onQty(line.productId, v.coerceAtLeast(0)) },
                                    )
                                }
                                Text(
                                    formatDecimal((line.referenceUnitPrice ?: 0.0) * line.quantity),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WholesaleText,
                                )
                            }
                        }
                    }
                    item {
                        FmCard {
                            FmMoneyRow("Subtotal", estimatedLabel)
                            FmMoneyRow("Shopkeeper discount", "—")
                            FmMoneyRow("GST", "At checkout")
                            Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder.copy(alpha = 0.6f)))
                            FmMoneyRow("Total payable", estimatedLabel, strong = true)
                        }
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                        ) {
                            Text("⚡", fontSize = 14.sp)
                            Text(
                                "Delivered by your dealer · checkout uses server pricing",
                                fontSize = 12.sp,
                                color = WholesaleMuted,
                            )
                        }
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
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    androidx.compose.ui.graphics.Color.Transparent,
                                    WholesaleBg,
                                    WholesaleBg,
                                ),
                            ),
                        )
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                placeError?.let { Text(it, color = com.mart.distribution.demo.ui.theme.WholesaleRed, fontSize = 13.sp) }
                FmButton(
                    text = "Proceed to payment · $estimatedLabel",
                    onClick = onProceedToPayment,
                    variant = FmButtonVariant.Primary,
                )
                FmButton(
                    text = "Place order (pay later)",
                    onClick = onCheckout,
                    variant = FmButtonVariant.Outline,
                )
            }
        }
    }
}
