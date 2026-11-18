package com.example.soul_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.soul_android.data.ThemeState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/*
 * ============================================================
 * Skeuomorphic Glass — 严格按样式规范实现
 * 玻璃: linear-gradient(145deg, white .58 → .28)
 *       border 1px white .68
 *       shadow 0 18 44 rgba(73,57,40,.14)
 *       backdrop blur 24
 * ============================================================
 */

val LocalHazeState = compositionLocalOf<HazeState?> { null }

@Composable
fun SoulHazeHost(content: @Composable () -> Unit) {
    val state = rememberHazeState()
    CompositionLocalProvider(LocalHazeState provides state) {
        content()
    }
}

@Composable
fun HazeBackground(content: @Composable BoxScope.() -> Unit) {
    val state = LocalHazeState.current
    Box(
        modifier = if (state != null) {
            Modifier.fillMaxSize().hazeSource(state = state)
        } else {
            Modifier.fillMaxSize()
        },
        content = content
    )
}

/**
 * 标准磨砂玻璃卡。
 * @param elevation 规范 level2 = 14dp，主卡片 18dp
 * @param accent 是否叠加右上角橙色折射光斑
 */
@Composable
fun Modifier.soulGlass(
    shape: Shape = RoundedCornerShape(26.dp),
    blur: Dp = 24.dp,
    elevation: Dp = 14.dp,
    accent: Boolean = false
): Modifier {
    val state = LocalHazeState.current
    val dark = ThemeState.isDark.value

    val fillTop = if (dark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.58f)
    val fillBottom = if (dark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.28f)
    val stroke = if (dark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.55f)
    val shadowColor = if (dark) Color.Black.copy(alpha = 0.55f) else Color(0x24493928)

    var m = this

    if (elevation > 0.dp) {
        m = m.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
    }

    m = m.clip(shape)

    if (state != null) {
        m = m.hazeEffect(
            state = state,
            style = HazeStyle(
                backgroundColor = Color.Transparent,
                tint = HazeTint(
                    if (dark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.10f)
                ),
                blurRadius = blur,
                noiseFactor = 0.02f
            )
        )
    }

    // 玻璃渐变填充（145deg 近似为左上→右下）
    m = m.background(
        Brush.linearGradient(
            colors = listOf(fillTop, fillBottom)
        )
    )

    // 橙色折射光斑（仅关键卡）
    if (accent) {
        m = m.background(
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF9D61).copy(alpha = 0.25f),
                    Color.Transparent
                )
            )
        )
    }

    // 1dp 玻璃描边
    m = m.border(width = 1.dp, color = stroke, shape = shape)

    return m
}

/** 星空背景（静态星点 + 星云），用于 AI 测试/客服等页面 */
fun Modifier.starryNightBackground(): Modifier = this.drawBehind {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF0B0D14), Color(0xFF07080D))))
    val rnd = kotlin.random.Random(42)
    repeat(150) {
        val x = rnd.nextFloat() * size.width
        val y = rnd.nextFloat() * size.height
        val r = rnd.nextFloat() * 1.6f + 0.4f
        val a = rnd.nextFloat() * 0.55f + 0.20f
        drawCircle(Color.White.copy(alpha = a), radius = r, center = Offset(x, y))
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF00D0D9).copy(alpha = 0.10f), Color.Transparent),
            center = Offset(size.width * 0.2f, size.height * 0.22f),
            radius = size.width * 0.7f
        ),
        radius = size.width * 0.7f
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF7C5CFF).copy(alpha = 0.10f), Color.Transparent),
            center = Offset(size.width * 0.82f, size.height * 0.78f),
            radius = size.width * 0.8f
        ),
        radius = size.width * 0.8f
    )
}

/**
 * 橙色柔光（关键操作 / 当前状态）
 */
@Composable
fun Modifier.soulOrangeGlow(radius: Dp = 10.dp): Modifier {
    return this.shadow(
        elevation = radius,
        shape = RoundedCornerShape(999.dp),
        ambientColor = Color(0x4DFF7A2F),
        spotColor = Color(0x4DFF7A2F)
    )
}
