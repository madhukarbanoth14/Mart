package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface2
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.isProbablyEmulator

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
    onForgotPassword: () -> Unit = {},
) {
    val ui by viewModel.uiState.collectAsState()
    val showBackendUrl = !BuildConfig.USE_LOCAL_DEMO_AUTH && !isProbablyEmulator()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(WholesaleInkSurface, WholesaleInkSurface2))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 36.dp),
            ) {
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                        .background(WholesaleBlue),
                    contentAlignment = Alignment.Center,
                ) { Text("⚡", fontSize = 20.sp) }
                Column {
                    Text("Flashmart", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, letterSpacing = (-0.3).sp)
                    Text("Distribution OS", fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                }
            }

            Text("Welcome back", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = Color.White, letterSpacing = (-0.6).sp,
                modifier = Modifier.padding(bottom = 6.dp))
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
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (BuildConfig.USE_LOCAL_DEMO_AUTH) {
                    Text(
                        "Investor demo · works on any network. Use a seed email and password ${LocalDemoAuthConfig.DEMO_PASSWORD}.",
                        fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f), lineHeight = 17.sp,
                    )
                }
                if (showBackendUrl) {
                    Text("Backend URL — server LAN IP, port 3000. Saved on device after sign-in.",
                        fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f), lineHeight = 17.sp)
                    OutlinedTextField(
                        value = ui.serverUrl,
                        onValueChange = { viewModel.setServerUrl(FieldFilters.url(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Backend URL", color = Color.White.copy(0.6f)) },
                        placeholder = { Text("http://192.168.1.10:3000", color = Color.White.copy(0.3f)) },
                        leadingIcon = { Icon(Icons.Outlined.Dns, null, tint = Color.White.copy(0.55f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = MartFieldDefaults.outlinedOnDarkColors(),
                    )
                }
                OutlinedTextField(
                    value = ui.identifier,
                    onValueChange = { viewModel.setIdentifier(FieldFilters.loginIdentifier(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mobile or email", color = Color.White.copy(0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Email, null, tint = Color.White.copy(0.55f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = MartFieldDefaults.outlinedOnDarkColors(),
                )
                OutlinedTextField(
                    value = ui.password, onValueChange = viewModel::setPassword,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password", color = Color.White.copy(0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Color.White.copy(0.55f)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = MartFieldDefaults.outlinedOnDarkColors(),
                )
                ui.error?.let { err ->
                    Text(err, fontSize = 12.sp, color = WholesaleRed)
                }
            }
            Spacer(Modifier.height(20.dp))
            GradientGoldButton(
                text = if (ui.loading) "Signing in…" else "Sign in",
                onClick = { viewModel.login(onSignedIn) },
                enabled = !ui.loading,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Forgot password? Reset with your onboard link →",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = WholesaleBlue,
                modifier =
                    Modifier
                        .padding(top = 4.dp)
                        .clickable(enabled = !ui.loading, onClick = onForgotPassword),
            )
        }
    }
}
