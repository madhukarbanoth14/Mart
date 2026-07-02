package com.mart.distribution.demo.ui.shopkeeper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.cartMath
import com.mart.distribution.demo.data.cart.CartLine
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmAvatar
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmMoneyRow
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.navigation.NavBackHandler
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleDealerBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

@Composable
fun ShopkeeperCheckoutScreen(
    lines: List<CartLine>,
    buyerRole: String,
    storeName: String,
    storeAddress: String,
    dealerName: String?,
    dealerArea: String?,
    outstandingLabel: String?,
    useRazorpayCheckout: Boolean,
    onBack: () -> Unit,
    onChangeAddress: () -> Unit,
    onPlaceOrder: (paymentMethod: String) -> Unit,
    placing: Boolean,
    placeError: String?,
) {
    NavBackHandler(onBack)
    val math = remember(lines, buyerRole) { lines.cartMath(buyerRole) }
    val totalLabel = if (lines.isEmpty()) "—" else formatDecimal(math.total)
    val methods = remember(outstandingLabel, useRazorpayCheckout) {
        checkoutPaymentMethods(outstandingLabel, useRazorpayCheckout)
    }
    var method by remember { mutableStateOf("upi") }

    Box(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            FmAppHeader(
                title = "Checkout",
                subtitle = if (lines.isEmpty()) "Empty cart" else "${lines.size} items",
                onBack = if (placing) null else onBack,
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = FmSpacing.listH),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FmSectionLabel(title = "Delivery address", action = "Change", onAction = onChangeAddress)
                FmCard(padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(WholesaleBlueTint),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.Outlined.LocationOn,
                                null,
                                tint = WholesaleBlue,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(storeName, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                            Text(
                                storeAddress,
                                fontSize = 13.sp,
                                color = WholesaleMuted,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }

                if (dealerName != null) {
                    FmCard(
                        modifier = Modifier.background(WholesaleSurface2, RoundedCornerShape(18.dp)),
                        padding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                            FmAvatar(dealerName, size = 40.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dealerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                                Text(
                                    buildString {
                                        append("Your dealer")
                                        dealerArea?.let { append(" · $it") }
                                    },
                                    fontSize = 12.5.sp,
                                    color = WholesaleMuted,
                                )
                            }
                            FmBadge("ACTIVE")
                        }
                    }
                }

                FmSectionLabel(title = "Payment method")
                FmCard(padding = androidx.compose.foundation.layout.PaddingValues(6.dp)) {
                    methods.forEachIndexed { index, option ->
                        CheckoutPaymentRow(
                            option = option,
                            selected = method == option.id,
                            enabled = option.isEnabled(useRazorpayCheckout),
                            onSelect = { method = option.id },
                            showDivider = index < methods.lastIndex,
                        )
                    }
                }

                FmCard {
                    FmMoneyRow("Subtotal", formatDecimal(math.subtotal))
                    FmMoneyRow("Discount", "− " + formatDecimal(math.discount), accent = WholesaleGreen)
                    FmMoneyRow("GST", "+ " + formatDecimal(math.gst))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder.copy(alpha = 0.6f)))
                    FmMoneyRow("Total payable", totalLabel, strong = true)
                }

                placeError?.let { FmErrorBanner(message = it) }
                Spacer(Modifier.height(100.dp))
            }
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, WholesaleBg, WholesaleBg)))
                    .padding(horizontal = FmSpacing.listH, vertical = 24.dp),
        ) {
            FmButton(
                text = if (placing) "Placing order…" else "Place order · $totalLabel",
                onClick = { onPlaceOrder(method) },
                variant = FmButtonVariant.Primary,
                enabled = lines.isNotEmpty() && !placing,
            )
        }
    }
}

@Composable
private fun CheckoutPaymentRow(
    option: PaymentMethodOption,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    showDivider: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onSelect)
                .padding(horizontal = 8.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(WholesaleSurface2),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(option.icon, null, tint = WholesaleMuted, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(option.label, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
            Text(option.subtitle, fontSize = 12.sp, color = WholesaleMuted)
        }
        Box(
            modifier =
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (selected && enabled) 7.dp else 2.dp,
                        color = if (selected && enabled) WholesaleBlue else WholesaleBorder,
                        shape = CircleShape,
                    ),
        )
    }
    if (showDivider) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder))
    }
}

fun checkoutPaymentMethods(
    outstandingLabel: String?,
    useRazorpayCheckout: Boolean,
): List<PaymentMethodOption> {
    val creditSub = outstandingLabel?.let { "$it outstanding" } ?: "Pay from FlashMart credit"
    return listOf(
        PaymentMethodOption(
            id = "upi",
            label = "UPI",
            subtitle = "GPay · PhonePe · Paytm",
            icon = Icons.Outlined.Payments,
            razorpayGateway = true,
        ),
        PaymentMethodOption(
            id = "card",
            label = "Credit / Debit card",
            subtitle = "Visa, Mastercard, RuPay",
            icon = Icons.Outlined.CreditCard,
            razorpayGateway = true,
        ),
        PaymentMethodOption(
            id = "cod",
            label = "FlashMart credit",
            subtitle = creditSub,
            icon = Icons.Outlined.AccountBalanceWallet,
            razorpayGateway = false,
        ),
    )
}
