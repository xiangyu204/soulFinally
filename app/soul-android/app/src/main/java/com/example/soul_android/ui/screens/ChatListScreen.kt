package com.example.soul_android.ui.screens
import com.example.soul_android.ui.components.HazeBackground
import com.example.soul_android.ui.theme.SoulColors

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soul_android.data.DummyData
import com.example.soul_android.data.LanguageState
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.models.User
import com.example.soul_android.ui.components.BackgroundGalaxy
import com.example.soul_android.ui.components.BrandingSection
import com.example.soul_android.ui.components.LanguageSelector
import com.example.soul_android.ui.components.LanguageThemeActions
import com.example.soul_android.ui.components.soulGlass
import com.example.soul_android.ui.components.UserAvatar
import com.example.soul_android.ui.viewmodels.ChatListUiState
import com.example.soul_android.ui.viewmodels.ChatListViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (String) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateToMatches: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    chatListViewModel: ChatListViewModel = viewModel()
) {
    val language by LanguageState.language

    var languageMenuExpanded by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    /*
     * 현재 로그인한 사용자 가져오기
     */
    val currentUsername = remember(context) {

        val soulPrefs = context.getSharedPreferences(
            "soul_login_prefs",
            Context.MODE_PRIVATE
        )

        val userPrefs = context.getSharedPreferences(
            "user_prefs",
            Context.MODE_PRIVATE
        )

        soulPrefs.getString("username", "")
            ?.takeIf { it.isNotBlank() }
            ?: userPrefs.getString("username", "")
                ?.takeIf { it.isNotBlank() }
            ?: "xiangyu"
    }

    /*
     * 채팅방 상태
     */
    val chatListState by chatListViewModel.uiState.collectAsState()

    /*
     * 화면 진입 시 채팅방 조회
     */
    LaunchedEffect(currentUsername) {
        while (true) {
            if (currentUsername.isNotBlank()) {
                chatListViewModel.fetchRooms(currentUsername)
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    val strings = when (language) {
        AppLanguage.KOREAN -> ChatListStrings(
            title = "채팅",
            home = "홈",
            explore = "탐색",
            matches = "매칭",
            chat = "채팅",
            profile = "프로필",
            lastMsgPrefix = "마지막 메시지: "
        )
        AppLanguage.ENGLISH -> ChatListStrings(
            title = "Chats",
            home = "Home",
            explore = "Explore",
            matches = "Matches",
            chat = "Chat",
            profile = "Profile",
            lastMsgPrefix = "Last message: "
        )
        AppLanguage.CHINESE -> ChatListStrings(
            title = "聊天",
            home = "首页",
            explore = "探索",
            matches = "匹配",
            chat = "聊天",
            profile = "我的",
            lastMsgPrefix = "最后一条消息："
        )
    }

    /*
     * 실제 채팅 데이터
     *
     * Success -> 서버 데이터
     * Loading/Error -> 임시 데이터
     */
    val activeChats: List<User> = when (val state = chatListState) {

        is ChatListUiState.Success -> {
            state.rooms
        }

        else -> {
            DummyData.users.take(6)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        /*
         * Galaxy 배경
         */
        HazeBackground { BackgroundGalaxy() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,

            /*
             * 상단
             */
            topBar = {

                Column(
                    modifier = Modifier.statusBarsPadding()
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = strings.title,
                            color = SoulColors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LanguageThemeActions()
                    }
                }
            },

            /*
             * 하단 네비게이션
             */
            bottomBar = {

                SoulNavigationBar(
                    strings = strings,
                    onHome = onNavigateToHome,
                    onExplore = onNavigateToExplore,
                    onMatches = onNavigateToMatches
                )
            }

        ) { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),

                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 100.dp
                ),

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = activeChats,
                    key = { user -> user.id }
                ) { user ->

                    ChatListItem(
                        user = user,
                        prefix = strings.lastMsgPrefix,
                        onChatClick = onChatClick
                    )
                }
            }
        }
    }
}


/**
 * 하단 네비게이션
 */
@Composable
private fun SoulNavigationBar(
    strings: ChatListStrings,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onMatches: () -> Unit
) {

    val navDark = SoulColors.isDark
    val navShape = if (navDark) RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp) else RoundedCornerShape(34.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (navDark) Modifier else Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
            .navigationBarsPadding()
            .soulGlass(shape = navShape, elevation = if (navDark) 0.dp else 16.dp),
        shape = navShape,
        color = Color.Transparent
    ) {

        NavigationBar(
            modifier = Modifier.height(68.dp),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {

            val navItems = listOf(
                Triple(
                    Icons.Default.Home,
                    strings.home,
                    false
                ),
                Triple(
                    Icons.Default.Search,
                    strings.explore,
                    false
                ),
                Triple(
                    Icons.Default.Favorite,
                    strings.matches,
                    false
                ),
                Triple(
                    Icons.AutoMirrored.Filled.Chat,
                    strings.chat,
                    true
                )
            )

            navItems.forEach { (icon, label, selected) ->

                NavigationBarItem(

                    selected = selected,

                    onClick = {

                        if (!selected) {

                            when (label) {

                                strings.home -> onHome()

                                strings.explore -> onExplore()

                                strings.matches -> onMatches()
                            }
                        }
                    },

                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(26.dp)
                        )
                    },

                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },

                    colors = NavigationBarItemDefaults.colors(

                        selectedIconColor =
                            if (navDark) MaterialTheme.colorScheme.primary else Color.White,

                        selectedTextColor =
                            if (navDark) MaterialTheme.colorScheme.primary else Color(0xFFFF7A2F),

                        unselectedIconColor =
                            SoulColors.textSecondary,

                        unselectedTextColor =
                            SoulColors.textSecondary,

                        indicatorColor = if (navDark) Color.Transparent else Color(0xFFFF7A2F)
                    )
                )
            }
        }
    }
}


