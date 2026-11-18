package com.example.soul_android.ui.screens
import com.example.soul_android.ui.components.HazeBackground
import com.example.soul_android.ui.theme.SoulColors

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
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
import androidx.compose.ui.window.Dialog
import com.example.soul_android.call.RoomManager
import com.example.soul_android.data.LanguageState
import com.example.soul_android.data.api.MatchUserResponse
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.ui.components.BackgroundGalaxy
import com.example.soul_android.ui.components.UserAvatar
import kotlinx.coroutines.launch

@Composable
fun RoomScreen(onLeave: () -> Unit = {}) {
    val state by RoomManager.state.collectAsState()
    val messages by RoomManager.messages.collectAsState()
    val language by LanguageState.language
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var showInvite by remember { mutableStateOf(false) }

    val title = state.roomName ?: "语聊房"

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF090B18))) {
        HazeBackground { BackgroundGalaxy() }
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // 顶部
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = SoulColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${state.participants.size}/8", color = SoulColors.textSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = { showInvite = true }, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF00D0D9).copy(alpha = 0.15f))) {
                    Icon(Icons.Default.GroupAdd, null, tint = Color(0xFF00D0D9), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { RoomManager.leaveRoom(); onLeave() }, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE53935))) {
                    Icon(Icons.Default.CallEnd, null, tint = SoulColors.textPrimary, modifier = Modifier.size(20.dp))
                }
            }

            // 参与人头像网格
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally)) {
                    state.participantInfos.take(8).forEach { p ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                            UserAvatar(avatar = p.avatar, name = p.name ?: p.username ?: "", size = 52.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(p.name ?: p.username ?: "", color = SoulColors.textSecondary, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // 文字消息
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                items(messages, key = { "${it.timestamp}-${it.sender}-${it.content}" }) { msg ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("${msg.sender}：", color = Color(0xFF00D0D9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(msg.content ?: "", color = SoulColors.textPrimary, fontSize = 14.sp)
                    }
                }
            }

            // 底部输入 + 麦克风
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { RoomManager.toggleMute() }, modifier = Modifier.size(44.dp).clip(CircleShape).background(if (state.muted) Color(0xFFE53935) else Color.White.copy(alpha = 0.15f))) {
                    Icon(if (state.muted) Icons.Default.MicOff else Icons.Default.Mic, null, tint = SoulColors.textPrimary, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = { RoomManager.toggleSpeaker() }, modifier = Modifier.size(44.dp).clip(CircleShape).background(if (state.speakerMuted) Color(0xFFE53935) else Color.White.copy(alpha = 0.15f))) {
                    Icon(if (state.speakerMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, null, tint = SoulColors.textPrimary, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    placeholder = { Text(if (language == AppLanguage.CHINESE) "发消息..." else if (language == AppLanguage.ENGLISH) "Say something..." else "메시지...", color = SoulColors.textMuted) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.06f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                        focusedBorderColor = Color(0xFF00D0D9),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = SoulColors.textPrimary, unfocusedTextColor = SoulColors.textPrimary
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(
                    onClick = { RoomManager.sendMessage(input); input = "" },
                    enabled = input.isNotBlank(),
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF00D0D9), Color(0xFF7E57C2))))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = SoulColors.textPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showInvite) {
        InviteFriendDialog(context, onDismiss = { showInvite = false })
    }
}

@Composable
private fun InviteFriendDialog(context: Context, onDismiss: () -> Unit) {
    val api = remember { SoulApiService.create() }
    val scope = rememberCoroutineScope()
    var friends by remember { mutableStateOf<List<MatchUserResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val currentUsername = remember(context) {
        context.getSharedPreferences("soul_login_prefs", Context.MODE_PRIVATE).getString("username", "")
            ?.takeIf { it.isNotBlank() }
            ?: context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("username", "")
    }

    LaunchedEffect(Unit) {
        loading = true
        try {
            friends = currentUsername?.let { api.listFriends(it) } ?: emptyList()
        } catch (_: Exception) {
        }
        loading = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = SoulColors.popupSurface, shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("邀请好友", color = SoulColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00D0D9))
                    }
                } else if (friends.isEmpty()) {
                    Text("暂无好友", color = SoulColors.textSecondary, fontSize = 14.sp)
                } else {
                    friends.forEach { f ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(avatar = f.avatar, name = f.name ?: f.username, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(f.name ?: f.username, color = SoulColors.textPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                RoomManager.inviteFriend(f.username)
                                onDismiss()
                            }) {
                                Text("邀请", color = Color(0xFF00D0D9))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("关闭", color = SoulColors.textSecondary)
                }
            }
        }
    }
}
