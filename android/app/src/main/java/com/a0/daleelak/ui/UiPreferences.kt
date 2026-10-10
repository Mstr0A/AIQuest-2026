package com.a0.daleelak.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

data class UiPreferences(val english: Boolean, val textSize: Int,
    val changeLanguage: () -> Unit, val changeTextSize: () -> Unit)

val LocalUiPreferences = staticCompositionLocalOf<UiPreferences> { error("UI preferences provider missing") }

/** Local display preferences survive app restarts and preserve the system font scale. */
@Composable
fun UiPreferencesProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("daleelak_display", 0) }
    var english by remember { mutableStateOf(preferences.getBoolean("english", false)) }
    var textSize by remember { mutableIntStateOf(preferences.getInt("text_size", 0).coerceIn(0, 2)) }
    val strings = remember(context, english) { UiStrings(context, english) }
    val density = LocalDensity.current
    val scale = listOf(1f, 1.2f, 1.4f)[textSize]
    val settings = UiPreferences(english, textSize,
        changeLanguage = {
            english = !english
            preferences.edit().putBoolean("english", english).apply()
        },
        changeTextSize = {
            textSize = (textSize + 1) % 3
            preferences.edit().putInt("text_size", textSize).apply()
        })
    CompositionLocalProvider(LocalUiStrings provides strings, LocalUiPreferences provides settings,
        LocalDensity provides Density(density.density, density.fontScale * scale), content = content)
}
