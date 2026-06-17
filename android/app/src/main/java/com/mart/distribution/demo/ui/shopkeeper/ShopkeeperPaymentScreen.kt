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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.cart.CartLine
import com.mart.distribution.demo.data.profile.ShopkeeperProfileStore
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleBorder2
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators
import com.mart.distribution.demo.ui.util.formatDecimal

@Composable
fun ShopkeeperPaymentScreen(
    lines: List<CartLine>,
    onBack: () -> Unit,
    onPayOnline: (paymentMethod: String) -> Unit,
    onPayLater: () -> Unit,
    paying: Boolean,
    placeError: String?,
) {
    var method by remember { mutableStateOf("card") }
    var showCardDialog by remember { mutableStateOf(false) }
    var showUpiDialog by remember { mutableStateOf(false) }
    val total =
        remember(lines) {
            lines.sumOf { (it.referenceUnitPrice ?: 0.0) * it.quantity }
        }
    val totalLabel = if (lines.isEmpty()) "—" else formatDecimal(total)

    fun startPay() {
        when (method) {
            "wallet" -> onPayLater()
            "card" -> showCardDialog = true
            "upi" -> showUpiDialog = true
            else -> onPayOnline(method)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            FmAppHeader(title = "Payment", subtitle = "Secure checkout", onBack = if (paying) null else onBack)
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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

                FmSectionLabel(title = "Payment method")
                PaymentMethodCard(
                    id = "card",
                    selected = method,
                    label = "Card",
                    sub = "Demo credit / debit card",
                    icon = Icons.Outlined.CreditCard,
                    onSelect = { method = it },
                )
                PaymentMethodCard(
                    id = "upi",
                    selected = method,
                    label = "UPI",
                    sub = "GPay · PhonePe · Paytm",
                    icon = Icons.Outlined.Payments,
                    onSelect = { method = it },
                )
                PaymentMethodCard(
                    id = "wallet",
                    selected = method,
                    label = "Pay on delivery",
                    sub = "Cash / UPI to dealer",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    onSelect = { method = it },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⚡", fontSize = 14.sp)
                    Spacer(Modifier.size(8.dp))
                    Text("Demo mode · no real charges", fontSize = 12.sp, color = WholesaleMuted)
                }
                placeError?.let { Text(it, color = com.mart.distribution.demo.ui.theme.WholesaleRed, fontSize = 13.sp) }
                Spacer(Modifier.height(100.dp))
            }
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, WholesaleBg, WholesaleBg)))
                    .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            if (paying) {
                FmButton(
                    text = "Processing…",
                    onClick = {},
                    enabled = false,
                    variant = FmButtonVariant.Dark,
                )
                Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WholesaleBlue)
                }
            } else {
                FmButton(
                    text =
                        when (method) {
                            "wallet" -> "Place order · pay later"
                            else -> "Pay $totalLabel"
                        },
                    onClick = { startPay() },
                    variant = FmButtonVariant.Dark,
                    enabled = lines.isNotEmpty(),
                )
            }
        }
    }

    if (showCardDialog) {
        DemoCardPaymentDialog(
            totalLabel = totalLabel,
            onDismiss = { showCardDialog = false },
            onConfirm = {
                showCardDialog = false
                onPayOnline("card")
            },
        )
    }

    if (showUpiDialog) {
        AlertDialog(
            onDismissRequest = { showUpiDialog = false },
            title = { Text("Pay with UPI") },
            text = {
                Text("Confirm demo payment from ${ShopkeeperProfileStore.DUMMY_UPI_ID} for $totalLabel?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUpiDialog = false
                        onPayOnline("upi")
                    },
                ) {
                    Text("Pay now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpiDialog = false }) { Text("Cancel") }
            },
        )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter card details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Demo card — no real payment is processed.", fontSize = 12.sp, color = WholesaleMuted)
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = FieldFilters.cardNumber(it); cardError = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Card number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = MartFieldDefaults.outlinedColors(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = FieldFilters.cardExpiry(it); cardError = null },
                        modifier = Modifier.weight(1f),
                        label = { Text("Expiry") },
                        placeholder = { Text("MM/YY") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = MartFieldDefaults.outlinedColors(),
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = FieldFilters.cvv(it); cardError = null },
                        modifier = Modifier.weight(1f),
                        label = { Text("CVV") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = MartFieldDefaults.outlinedColors(),
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = FieldFilters.personName(it); cardError = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name on card") },
                    singleLine = true,
                    colors = MartFieldDefaults.outlinedColors(),
                )
                cardError?.let { Text(it, fontSize = 12.sp, color = com.mart.distribution.demo.ui.theme.WholesaleRed) }
                Text("Pay $totalLabel", fontWeight = FontWeight.SemiBold, color = WholesaleText)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    FieldValidators.cardNumber(cardNumber)?.let { cardError = it; return@TextButton }
                    FieldValidators.cardExpiry(expiry)?.let { cardError = it; return@TextButton }
                    FieldValidators.cvv(cvv)?.let { cardError = it; return@TextButton }
                    FieldValidators.cardName(name)?.let { cardError = it; return@TextButton }
                    onConfirm()
                },
            ) { Text("Pay $totalLabel") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun PaymentMethodCard(
    id: String,
    selected: String,
    label: String,
    sub: String,
    icon: ImageVector,
    onSelect: (String) -> Unit,
) {
    val on = selected == id
    FmCard(
        onClick = { onSelect(id) },
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (on) {
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
                        .background(if (on) WholesaleBlue else WholesaleSurface2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = if (on) Color.White else WholesaleMuted, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                Text(sub, fontSize = 12.sp, color = WholesaleMuted)
            }
            Box(
                modifier =
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .then(
                            if (on) {
                                Modifier.background(WholesaleBlue).border(7.dp, WholesaleBlue, CircleShape)
                            } else {
                                Modifier.border(2.dp, WholesaleBorder2, CircleShape)
                            },
                        ),
            )
        }
    }
}
