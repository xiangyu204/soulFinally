package com.example.soul_android.ui.screens
import com.example.soul_android.ui.theme.SoulColors

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soul_android.data.api.FriendRequestResponse
import com.example.soul_android.data.api.MatchUserResponse
import com.example.soul_android.data.LanguageState
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.models.User
import com.example.soul_android.ui.components.*
import com.example.soul_android.ui.viewmodels.MatchUiState
import com.example.soul_android.ui.viewmodels.MatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    onUserClick: (String) -> Unit = {},
    onChatClick: (String) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onReviewClick: (String) -> Unit = {},
    matchViewModel: MatchViewModel = viewModel()
) {
    val language by LanguageState.language
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showMatchDialog by remember { mutableStateOf(false) }
    var lastMatchedUser by remember { mutableStateOf<User?>(null) }
    var remarkTarget by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val currentUsername = remember(context) {
        val soulPrefs = context.getSharedPreferences("soul_login_prefs", Context.MODE_PRIVATE)
        val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        soulPrefs.getString("username", "")?.takeIf { it.isNotBlank() }
            ?: userPrefs.getString("username", "")?.takeIf { it.isNotBlank() }
            ?: ""
    }

    val matchState by matchViewModel.uiState.collectAsState()
    val friendRequests by matchViewModel.friendRequests.collectAsState()

    LaunchedEffect(currentUsername) {
        if (currentUsername.isNotBlank()) {
            matchViewModel.findMatches(username = currentUsername)
            // 实时刷新好友申请（有新申请立即可见）
            while (true) {
                matchViewModel.loadFriendRequests(currentUsername)
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    fun toUser(resp: MatchUserResponse): User = User(
        id = resp.username,
        name = resp.name,
        bio = resp.nationality ?: "Soul User",
        languages = emptyList(),
        teachSkills = resp.skillOffer ?: emptyList(),
        learnSkills = resp.skillWant ?: emptyList(),
        matchRate = ((resp.averageRating ?: 4.0) * 20).toInt().coerceIn(0, 100),
        isOnline = true,
        avatar = resp.avatar
    )

    val recommendedUsers = (matchState as? MatchUiState.Success)
        ?.recommendedUsers?.map(::toUser).orEmpty()
    val myMatches = (matchState as? MatchUiState.Success)
        ?.myMatches?.map(::toUser).orEmpty()
    val friends = (matchState as? MatchUiState.Success)
        ?.friends?.map(::toUser).orEmpty()
    val blocks = (matchState as? MatchUiState.Success)
        ?.blocks?.map(::toUser).orEmpty()

    val strings = when (language) {
        AppLanguage.KOREAN -> MatchesStrings(
            title = "친구",
            receivedTab = "추천",
            myMatchesTab = "친구",
            blacklistTab = "차단",
            requestTab = "요청",
            acceptReq = "수락",
            rejectReq = "거절",
            remark = "메모",
            deleteFriend = "친구 삭제",
            block = "차단",
            unblock = "차단 해제",
            accept = "매칭",
            reject = "건너뛰기",
            chat = "채팅하기",
            teachPrefix = "교류 가능한 스킬",
            learnPrefix = "관심 스킬",
            matchRate = "매칭률: ",
            home = "홈",
            explore = "탐색",
            matches = "친구",
            chatLabel = "채팅",
            profile = "프로필",
            acceptedMsg = "새로운 매칭이 탄생했습니다!",
            rejectedMsg = "새로운 소울이 연결되었어요. 지금 바로 대화를 시작해보세요!"
        )
        AppLanguage.ENGLISH -> MatchesStrings(
            title = "Friends",
            receivedTab = "Suggest",
            myMatchesTab = "Friends",
            blacklistTab = "Blocked",
            requestTab = "Requests",
            acceptReq = "Accept",
            rejectReq = "Decline",
            remark = "Remark",
            deleteFriend = "Remove",
            block = "Block",
            unblock = "Unblock",
            accept = "Match",
            reject = "Skip",
            chat = "Chat",
            teachPrefix = "Can teach",
            learnPrefix = "Wants to learn",
            matchRate = "Match rate: ",
            home = "Home",
            explore = "Explore",
            matches = "Friends",
            chatLabel = "Chat",
            profile = "Profile",
            acceptedMsg = "A new match has been made!",
            rejectedMsg = "A new soul has connected. Start chatting now!"
        )
        AppLanguage.CHINESE -> MatchesStrings(
            title = "好友",
            receivedTab = "推荐",
            myMatchesTab = "好友",
            blacklistTab = "黑名单",
            requestTab = "申请",
            acceptReq = "同意",
            rejectReq = "拒绝",
            remark = "备注",
            deleteFriend = "删除好友",
            block = "拉黑",
            unblock = "解除拉黑",
            accept = "匹配",
            reject = "跳过",
            chat = "聊天",
            teachPrefix = "可教技能",
            learnPrefix = "想学技能",
            matchRate = "匹配率：",
            home = "首页",
            explore = "探索",
            matches = "好友",
            chatLabel = "聊天",
            profile = "我的",
            acceptedMsg = "新的匹配诞生了！",
            rejectedMsg = "新的灵魂已连接，快去聊天吧！"
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HazeBackground { BackgroundGalaxy() }
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = { Text(strings.title, color = SoulColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    actions = {
                        LanguageThemeActions()
                    }
                )
            },
            bottomBar = {
                SoulNavigationBar(strings, onNavigateToHome, onNavigateToExplore, onNavigateToChat)
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White.copy(alpha = 0.02f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text(strings.receivedTab, fontSize = 12.sp, maxLines = 1, softWrap = false, textAlign = TextAlign.Center, fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal, color = if (selectedTabIndex == 0) SoulColors.textPrimary else SoulColors.textSecondary) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text(strings.myMatchesTab, fontSize = 12.sp, maxLines = 1, softWrap = false, textAlign = TextAlign.Center, fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal, color = if (selectedTabIndex == 1) SoulColors.textPrimary else SoulColors.textSecondary) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text(strings.requestTab, fontSize = 12.sp, maxLines = 1, softWrap = false, textAlign = TextAlign.Center, fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal, color = if (selectedTabIndex == 2) SoulColors.textPrimary else SoulColors.textSecondary) }
                    )
                    Tab(
                        selected = selectedTabIndex == 3,
                        onClick = { selectedTabIndex = 3 },
                        text = { Text(strings.blacklistTab, fontSize = 12.sp, maxLines = 1, softWrap = false, textAlign = TextAlign.Center, fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal, color = if (selectedTabIndex == 3) SoulColors.textPrimary else SoulColors.textSecondary) }
                    )
                }

                when (val state = matchState) {
                    MatchUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00D0D9))
                    }
                    is MatchUiState.Error -> MatchEmptyState(state.message)
                    is MatchUiState.Success -> {
                        if (selectedTabIndex == 0) {
                            if (recommendedUsers.isEmpty()) {
                                MatchEmptyState(if (language == AppLanguage.CHINESE) "暂无推荐用户" else if (language == AppLanguage.ENGLISH) "No recommendations yet" else "추천할 사용자가 없습니다")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                    contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(recommendedUsers, key = { it.id }) { user ->
                                        ReceivedRequestCard(
                                            user = user,
                                            strings = strings,
                                            onUserClick = onUserClick,
                                            onAccept = {
                                                matchViewModel.acceptMatch(
                                                    currentUsername = currentUsername,
                                                    targetUsername = user.id,
                                                    onSuccess = {
                                                        lastMatchedUser = user
                                                        showMatchDialog = true
                                                    },
                                                    onError = { errorMessage = it }
                                                )
                                            },
                                            onReject = { matchViewModel.rejectLocally(user.id) }
                                        )
                                    }
                                }
                            }
                        } else if (selectedTabIndex == 1) {
                            if (friends.isEmpty()) {
                                MatchEmptyState(if (language == AppLanguage.CHINESE) "暂无好友，去聊天页加好友吧" else if (language == AppLanguage.ENGLISH) "No friends yet" else "아직 친구가 없습니다")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                    contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(friends, key = { it.id }) { user ->
                                        FriendCard(user, strings, onUserClick, onChatClick, onDelete = { matchViewModel.removeFriend(currentUsername, user.id) }, onBlock = { matchViewModel.blockUser(currentUsername, user.id) }, onRemark = { remarkTarget = user })
                                    }
                                }
                            }
                        } else if (selectedTabIndex == 2) {
                            if (friendRequests.isEmpty()) {
                                MatchEmptyState(if (language == AppLanguage.CHINESE) "暂无好友申请" else if (language == AppLanguage.ENGLISH) "No friend requests" else "친구 요청이 없습니다")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                    contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(friendRequests, key = { it.requestId ?: 0L }) { req ->
                                        FriendRequestCard(req, strings, onAccept = { matchViewModel.respondFriendRequest(req.requestId ?: 0, true, currentUsername) }, onReject = { matchViewModel.respondFriendRequest(req.requestId ?: 0, false, currentUsername) })
                                    }
                                }
                            }
                        } else {
                            if (blocks.isEmpty()) {
                                MatchEmptyState(if (language == AppLanguage.CHINESE) "黑名单为空" else if (language == AppLanguage.ENGLISH) "No blocked users" else "차단한 사용자가 없습니다")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                    contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(blocks, key = { it.id }) { user ->
                                        BlockCard(user, strings, onUnblock = { matchViewModel.unblockUser(currentUsername, user.id) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        RoomEntryButton()

        remarkTarget?.let { target ->
            RemarkDialog(targetName = target.name, onSave = { text -> matchViewModel.setRemark(currentUsername, target.id, text) }, onDismiss = { remarkTarget = null })
        }

        if (showMatchDialog && lastMatchedUser != null) {
            ItsAMatchDialog(
                user = lastMatchedUser!!,
                strings = strings,
                onDismiss = { showMatchDialog = false },
                onChatClick = {
                    showMatchDialog = false
                    onChatClick(it)
                }
            )
        }

        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text(if (language == AppLanguage.CHINESE) "匹配失败" else if (language == AppLanguage.ENGLISH) "Match failed" else "매칭 실패") },
                text = { Text(errorMessage.orEmpty()) },
                confirmButton = { TextButton(onClick = { errorMessage = null }) { Text(if (language == AppLanguage.CHINESE) "确定" else if (language == AppLanguage.ENGLISH) "OK" else "확인") } }
            )
        }
    }
}

@Composable
private fun MatchEmptyState(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart), label = "p1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart), label = "a1"
    )
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(3000, delayMillis = 1500, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart), label = "p2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3000, delayMillis = 1500, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart), label = "a2"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = Color(0xFF00D0D9), radius = (size.minDimension / 2) * pulseScale1, alpha = pulseAlpha1, style = Stroke(2.dp.toPx()))
                    drawCircle(color = Color(0xFF7E57C2), radius = (size.minDimension / 2) * pulseScale2, alpha = pulseAlpha2, style = Stroke(2.dp.toPx()))
                }
                Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FavoriteBorder, null, tint = Color(0xFF00D0D9), modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = message, color = SoulColors.textSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SoulNavigationBar(strings: MatchesStrings, onHome: () -> Unit, onExplore: () -> Unit, onChat: () -> Unit) {
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
        NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp, modifier = Modifier.height(68.dp)) {
            val navItems = listOf(Triple(Icons.Default.Home, strings.home, false), Triple(Icons.Default.Search, strings.explore, false), Triple(Icons.Default.Favorite, strings.matches, true), Triple(Icons.AutoMirrored.Filled.Chat, strings.chatLabel, false))
            navItems.forEach { (icon, label, selected) ->
                NavigationBarItem(selected = selected, onClick = { if(!selected) when(label) { strings.home -> onHome(); strings.explore -> onExplore(); strings.chatLabel -> onChat() } }, icon = { Icon(icon, null, modifier = Modifier.size(26.dp)) }, label = { Text(label, fontSize = 11.sp, fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = if (navDark) MaterialTheme.colorScheme.primary else Color.White, selectedTextColor = if (navDark) MaterialTheme.colorScheme.primary else Color(0xFFFF7A2F), unselectedIconColor = SoulColors.textSecondary, unselectedTextColor = SoulColors.textSecondary, indicatorColor = if (navDark) Color.Transparent else Color(0xFFFF7A2F)))
            }
        }
    }
}

