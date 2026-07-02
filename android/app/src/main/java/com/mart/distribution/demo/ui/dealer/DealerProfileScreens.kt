package com.mart.distribution.demo.ui.dealer

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
import androidx.compose.material.icons.outlined.AccountBalance
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
import com.mart.distribution.demo.data.profile.DealerProfileStore
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmNotificationsInbox
import com.mart.distribution.demo.ui.flashmart.buildDealerNotifications
import com.mart.distribution.demo.ui.flashmart.FmCard
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
fun DealerAccountScreen(
    user: SessionUser,
    onBack: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(DealerProfileStore.displayName.ifBlank { user.name }) }
    var phone by rememberSaveable { mutableStateOf(DealerProfileStore.contactPhone.ifBlank { user.phone.orEmpty() }) }
    var saveError by remember { mutableStateOf<String?>(null) }

    DealerProfileFormShell(title = "Account", subtitle = "Your login identity", onBack = onBack) {
        FmTextField(
            value = name,
            onValueChange = { name = FieldFilters.personName(it); saveError = null },
            label = "Display name",
        )
        FmTextField(
            value = user.email,
            onValueChange = {},
            label = "Email",
            enabled = false,
        )
        FmTextField(
            value = phone,
            onValueChange = { phone = FieldFilters.phone(it); saveError = null },
            label = "Mobile",
            placeholder = "10-digit mobile",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        FmInfoBanner(message = "Email is managed by KNSR admin. Contact support to change it.")
        saveError?.let { FmErrorBanner(message = it) }
        FmButton(
            text = "Save account",
            onClick = {
                FieldValidators.personName(name)?.let { saveError = it; return@FmButton }
                FieldValidators.phoneRequired(phone)?.let { saveError = it; return@FmButton }
                DealerProfileStore.displayName = name.trim()
                DealerProfileStore.contactPhone = phone.trim()
                if (DealerProfileStore.businessName.isBlank()) {
                    DealerProfileStore.businessName = name.trim()
                }
                onBack()
            },
        )
    }
}

@Composable
fun DealerBusinessAddressScreen(
    user: SessionUser,
    onBack: () -> Unit,
) {
    var business by rememberSaveable { mutableStateOf(DealerProfileStore.businessName.ifBlank { user.name }) }
    var address by rememberSaveable { mutableStateOf(DealerProfileStore.warehouseAddress) }
    var phone by rememberSaveable { mutableStateOf(DealerProfileStore.contactPhone.ifBlank { user.phone.orEmpty() }) }
    var saveError by remember { mutableStateOf<String?>(null) }

    DealerProfileFormShell(title = "Business address", subtitle = "Warehouse / distribution point", onBack = onBack) {
        FmTextField(
            value = business,
            onValueChange = { business = FieldFilters.businessName(it); saveError = null },
            label = "Business name",
        )
        FmTextField(
            value = address,
            onValueChange = { address = it.take(300); saveError = null },
            label = "Warehouse address",
            singleLine = false,
            minLines = 3,
            maxLines = 5,
        )
        FmTextField(
            value = phone,
            onValueChange = { phone = FieldFilters.phone(it); saveError = null },
            label = "Business phone",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        saveError?.let { FmErrorBanner(message = it) }
        FmButton(
            text = "Save address",
            onClick = {
                FieldValidators.businessName(business)?.let { saveError = it; return@FmButton }
                if (address.trim().length < 5) {
                    saveError = "Enter a complete address"
                    return@FmButton
                }
                FieldValidators.phoneRequired(phone)?.let { saveError = it; return@FmButton }
                DealerProfileStore.businessName = business.trim()
                DealerProfileStore.warehouseAddress = address.trim()
                DealerProfileStore.contactPhone = phone.trim()
                onBack()
            },
        )
    }
}

@Composable
fun DealerPaymentDetailsScreen(onBack: () -> Unit) {
    var holder by rememberSaveable { mutableStateOf(DealerProfileStore.accountHolderName) }
    var bank by rememberSaveable { mutableStateOf(DealerProfileStore.bankName) }
    var account by rememberSaveable { mutableStateOf(DealerProfileStore.bankAccountNumber) }
    var ifsc by rememberSaveable { mutableStateOf(DealerProfileStore.bankIfsc) }
    var upi by rememberSaveable { mutableStateOf(DealerProfileStore.upiId) }
    var saveError by remember { mutableStateOf<String?>(null) }

    DealerProfileFormShell(title = "Payment details", subtitle = "Settlements & collections", onBack = onBack) {
        FmInfoBanner(
            message = "Add bank account and/or UPI for receiving payments from shopkeepers and KNSR settlements.",
        )
        FmSectionLabel(title = "Bank account")
        FmTextField(
            value = holder,
            onValueChange = { holder = FieldFilters.personName(it); saveError = null },
            label = "Account holder name",
        )
        FmTextField(
            value = bank,
            onValueChange = { bank = FieldFilters.businessName(it); saveError = null },
            label = "Bank name",
        )
        FmTextField(
            value = account,
            onValueChange = { account = it.filter { ch -> ch.isDigit() }.take(18); saveError = null },
            label = "Account number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        FmTextField(
            value = ifsc,
            onValueChange = { ifsc = FieldFilters.ifsc(it); saveError = null },
            label = "IFSC code",
        )
        FmSectionLabel(title = "UPI")
        FmTextField(
            value = upi,
            onValueChange = { upi = FieldFilters.upiId(it); saveError = null },
            label = "UPI ID",
            placeholder = "name@bank",
        )
        saveError?.let { FmErrorBanner(message = it) }
        FmButton(
            text = "Save payment details",
            onClick = {
                val hasBank = account.isNotBlank() || ifsc.isNotBlank() || bank.isNotBlank()
                val hasUpi = upi.trim().isNotEmpty()
                if (!hasBank && !hasUpi) {
                    saveError = "Add at least a bank account or UPI ID"
                    return@FmButton
                }
                if (hasBank) {
                    if (account.length < 8) {
                        saveError = "Enter a valid account number"
                        return@FmButton
                    }
                    FieldValidators.ifscOptional(ifsc)?.let { saveError = it; return@FmButton }
                }
                if (hasUpi) {
                    FieldValidators.upiIdOptional(upi)?.let { saveError = it; return@FmButton }
                }
                DealerProfileStore.accountHolderName = holder.trim()
                DealerProfileStore.bankName = bank.trim()
                DealerProfileStore.bankAccountNumber = account.trim()
                DealerProfileStore.bankIfsc = ifsc.trim().uppercase()
                DealerProfileStore.upiId = upi.trim()
                onBack()
            },
        )
    }
}

@Composable
fun DealerGstDetailsScreen(onBack: () -> Unit) {
    var gstin by rememberSaveable { mutableStateOf(DealerProfileStore.gstin) }
    var business by rememberSaveable { mutableStateOf(DealerProfileStore.gstBusinessName) }
    var saveError by remember { mutableStateOf<String?>(null) }

    DealerProfileFormShell(title = "GST details", subtitle = "Shown on invoices", onBack = onBack) {
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
                DealerProfileStore.gstBusinessName = business.trim()
                DealerProfileStore.gstin = gstin.trim().uppercase()
                onBack()
            },
        )
    }
}

