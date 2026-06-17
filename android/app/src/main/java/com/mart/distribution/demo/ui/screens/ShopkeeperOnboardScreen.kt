package com.mart.distribution.demo.ui.screens

import com.mart.distribution.demo.data.onboarding.OnboardingDocumentStorage
import com.mart.distribution.demo.data.onboarding.PendingOnboardingDocument
import com.mart.distribution.demo.ui.onboarding.OnboardingDocumentsSection
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators
import com.mart.distribution.demo.ui.onboarding.OnboardingDocumentsSection
import com.mart.distribution.demo.ui.onboarding.validateRequiredDocuments
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.demo.LocalDemoAuthConfig
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.MartElevatedCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopkeeperOnboardScreen(
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

    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successInfo by remember { mutableStateOf<String?>(null) }
    var onboardingNotesText by remember { mutableStateOf("") }
    var pendingDocuments by remember { mutableStateOf<List<PendingOnboardingDocument>>(emptyList()) }
    val documentSlots = remember { OnboardingDocumentStorage.shopkeeperDocumentSlots() }

    fun combinedOnboardingNotes(): String? =
        onboardingNotesText.trim().takeIf { it.isNotEmpty() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Onboard shopkeeper") },
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
                        "Creates a SHOPKEEPER pending admin approval. Upload required documents. They receive a confirmation email only after approval."
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
                placeholder = { Text("ID checks, shop address, remarks…") },
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
                        Text("No areas available. Create an area in the backend first.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Assigned area", style = MaterialTheme.typography.labelLarge)
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
                                    ar.dealer?.name?.let { dn ->
                                        Text(
                                            "Dealer · $dn",
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
                text = if (busy) "Saving…" else "Add shopkeeper",
                onClick = {
                    if (busy) return@GradientGoldButton
                    FieldValidators.personName(name)?.let { error = it; return@GradientGoldButton }
                    FieldValidators.email(email)?.let { error = it; return@GradientGoldButton }
                    FieldValidators.phoneOptional(phone)?.let { error = it; return@GradientGoldButton }
                    if (selectedAreaId.isBlank()) {
                        error = "Area is required"
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
                    mainViewModel.onboardShopkeeper(
                        name = name,
                        email = email,
                        phone = phone.takeIf { it.isNotBlank() },
                        password = if (BuildConfig.USE_LOCAL_DEMO_AUTH) null else password,
                        areaId = selectedAreaId,
                        onboardingNotes = combinedOnboardingNotes(),
                        documents = pendingDocuments,
                    ) { err, info ->
                        busy = false
                        if (err != null) {
                            error = err
                        } else if (!info.isNullOrBlank()) {
                            successInfo = info
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
                enabled = !busy && areas.isNotEmpty(),
            )
        }
    }

    successInfo?.let { info ->
        AlertDialog(
            onDismissRequest = {
                successInfo = null
                navController.popBackStack()
            },
            title = { Text("Submitted for approval") },
            text = { Text(info) },
            confirmButton = {
                TextButton(
                    onClick = {
                        successInfo = null
                        navController.popBackStack()
                    },
                ) {
                    Text("OK")
                }
            },
        )
    }
}
