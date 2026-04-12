package com.mart.distribution.demo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MartElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier =
            modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), shape)
                .padding(20.dp),
        content = content,
    )
}

@Composable
fun GradientGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.98f,
        animationSpec = tween(180),
        label = "btnScale",
    )
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .scale(scale)
                .height(52.dp)
                .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors =
            androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun OrderStatusChip(status: String) {
    val upper = status.uppercase()
    val (bg, fg) =
        when (upper) {
            "PENDING" ->
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f) to MaterialTheme.colorScheme.tertiary
            "ACCEPTED" ->
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) to MaterialTheme.colorScheme.primary
            "DELIVERED" ->
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f) to MaterialTheme.colorScheme.secondary
            else ->
                MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }
    val animatedBg by animateColorAsState(bg, tween(220), label = "chipBg")
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(animatedBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = upper,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ScreenTitleBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier =
                Modifier
                    .height(3.dp)
                    .fillMaxWidth(0.18f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            ),
                        ),
                    ),
        )
    }
}