@Composable
private fun ReceivedRequestCard(user: User, strings: MatchesStrings, onUserClick: (String) -> Unit, onAccept: () -> Unit, onReject: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().soulGlass(shape = RoundedCornerShape(28.dp), elevation = 10.dp).clickable { onUserClick(user.id) }.padding(20.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(avatar = user.avatar, name = user.name, size = 56.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SoulColors.textPrimary)
                        Surface(color = Color(0xFF00D0D9).copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)) {
                            Text("${user.matchRate}% Match", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 11.sp, color = Color(0xFF00D0D9), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(user.bio, fontSize = 13.sp, color = SoulColors.textSecondary, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(14.dp))

            // Skills Badges Display
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (user.teachSkills.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.teachPrefix, fontSize = 12.sp, color = SoulColors.textMuted, modifier = Modifier.width(85.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            user.teachSkills.forEach { skill ->
                                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF00D0D9).copy(alpha = 0.1f)).border(1.dp, Color(0xFF00D0D9).copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text(skill, color = Color(0xFF00D0D9), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
                if (user.learnSkills.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.learnPrefix, fontSize = 12.sp, color = SoulColors.textMuted, modifier = Modifier.width(85.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            user.learnSkills.forEach { skill ->
                                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF7E57C2).copy(alpha = 0.1f)).border(1.dp, Color(0xFF7E57C2).copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text(skill, color = Color(0xFFB39DDB), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onReject, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(23.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.06f), contentColor = SoulColors.textSecondary)) {
                    Text(strings.reject, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Button(onClick = onAccept, modifier = Modifier.weight(1.3f).height(46.dp), shape = RoundedCornerShape(23.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text(strings.accept, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MyMatchCard(user: User, strings: MatchesStrings, onUserClick: (String) -> Unit, onChatClick: (String) -> Unit, onReviewClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp)).clickable { onUserClick(user.id) }.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(avatar = user.avatar, name = user.name, size = 56.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SoulColors.textPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    user.teachSkills.take(2).forEach { skill ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF00D0D9).copy(alpha = 0.08f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(skill, color = Color(0xFF00D0D9), fontSize = 11.sp)
                        }
                    }
                }
            }
            IconButton(onClick = { onReviewClick(user.id) }, modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFFFFC107).copy(alpha = 0.12f)).border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), CircleShape)) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { onChatClick(user.id) }, modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF00D0D9).copy(alpha = 0.12f)).border(1.dp, Color(0xFF00D0D9).copy(alpha = 0.2f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color(0xFF00D0D9), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ItsAMatchDialog(
    user: User,
    strings: MatchesStrings,
    onDismiss: () -> Unit,
    onChatClick: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1500, easing = SineWaveEasing), repeatMode = RepeatMode.Reverse), label = "g"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.88f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp).clickable(enabled = false) {}) {

                // Animated Match Heart / Icon
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp).scale(glowScale)) {
                    Box(modifier = Modifier.size(140.dp).blur(30.dp).background(Color(0xFF00D0D9).copy(alpha = 0.25f), CircleShape))
                    Box(modifier = Modifier.size(100.dp).blur(25.dp).background(Color(0xFF7E57C2).copy(alpha = 0.25f), CircleShape))
                    Icon(Icons.Default.Favorite, null, tint = Color(0xFFFF4081), modifier = Modifier.size(80.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = strings.acceptedMsg, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = SoulColors.textPrimary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = strings.rejectedMsg, fontSize = 14.sp, color = SoulColors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))

                Spacer(modifier = Modifier.height(40.dp))

                // Avatar connection graphic
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).border(2.dp, SoulColors.textPrimary, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = SoulColors.textPrimary, modifier = Modifier.size(36.dp))
                    }
                    Box(modifier = Modifier.width(40.dp).height(2.dp).background(Brush.horizontalGradient(listOf(SoulColors.textPrimary, Color(0xFF00D0D9)))))
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(Brush.linearGradient(colors = listOf(Color(0xFF00D0D9), Color(0xFF7E57C2)))).border(2.dp, Color(0xFF00D0D9), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = SoulColors.textPrimary, modifier = Modifier.size(36.dp))
                    }
                }
                Text(user.name, fontWeight = FontWeight.Bold, color = SoulColors.textPrimary, fontSize = 18.sp, modifier = Modifier.padding(top = 12.dp))

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { onChatClick(user.id) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D0D9), contentColor = SoulColors.textPrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.chat, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss) {
                    Text(if(strings.title == "매칭") "계속 매칭하기" else if(strings.title == "Matches") "Keep Exploring" else "继续探索", color = SoulColors.textSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun FriendCard(user: User, strings: MatchesStrings, onUserClick: (String) -> Unit, onChatClick: (String) -> Unit, onDelete: () -> Unit, onBlock: () -> Unit, onRemark: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp)).clickable { onUserClick(user.id) }.padding(16.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(avatar = user.avatar, name = user.name, size = 56.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SoulColors.textPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        user.teachSkills.take(2).forEach { skill ->
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF00D0D9).copy(alpha = 0.08f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(skill, color = Color(0xFF00D0D9), fontSize = 11.sp)
                            }
                        }
                    }
                }
                IconButton(onClick = { onChatClick(user.id) }, modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF00D0D9).copy(alpha = 0.12f)).border(1.dp, Color(0xFF00D0D9).copy(alpha = 0.2f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color(0xFF00D0D9), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onRemark, modifier = Modifier.weight(1f), colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF00D0D9))) {
                    Text(strings.remark, fontSize = 13.sp)
                }
                TextButton(onClick = onDelete, modifier = Modifier.weight(1f), colors = ButtonDefaults.textButtonColors(contentColor = SoulColors.textSecondary)) {
                    Text(strings.deleteFriend, fontSize = 13.sp)
                }
                TextButton(onClick = onBlock, modifier = Modifier.weight(1f), colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))) {
                    Text(strings.block, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun FriendRequestCard(req: FriendRequestResponse, strings: MatchesStrings, onAccept: () -> Unit, onReject: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp)).padding(16.dp)) {
        Column {
            Text(req.fromName ?: req.fromUser ?: "", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SoulColors.textPrimary)
            if (!req.message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(req.message, color = SoulColors.textSecondary, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(23.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = SoulColors.textSecondary)) {
                    Text(strings.rejectReq, fontSize = 13.sp)
                }
                Button(onClick = onAccept, modifier = Modifier.weight(1f), shape = RoundedCornerShape(23.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D0D9))) {
                    Text(strings.acceptReq, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun BlockCard(user: User, strings: MatchesStrings, onUnblock: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(avatar = user.avatar, name = user.name, size = 56.dp, modifier = Modifier.alpha(0.6f))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SoulColors.textSecondary)
            }
            TextButton(onClick = onUnblock, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF00D0D9))) {
                Text(strings.unblock, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RemarkDialog(targetName: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = SoulColors.popupSurface, shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("备注好友", color = SoulColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("给 $targetName 设置备注（仅自己可见）", color = SoulColors.textSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text, onValueChange = { text = it },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("输入备注...", color = SoulColors.textMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoulColors.textPrimary, unfocusedTextColor = SoulColors.textPrimary, focusedBorderColor = Color(0xFF00D0D9), unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消", color = SoulColors.textSecondary) }
                    TextButton(onClick = { onSave(text.trim()); onDismiss() }) { Text("保存", color = Color(0xFF00D0D9), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

private val SineWaveEasing = Easing { fraction ->
    val radians = fraction * Math.PI * 2
    ((Math.sin(radians) + 1) / 2).toFloat()
}

private data class MatchesStrings(
    val title: String, val receivedTab: String, val myMatchesTab: String, val blacklistTab: String,
    val requestTab: String, val acceptReq: String, val rejectReq: String, val remark: String,
    val deleteFriend: String, val block: String, val unblock: String, val accept: String, val reject: String,
    val chat: String, val teachPrefix: String, val learnPrefix: String, val matchRate: String, val home: String,
    val explore: String, val matches: String, val chatLabel: String, val profile: String, val acceptedMsg: String, val rejectedMsg: String
)

