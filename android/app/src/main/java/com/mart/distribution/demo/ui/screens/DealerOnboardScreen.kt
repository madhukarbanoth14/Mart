package com.mart.distribution.demo.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.mart.distribution.demo.data.onboarding.OnboardingDocumentStorage
import com.mart.distribution.demo.data.onboarding.PendingOnboardingDocument
import com.mart.distribution.demo.ui.onboarding.OnboardingDocumentsSection
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators
import com.mart.distribution.demo.ui.onboarding.OnboardingDocumentsSection
import com.mart.distribution.demo.ui.onboarding.validateRequiredDocuments
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.mart.distribution.demo.data.api.dto.CreateDealerResponse
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.demo.LocalDemoAuthConfig
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.components.MartElevatedCard
import com.mart.distribution.demo.ui.components.GradientGoldButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerOnboardScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
) {
    val ui by mainViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        mainViewModel.loadAreas()
    }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var onboardSuccess by remember { mutableStateOf<CreateDealerResponse?>(null) }
    var pendingInfo by remember { mutableStateOf<String?>(null) }
    var onboardingNotesText by remember { mutableStateOf("") }
    var pendingDocuments by remember { mutableStateOf<List<PendingOnboardingDocument>>(emptyList()) }
    val documentSlots = remember { OnboardingDocumentStorage.dealerDocumentSlots() }
    val areas =
        when (val a = ui.areas) {
            is LoadState.Ok -> a.data
            else -> emptyList()
        }
    var selectedAreaId by remember { mutableStateOf("") }
    LaunchedEffect(areas) {
        if (selectedAreaId.isEmpty() && areas.isNotEmpty()) {
            selectedAreaId = areas.first().id
        }
    }

    fun combinedOnboardingNotes(): String? =
        onboardingNotesText.trim().takeIf { it.isNotEmpty() }

    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Onboard dealer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (BuildConfig.USE_LOCAL_DEMO_AUTH) {
                        "Employee submissions go to admin for approval. Upload KYC documents below. Confirmation email is sent only after admin approval."
                    } else {
                        "Creates a DEALER account pending admin approval. Upload required documents. The dealer receives a confirmation email only after approval."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = FieldFilters.personName(it); error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full name") },
                singleLine = true,
                keyboardOptions = MartFieldDefaults.englishTextKeyboard,
                colors = MartFieldDefaults.outlinedColors(),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = FieldFilters.email(it); error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = FieldFilters.phone(it); error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone (optional)") },
                placeholder = { Text("10-digit mobile") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            if (!BuildConfig.USE_LOCAL_DEMO_AUTH) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Sign-in password") },
                    placeholder = { Text("Min 8 characters") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = MartFieldDefaults.outlinedColors(),
                )
            }
            OutlinedTextField(
                value = onboardingNotesText,
                onValueChange = { onboardingNotesText = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Onboarding notes (optional)") },
                placeholder = { Text("GST, warehouse, remarks…") },
                keyboardOptions = MartFieldDefaults.englishMultilineKeyboard,
                minLines = 2,
                colors = MartFieldDefaults.outlinedColors(),
            )
            OnboardingDocumentsSection(
                slots = documentSlots,
                modifier = Modifier.fillMaxWidth(),
                onDocumentsChanged = { pendingDocuments = it },
            )
            when (val a = ui.areas) {
                is LoadState.Loading ->
                    Text("Loading areas…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                is LoadState.Err -> Text(a.message, color = MaterialTheme.colorScheme.error)
                is LoadState.Ok ->
                    if (areas.isEmpty()) {
                        Text("No areas available. Create an area first.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Assign area", style = MaterialTheme.typography.labelLarge)
                        areas.forEach { ar ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAreaId = ar.id },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selectedAreaId == ar.id,
                                    onClick = { selectedAreaId = ar.id },
                                    colors =
                                        RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                        ),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ar.name, style = MaterialTheme.typography.bodyLarge)
                                    ar.dealer?.name?.let { current ->
                                        Text(
                                            "Current dealer · $current",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                else -> {}
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            GradientGoldButton(
                text = if (busy) "Saving…" else "Add dealer",
                onClick = {
                    if (busy) return@GradientGoldButton
                    FieldValidators.personName(name)?.let { error = it; return@GradientGoldButton }
                    FieldValidators.email(email)?.let { error = it; return@GradientGoldButton }
                    FieldValidators.phoneOptional(phone)?.let { error = it; return@GradientGoldButton }
                    if (selectedAreaId.isBlank()) {
                        error = "Area assignment is required"
                        return@GradientGoldButton
                    }
                    validateRequiredDocuments(documentSlots, pendingDocuments)?.let {
                        error = it
                        return@GradientGoldButton
                    }
                    if (!BuildConfig.USE_LOCAL_DEMO_AUTH) {
                        FieldValidators.password(password)?.let { error = it; return@GradientGoldButton }
                    }
                    busy = true
                    error = null
                    mainViewModel.onboardDealer(
                        name = name,
                        email = email,
                        phone = phone.takeIf { it.isNotBlank() },
                        password = if (BuildConfig.USE_LOCAL_DEMO_AUTH) null else password,
                        areaId = selectedAreaId,
                        onboardingNotes = combinedOnboardingNotes(),
                        documents = pendingDocuments,
                    ) { err, success, info ->
                        busy = false
                        if (err != null) {
                            error = err
                        } else if (!info.isNullOrBlank()) {
                            pendingInfo = info
                        } else if (success != null) {
                            onboardSuccess = success
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
                enabled = !busy && areas.isNotEmpty(),
            )
        }
    }

    onboardSuccess?.let { success ->
        val shareText =
            buildString {
                appendLine("KNSR Mart dealer login")
                appendLine("Email: ${success.loginEmail ?: success.email}")
                success.loginPassword?.let { appendLine("Password: $it") }
                success.userId?.let { appendLine("User ID: $it") }
                appendLine("Open the app and sign in with the email and password above.")
            }
        AlertDialog(
            onDismissRequest = {
                onboardSuccess = null
                navController.popBackStack()
            },
            title = { Text("Dealer onboarded") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(success.message ?: "Share these credentials with the dealer:")
                    if (success.emailSent == true) {
                        Text(
                            "Welcome email sent to ${success.loginEmail ?: success.email}. Ask the dealer to check inbox/spam.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else if (!BuildConfig.USE_LOCAL_DEMO_AUTH) {
                        Text(
                            success.emailError?.let { "Email not sent ($it). Copy details below." }
                                ?: "Email not configured on server. Copy details below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text("Login email: ${success.loginEmail ?: success.email}", style = MaterialTheme.typography.bodyMedium)
                    success.loginPassword?.let {
                        Text("Password: $it", style = MaterialTheme.typography.bodyMedium)
                    }
                    success.userId?.let {
                        Text("User ID: $it", style = MaterialTheme.typography.bodyMedium)
                    }
                    success.resetPasswordToken?.let {
                        Text("Reset token: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Dealer login", shareText))
                    },
                ) {
                    Text("Copy details")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onboardSuccess = null
                        navController.popBackStack()
                    },
                ) {
                    Text("Done")
                }
            },
        )
    }

    pendingInfo?.let { info ->
        AlertDialog(
            onDismissRequest = {
                pendingInfo = null
                navController.popBackStack()
            },
            title = { Text("Submitted for approval") },
            text = { Text(info) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingInfo = null
                        navController.popBackStack()
                    },
                ) {
                    Text("OK")
                }
            },
        )
    }
}
