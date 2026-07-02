package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.demo.LocalDemoAuthConfig
import com.mart.distribution.demo.feature.auth.LoginViewModel
import com.mart.distribution.demo.ui.components.MartAppFooter
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmLogoHeader
import com.mart.distribution.demo.ui.flashmart.FmTextField
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface2
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.isProbablyEmulator

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onRegister: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
) {
    val ui by viewModel.uiState.collectAsState()
    val showBackendUrl = BuildConfig.SHOW_BACKEND_URL && !BuildConfig.USE_LOCAL_DEMO_AUTH && !isProbablyEmulator()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(WholesaleInkSurface, WholesaleInkSurface2))),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .size(420.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(WholesaleBlue.copy(alpha = 0.22f), Color.Transparent),
                        ),
                    ),
        )
        Column(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 28.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            FmLogoHeader(modifier = Modifier.padding(bottom = 36.dp))

            Text(
                "Welcome back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.6).sp,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            Text(
                if (BuildConfig.USE_LOCAL_DEMO_AUTH) {
                    "Sign in with your demo account"
                } else {
                    "Sign in with your mobile or email"
                },
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = 28.dp),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                        .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(FmSpacing.fieldGap),
            ) {
                if (BuildConfig.USE_LOCAL_DEMO_AUTH) {
                    Text(
                        "Investor demo · works on any network. Use a seed email and password ${LocalDemoAuthConfig.DEMO_PASSWORD}.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        lineHeight = 17.sp,
                    )
                }
                if (showBackendUrl) {
                    Text(
                        "Backend URL — server LAN IP, port 3000. Saved on device after sign-in.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        lineHeight = 17.sp,
                    )
                    FmTextField(
                        value = ui.serverUrl,
                        onValueChange = { viewModel.setServerUrl(FieldFilters.url(it)) },
                        label = "Backend URL",
                        placeholder = "http://192.168.1.10:3000",
                        leadingIcon = { Icon(Icons.Outlined.Dns, null, tint = WholesaleBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        onDark = true,
                    )
                }
                FmTextField(
                    value = ui.identifier,
                    onValueChange = { viewModel.setIdentifier(FieldFilters.loginIdentifier(it)) },
                    label = "Mobile or email",
                    placeholder =
                        if (BuildConfig.USE_LOCAL_DEMO_AUTH) {
                            "shop1@martdemo.com"
                        } else {
                            "you@company.com or 10-digit mobile"
                        },
                    leadingIcon = { Icon(Icons.Outlined.Email, null, tint = WholesaleBlue) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    onDark = true,
                )
                FmTextField(
                    value = ui.password,
                    onValueChange = viewModel::setPassword,
                    label = "Password",
                    placeholder = "Enter your password",
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = WholesaleBlue) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    onDark = true,
                )
                ui.error?.let { err ->
                    FmErrorBanner(message = err)
                }
            }
            Spacer(Modifier.height(20.dp))
            FmButton(
                text = if (ui.loading) "Signing in…" else "Sign in",
                onClick = { viewModel.login(onSignedIn) },
                enabled = !ui.loading,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Forgot password?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WholesaleBlue,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp)
                        .clickable(enabled = !ui.loading, onClick = onForgotPassword),
            )
            Text(
                "New user? Create account",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WholesaleBlue,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp)
                        .clickable(enabled = !ui.loading, onClick = onRegister),
            )
            MartAppFooter(
                onPrivacyPolicy = onOpenPrivacyPolicy,
                darkBackground = true,
            )
        }
    }
}
