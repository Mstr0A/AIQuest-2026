# Theme tokens

Fixed light Navy and Sky: primary #1E3A8A, ink #0F172A, background #F8FAFC, selected #DBEAFE, white cards, muted #64748B, border #E2E8F0, optional amber #F59E0B. Default Android sans font, no decorative fonts; Arabic letter spacing 0. Headline 28/38, title 16/24, body 16/26, small 12/20. Page padding 20; row/card padding 16–20; gaps 8/12/16/24; radii 12/16/20; flat outlines, no gradients. Touch targets >=48dp. Single phone column; long content scrolls.

## android/app/src/main/java/com/a0/daleelak/ui/theme/Color.kt

```kotlin
package com.a0.daleelak.ui.theme

import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF1E3A8A)
val Ink = Color(0xFF0F172A)
val Page = Color(0xFFF8FAFC)
val Sky = Color(0xFFDBEAFE)
val Muted = Color(0xFF64748B)
val Border = Color(0xFFE2E8F0)
val Amber = Color(0xFFF59E0B)

```

## android/app/src/main/java/com/a0/daleelak/ui/theme/Theme.kt

```kotlin
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

```

## android/app/src/main/java/com/a0/daleelak/ui/theme/Type.kt

```kotlin
package com.a0.daleelak.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun textStyle(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.Default, fontWeight = weight,
    fontSize = size.sp, lineHeight = height.sp, letterSpacing = 0.sp,
)

val Typography = Typography(
    headlineMedium = textStyle(28, 38, FontWeight.SemiBold),
    headlineSmall = textStyle(24, 34, FontWeight.SemiBold),
    titleLarge = textStyle(22, 30, FontWeight.Medium),
    titleMedium = textStyle(16, 24, FontWeight.SemiBold),
    bodyLarge = textStyle(16, 26), bodyMedium = textStyle(14, 24), bodySmall = textStyle(12, 20),
    labelLarge = textStyle(14, 22, FontWeight.Medium),
    labelMedium = textStyle(12, 20, FontWeight.Medium),
    labelSmall = textStyle(11, 18, FontWeight.Medium),
)

```
