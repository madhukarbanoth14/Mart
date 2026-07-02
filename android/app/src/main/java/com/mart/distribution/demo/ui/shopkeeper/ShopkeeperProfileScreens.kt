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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.profile.ShopkeeperProfileStore
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmNotificationsInbox
import com.mart.distribution.demo.ui.flashmart.buildShopkeeperNotifications
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmDataRow
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmInfoBanner
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmTextField
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
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

    ProfileFormShell(title = "Store address", subtitle = user.name, onBack = onBack) {
        FmTextField(
            value = storeName,
            onValueChange = { storeName = FieldFilters.businessName(it); saveError = null },
            label = "Store name",
        )
        FmTextField(
            value = address,
            onValueChange = { address = it.take(300); saveError = null },
            label = "Full address",
            singleLine = false,
            minLines = 3,
            maxLines = 5,
        )
        FmTextField(
            value = phone,
            onValueChange = { phone = FieldFilters.phone(it); saveError = null },
            label = "Contact phone",
            placeholder = "10-digit mobile",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        saveError?.let { FmErrorBanner(message = it) }
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

@Composable
fun ShopkeeperPaymentMethodsScreen(onBack: () -> Unit) {
    ProfileFormShell(title = "Payment methods", subtitle = "Saved for checkout", onBack = onBack) {
        FmCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.CreditCard, null, tint = WholesaleText)
                Column(modifier = Modifier.weight(1f)) {
                    Text(ShopkeeperProfileStore.DUMMY_CARD_MASKED, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                    Text("Demo credit card · use at checkout", fontSize = 12.sp, color = WholesaleMuted)
                }
                Text("Default", fontSize = 12.sp, color = WholesaleMuted)
            }
        }
        FmCard {
            FmDataRow(
                title = "Demo card details",
                subtitle =
                    "Number: ${ShopkeeperProfileStore.DUMMY_CARD_NUMBER}\n" +
                        "Expiry: ${ShopkeeperProfileStore.DUMMY_CARD_EXPIRY} · CVV: ${ShopkeeperProfileStore.DUMMY_CARD_CVV}\n" +
                        "Name: ${ShopkeeperProfileStore.DUMMY_CARD_NAME}",
            )
        }
        FmCard {
            FmDataRow(title = "UPI", subtitle = ShopkeeperProfileStore.DUMMY_UPI_ID)
        }
        FmInfoBanner(message = "These are test credentials for demo checkout. No real charges are made.")
    }
}

@Composable
fun ShopkeeperGstDetailsScreen(onBack: () -> Unit) {
    var gstin by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.gstin) }
    var business by rememberSaveable { mutableStateOf(ShopkeeperProfileStore.gstBusinessName) }
    var saveError by remember { mutableStateOf<String?>(null) }

    ProfileFormShell(title = "GST details", subtitle = "Shown on invoices", onBack = onBack) {
        FmTextField(
            value = business,
            onValueChange = { business = FieldFilters.businessName(it); saveError = null },
            label = "Registered business name",
        )
        FmTextField(
            value = gstin,
            onValueChange = { gstin = FieldFilters.gstin(it); saveError = null },
            label = "GSTIN",
            placeholder = "15 characters",
        )
        saveError?.let { FmErrorBanner(message = it) }
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

@Composable
fun ShopkeeperNotificationsScreen(
    mainViewModel: com.mart.distribution.demo.feature.home.MainViewModel,
    onBack: () -> Unit,
) {
    val ui by mainViewModel.uiState.collectAsState()
    var readIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    val orders = when (val o = ui.orders) { is com.mart.distribution.demo.feature.home.LoadState.Ok -> o.data; else -> emptyList() }
    val docs = when (val d = ui.myDocuments) { is com.mart.distribution.demo.feature.home.LoadState.Ok -> d.data; else -> emptyList() }
    val groups =
        remember(orders, docs, readIds) {
            buildShopkeeperNotifications(orders, docs, readIds)
        }

    LaunchedEffect(Unit) {
        mainViewModel.loadOrders()
        mainViewModel.loadMyDocuments()
    }

    FmNotificationsInbox(
        groups = groups,
        onBack = onBack,
        onMarkAllRead = {
            readIds = readIds + groups.flatMap { it.items }.map { it.id }.toSet()
        },
    )
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
    ProfileFormShell(title = "Help & support", subtitle = "We're here to help", onBack = onBack) {
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

@Composable
private fun HelpRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sub: String,
) {
    FmCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = WholesaleText)
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                Text(sub, fontSize = 12.sp, color = WholesaleMuted)
            }
        }
    }
}

@Composable
private fun ProfileFormShell(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        FmAppHeader(title = title, subtitle = subtitle, onBack = onBack)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = FmSpacing.screenH, vertical = FmSpacing.itemGap),
            verticalArrangement = Arrangement.spacedBy(FmSpacing.itemGap),
        ) {
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}
