package com.incoteam.realsaathi.core.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Context.withStableFontScale(): Context {
    val currentConfig = resources.configuration
    if (currentConfig.fontScale == 1f) {
        return this
    }

    val stableConfig = Configuration(currentConfig).apply {
        fontScale = 1f
    }
    return createConfigurationContext(stableConfig)
}

@Composable
fun StableMobileUi(content: @Composable () -> Unit) {
    // Measure at the actual window size. Scaling a fixed canvas shrinks touch targets
    // and leaves unused space on phones with a different aspect ratio.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040206))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
        ) {
            content()
        }
    }
}