@Composable
fun DealerNotificationsScreen(
    mainViewModel: com.mart.distribution.demo.feature.home.MainViewModel,
    onBack: () -> Unit,
) {
    val ui by mainViewModel.uiState.collectAsState()
    var readIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    val orders = when (val o = ui.orders) { is com.mart.distribution.demo.feature.home.LoadState.Ok -> o.data; else -> emptyList() }
    val stock = when (val s = ui.stock) { is com.mart.distribution.demo.feature.home.LoadState.Ok -> s.data; else -> emptyList() }
    val docs = when (val d = ui.myDocuments) { is com.mart.distribution.demo.feature.home.LoadState.Ok -> d.data; else -> emptyList() }
    val groups =
        remember(orders, stock, docs, readIds) {
            buildDealerNotifications(orders, stock, docs, readIds)
        }

    LaunchedEffect(Unit) {
        mainViewModel.loadOrders()
        mainViewModel.loadStock()
        mainViewModel.loadMyDocuments()
    }

    FmNotificationsInbox(
        groups = groups,
        onBack = onBack,
        onMarkAllRead = {
            readIds = readIds + groups.flatMap { it.items }.map { it.id }.toSet()
        },
        emptyMessage = "No dealer alerts yet. New shopkeeper orders and stock warnings will appear here.",
    )
}

@Composable
private fun DealerNotificationToggle(
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
fun DealerHelpSupportScreen(onBack: () -> Unit) {
    DealerProfileFormShell(title = "Help & support", subtitle = "KNSR dealer desk", onBack = onBack) {
        DealerHelpRow(Icons.Outlined.Phone, "Call support", "1800-123-4567")
        DealerHelpRow(Icons.Outlined.Email, "Email", "dealers@knsrmart.com")
        DealerHelpRow(Icons.Outlined.AccountBalance, "Settlements", "settlements@knsrmart.com")
        DealerHelpRow(Icons.Outlined.QuestionAnswer, "FAQs", "Orders, stock, payments")
        FmSectionLabel(title = "Common questions")
        FmCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("How do shopkeepers pay me?", fontWeight = FontWeight.SemiBold, color = WholesaleText)
                Text(
                    "They pay online via Razorpay or cash on delivery. Add your bank/UPI under Payment details.",
                    fontSize = 13.sp,
                    color = WholesaleMuted,
                )
                Text("When does stock update?", fontWeight = FontWeight.SemiBold, color = WholesaleText)
                Text(
                    "Add SKUs under SKU management, then set quantities in the Stock tab.",
                    fontSize = 13.sp,
                    color = WholesaleMuted,
                )
            }
        }
    }
}

@Composable
private fun DealerHelpRow(
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
private fun DealerProfileFormShell(
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
