package com.example.soul_android.ui.screens
import com.example.soul_android.ui.theme.SoulColors

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.soul_android.call.RoomManager
import com.example.soul_android.data.LanguageState
import com.example.soul_android.models.AppLanguage
import kotlin.math.roundToInt

/**
 * 语聊房全局浮层：圆形可移动按钮 → 进入/创建房间；有邀请时弹接受/拒绝框。
 */
@Composable
fun RoomOverlay() {
    val state by RoomManager.state.collectAsState()
    val invites by RoomManager.incomingInvites.collectAsState()
    val language by LanguageState.language

    if (state.roomId != null) {
        RoomScreen()
        return
    }

    invites.firstOrNull()?.let { invite ->
        RoomInvitationDialog(
            invite = invite,
            language = language,
            onAccept = { RoomManager.respondToInvite(invite, true) },
            onReject = { RoomManager.respondToInvite(invite, false) }
        )
    }
}

/** 语聊房入口浮窗（只放在好友界面） */
@Composable
fun RoomEntryButton() {
    val state by RoomManager.state.collectAsState()
    val language by LanguageState.language
    val error by RoomManager.error.collectAsState()
    val context = LocalContext.current

    if (state.roomId != null) return

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            RoomManager.clearError()
        }
    }

    var showLobby by remember { mutableStateOf(false) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { showLobby = true },
            containerColor = Color(0xFF00D0D9),
            contentColor = SoulColors.textPrimary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .padding(horizontal = 8.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetY += dragAmount.y
                    }
                },
            shape = CircleShape
        ) {
            Icon(Icons.Default.Mic, contentDescription = null)
        }
    }

    if (showLobby) {
        RoomLobbyDialog(language, onDismiss = { showLobby = false })
    }
}

@Composable
private fun RoomLobbyDialog(language: AppLanguage, onDismiss: () -> Unit) {
    var roomName by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = SoulColors.popupSurface, shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text(
                    when (language) {
                        AppLanguage.CHINESE -> "语聊房"
                        AppLanguage.ENGLISH -> "Voice Room"
                        AppLanguage.KOREAN -> "음성 채팅방"
                    },
                    color = SoulColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    placeholder = {
                        Text(
                            when (language) {
                                AppLanguage.CHINESE -> "房间名（可留空）"
                                AppLanguage.ENGLISH -> "Room name (optional)"
                                AppLanguage.KOREAN -> "방 이름 (선택)"
                            },
                            color = SoulColors.textMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.06f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                        focusedBorderColor = Color(0xFF00D0D9),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = SoulColors.textPrimary, unfocusedTextColor = SoulColors.textPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { RoomManager.createRoom(roomName); onDismiss() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D0D9))
                ) {
                    Text(
                        when (language) {
                            AppLanguage.CHINESE -> "创建语聊房"
                            AppLanguage.ENGLISH -> "Create Room"
                            AppLanguage.KOREAN -> "방 만들기"
                        },
                        color = SoulColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(
                        when (language) {
                            AppLanguage.CHINESE -> "关闭"
                            AppLanguage.ENGLISH -> "Close"
                            AppLanguage.KOREAN -> "닫기"
                        },
                        color = SoulColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomInvitationDialog(
    invite: com.example.soul_android.data.api.RoomStatusResponse,
    language: AppLanguage,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(onDismissRequest = onReject) {
        Surface(color = SoulColors.popupSurface, shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text(
                    "${invite.owner ?: ""} " + when (language) {
                        AppLanguage.CHINESE -> "邀请你加入语聊房"
                        AppLanguage.ENGLISH -> "invites you to a voice room"
                        AppLanguage.KOREAN -> "음성 채팅방에 초대합니다"
                    },
                    color = SoulColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = SoulColors.textSecondary)
                    ) {
                        Text(when (language) { AppLanguage.CHINESE -> "拒绝"; AppLanguage.ENGLISH -> "Decline"; AppLanguage.KOREAN -> "거절" })
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D0D9))
                    ) {
                        Text(when (language) { AppLanguage.CHINESE -> "接受"; AppLanguage.ENGLISH -> "Accept"; AppLanguage.KOREAN -> "수락" })
                    }
                }
            }
        }
    }
}
