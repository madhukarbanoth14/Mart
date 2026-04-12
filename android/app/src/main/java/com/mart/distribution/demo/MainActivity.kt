package com.mart.distribution.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.mart.distribution.demo.ui.LocalAppContainer
import com.mart.distribution.demo.ui.MartNavHost
import com.mart.distribution.demo.ui.theme.MartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MartApplication
        setContent {
            MartTheme(darkTheme = true) {
                CompositionLocalProvider(LocalAppContainer provides app.container) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        MartNavHost()
                    }
                }
            }
        }
    }
}
