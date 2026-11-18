package com.example.soul_android.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.soul_android.data.ThemeState

/* 夜间：原 SOUL 深空配色，不做改动 */
private val SoulDarkScheme = darkColorScheme(
    primary = Color(0xFF00E5D0),
    onPrimary = Color(0xFF04120F),
    primaryContainer = Color(0xFF0C3B38),
    onPrimaryContainer = Color(0xFFB8FFF6),
    secondary = Color(0xFF4C6FFF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1B2550),
    onSecondaryContainer = Color(0xFFC9D6FF),
    tertiary = Color(0xFF7C5CFF),
    onTertiary = Color.White,
    background = Color(0xFF07080D),
    onBackground = Color(0xFFF2F4F8),
    surface = Color(0xFF12151F),
    onSurface = Color(0xFFF2F4F8),
    surfaceVariant = Color(0xFF191D2A),
    onSurfaceVariant = Color(0xFFA8B0C2),
    outline = Color(0x33FFFFFF),
    outlineVariant = Color(0x1FFFFFFF),
    error = Color(0xFFFF5A5F),
    onError = Color.White
)

/* 日间：Skeuomorphic Glass 规范 */
private val SoulLightScheme = lightColorScheme(
    primary = Color(0xFFFF7A2F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE3D2),
    onPrimaryContainer = Color(0xFF5B2A07),
    secondary = Color(0xFF716A62),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAE3D8),
    onSecondaryContainer = Color(0xFF2C2925),
    tertiary = Color(0xFFBDAE99),
    onTertiary = Color(0xFF2C2925),
    background = Color(0xFFF3EBDD),
    onBackground = Color(0xFF2C2925),
    surface = Color(0xFFFAF6EF),
    onSurface = Color(0xFF2C2925),
    surfaceVariant = Color(0xFFE9E1D4),
    onSurfaceVariant = Color(0xFF716A62),
    outline = Color(0xFFD9CBB8),
    outlineVariant = Color(0xFFE5DCCB),
    error = Color(0xFFC23B2E),
    onError = Color.White
)

private val SoulShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun SOUL_AndroidTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val dark = darkTheme ?: ThemeState.isDark.value
    val scheme = if (dark) SoulDarkScheme else SoulLightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        shapes = SoulShapes,
        content = content
    )
}
