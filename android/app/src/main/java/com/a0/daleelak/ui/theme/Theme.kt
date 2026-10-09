package com.a0.daleelak.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NavyAndSky = lightColorScheme(
    primary = Navy, onPrimary = Color.White,
    primaryContainer = Sky, onPrimaryContainer = Navy,
    secondary = Navy, onSecondary = Color.White,
    secondaryContainer = Sky, onSecondaryContainer = Ink,
    tertiary = Amber, onTertiary = Ink,
    tertiaryContainer = Color(0xFFFEF3C7), onTertiaryContainer = Ink,
    background = Page, onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    surfaceVariant = Page, onSurfaceVariant = Muted,
    surfaceTint = Navy, outline = Muted, outlineVariant = Border,
    inverseSurface = Ink, inverseOnSurface = Page, inversePrimary = Sky,
    surfaceDim = Border, surfaceBright = Color.White,
    surfaceContainerLowest = Color.White, surfaceContainerLow = Page,
    surfaceContainer = Page, surfaceContainerHigh = Border, surfaceContainerHighest = Sky,
)

/** Fixed light identity for the demo; wallpaper colors do not replace Navy and Sky. */
@Composable
fun DaleelakTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NavyAndSky, typography = Typography, content = content)
}
