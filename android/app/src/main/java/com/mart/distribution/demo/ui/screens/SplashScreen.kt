package com.mart.distribution.demo.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.AppContainer
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
        delay(380)
        container.sessionRepository.hydrateTokenCache()
        val user = container.sessionRepository.sessionUserFlow.first()
        if (user != null) {
            onContinueLoggedIn()
        } else {
            onContinueGuest()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha).padding(32.dp),
        ) {
            Text(
                text = "KNSR Mart",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Light,
                fontSize = 42.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Distribution · Investor preview",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(48.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}
