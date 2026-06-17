package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.MartElevatedCard
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEmployeeScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successInfo by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add employee") },
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
            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Field employees onboard dealers and shopkeepers. Login credentials are emailed when SMTP is configured.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = FieldFilters.personName(it) },
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = MartFieldDefaults.englishTextKeyboard,
                colors = MartFieldDefaults.outlinedColors(),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = FieldFilters.email(it) },
                label = { Text("Work email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = MartFieldDefaults.outlinedColors(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = FieldFilters.phone(it) },
                label = { Text("Phone (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = MartFieldDefaults.outlinedColors(),
            )
            if (!BuildConfig.USE_LOCAL_DEMO_AUTH) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (optional)") },
                    supportingText = {
                        Text("Leave blank to auto-generate and email a secure password")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = MartFieldDefaults.outlinedColors(),
                )
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            GradientGoldButton(
                text = if (busy) "Creating…" else "Create employee",
                onClick = {
                    if (busy) return@GradientGoldButton
                    FieldValidators.personName(name)?.let { error = it; return@GradientGoldButton }
                    FieldValidators.email(email)?.let { error = it; return@GradientGoldButton }
                    FieldValidators.phoneOptional(phone)?.let { error = it; return@GradientGoldButton }
                    if (!BuildConfig.USE_LOCAL_DEMO_AUTH && password.isNotBlank()) {
                        FieldValidators.passwordOptional(password)?.let { error = it; return@GradientGoldButton }
                    }
                    busy = true
                    error = null
                    mainViewModel.createEmployee(
                        name = name,
                        email = email,
                        phone = phone.takeIf { it.isNotBlank() },
                        password = password.takeIf { it.isNotBlank() },
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
                enabled = !busy,
            )
        }
    }

    successInfo?.let { info ->
        AlertDialog(
            onDismissRequest = {
                successInfo = null
                navController.popBackStack()
            },
            title = { Text("Employee created") },
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
