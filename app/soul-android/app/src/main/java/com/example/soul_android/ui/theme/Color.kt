package com.example.soul_android.ui.theme

import androidx.compose.ui.graphics.Color

/* ============ 夜间（原 SOUL 深空配色，保持不变） ============ */
val SoulCyan = Color(0xFF00E5D0)
val SoulPurple = Color(0xFF7C5CFF)
val SoulPink = Color(0xFFFF4D8D)
val SoulIndigo = Color(0xFF4C6FFF)

val SoulDarkBg = Color(0xFF07080D)
val SoulDeepBg = Color(0xFF0B0D14)
val SoulCardDark = Color(0xFF12151F)
val SoulCardElevated = Color(0xFF191D2A)
val SoulGlass = Color(0xFF171B26)

val SoulTextWhite = Color(0xFFF2F4F8)
val SoulTextSecondary = Color(0xFFA8B0C2)
val SoulTextGray = Color(0xFF6E7789)

val SoulHairline = Color(0x1FFFFFFF)
val SoulHairlineStrong = Color(0x33FFFFFF)
val SoulSuccess = Color(0xFF31D0AA)
val SoulWarning = Color(0xFFFFB020)
val SoulDanger = Color(0xFFFF5A5F)

val SoulAccentGradient = listOf(SoulCyan, SoulIndigo, SoulPurple)
val SoulWarmGradient = listOf(SoulPink, SoulPurple)
val SoulAuroraGradient = listOf(
    Color(0xFF00E5D0),
    Color(0xFF4C6FFF),
    Color(0xFF7C5CFF),
    Color(0xFFFF4D8D)
)

/* ============ 日间（Skeuomorphic Glass 规范） ============ */
val SoulOrange = Color(0xFFFF7A2F)
val SoulOrangeSoft = Color(0xFFFF9D61)
val SoulOrangeDeep = Color(0xFFFF6A24)

val SoulWarmBg = Color(0xFFF3EBDD)
val SoulWarmElevated = Color(0xFFFAF6EF)
val SoulStone = Color(0xFFD9CBB8)
val SoulStoneDark = Color(0xFFBDAE99)

val SoulWarmTextPrimary = Color(0xFF2C2925)
val SoulWarmTextSecondary = Color(0xFF716A62)
val SoulWarmTextMuted = Color(0xFF9C948B)

val SoulGlassFill = Color(0x6BFFFFFF)      // white 42%
val SoulGlassStroke = Color(0xB3FFFFFF)    // white 70%
val SoulShadowWarm = Color(0x29493928)     // rgba(73,57,40,.16)

// 兼容 Compose 模板默认值
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
