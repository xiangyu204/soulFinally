@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.example.soul_android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.soul_android.R

/*
 * Manrope 可变字体：现代几何无衬线，比系统默认字体更有设计感。
 * 中/韩文会自动回退到系统字体，拉丁字母使用 Manrope。
 */
private val Manrope = FontFamily(
    Font(R.font.manrope, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.manrope, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.manrope, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.manrope, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.manrope, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800)))
)

private fun style(
    weight: FontWeight,
    size: Int,
    line: Int,
    tracking: Float = 0f
) = TextStyle(
    fontFamily = Manrope,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp
)

val Typography = Typography(
    displayLarge = style(FontWeight.Bold, 38, 46, (-1.0f)),
    displayMedium = style(FontWeight.Bold, 32, 40, (-0.8f)),
    displaySmall = style(FontWeight.SemiBold, 28, 36, (-0.6f)),

    headlineLarge = style(FontWeight.SemiBold, 26, 34, (-0.5f)),
    headlineMedium = style(FontWeight.SemiBold, 22, 30, (-0.4f)),
    headlineSmall = style(FontWeight.SemiBold, 19, 26, (-0.3f)),

    titleLarge = style(FontWeight.SemiBold, 18, 24, (-0.2f)),
    titleMedium = style(FontWeight.Medium, 16, 22, 0f),
    titleSmall = style(FontWeight.Medium, 14, 20, 0.1f),

    bodyLarge = style(FontWeight.Normal, 15, 23, 0.1f),
    bodyMedium = style(FontWeight.Normal, 14, 21, 0.1f),
    bodySmall = style(FontWeight.Normal, 13, 19, 0.2f),

    labelLarge = style(FontWeight.Medium, 14, 20, 0.2f),
    labelMedium = style(FontWeight.Medium, 12, 17, 0.3f),
    labelSmall = style(FontWeight.Medium, 11, 15, 0.4f)
)
