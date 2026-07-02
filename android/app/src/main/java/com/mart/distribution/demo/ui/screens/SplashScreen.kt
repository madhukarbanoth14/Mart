package com.mart.distribution.demo.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.mart.distribution.demo.ui.flashmart.FmLogoMark
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueDark
import com.mart.distribution.demo.ui.theme.WholesaleBlueDeep
import com.mart.distribution.demo.ui.theme.WholesaleGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashRoute(
    container: AppContainer,
    onContinueLoggedIn: () -> Unit,
    onContinueGuest: () -> Unit,
    onContinueOnboarding: () -> Unit = onContinueGuest,
) {
    val alpha by animateFloatAsState(1f, tween(600), label = "splashA")

    LaunchedEffect(Unit) {
        delay(200)
        container.networkConfigRepository.hydrate()
        container.sessionManager.hydrate()
        val user = container.sessionManager.sessionUserFlow.first()
        when {
            user != null -> onContinueLoggedIn()
            !container.onboardingPreferences.hasCompleted() -> onContinueOnboarding()
            else -> onContinueGuest()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(WholesaleBlueDark, WholesaleBlue, WholesaleBlueDeep))),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(360.dp)
                    .alpha(alpha)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        ),
                    ),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha).padding(32.dp),
        ) {
            FmLogoMark(size = 104.dp)
            Spacer(Modifier.height(24.dp))
            Row {
                Text(
                    "Flash",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-1).sp,
                )
                Text(
                    "Mart",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WholesaleGold,
                    letterSpacing = (-1).sp,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Fast Delivery · Trusted Quality",
                fontSize = 14.5.sp,
                color = Color.White.copy(alpha = 0.82f),
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(48.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf("QUALITY", "SPEED", "TRUST").forEachIndexed { index, label ->
                    if (index > 0) {
                        Text("·", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                    Text(
                        label,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.65f),
                        letterSpacing = 1.sp,
                    )
                }
            }
        }
    }
}
