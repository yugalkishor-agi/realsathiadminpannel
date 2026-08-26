package com.incoteam.frndzz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FrndzzPink,
    onPrimary = FrndzzOnDark,
    primaryContainer = FrndzzPurpleDeep,
    onPrimaryContainer = FrndzzOnDark,
    secondary = FrndzzPurple,
    onSecondary = FrndzzOnDark,
    tertiary = FrndzzLime,
    onTertiary = FrndzzDarkBg,
    background = FrndzzDarkBg,
    onBackground = FrndzzOnDark,
    surface = FrndzzDarkSurface,
    onSurface = FrndzzOnDark,
    surfaceVariant = FrndzzDarkSurfaceAlt,
    onSurfaceVariant = FrndzzOnSurfaceVariant,
    outline = FrndzzOutline
)

private val LightColorScheme = lightColorScheme(
    primary = FrndzzPink,
    onPrimary = FrndzzOnDark,
    primaryContainer = FrndzzPurple,
    onPrimaryContainer = FrndzzOnDark,
    secondary = FrndzzPurple,
    onSecondary = FrndzzOnDark,
    tertiary = FrndzzLime,
    onTertiary = FrndzzDarkBg,
    background = FrndzzLightBg,
    onBackground = FrndzzOnLight,
    surface = FrndzzLightSurface,
    onSurface = FrndzzOnLight,
    surfaceVariant = Color(0xFFF2E4EC),
    onSurfaceVariant = Color(0xFF745A69),
    outline = Color(0xFFD4BBCB)
)

@Composable
fun FrndzzTheme(
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
