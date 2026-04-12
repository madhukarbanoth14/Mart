package com.mart.distribution.demo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MartDark =
    darkColorScheme(
        primary = MartGold,
        onPrimary = MartInk,
        primaryContainer = MartGoldDim,
        onPrimaryContainer = MartCream,
        secondary = MartChampagne,
        onSecondary = MartInk,
        tertiary = MartSuccess,
        onTertiary = MartInk,
        background = MartBg,
        onBackground = MartCream,
        surface = MartSurface,
        onSurface = MartCream,
        surfaceVariant = MartSurfaceElevated,
        onSurfaceVariant = MartMuted,
        outline = MartOutline,
        error = MartError,
        onError = Color.White,
    )

private val MartLight =
    lightColorScheme(
        primary = Color(0xFF7A6228),
        onPrimary = Color.White,
        primaryContainer = MartChampagne,
        onPrimaryContainer = MartInk,
        secondary = Color(0xFF5C5A66),
        onSecondary = Color.White,
        background = Color(0xFFF8F6F2),
        onBackground = MartInk,
        surface = Color.White,
        onSurface = MartInk,
        surfaceVariant = Color(0xFFECE8E0),
        onSurfaceVariant = Color(0xFF5C5A66),
        outline = Color(0xFFC4BFB5),
    )

@Composable
fun MartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val ctx = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
            }
            darkTheme -> MartDark
            else -> MartLight
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MartTypography,
        content = content,
    )
}
