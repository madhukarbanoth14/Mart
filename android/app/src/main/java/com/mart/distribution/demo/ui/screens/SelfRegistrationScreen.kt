package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.onboarding.OnboardingDocumentStorage
import com.mart.distribution.demo.data.onboarding.PendingOnboardingDocument
import com.mart.distribution.demo.feature.auth.RegisterAuthMethod
import com.mart.distribution.demo.feature.auth.RegisterUiState
import com.mart.distribution.demo.feature.auth.RegisterViewModel
import com.mart.distribution.demo.ui.components.OtpBoxes
import com.mart.distribution.demo.ui.components.RegistrationStepper
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmInfoBanner
import com.mart.distribution.demo.ui.flashmart.FmLogoMark
import com.mart.distribution.demo.ui.flashmart.FmTextField
import com.mart.distribution.demo.ui.navigation.NavBackHandler
import com.mart.distribution.demo.ui.onboarding.OnboardingDocumentsSection
import com.mart.distribution.demo.ui.onboarding.OnboardingShopLocationSection
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleGoldTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import kotlinx.coroutines.delay

private val WIZARD_STEP_LABELS = listOf("Business", "Area", "Address", "Documents", "Referral")
private const val WIZARD_BASE_STEP = 2
private const val OTP_RESEND_SECONDS = 18

@Composable
fun SelfRegistrationScreen(
    viewModel: RegisterViewModel,
    onBack: () -> Unit,
    onRegistered: () -> Unit,
) {
    val ui by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadGeo()
    }

    val onScreenBack = {
        when {
            ui.step >= WIZARD_BASE_STEP + 1 -> viewModel.update { it.copy(step = it.step - 1) }
            ui.step >= WIZARD_BASE_STEP -> viewModel.update { it.copy(step = 0) }
            ui.authMethod == RegisterAuthMethod.MOBILE && ui.otpSent ->
                viewModel.update { it.copy(otpSent = false, otp = "") }
            else -> onBack()
        }
    }
    NavBackHandler(onScreenBack)

    when {
        !ui.authComplete -> AuthMethodStep(ui, viewModel, onScreenBack)
        else -> RegistrationWizard(ui, viewModel, onScreenBack, onRegistered)
    }
}

