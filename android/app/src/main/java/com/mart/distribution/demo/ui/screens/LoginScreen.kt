package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mart.distribution.demo.feature.auth.LoginViewModel
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.MartElevatedCard
import com.mart.distribution.demo.ui.components.ScreenTitleBar

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
) {
    val ui by viewModel.uiState.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        ScreenTitleBar(title = "Welcome back")
        Spacer(Modifier.height(28.dp))
        MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Sign in with your demo account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = ui.email,
                onValueChange = viewModel::setEmail,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = fieldColors(),
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = ui.password,
                onValueChange = viewModel::setPassword,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = fieldColors(),
            )
            ui.error?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(24.dp))
            GradientGoldButton(
                text = if (ui.loading) "Signing in…" else "Sign in",
                onClick = { viewModel.login(onSignedIn) },
                enabled = !ui.loading,
            )
        }
    }
}

@Composable
private fun fieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary,
    )
