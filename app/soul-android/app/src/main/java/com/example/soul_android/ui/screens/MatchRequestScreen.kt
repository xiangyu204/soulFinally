package com.example.soul_android.ui.screens
import com.example.soul_android.ui.theme.SoulColors

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soul_android.data.LanguageState
import com.example.soul_android.data.api.FriendRequest
import com.example.soul_android.data.api.ProfileResponse
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.ui.components.LanguageSelector
import com.example.soul_android.ui.components.SoulButton
import com.example.soul_android.ui.components.UserAvatar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchRequestScreen(
    userId: String,
    onBackClick: () -> Unit = {},
    onSendRequestClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val language by LanguageState.language
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<ProfileResponse?>(null) }
    val scope = rememberCoroutineScope()

    val api = remember { SoulApiService.create() }

    val currentUsername = remember(context) {
        val soul = context.getSharedPreferences("soul_login_prefs", Context.MODE_PRIVATE)
        val user = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        soul.getString("username", null)
            ?: soul.getString("logged_in_user", null)
            ?: user.getString("username", null)
            ?: ""
    }

    val strings = when (language) {
        AppLanguage.KOREAN -> MatchRequestStrings(
            "친구 요청", "함께 교환할 스킬", "요청 메시지",
            "요청 메시지를 입력하세요", "친구 요청 보내기",
            "요청 완료", "친구 요청을 성공적으로 보냈습니다.",
            "기술 정보 없음", "전송 중...", "전송 실패", "확인", "취소"
        )
        AppLanguage.ENGLISH -> MatchRequestStrings(
            "Send Friend Request", "Matching Skills", "Request Message",
            "Type a message to the user", "Send Friend Request",
            "Sent", "Friend request sent successfully.",
            "No skills", "Sending...", "Failed to send", "OK", "Cancel"
        )
        AppLanguage.CHINESE -> MatchRequestStrings(
            "发送好友请求", "匹配技能", "请求消息",
            "输入给对方的消息", "发送好友请求",
            "已发送", "好友请求已成功发送。",
            "暂无技能", "发送中...", "发送失败", "确定", "取消"
        )
    }

    // 拉取真实资料
    LaunchedEffect(userId) {
        try {
            profile = api.getUserProfile(userId).body()
        } catch (_: Exception) {
        }
    }

    val displayName = profile?.name?.takeIf { it.isNotBlank() } ?: userId
    val avatar = profile?.avatar
    val skills = remember(profile) {
        val list = mutableListOf<String>()
        profile?.skillOffer?.split(",", "，", "、", "/")?.forEach {
            val t = it.trim()
            if (t.isNotBlank()) list.add(t)
        }
        profile?.skillWant?.split(",", "，", "、", "/")?.forEach {
            val t = it.trim()
            if (t.isNotBlank()) list.add(t)
        }
        list.distinct()
    }

    fun send() {
        if (sending) return
        errorText = null
        if (currentUsername.isBlank()) {
            errorText = strings.failed
            return
        }
        if (userId.isBlank() || userId == currentUsername) {
            errorText = strings.failed
            return
        }
        sending = true
        scope.launch {
            try {
                val resp = api.requestFriend(
                    FriendRequest(
                        username = currentUsername,
                        friendUsername = userId,
                        message = message.trim()
                    )
                )
                if (resp.isSuccessful) {
                    sent = true
                } else {
                    errorText = strings.failed
                }
            } catch (_: Exception) {
                errorText = strings.failed
            } finally {
                sending = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    LanguageSelector(
                        currentLanguage = language,
                        expanded = languageMenuExpanded,
                        onExpandedChange = { languageMenuExpanded = it },
                        onLanguageSelected = { LanguageState.setLanguage(it) }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12151F))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        avatar = buildRequestAvatarUrl(avatar),
                        name = displayName,
                        size = 60.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SoulColors.textPrimary
                        )
                        Text(
                            text = if (skills.isEmpty()) strings.noSkills else skills.joinToString(", "),
                            color = Color(0xFF00E5D0),
                            fontSize = 14.sp,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = strings.matchingSkills,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SoulColors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (skills.isEmpty()) {
                Text(strings.noSkills, color = Color.Gray, fontSize = 13.sp)
            } else {
                skills.forEach { skill ->
                    Box(modifier = Modifier.padding(bottom = 8.dp)) {
                        SkillBadgeComponent(skill)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = strings.messageLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SoulColors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text(strings.messageHint, fontSize = 14.sp, color = Color.White.copy(alpha = 0.35f)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.04f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                    focusedTextColor = SoulColors.textPrimary,
                    unfocusedTextColor = SoulColors.textPrimary,
                    focusedBorderColor = Color(0xFF00E5D0),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                    cursorColor = Color(0xFF00E5D0)
                )
            )

            if (errorText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = errorText ?: "", color = Color(0xFFE53935), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))
            SoulButton(
                text = if (sending) strings.sending else strings.sendBtn,
                onClick = { send() }
            )
        }
    }

    if (sent) {
        AlertDialog(
            onDismissRequest = {
                sent = false
                onSendRequestClick()
            },
            containerColor = Color(0xFF12151F),
            title = { Text(strings.successTitle, fontWeight = FontWeight.Bold, color = SoulColors.textPrimary) },
            text = { Text(strings.successMsg, color = SoulColors.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        sent = false
                        onSendRequestClick()
                    }
                ) {
                    Text(strings.ok, color = Color(0xFF00BFA5), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SkillBadgeComponent(skill: String) {
    Surface(color = Color.White.copy(alpha = 0.06f), shape = RoundedCornerShape(10.dp)) {
        Text(
            text = skill,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

private fun buildRequestAvatarUrl(avatar: String?): String? {
    if (avatar.isNullOrBlank()) return null
    if (avatar.startsWith("http://") || avatar.startsWith("https://")) return avatar
    return SoulApiService.BASE_URL.trimEnd('/') + "/" + avatar.trimStart('/')
}

private data class MatchRequestStrings(
    val title: String,
    val matchingSkills: String,
    val messageLabel: String,
    val messageHint: String,
    val sendBtn: String,
    val successTitle: String,
    val successMsg: String,
    val noSkills: String,
    val sending: String,
    val failed: String,
    val ok: String,
    val cancel: String
)
