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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmSuccessBanner
import com.mart.distribution.demo.ui.flashmart.FmTextField
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface2
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators

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

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(WholesaleInkSurface, WholesaleInkSurface2))),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
        ) {
            FmAppHeader(
                title = "Reset password",
                subtitle = "Request a link or set a new password",
                onBack = onBack,
                lightOnDark = true,
            )
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(FmSpacing.sectionGap),
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
                    verticalArrangement = Arrangement.spacedBy(FmSpacing.fieldGap),
                ) {
                    FmSectionLabel(title = "Step 1 · Request link")
                    FmTextField(
                        value = email,
                        onValueChange = { email = FieldFilters.email(it); resetInfo = null; localError = null },
                        label = "Email",
                        placeholder = "you@company.com",
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = WholesaleBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        onDark = true,
                    )
                    FmButton(
                        text = if (ui.loading) "Requesting…" else "Get reset link",
                        onClick = {
                            FieldValidators.email(email)?.let { localError = it; return@FmButton }
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
                        FmSuccessBanner(message = it)
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
                    verticalArrangement = Arrangement.spacedBy(FmSpacing.fieldGap),
                ) {
                    FmSectionLabel(title = "Step 2 · Set password")
                    FmTextField(
                        value = token,
                        onValueChange = { token = FieldFilters.hexToken(it); localError = null },
                        label = "Reset token",
                        placeholder = "Paste token from email",
                        leadingIcon = { Icon(Icons.Outlined.Key, null, tint = WholesaleBlue) },
                        onDark = true,
                    )
                    FmTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "New password",
                        placeholder = "Min 8 characters",
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = WholesaleBlue) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        onDark = true,
                    )
                    FmTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm password",
                        placeholder = "Re-enter new password",
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = WholesaleBlue) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        onDark = true,
                    )
                    ui.error?.let { FmErrorBanner(message = it) }
                    localError?.let { FmErrorBanner(message = it) }
                    Spacer(Modifier.height(4.dp))
                    FmButton(
                        text = if (ui.loading) "Saving…" else "Set new password",
                        onClick = {
                            FieldValidators.resetToken(token)?.let { localError = it; return@FmButton }
                            FieldValidators.password(newPassword)?.let { localError = it; return@FmButton }
                            if (newPassword != confirmPassword) {
                                localError = "Passwords do not match"
                                return@FmButton
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
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
