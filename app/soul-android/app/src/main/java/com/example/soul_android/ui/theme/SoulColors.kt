package com.example.soul_android.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.soul_android.data.ThemeState

/*
 * 主题感知语义色。
 * 夜间（深色）= 原 SOUL 深空配色，保持原样不动。
 * 日间（浅色）= Skeuomorphic Glass 规范（暖沙 / 米白 / 柔灰 + 橙色强调）。
 */
object SoulColors {
    val isDark: Boolean get() = ThemeState.isDark.value

    val textPrimary: Color get() = if (isDark) Color(0xFFF2F4F8) else Color(0xFF2C2925)
    val textSecondary: Color get() = if (isDark) Color(0xFFA8B0C2) else Color(0xFF716A62)
    val textMuted: Color get() = if (isDark) Color(0xFF6E7789) else Color(0xFF9C948B)

    val background: Color
        get() = if (isDark) Color(0xFF07080D) else Color(0xFFF3EBDD)
    val backgroundElevated: Color
        get() = if (isDark) Color(0xFF12151F) else Color(0xFFFAF6EF)
    val card: Color
        get() = if (isDark) Color(0xFF12151F) else Color(0xFFFAF6EF)

    val glassFill: Color
        get() = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.42f)
    val glassFillStrong: Color
        get() = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.58f)
    val glassStroke: Color
        get() = if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.70f)

    val stone: Color get() = if (isDark) Color(0xFF2C251E) else Color(0xFFD9CBB8)
    val stoneDark: Color get() = if (isDark) Color(0xFF1E1813) else Color(0xFFBDAE99)

    val hairline: Color get() = if (isDark) Color(0x33FFFFFF) else Color(0xB3FFFFFF)
    val popupSurface: Color get() = if (isDark) Color(0xFF1C1F26) else Color(0xFFFFFFFF)
    val shadow: Color
        get() = if (isDark) Color.Black.copy(alpha = 0.55f) else Color(0x29493928)

    // 强调色：夜间继承原青色，日间用规范橙色
    val accent: Color get() = if (isDark) Color(0xFF00E5D0) else Color(0xFFFF7A2F)
    val accentSoft: Color get() = if (isDark) Color(0xFF4C6FFF) else Color(0xFFFF9D61)
    val accentDeep: Color get() = if (isDark) Color(0xFF7C5CFF) else Color(0xFFFF6A24)
    val onAccent: Color get() = if (isDark) Color(0xFF04120F) else Color.White
}
