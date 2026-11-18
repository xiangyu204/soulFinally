package com.example.soul_android.ui.screens
import com.example.soul_android.ui.theme.SoulColors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soul_android.call.CallManager
import com.example.soul_android.data.LanguageState
import com.example.soul_android.models.AppLanguage
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * 全局通话浮层：
 * - 来电/呼出/通话中时覆盖整个 App 显示。
 * - 支持「小窗 / 全屏」切换。
 */
@Composable
fun CallOverlay() {
    val state by CallManager.state.collectAsState()

    if (state.phase == CallManager.Phase.IDLE) return

    var minimized by remember { mutableStateOf(false) }

    if (minimized) {
        MinimizedCallWindow(
            peerName = state.peerName ?: state.peerUsername ?: "",
            onExpand = { minimized = false }
        )
    } else {
        CallScreen(onMinimize = { minimized = true })
    }
}

@Composable
private fun MinimizedCallWindow(
    peerName: String,
    onExpand: () -> Unit
) {
    val state by CallManager.state.collectAsState()
    val language by LanguageState.language
    var elapsedSeconds by remember { mutableStateOf(0) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(state.startTimeMs) {
        while (state.startTimeMs != null) {
            elapsedSeconds =
                ((System.currentTimeMillis() - (state.startTimeMs ?: System.currentTimeMillis())) / 1000L).toInt()
            delay(1000)
        }
    }

    val label = when (language) {
        AppLanguage.CHINESE -> "通话中"
        AppLanguage.ENGLISH -> "On call"
        AppLanguage.KOREAN -> "통화 중"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF00D0D9), Color(0xFF7E57C2))
                    )
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetY += dragAmount.y
                    }
                }
                .clickable { onExpand() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "●",
                color = SoulColors.textPrimary,
                fontSize = 14.sp
            )
            Column {
                Text(
                    text = peerName,
                    color = SoulColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$label ${formatDurationShort(elapsedSeconds)}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun formatDurationShort(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}
