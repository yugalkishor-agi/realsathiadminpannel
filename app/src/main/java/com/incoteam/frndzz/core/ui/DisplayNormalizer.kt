package com.incoteam.frndzz.core.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.min

private const val DesignWidthDp = 411f
private const val DesignHeightDp = 891f

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
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val scale = min(
            maxWidth.value / DesignWidthDp,
            maxHeight.value / DesignHeightDp
        )

        Box(
            modifier = Modifier
                .width(DesignWidthDp.dp)
                .height(DesignHeightDp.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
        ) {
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = density.density,
                    fontScale = 1f
                )
            ) {
                content()
            }
        }
    }
}