/**
 * 채팅 목록 Item
 */
@Composable
private fun ChatListItem(
    user: User,
    prefix: String,
    onChatClick: (String) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .soulGlass(
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp
            )
            .clickable {
                onChatClick(user.id)
            }
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            /*
             * 사용자头像
             */
            UserAvatar(
                avatar = user.avatar,
                name = user.name,
                size = 56.dp
            )

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SoulColors.textPrimary
                    )

                    Text(
                        text = formatChatListTime(user.lastMessageTime),
                        fontSize = 12.sp,
                        color = SoulColors.textSecondary
                    )
                }

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                val displayMsg = when {
                    user.lastMessageType == "CALL" -> {
                        val isVideo = user.lastMessageMedia.equals("VIDEO", true)
                        val callName = when (LanguageState.language.value) {
                            AppLanguage.CHINESE -> if (isVideo) "[视频通话]" else "[语音通话]"
                            AppLanguage.ENGLISH -> if (isVideo) "[Video call]" else "[Voice call]"
                            AppLanguage.KOREAN -> if (isVideo) "[영상 통화]" else "[음성 통화]"
                        }
                        val raw = user.bio.trim()
                        val status = when {
                            raw.startsWith("ended:", true) -> {
                                val secs = raw.substringAfter(":").trim().ifBlank { "0" }
                                when (LanguageState.language.value) {
                                    AppLanguage.CHINESE -> "$secs 秒"
                                    AppLanguage.ENGLISH -> "$secs s"
                                    AppLanguage.KOREAN -> "$secs 초"
                                }
                            }
                            raw.equals("missed", true) -> when (LanguageState.language.value) {
                                AppLanguage.CHINESE -> "未接听"
                                AppLanguage.ENGLISH -> "Missed"
                                AppLanguage.KOREAN -> "부재중"
                            }
                            raw.equals("canceled", true) || raw.equals("cancelled", true) -> when (LanguageState.language.value) {
                                AppLanguage.CHINESE -> "已取消"
                                AppLanguage.ENGLISH -> "Canceled"
                                AppLanguage.KOREAN -> "취소됨"
                            }
                            raw.equals("rejected", true) -> when (LanguageState.language.value) {
                                AppLanguage.CHINESE -> "对方已拒绝"
                                AppLanguage.ENGLISH -> "Declined"
                                AppLanguage.KOREAN -> "상대방이 거절함"
                            }
                            else -> when (LanguageState.language.value) {
                                AppLanguage.CHINESE -> "通话记录"
                                AppLanguage.ENGLISH -> "Call record"
                                AppLanguage.KOREAN -> "통화 기록"
                            }
                        }
                        "$callName $status"
                    }
                    user.lastMessageType == "IMAGE" -> {
                        when (LanguageState.language.value) {
                            AppLanguage.CHINESE -> "[图片]"
                            AppLanguage.ENGLISH -> "[Image]"
                            AppLanguage.KOREAN -> "[사진]"
                        }
                    }
                    user.bio.isNotBlank() && user.bio != "SOUL User" -> user.bio
                    else -> (if (LanguageState.language.value == AppLanguage.CHINESE) "开始对话吧！" else if (LanguageState.language.value == AppLanguage.ENGLISH) "Start a conversation!" else "대화를 시작해보세요!")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayMsg,
                        fontSize = 14.sp,
                        color = SoulColors.textSecondary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (user.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        UnreadBadge(user.unreadCount)
                    }
                }
            }
        }
    }
}


@Composable
private fun UnreadBadge(count: Int) {
    val text = if (count > 99) "99+" else count.toString()
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
            .background(Color(0xFFE53935), CircleShape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = SoulColors.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


private fun formatChatListTime(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(createdAt)
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date.from(instant))
    } catch (e: Exception) {
        ""
    }
}


/**
 * 채팅 화면文字
 */
private data class ChatListStrings(
    val title: String,
    val home: String,
    val explore: String,
    val matches: String,
    val chat: String,
    val profile: String,
    val lastMsgPrefix: String
)