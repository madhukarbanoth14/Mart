package com.mart.distribution.demo.ui.shopkeeper

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.cartMath
import com.mart.distribution.demo.data.cart.CartLine
import com.mart.distribution.demo.data.payment.RazorpayCheckoutOptions
import com.mart.distribution.demo.data.profile.ShopkeeperProfileStore
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmDialog
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmInfoBanner
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmTextField
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBorder2
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators

@Composable
fun ShopkeeperPaymentScreen(
    lines: List<CartLine>,
    buyerRole: String = "SHOPKEEPER",
    useRazorpayCheckout: Boolean = false,
    onBack: () -> Unit,
    onPayOnline: (paymentMethod: String) -> Unit,
    onPayLater: () -> Unit,
    paying: Boolean,
    placeError: String?,
    paymentMessage: String? = null,
) {
    val methods = remember(useRazorpayCheckout) { paymentMethodOptions(useRazorpayCheckout) }
    var method by remember { mutableStateOf("card") }
    var showCardDialog by remember { mutableStateOf(false) }
    var showUpiDialog by remember { mutableStateOf(false) }
    var methodError by remember { mutableStateOf<String?>(null) }
    val math = remember(lines, buyerRole) { lines.cartMath(buyerRole) }
    val totalLabel = if (lines.isEmpty()) "—" else formatDecimal(math.total)
    val selected = methods.firstOrNull { it.id == method }

    fun startPay() {
        methodError = null
        val option = methods.firstOrNull { it.id == method } ?: return
        if (!option.isEnabled(useRazorpayCheckout)) {
            methodError = "This method requires Razorpay (production / live API build)."
            return
        }
        when {
            method == "cod" -> onPayLater()
            useRazorpayCheckout && RazorpayCheckoutOptions.isRazorpayOnlineMethod(method) ->
                onPayOnline(method)
            method == "card" -> showCardDialog = true
            method == "upi" -> showUpiDialog = true
            else -> onPayOnline(method)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            FmAppHeader(
                title = "Payment",
                subtitle = if (lines.isEmpty()) "No items" else "${lines.size} items · secure checkout",
                onBack = if (paying) null else onBack,
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = FmSpacing.listH),
                verticalArrangement = Arrangement.spacedBy(FmSpacing.itemGap),
            ) {
                FmCard(
                    padding = androidx.compose.foundation.layout.PaddingValues(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Amount payable", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted)
                        Text(totalLabel, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = WholesaleText, letterSpacing = (-1).sp)
                        Text("Incl. GST · ${lines.size} items", fontSize = 12.sp, color = WholesaleMuted, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                com.mart.distribution.demo.ui.flashmart.FmSectionLabel(title = "Payment method")
                methods.forEach { option ->
                    PaymentMethodCard(
                        option = option,
                        selected = method == option.id,
                        enabled = option.isEnabled(useRazorpayCheckout),
                        onSelect = {
                            method = option.id
                            methodError = null
                        },
                    )
                }

                FmInfoBanner(
                    message =
                        if (useRazorpayCheckout) {
                            "Secured by Razorpay · enter payment details in the gateway"
                        } else {
                            "Demo mode · card & UPI only · no real charges"
                        },
                )
                methodError?.let { FmErrorBanner(message = it) }
                placeError?.let { FmErrorBanner(message = it) }
                paymentMessage?.let {
                    FmInfoBanner(message = it, tint = com.mart.distribution.demo.ui.theme.WholesaleBlueTint)
                }
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
            if (paying) {
                FmButton(text = "Processing…", onClick = {}, enabled = false, variant = FmButtonVariant.Dark)
                Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WholesaleBlue)
                }
            } else {
                FmButton(
                    text =
                        when {
                            method == "cod" -> "Place order · pay later"
                            useRazorpayCheckout && selected?.razorpayGateway == true ->
                                "Continue to Razorpay · $totalLabel"
                            else -> "Pay $totalLabel"
                        },
                    onClick = { startPay() },
                    variant = FmButtonVariant.Dark,
                    enabled = lines.isNotEmpty() && (selected?.isEnabled(useRazorpayCheckout) != false),
                )
            }
        }
    }

    if (!useRazorpayCheckout && showCardDialog) {
        DemoCardPaymentDialog(
            totalLabel = totalLabel,
            onDismiss = { showCardDialog = false },
            onConfirm = {
                showCardDialog = false
                onPayOnline("card")
            },
        )
    }

    if (!useRazorpayCheckout && showUpiDialog) {
        FmDialog(
            title = "Pay with UPI",
            onDismiss = { showUpiDialog = false },
            confirmLabel = "Pay now",
            onConfirm = {
                showUpiDialog = false
                onPayOnline("upi")
            },
        ) {
            Text(
                "Confirm demo payment from ${ShopkeeperProfileStore.DUMMY_UPI_ID} for $totalLabel?",
                fontSize = 14.sp,
                color = WholesaleText,
            )
        }
    }
}

@Composable
private fun DemoCardPaymentDialog(
    totalLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var cardNumber by remember { mutableStateOf(ShopkeeperProfileStore.DUMMY_CARD_NUMBER.filter(Char::isDigit)) }
    var expiry by remember { mutableStateOf(ShopkeeperProfileStore.DUMMY_CARD_EXPIRY) }
    var cvv by remember { mutableStateOf(ShopkeeperProfileStore.DUMMY_CARD_CVV) }
    var name by remember { mutableStateOf(ShopkeeperProfileStore.DUMMY_CARD_NAME) }
    var cardError by remember { mutableStateOf<String?>(null) }

    FmDialog(
        title = "Enter card details",
        onDismiss = onDismiss,
        confirmLabel = "Pay $totalLabel",
        onConfirm = {
            FieldValidators.cardNumber(cardNumber)?.let { cardError = it; return@FmDialog }
            FieldValidators.cardExpiry(expiry)?.let { cardError = it; return@FmDialog }
            FieldValidators.cvv(cvv)?.let { cardError = it; return@FmDialog }
            FieldValidators.cardName(name)?.let { cardError = it; return@FmDialog }
            onConfirm()
        },
    ) {
        FmInfoBanner(message = "Demo card — no real payment is processed.")
        FmTextField(
            value = cardNumber,
            onValueChange = { cardNumber = FieldFilters.cardNumber(it); cardError = null },
            label = "Card number",
            placeholder = "4111 1111 1111 1111",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FmTextField(
                value = expiry,
                onValueChange = { expiry = FieldFilters.cardExpiry(it); cardError = null },
                label = "Expiry",
                placeholder = "MM/YY",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            FmTextField(
                value = cvv,
                onValueChange = { cvv = FieldFilters.cvv(it); cardError = null },
                label = "CVV",
                placeholder = "123",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )
        }
        FmTextField(
            value = name,
            onValueChange = { name = FieldFilters.personName(it); cardError = null },
            label = "Name on card",
        )
        cardError?.let { FmErrorBanner(message = it) }
        Text("Pay $totalLabel", fontWeight = FontWeight.SemiBold, color = WholesaleText, fontSize = 15.sp)
    }
}

@Composable
private fun PaymentMethodCard(
    option: PaymentMethodOption,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    val alpha = if (enabled) 1f else 0.45f
    FmCard(
        onClick = { if (enabled) onSelect() },
        modifier =
            Modifier
                .fillMaxWidth()
                .alpha(alpha)
                .then(
                    if (selected && enabled) {
                        Modifier.border(1.dp, WholesaleBlue, RoundedCornerShape(16.dp))
                    } else {
                        Modifier
                    },
                ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Box(
                modifier =
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected && enabled) WholesaleBlue else WholesaleSurface2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    option.icon,
                    null,
                    tint = if (selected && enabled) Color.White else WholesaleMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(option.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                Text(option.subtitle, fontSize = 12.sp, color = WholesaleMuted)
            }
            Box(
                modifier =
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .then(
                            if (selected && enabled) {
                                Modifier.background(WholesaleBlue).border(7.dp, WholesaleBlue, CircleShape)
                            } else {
                                Modifier.border(2.dp, WholesaleBorder2, CircleShape)
                            },
                        ),
            )
        }
    }
}
