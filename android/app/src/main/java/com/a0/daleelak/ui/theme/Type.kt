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
