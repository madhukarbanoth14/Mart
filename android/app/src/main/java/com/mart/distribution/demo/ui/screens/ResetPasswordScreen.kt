package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.feature.auth.LoginViewModel
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface2
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators
import com.mart.distribution.demo.ui.theme.WholesaleRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    viewModel: LoginViewModel,
    initialEmail: String = "",
    initialToken: String = "",
    onBack: () -> Unit,
    onPasswordReset: () -> Unit,
) {
    val ui by viewModel.uiState.collectAsState()
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var token by remember(initialToken) { mutableStateOf(initialToken) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var resetInfo by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Reset password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(WholesaleInkSurface, WholesaleInkSurface2)))
                    .padding(padding),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    "Dealers onboarded by your team receive a reset link. Enter the token to set a new password, or request a new link by email.",
                    fontSize = 13.sp,
                    color = Color.White.copy(0.65f),
                    lineHeight = 18.sp,
                )
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(0.06f))
                            .border(1.dp, Color.White.copy(0.10f), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = FieldFilters.email(it); resetInfo = null; localError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email", color = Color.White.copy(0.6f)) },
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = Color.White.copy(0.55f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = MartFieldDefaults.outlinedOnDarkColors(),
                    )
                    GradientGoldButton(
                        text = if (ui.loading) "Requesting…" else "Get reset link",
                        onClick = {
                            FieldValidators.email(email)?.let { localError = it; return@GradientGoldButton }
                            viewModel.requestPasswordReset(email.trim()) { resp ->
                                if (resp?.emailSent == true) {
                                    resetInfo =
                                        resp.message +
                                            " Check ${resp.loginEmail ?: email} (including spam)."
                                } else {
                                    resp?.resetPasswordToken?.let { token = it }
                                    resetInfo =
                                        resp?.message
                                            ?: "If an account exists, a reset link was generated."
                                }
                            }
                        },
                        enabled = !ui.loading && email.isNotBlank(),
                    )
                    resetInfo?.let {
                        Text(it, fontSize = 12.sp, color = Color.White.copy(0.75f))
                    }
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(0.06f))
                            .border(1.dp, Color.White.copy(0.10f), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = FieldFilters.hexToken(it); localError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Reset token", color = Color.White.copy(0.6f)) },
                        leadingIcon = { Icon(Icons.Outlined.Key, null, tint = Color.White.copy(0.55f)) },
                        singleLine = true,
                        colors = MartFieldDefaults.outlinedOnDarkColors(),
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New password", color = Color.White.copy(0.6f)) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Color.White.copy(0.55f)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = MartFieldDefaults.outlinedOnDarkColors(),
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm password", color = Color.White.copy(0.6f)) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Color.White.copy(0.55f)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = MartFieldDefaults.outlinedOnDarkColors(),
                    )
                    ui.error?.let { Text(it, fontSize = 12.sp, color = WholesaleRed) }
                    localError?.let { Text(it, fontSize = 12.sp, color = WholesaleRed) }
                    Spacer(Modifier.height(4.dp))
                    GradientGoldButton(
                        text = if (ui.loading) "Saving…" else "Set new password",
                        onClick = {
                            FieldValidators.resetToken(token)?.let { localError = it; return@GradientGoldButton }
                            FieldValidators.password(newPassword)?.let { localError = it; return@GradientGoldButton }
                            if (newPassword != confirmPassword) {
                                localError = "Passwords do not match"
                                return@GradientGoldButton
                            }
                            localError = null
                            viewModel.resetPassword(token.trim(), newPassword, onPasswordReset)
                        },
                        enabled =
                            !ui.loading &&
                                token.isNotBlank() &&
                                newPassword.isNotBlank() &&
                                confirmPassword.isNotBlank(),
                    )
                }
            }
        }
    }
}
