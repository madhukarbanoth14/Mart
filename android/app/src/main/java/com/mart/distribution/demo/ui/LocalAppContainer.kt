package com.mart.distribution.demo.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.mart.distribution.demo.AppContainer

val LocalAppContainer =
    staticCompositionLocalOf<AppContainer> {
        error("AppContainer not provided")
    }
