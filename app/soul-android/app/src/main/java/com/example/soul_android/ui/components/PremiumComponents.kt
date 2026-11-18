package com.example.soul_android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soul_android.data.LanguageState
import com.example.soul_android.data.ThemeState
import com.example.soul_android.ui.theme.SoulAccentGradient
import com.example.soul_android.ui.theme.SoulColors
import com.example.soul_android.ui.theme.SoulCyan
import com.example.soul_android.ui.theme.SoulHairline
import com.example.soul_android.ui.theme.SoulHairlineStrong
import com.example.soul_android.ui.theme.SoulPurple
import com.example.soul_android.ui.theme.SoulTextSecondary

/*
 * ============================================================
 * SOUL Premium Components
 * 按压形变 / 渐变描边玻璃卡 / 极光按钮 / 分段标题
 * ============================================================
 */

/** 按下时轻微缩放 + 弹性回弹，替代 Material 默认的涟漪 */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 900f),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** 极光渐变描边玻璃卡 */
@Composable
fun SoulGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: Dp = 20.dp,
    glow: Color? = null,
    borderAlpha: Float = 0.12f,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .then(if (onClick != null) Modifier.pressScale(interaction, 0.99f) else Modifier)
            .soulGlass(
                shape = shape,
                elevation = if (glow != null) 16.dp else 12.dp,
                accent = glow != null
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null
                    ) { onClick() }
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/** 主按钮：极光渐变 + 顶部高光 + 按压回弹 */
@Composable
fun SoulPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 56.dp
) {
    val dark = SoulColors.isDark
    val interaction = remember { MutableInteractionSource() }
    val shape = if (dark) RoundedCornerShape(18.dp) else RoundedCornerShape(999.dp)
    val active = enabled && !loading
    val buttonHeight = if (dark) 54.dp else height

    val gradient = if (dark) {
        listOf(Color(0xFF00E5D0), Color(0xFF4C6FFF), Color(0xFF7C5CFF))
    } else {
        listOf(Color(0xFFFF9D61), Color(0xFFFF6A24))
    }
    val glowColor = if (dark) Color(0xFF00E5D0) else Color(0x33FF7A2F)
    val glowColor2 = if (dark) Color(0xFF7C5CFF) else Color(0x33FF6A24)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .pressScale(interaction, if (dark) 0.97f else 0.98f)
            .shadow(
                elevation = if (active) (if (dark) 18.dp else 12.dp) else 0.dp,
                shape = shape,
                ambientColor = glowColor,
                spotColor = glowColor2
            )
            .clip(shape)
            .background(
                if (active) {
                    Brush.linearGradient(gradient)
                } else {
                    Brush.linearGradient(listOf(Color(0xFFE8E0D2), Color(0xFFDED5C6)))
                }
            )
            .border(
                width = 1.dp,
                color = if (active) {
                    if (dark) Color.Transparent else Color.White.copy(alpha = 0.50f)
                } else Color.White.copy(alpha = 0.30f),
                shape = shape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = active
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!dark) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = if (active) 0.18f else 0.04f), Color.Transparent)
                        )
                    )
            )
        }
        Text(
            text = if (loading) "···" else text,
            color = if (active) {
                if (dark) Color(0xFF04120F) else Color.White
            } else SoulColors.textMuted,
            fontSize = 16.sp,
            fontWeight = if (dark) FontWeight.Bold else FontWeight.SemiBold,
            letterSpacing = if (dark) 0.4.sp else 0.3.sp
        )
    }
}


/** 次级按钮：玻璃描边 */
@Composable
fun SoulGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .pressScale(interaction, 0.98f)
            .soulGlass(shape = shape, elevation = 8.dp)
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = SoulColors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** 区块标题：主标题 + 可选副标题 + 右侧操作 */
@Composable
fun SoulSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = SoulColors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = SoulTextSecondary,
                    fontSize = 12.5.sp
                )
            }
        }
        action?.invoke()
    }
}

/** 标签胶囊：选中态渐变，未选中态玻璃描边 */
@Composable
fun SoulTag(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .then(if (onClick != null) Modifier.pressScale(interaction, 0.94f) else Modifier)
            .clip(shape)
            .background(
                if (selected) {
                    Brush.linearGradient(SoulAccentGradient)
                } else {
                    Brush.linearGradient(
                        listOf(SoulColors.glassFill, SoulColors.glassFill)
                    )
                }
            )
            .border(
                1.dp,
                if (selected) Color.Transparent else SoulColors.hairline,
                shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null
                    ) { onClick() }
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = if (selected) SoulColors.onAccent else SoulColors.textSecondary,
            fontSize = 12.5.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

/** 渐变图标底座 */
@Composable
fun SoulIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = SoulCyan
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(
                Brush.linearGradient(
                    listOf(tint.copy(alpha = 0.22f), tint.copy(alpha = 0.06f))
                )
            )
            .border(1.dp, tint.copy(alpha = 0.22f), RoundedCornerShape(size * 0.32f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.46f)
        )
    }
}

/** 细分割线，带渐隐 */
@Composable
fun SoulDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, SoulHairlineStrong, Color.Transparent)
                )
            )
    )
}

/** 在线状态小圆点（带光晕） */
@Composable
fun SoulStatusDot(
    online: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (online) Color(0xFF31D0AA) else Color.White.copy(alpha = 0.25f)
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/*
 * ============================================================
 * 主题切换 & 语言切换（组合在顶栏右侧）
 * ============================================================
 */

/** 太阳 / 月亮切换按钮：点击切换深浅主题 */
@Composable
fun ThemeToggleButton() {
    val dark by ThemeState.isDark
    IconButton(onClick = { ThemeState.toggle() }) {
        Icon(
            imageVector = if (dark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = if (dark) "Light mode" else "Dark mode",
            tint = SoulColors.textPrimary.copy(alpha = 0.82f)
        )
    }
}

/** 顶栏右侧组合：主题切换 + 语言切换 */
@Composable
fun LanguageThemeActions() {
    val language by LanguageState.language
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        ThemeToggleButton()
        LanguageSelector(
            currentLanguage = language,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onLanguageSelected = { LanguageState.setLanguage(it) }
        )
    }
}