@Composable
private fun AuthMethodStep(
    ui: RegisterUiState,
    viewModel: RegisterViewModel,
    onBack: () -> Unit,
) {
    var secondsLeft by remember { mutableIntStateOf(OTP_RESEND_SECONDS) }
    LaunchedEffect(ui.otpSent, ui.authMethod) {
        secondsLeft = OTP_RESEND_SECONDS
        while (ui.authMethod == RegisterAuthMethod.MOBILE && ui.otpSent && secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
    }

    val phoneValid = ui.phone.filter { it.isDigit() }.length == 10
    val showEmailPassword = ui.authMethod == RegisterAuthMethod.EMAIL && ui.email.contains('@')

    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        FmAppHeader(title = "", onBack = onBack)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp),
        ) {
            FmLogoMark(size = 56.dp)
            Text(
                if (ui.authMethod == RegisterAuthMethod.MOBILE) "Verify your number" else "Sign up with email",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WholesaleText,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                if (ui.authMethod == RegisterAuthMethod.MOBILE) {
                    "We'll send a 6-digit code to your mobile. Email and password are optional later."
                } else {
                    "Use your email and password to create an account."
                },
                fontSize = 15.sp,
                color = WholesaleMuted,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            AuthMethodToggle(ui.authMethod, onSelect = viewModel::setAuthMethod, modifier = Modifier.padding(top = 20.dp))
            ui.error?.let { FmErrorBanner(it, modifier = Modifier.padding(top = 16.dp)) }

            when (ui.authMethod) {
                RegisterAuthMethod.MOBILE -> {
                    FmTextField(
                        value = ui.phone,
                        onValueChange = { v ->
                            viewModel.update {
                                it.copy(phone = v.filter { c -> c.isDigit() }.take(10), otpSent = false, otp = "")
                            }
                        },
                        modifier = Modifier.padding(top = 20.dp),
                        label = "Mobile number",
                        placeholder = "98XXX XXXXX",
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = WholesaleMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                    if (ui.otpSent) {
                        ui.devOtp?.let { FmInfoBanner("Dev OTP: $it", modifier = Modifier.padding(top = 16.dp)) }
                        OtpBoxes(code = ui.otp, modifier = Modifier.padding(top = 24.dp))
                        FmTextField(
                            value = ui.otp,
                            onValueChange = { v -> viewModel.update { it.copy(otp = v.filter { c -> c.isDigit() }.take(6)) } },
                            modifier = Modifier.padding(top = 16.dp),
                            label = "6-digit OTP",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (secondsLeft > 0) {
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = WholesaleMuted, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(7.dp))
                                Text(
                                    "Resend OTP in 0:${secondsLeft.toString().padStart(2, '0')}",
                                    fontSize = 13.5.sp,
                                    color = WholesaleMuted,
                                )
                            } else {
                                Text(
                                    "Resend OTP",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WholesaleBlue,
                                    modifier = Modifier.clickable(onClick = { viewModel.sendOtp() }),
                                )
                            }
                        }
                    }
                }
                RegisterAuthMethod.EMAIL -> {
                    FmTextField(
                        value = ui.email,
                        onValueChange = { v -> viewModel.update { it.copy(email = v.trim(), error = null) } },
                        modifier = Modifier.padding(top = 20.dp),
                        label = "Email address",
                        placeholder = "you@shop.com",
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = WholesaleMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    if (showEmailPassword) {
                        FmTextField(
                            value = ui.password,
                            onValueChange = { v -> viewModel.update { it.copy(password = v, error = null) } },
                            modifier = Modifier.padding(top = 16.dp),
                            label = "Password",
                            placeholder = "Min 8 characters",
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = WholesaleMuted) },
                            visualTransformation = PasswordVisualTransformation(),
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 26.dp, vertical = 16.dp)) {
            when (ui.authMethod) {
                RegisterAuthMethod.MOBILE -> {
                    if (!ui.otpSent) {
                        FmButton(
                            text = if (ui.loading) "Sending…" else "Send OTP",
                            onClick = { viewModel.sendOtp() },
                            enabled = phoneValid && !ui.loading,
                        )
                    } else {
                        FmButton(
                            text = if (ui.loading) "Verifying…" else "Verify & continue",
                            onClick = { viewModel.verifyOtp { } },
                            enabled = ui.otp.length == 6 && !ui.loading,
                        )
                    }
                }
                RegisterAuthMethod.EMAIL -> {
                    FmButton(
                        text = "Continue",
                        onClick = { viewModel.continueWithEmailAuth() },
                        enabled = showEmailPassword && ui.password.length >= 8,
                    )
                }
            }
            Text(
                "By continuing you agree to FlashMart's Terms & Privacy Policy",
                fontSize = 11.5.sp,
                color = WholesaleMuted,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun AuthMethodToggle(
    selected: RegisterAuthMethod,
    onSelect: (RegisterAuthMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(WholesaleGoldTint).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AuthMethodChip("Mobile", selected == RegisterAuthMethod.MOBILE, Modifier.weight(1f)) { onSelect(RegisterAuthMethod.MOBILE) }
        AuthMethodChip("Email", selected == RegisterAuthMethod.EMAIL, Modifier.weight(1f)) { onSelect(RegisterAuthMethod.EMAIL) }
    }
}

@Composable
private fun AuthMethodChip(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (active) WholesaleBlue else androidx.compose.ui.graphics.Color.Transparent)
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) androidx.compose.ui.graphics.Color.White else WholesaleText,
        )
    }
}

@Composable
private fun RegistrationWizard(
    ui: RegisterUiState,
    viewModel: RegisterViewModel,
    onBack: () -> Unit,
    onRegistered: () -> Unit,
) {
    val wizardStep = (ui.step - WIZARD_BASE_STEP).coerceIn(0, WIZARD_STEP_LABELS.lastIndex)
    val last = wizardStep == WIZARD_STEP_LABELS.lastIndex
    var pendingDocuments by remember { mutableStateOf<List<PendingOnboardingDocument>>(emptyList()) }
    val documentSlots = remember { OnboardingDocumentStorage.standardDocumentSlots() }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
    FmAppHeader(title = "Create your account", onBack = onBack)
    RegistrationStepper(
        step = wizardStep,
        totalSteps = WIZARD_STEP_LABELS.size,
        stepLabel = WIZARD_STEP_LABELS[wizardStep],
        modifier = Modifier.padding(horizontal = FmSpacing.screenH, vertical = 4.dp),
    )
    Column(
        modifier =
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FmSpacing.screenH, vertical = FmSpacing.itemGap),
        verticalArrangement = Arrangement.spacedBy(FmSpacing.itemGap),
    ) {
        ui.error?.let { FmErrorBanner(it) }
        localError?.let { FmErrorBanner(it) }
        when (wizardStep) {
            0 -> BusinessStep(ui, viewModel)
            1 -> AreaStep(ui, viewModel)
            2 -> AddressStep(ui, viewModel)
            3 -> DocumentsStep(documentSlots, onDocumentsChanged = { pendingDocuments = it })
            4 -> ReferralStep(ui, viewModel)
        }
        Spacer(Modifier.height(8.dp))
    }
    Column(modifier = Modifier.padding(horizontal = FmSpacing.screenH, vertical = 16.dp)) {
        FmButton(
            text = if (ui.loading) "Creating account…" else if (last) "Submit & finish" else "Continue",
            enabled = !ui.loading && viewModel.validateWizardStep(wizardStep) == null,
            onClick = {
                localError = viewModel.validateWizardStep(wizardStep)
                if (localError != null) return@FmButton
                if (last) {
                    viewModel.submitRegistration(onRegistered)
                } else {
                    viewModel.update { it.copy(step = it.step + 1, error = null) }
                }
            },
        )
    }
    }
}

