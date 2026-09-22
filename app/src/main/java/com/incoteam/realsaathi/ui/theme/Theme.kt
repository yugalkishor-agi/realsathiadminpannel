package com.incoteam.realsaathi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RealSaathiPink,
    onPrimary = RealSaathiOnDark,
    primaryContainer = RealSaathiPurpleDeep,
    onPrimaryContainer = RealSaathiOnDark,
    secondary = RealSaathiPurple,
    onSecondary = RealSaathiOnDark,
    tertiary = RealSaathiLime,
    onTertiary = RealSaathiDarkBg,
    background = RealSaathiDarkBg,
    onBackground = RealSaathiOnDark,
    surface = RealSaathiDarkSurface,
    onSurface = RealSaathiOnDark,
    surfaceVariant = RealSaathiDarkSurfaceAlt,
    onSurfaceVariant = RealSaathiOnSurfaceVariant,
    outline = RealSaathiOutline
)

private val LightColorScheme = lightColorScheme(
    primary = RealSaathiPink,
    onPrimary = RealSaathiOnDark,
    primaryContainer = RealSaathiPurple,
    onPrimaryContainer = RealSaathiOnDark,
    secondary = RealSaathiPurple,
    onSecondary = RealSaathiOnDark,
    tertiary = RealSaathiLime,
    onTertiary = RealSaathiDarkBg,
    background = RealSaathiLightBg,
    onBackground = RealSaathiOnLight,
    surface = RealSaathiLightSurface,
    onSurface = RealSaathiOnLight,
    surfaceVariant = Color(0xFFF2E4EC),
    onSurfaceVariant = Color(0xFF745A69),
    outline = Color(0xFFD4BBCB)
)

@Composable
fun RealSaathiTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
