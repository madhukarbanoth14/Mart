package com.mart.distribution.demo.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface2
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashRoute(
    container: AppContainer,
    onContinueLoggedIn: () -> Unit,
    onContinueGuest: () -> Unit,
) {
    val alpha by animateFloatAsState(1f, tween(600), label = "splashA")

    LaunchedEffect(Unit) {
        delay(200)
        container.networkConfigRepository.hydrate()
        container.sessionManager.hydrate()
        val user = container.sessionManager.sessionUserFlow.first()
        if (user != null) onContinueLoggedIn() else onContinueGuest()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(WholesaleInkSurface, WholesaleInkSurface2))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha).padding(32.dp),
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(22.dp))
                    .background(WholesaleBlue),
                contentAlignment = Alignment.Center,
            ) { Text("⚡", fontSize = 36.sp) }
            Spacer(Modifier.height(20.dp))
            Text(
                "Flashmart",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.6).sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Distribution OS",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(48.dp))
            CircularProgressIndicator(
                color = WholesaleBlue,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