@Composable
private fun BusinessStep(
    ui: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    FmTextField(ui.name, { v -> viewModel.update { it.copy(name = v) } }, label = "Owner name", leadingIcon = { Icon(Icons.Filled.Person, null, tint = WholesaleMuted) })
    FmTextField(
        ui.shopName,
        { v -> viewModel.update { it.copy(shopName = v) } },
        label = if (ui.role == "DEALER") "Business name" else "Shop / business name",
        leadingIcon = { Icon(Icons.Filled.Storefront, null, tint = WholesaleMuted) },
    )
    if (ui.authMethod == RegisterAuthMethod.MOBILE) {
        FmTextField(
            ui.phone,
            {},
            label = "Verified mobile",
            enabled = false,
            leadingIcon = { Icon(Icons.Filled.Phone, null, tint = WholesaleMuted) },
        )
        FmTextField(
            ui.email,
            { v -> viewModel.update { it.copy(email = v) } },
            label = "Email (optional)",
            leadingIcon = { Icon(Icons.Filled.Email, null, tint = WholesaleMuted) },
        )
    } else {
        FmTextField(
            ui.email,
            {},
            label = "Email",
            enabled = false,
            leadingIcon = { Icon(Icons.Filled.Email, null, tint = WholesaleMuted) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AreaStep(
    ui: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }
    val districts = ui.geo.find { it.name == ui.state }?.districts.orEmpty()
    ExposedDropdownMenuBox(expanded = stateExpanded, onExpandedChange = { stateExpanded = it }) {
        OutlinedTextField(
            value = ui.state,
            onValueChange = {},
            readOnly = true,
            label = { Text("State") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
            shape = MartFieldDefaults.fieldShape,
            colors = MartFieldDefaults.outlinedColors(),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        DropdownMenu(expanded = stateExpanded, onDismissRequest = { stateExpanded = false }) {
            ui.geo.forEach { state ->
                DropdownMenuItem(
                    text = { Text(state.name) },
                    onClick = {
                        stateExpanded = false
                        viewModel.update {
                            it.copy(state = state.name, district = state.districts.firstOrNull() ?: "")
                        }
                        viewModel.loadAreas()
                    },
                )
            }
        }
    }
    if (districts.isNotEmpty()) {
        ExposedDropdownMenuBox(expanded = districtExpanded, onExpandedChange = { districtExpanded = it }) {
            OutlinedTextField(
                value = ui.district,
                onValueChange = {},
                readOnly = true,
                label = { Text("District") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                shape = MartFieldDefaults.fieldShape,
                colors = MartFieldDefaults.outlinedColors(),
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            DropdownMenu(expanded = districtExpanded, onDismissRequest = { districtExpanded = false }) {
                districts.forEach { district ->
                    DropdownMenuItem(
                        text = { Text(district) },
                        onClick = {
                            districtExpanded = false
                            viewModel.update { it.copy(district = district) }
                            viewModel.loadAreas()
                        },
                    )
                }
            }
        }
    }
    if (ui.areas.isNotEmpty()) {
        val areaLabel = ui.areas.find { it.id == ui.areaId }?.name ?: ui.areas.first().name
        ExposedDropdownMenuBox(expanded = areaExpanded, onExpandedChange = { areaExpanded = it }) {
            OutlinedTextField(
                value = areaLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Area / route") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                shape = MartFieldDefaults.fieldShape,
                colors = MartFieldDefaults.outlinedColors(),
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            DropdownMenu(expanded = areaExpanded, onDismissRequest = { areaExpanded = false }) {
                ui.areas.forEach { area ->
                    DropdownMenuItem(
                        text = { Text(area.name) },
                        onClick = {
                            areaExpanded = false
                            viewModel.update { it.copy(areaId = area.id) }
                        },
                    )
                }
            }
        }
    } else {
        FmInfoBanner("No service areas found for this district. Pick another district or contact support.")
    }
}

@Composable
private fun AddressStep(
    ui: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(com.mart.distribution.demo.ui.theme.WholesaleSurface2),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Place, contentDescription = null, tint = WholesaleBlue, modifier = Modifier.size(28.dp))
    }
    OnboardingShopLocationSection(
        shopName = ui.shopName,
        onShopNameChange = { v -> viewModel.update { it.copy(shopName = v) } },
        address = ui.address,
        onAddressChange = { v -> viewModel.update { it.copy(address = v) } },
        latitude = ui.latitude,
        longitude = ui.longitude,
        onLocationCaptured = { lat, lng -> viewModel.update { it.copy(latitude = lat, longitude = lng) } },
        shopNameLabel = if (ui.role == "DEALER") "Business name" else "Shop name",
    )
}

@Composable
private fun DocumentsStep(
    documentSlots: List<com.mart.distribution.demo.data.onboarding.OnboardingDocumentSlot>,
    onDocumentsChanged: (List<PendingOnboardingDocument>) -> Unit,
) {
    FmInfoBanner("Document upload is optional now. You can add or finish documents later from Profile → Documents.")
    OnboardingDocumentsSection(slots = documentSlots, onDocumentsChanged = onDocumentsChanged)
}

@Composable
private fun ReferralStep(
    ui: RegisterUiState,
    viewModel: RegisterViewModel,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(WholesaleGoldTint)
                .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(com.mart.distribution.demo.ui.theme.WholesaleGold.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = com.mart.distribution.demo.ui.theme.WholesaleGoldInk, modifier = Modifier.size(28.dp))
        }
        Text(
            "Were you referred?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = WholesaleText,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            "If a FlashMart employee onboarded you, enter their referral code to link your account.",
            fontSize = 13.sp,
            color = WholesaleMuted,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    FmTextField(
        ui.referralCode,
        { v -> viewModel.update { it.copy(referralCode = v) } },
        label = "Referral code (optional)",
    )
}
