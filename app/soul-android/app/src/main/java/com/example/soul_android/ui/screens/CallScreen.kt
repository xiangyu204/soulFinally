package com.example.soul_android.ui.screens
import com.example.soul_android.ui.components.HazeBackground
import com.example.soul_android.ui.theme.SoulColors

import android.Manifest
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.soul_android.call.CallManager
import com.example.soul_android.data.LanguageState
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.ui.components.BackgroundGalaxy
import com.example.soul_android.ui.components.UserAvatar
import kotlinx.coroutines.delay

/**
 * 通话中界面（全屏）。
 * 视频：对方摄像头占全屏，自己摄像头在右上角小窗；支持定向静音（自己 / 对方）。
 */
@Composable
fun CallScreen(
    onMinimize: () -> Unit = {}
) {
    val language by LanguageState.language
    val state by CallManager.state.collectAsState()
    val remoteUid by CallManager.remoteUid.collectAsState()
    val error by CallManager.error.collectAsState()
    val context = LocalContext.current

    // 被叫方接听前申请麦克风/摄像头权限（否则单向、无小窗）
    val acceptPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            CallManager.acceptCall(context)
        } else {
            CallManager.rejectCall()
        }
    }

    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(state.startTimeMs) {
        while (state.startTimeMs != null) {
            elapsedSeconds =
                ((System.currentTimeMillis() - (state.startTimeMs ?: System.currentTimeMillis())) / 1000L).toInt()
            delay(1000)
        }
    }

    val peerLabel = state.peerName?.takeIf { it.isNotBlank() } ?: state.peerUsername ?: ""

    /*
     * 对方头像：有自定义头像用头像，没有则用用户名首字母
     */
    var peerAvatar by remember(state.peerUsername) { mutableStateOf<String?>(null) }

    LaunchedEffect(state.peerUsername) {
        val u = state.peerUsername ?: return@LaunchedEffect
        if (u.isBlank()) return@LaunchedEffect
        try {
            peerAvatar = SoulApiService.create().getUserProfile(u).body()?.avatar
        } catch (_: Exception) {
        }
    }

    val peerFallback = state.peerUsername?.takeIf { it.isNotBlank() }?.take(1)
        ?: peerLabel.take(1)
    val isVideo = state.callType == "VIDEO"
    val isIncoming = state.phase == CallManager.Phase.INCOMING
    val isOutgoing = state.phase == CallManager.Phase.OUTGOING
    val isActive = state.phase == CallManager.Phase.ACTIVE
    val isEnded = state.phase == CallManager.Phase.ENDED

    val timeText = formatDuration(elapsedSeconds)

    val statusText = when {
        isIncoming -> when (language) {
            AppLanguage.CHINESE -> "邀请你进行${if (isVideo) "视频" else "语音"}通话"
            AppLanguage.ENGLISH -> "invites you to a ${if (isVideo) "video" else "voice"} call"
            AppLanguage.KOREAN -> "${if (isVideo) "영상" else "음성"} 통화 요청"
        }
        isOutgoing -> when (language) {
            AppLanguage.CHINESE -> "正在等待对方接听..."
            AppLanguage.ENGLISH -> "Waiting for answer..."
            AppLanguage.KOREAN -> "상대방의 응답을 기다리는 중..."
        }
        isEnded -> state.endReason ?: when (language) {
            AppLanguage.CHINESE -> "通话已结束"
            AppLanguage.ENGLISH -> "Call ended"
            AppLanguage.KOREAN -> "통화 종료"
        }
        else -> timeText
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090B18))
    ) {

        val showVideo = isVideo && isActive

        if (showVideo) {
            // 对方摄像头占全屏
            if (remoteUid != null && !state.remoteVideoOff) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceView(ctx).also { CallManager.setupRemoteVideo(it) }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 对方关闭摄像头（或未接通）：黑屏 + 头像
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatar(avatar = buildCallAvatarUrl(peerAvatar), name = peerFallback, size = 110.dp)
                }
            }

            // 自己的摄像头（右上角小窗）
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .size(width = 100.dp, height = 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(1.dp, SoulColors.textSecondary, RoundedCornerShape(16.dp))
            ) {
                if (state.cameraOff) {
                    // 自己关闭摄像头：黑屏 + 头像
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        UserAvatar(avatar = null, name = state.myName.ifBlank { peerLabel }, size = 56.dp)
                    }
                } else {
                    AndroidView(
                        factory = { ctx ->
                            SurfaceView(ctx).also { CallManager.setupLocalVideo(it) }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 顶部：对方名字 + 时长
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = peerLabel,
                    color = SoulColors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isActive) {
                    Text(
                        text = timeText,
                        color = SoulColors.textPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            HazeBackground { BackgroundGalaxy() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.weight(1f))

                // 头像
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00D0D9), Color(0xFF7E57C2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = peerFallback,
                        color = SoulColors.textPrimary,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = peerLabel,
                    color = SoulColors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = statusText,
                    color = SoulColors.textSecondary,
                    fontSize = 15.sp
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = Color(0xFFFF8A80),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // 顶部右侧：最小化（挂断态隐藏）
        if (!isEnded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onMinimize) {
                    Text(
                        text = when (language) {
                            AppLanguage.CHINESE -> "小窗"
                            AppLanguage.ENGLISH -> "Minimize"
                            AppLanguage.KOREAN -> "최소화"
                        },
                        color = SoulColors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 挂断提示
        if (isEnded) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = null,
                        tint = SoulColors.textPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = statusText,
                        color = SoulColors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 底部：控制按钮
        if (!isEnded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 40.dp)
            ) {
                CallControls(
                    isVideo = isVideo,
                    muted = state.muted,
                    cameraOff = state.cameraOff,
                    remoteMuted = state.remoteMuted,
                    isIncoming = isIncoming,
                    isOutgoing = isOutgoing,
                    onAccept = {
                        val perms = if (isVideo) {
                            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                        } else {
                            arrayOf(Manifest.permission.RECORD_AUDIO)
                        }
                        acceptPermissionLauncher.launch(perms)
                    },
                    onReject = {
                        if (isIncoming) CallManager.rejectCall()
                        else if (isOutgoing) CallManager.cancelOutgoing()
                        else CallManager.endCall()
                    },
                    onEnd = { CallManager.endCall() },
                    onToggleMute = { CallManager.toggleMute() },
                    onToggleCamera = { CallManager.toggleCamera() },
                    onSwitchCamera = { CallManager.switchCamera() },
                    onToggleRemoteMute = { CallManager.toggleRemoteMute() }
                )
            }
        }
    }
}

@Composable
private fun CallControls(
    isVideo: Boolean,
    muted: Boolean,
    cameraOff: Boolean,
    remoteMuted: Boolean,
    isIncoming: Boolean,
    isOutgoing: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onEnd: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleRemoteMute: () -> Unit
) {
    if (isIncoming) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallCircleButton(
                icon = Icons.Default.CallEnd,
                tint = SoulColors.textPrimary,
                background = Color(0xFFE53935),
                onClick = onReject
            )
            Spacer(modifier = Modifier.width(56.dp))
            CallCircleButton(
                icon = Icons.Default.Call,
                tint = SoulColors.textPrimary,
                background = Color(0xFF43A047),
                onClick = onAccept
            )
        }
    } else if (isOutgoing) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CallCircleButton(
                icon = Icons.Default.CallEnd,
                tint = SoulColors.textPrimary,
                background = Color(0xFFE53935),
                onClick = onReject
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 自己麦克风
            CallCircleButton(
                icon = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                tint = SoulColors.textPrimary,
                background = Color.White.copy(alpha = 0.15f),
                onClick = onToggleMute
            )
            // 对方声音（远程静音）
            CallCircleButton(
                icon = if (remoteMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                tint = SoulColors.textPrimary,
                background = Color.White.copy(alpha = 0.15f),
                onClick = onToggleRemoteMute
            )
            if (isVideo) {
                CallCircleButton(
                    icon = if (cameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    tint = SoulColors.textPrimary,
                    background = Color.White.copy(alpha = 0.15f),
                    onClick = onToggleCamera
                )
                CallCircleButton(
                    icon = Icons.Default.Cameraswitch,
                    tint = SoulColors.textPrimary,
                    background = Color.White.copy(alpha = 0.15f),
                    onClick = onSwitchCamera
                )
            }
            CallCircleButton(
                icon = Icons.Default.CallEnd,
                tint = SoulColors.textPrimary,
                background = Color(0xFFE53935),
                onClick = onEnd
            )
        }
    }
}

@Composable
private fun CallCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    background: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(58.dp),
        shape = CircleShape,
        color = background,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

private fun buildCallAvatarUrl(avatar: String?): String? {
    if (avatar.isNullOrBlank()) return null
    if (avatar.startsWith("http://") || avatar.startsWith("https://")) return avatar
    return SoulApiService.BASE_URL.trimEnd('/') + "/" + avatar.trimStart('/')
}
