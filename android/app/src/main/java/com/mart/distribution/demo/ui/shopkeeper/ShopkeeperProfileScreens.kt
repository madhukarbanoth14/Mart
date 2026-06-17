package com.mart.distribution.demo.ui.shopkeeper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.profile.ShopkeeperProfileStore
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators

@Composable
fun ShopkeeperStoreAddressScreen(
    user: SessionUser,
    onBack: () -> Unit,
) {
    var storeName by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.storeName) }
    var address by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.storeAddress) }
    var phone by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.storePhone) }
    var saveError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        FmAppHeader(title = "Store address", subtitle = user.name, onBack = onBack)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = FieldFilters.businessName(it); saveError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Store name") },
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it.take(300); saveError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full address") },
                minLines = 3,
                colors = MartFieldDefaults.outlinedColors(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = FieldFilters.phone(it); saveError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contact phone") },
                placeholder = { Text("10-digit mobile") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            saveError?.let { Text(it, fontSize = 12.sp, color = com.mart.distribution.demo.ui.theme.WholesaleRed) }
            FmButton(
                text = "Save address",
                onClick = {
                    FieldValidators.businessName(storeName)?.let { saveError = it; return@FmButton }
                    if (address.trim().length < 5) {
                        saveError = "Enter a complete address"
                        return@FmButton
                    }
                    FieldValidators.phoneRequired(phone)?.let { saveError = it; return@FmButton }
                    saveError = null
                    ShopkeeperProfileStore.storeName = storeName.trim()
                    ShopkeeperProfileStore.storeAddress = address.trim()
                    ShopkeeperProfileStore.storePhone = phone.trim()
                    onBack()
                },
            )
        }
    }
}

@Composable
fun ShopkeeperPaymentMethodsScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        FmAppHeader(title = "Payment methods", subtitle = "Saved for checkout", onBack = onBack)
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.material3.Icon(Icons.Outlined.CreditCard, null, tint = WholesaleText)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ShopkeeperProfileStore.DUMMY_CARD_MASKED, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                        Text("Demo credit card · use at checkout", fontSize = 12.sp, color = WholesaleMuted)
                    }
                    Text("Default", fontSize = 12.sp, color = WholesaleMuted)
                }
            }
            FmCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Demo card details", fontWeight = FontWeight.SemiBold, color = WholesaleText)
                    Text("Number: ${ShopkeeperProfileStore.DUMMY_CARD_NUMBER}", fontSize = 13.sp, color = WholesaleMuted)
                    Text("Expiry: ${ShopkeeperProfileStore.DUMMY_CARD_EXPIRY} · CVV: ${ShopkeeperProfileStore.DUMMY_CARD_CVV}", fontSize = 13.sp, color = WholesaleMuted)
                    Text("Name: ${ShopkeeperProfileStore.DUMMY_CARD_NAME}", fontSize = 13.sp, color = WholesaleMuted)
                }
            }
            FmCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("UPI", fontWeight = FontWeight.SemiBold, color = WholesaleText)
                    Text(ShopkeeperProfileStore.DUMMY_UPI_ID, fontSize = 13.sp, color = WholesaleMuted)
                }
            }
            Text(
                "These are test credentials for demo checkout. No real charges are made.",
                fontSize = 12.sp,
                color = WholesaleMuted,
            )
        }
    }
}

@Composable
fun ShopkeeperGstDetailsScreen(onBack: () -> Unit) {
    var gstin by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.gstin) }
    var business by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.gstBusinessName) }
    var saveError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        FmAppHeader(title = "GST details", subtitle = "Shown on invoices", onBack = onBack)
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = business,
                onValueChange = { business = FieldFilters.businessName(it); saveError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Registered business name") },
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            OutlinedTextField(
                value = gstin,
                onValueChange = { gstin = FieldFilters.gstin(it); saveError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("GSTIN") },
                placeholder = { Text("15 characters") },
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            saveError?.let { Text(it, fontSize = 12.sp, color = com.mart.distribution.demo.ui.theme.WholesaleRed) }
            FmButton(
                text = "Save GST details",
                onClick = {
                    FieldValidators.businessName(business)?.let { saveError = it; return@FmButton }
                    FieldValidators.gstinOptional(gstin)?.let { saveError = it; return@FmButton }
                    saveError = null
                    ShopkeeperProfileStore.gstBusinessName = business.trim()
                    ShopkeeperProfileStore.gstin = gstin.trim()
                    onBack()
                },
            )
        }
    }
}

@Composable
fun ShopkeeperNotificationsScreen(onBack: () -> Unit) {
    var orders by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.orderAlerts) }
    var delivery by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.deliveryAlerts) }
    var promos by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.promoAlerts) }

    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        FmAppHeader(title = "Notifications", subtitle = "Order & delivery alerts", onBack = onBack)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NotificationToggle("Order updates", "Status changes and confirmations", orders) { orders = it }
            NotificationToggle("Delivery alerts", "Out for delivery and delivered", delivery) { delivery = it }
            NotificationToggle("Offers & promos", "Deals from your dealer", promos) { promos = it }
            Spacer(Modifier.height(8.dp))
            FmButton(
                text = "Save preferences",
                onClick = {
                    ShopkeeperProfileStore.orderAlerts = orders
                    ShopkeeperProfileStore.deliveryAlerts = delivery
                    ShopkeeperProfileStore.promoAlerts = promos
                    onBack()
                },
            )
        }
    }
}

@Composable
private fun NotificationToggle(
    title: String,
    sub: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    FmCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                Text(sub, fontSize = 12.sp, color = WholesaleMuted)
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
fun ShopkeeperHelpSupportScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        FmAppHeader(title = "Help & support", subtitle = "We're here to help", onBack = onBack)
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HelpRow(Icons.Outlined.Phone, "Call support", "1800-123-4567 (demo)")
            HelpRow(Icons.Outlined.Email, "Email", "support@knsrmart.com")
            HelpRow(Icons.Outlined.QuestionAnswer, "FAQs", "Orders, payments, returns")
            FmSectionLabel(title = "Common questions")
            FmCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("How do I pay with card?", fontWeight = FontWeight.SemiBold, color = WholesaleText)
                    Text(
                        "At checkout choose Card and use the demo card shown under Payment methods in Profile.",
                        fontSize = 13.sp,
                        color = WholesaleMuted,
                    )
                    Text("When will my order arrive?", fontWeight = FontWeight.SemiBold, color = WholesaleText)
                    Text(
                        "Your dealer confirms, then marks the order out for delivery. Track status under Orders.",
                        fontSize = 13.sp,
                        color = WholesaleMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sub: String,
) {
    FmCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            androidx.compose.material3.Icon(icon, null, tint = WholesaleText)
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                Text(sub, fontSize = 12.sp, color = WholesaleMuted)
            }
        }
    }
}
