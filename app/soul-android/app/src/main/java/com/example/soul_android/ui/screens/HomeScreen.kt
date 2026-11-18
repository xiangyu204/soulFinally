package com.example.soul_android.ui.screens
import com.example.soul_android.ui.theme.SoulColors

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soul_android.R
import com.example.soul_android.data.DummyData
import com.example.soul_android.data.LanguageState
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.models.User
import com.example.soul_android.ui.components.*
import com.example.soul_android.ui.viewmodels.*
import com.example.soul_android.ui.viewmodels.MatchUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToExplore: () -> Unit = {},
    onNavigateToMatches: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCustomerService: () -> Unit = {},
    onNavigateToAiQuiz: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    matchViewModel: MatchViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val language by LanguageState.language
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var isMatchingAnimationByButton by remember { mutableStateOf(false) }
    var profileDrawerOpen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val matchState by matchViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()

    // 将后端真实推荐用户映射为 UI User。技能字段必须做 null 兜底，避免后端脏数据导致 NPE。
    val usersFromBackend = remember(matchState) {
        val state = matchState
        if (state is MatchUiState.Success) {
            state.recommendedUsers.map { resp ->
                User(
                    id = resp.username,
                    name = resp.name,
                    bio = resp.nationality ?: "",
                    languages = emptyList(),
                    teachSkills = resp.skillOffer ?: emptyList(),
                    learnSkills = resp.skillWant ?: emptyList(),
                    matchRate = ((resp.averageRating ?: 4.0) * 20).toInt().coerceIn(0, 100),
                    isOnline = true
                )
            }
        } else {
            emptyList()
        }
    }

    val context = LocalContext.current
    val currentUsername = remember(context) {
        val soulPrefs = context.getSharedPreferences("soul_login_prefs", Context.MODE_PRIVATE)
        val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        soulPrefs.getString("username", "")?.takeIf { it.isNotBlank() }
            ?: userPrefs.getString("username", "")?.takeIf { it.isNotBlank() }
            ?: ""
    }

    LaunchedEffect(currentUsername) {
        if (currentUsername.isNotBlank()) {
            matchViewModel.findMatches(username = currentUsername)
            profileViewModel.fetchProfile(currentUsername)
        }
    }

    val displayName = (profileState as? ProfileUiState.Success)?.data?.name ?: "User"
    val avatarUrl = (profileState as? ProfileUiState.Success)?.data?.avatar
    val drawerOpenLatest by rememberUpdatedState(profileDrawerOpen)
    val openDrawerLatest by rememberUpdatedState({ profileDrawerOpen = true })
    val strings = when (language) {
        AppLanguage.KOREAN -> HomeStrings(
            welcome = "안녕하세요, ${displayName}님!",
            subtitle = "오늘의 추천 매칭입니다.",
            matchTitle = "추천 매칭",
            matchRate = "매칭률",
            home = "홈",
            explore = "탐색",
            matches = "매칭",
            chat = "채팅",
            profile = "프로필",
            startMatchText = "소울 매칭 시작"
        )
        AppLanguage.ENGLISH -> HomeStrings(
            welcome = "Hello, ${displayName}!",
            subtitle = "Today's recommended matches.",
            matchTitle = "Recommended",
            matchRate = "Match rate",
            home = "Home",
            explore = "Explore",
            matches = "Matches",
            chat = "Chat",
            profile = "Profile",
            startMatchText = "Start SOUL Matching"
        )
        AppLanguage.CHINESE -> HomeStrings(
            welcome = "你好，${displayName}！",
            subtitle = "今日推荐匹配。",
            matchTitle = "推荐匹配",
            matchRate = "匹配率",
            home = "首页",
            explore = "探索",
            matches = "匹配",
            chat = "聊天",
            profile = "我的",
            startMatchText = "开始 SOUL 匹配"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var total = 0f
                detectHorizontalDragGestures(
                    onDragEnd = { total = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        total += dragAmount
                        if (total > 140f && !drawerOpenLatest) {
                            openDrawerLatest()
                            total = 0f
                        }
                    }
                )
            }
    ) {
        HazeBackground { BackgroundGalaxy() }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        avatar = avatarUrl,
                        name = displayName,
                        size = 45.dp,
                        modifier = Modifier.clickable { profileDrawerOpen = true }
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateToAiQuiz) {
                            Icon(Icons.Default.Quiz, contentDescription = "AI Quiz", tint = SoulColors.textPrimary)
                        }
                        IconButton(onClick = onNavigateToCustomerService) {
                            Icon(Icons.Default.Headset, contentDescription = "AI Customer Service", tint = SoulColors.textPrimary)
                        }
                        LanguageThemeActions()
                    }
                }
            },
            bottomBar = {
                SoulNavigationBar(strings, onNavigateToExplore, onNavigateToMatches, onNavigateToChat)
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                item {
                    Column {
                        Text(text = strings.welcome, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SoulColors.textPrimary)
                        Text(text = strings.subtitle, style = MaterialTheme.typography.bodyMedium, color = SoulColors.textSecondary)
                    }
                }

                item {
                    SectionHeader(title = if (language == AppLanguage.CHINESE) "在线灵魂" else if (language == AppLanguage.ENGLISH) "Online Souls" else "온라인 소울러")
                    Spacer(modifier = Modifier.height(16.dp))
                    Planet3D(
                        users = usersFromBackend,
                        onUserClick = onUserClick
                    )
                }

                // 核心加成：3D 星球正下方的炫酷匹配盲盒大按钮
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isMatchingAnimationByButton = true
                                    delay(2800) // 动效维持2.8秒，充满探索期待感
                                    isMatchingAnimationByButton = false
                                    // 随机挑选宇宙中的一个同频者直接配对路由
                                    val randomUser = usersFromBackend.randomOrNull()
                                    if (randomUser != null) {
                                        onUserClick(randomUser.id)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF00D0D9), Color(0xFF7E57C2))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = SoulColors.textPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = strings.startMatchText,
                                        color = SoulColors.textPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SectionHeader(title = if (language == AppLanguage.CHINESE) "我的学习进度" else if (language == AppLanguage.ENGLISH) "My Learning" else "나의 학습 현황")
                    Spacer(modifier = Modifier.height(16.dp))
                    LearningStatusCard()
                }

                item { Spacer(modifier = Modifier.height(68.dp)) }
            }
        }

        ProfileDrawerOverlay(
            open = profileDrawerOpen,
            onClose = { profileDrawerOpen = false },
            avatar = avatarUrl,
            name = displayName,
            username = currentUsername,
            onEditProfile = onEditProfileClick,
            onSettings = onSettingsClick,
            onCustomerService = onNavigateToCustomerService,
            onAiQuiz = onNavigateToAiQuiz,
            onLogout = onLogoutClick
        )

        AnimatedVisibility(
            visible = isMatchingAnimationByButton,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            FullScreenRadarOverlay(if (language == AppLanguage.CHINESE) "正在通过量子共振搜索灵魂伴侣..." else if (language == AppLanguage.ENGLISH) "Searching your soulmate via quantum resonance..." else "양자 공명으로 소울메이트 탐색 중...")
        }
    }
}

@Composable
fun FullScreenRadarOverlay(statusText: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "quantum_radar")

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "deg"
    )
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing)), label = "s1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing)), label = "a1"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(enabled = false) {}, // 锁定底层交互
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                // 脉冲波形扩散
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = Color(0xFF00D0D9), radius = (size.minDimension / 2) * scale1, alpha = alpha1, style = Stroke(2.dp.toPx()))

                    // 雷达扫描线
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, Color(0xFF00D0D9).copy(alpha = 0.4f), Color(0xFF7E57C2))
                        ),
                        startAngle = angle,
                        sweepAngle = 90f,
                        useCenter = true
                    )

                    // 同轴刻度环
                    drawCircle(color = Color.White.copy(alpha = 0.08f), radius = size.minDimension / 2, style = Stroke(1.dp.toPx()))
                    drawCircle(color = Color.White.copy(alpha = 0.05f), radius = size.minDimension / 3, style = Stroke(1.dp.toPx()))
                }

                // 核心悬浮发光晶体
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .scale(0.9f + 0.1f * sin(Math.toRadians(angle.toDouble())).toFloat())
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFE0F7FA), Color(0xFF00D0D9), Color(0xFF7E57C2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Language, null, tint = SoulColors.textPrimary, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = statusText,
                color = SoulColors.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = SoulColors.textPrimary, letterSpacing = 0.5.sp)
}

@Composable
fun Planet3D(users: List<User>, onUserClick: (String) -> Unit) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var dragRotation by remember { mutableFloatStateOf(0f) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            rotation += 0.3f
            if (rotation >= 360f) rotation -= 360f
            delay(30)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .soulGlass(shape = RoundedCornerShape(32.dp), elevation = 8.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    dragRotation += dragAmount.x * 0.5f
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val planetRadius = 90f

            // 增强后的星球发光
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00D0D9).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = planetRadius * 3f
                ),
                radius = planetRadius * 3f
            )

            // 星球本体 - 更丰富的渐变
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFF00D0D9), Color(0xFF1A237E)),
                    center = Offset(centerX - 15f, centerY - 15f),
                    radius = planetRadius
                ),
                radius = planetRadius,
                center = Offset(centerX, centerY)
            )
        }

        if (users.isNotEmpty()) {
            // 对用户列表按照匹配率从高到低进行排序，精确筛选出前三名黄金配对者！
            val topThreeIds = users.sortedByDescending { it.matchRate }.take(3).map { it.id }.toSet()

            users.forEachIndexed { index, user ->
                val baseAngle = 360f / users.size * index
                val angle = baseAngle + rotation + dragRotation
                val radians = Math.toRadians(angle.toDouble())

                // 多轨道精细分布
                val orbitRadius = if (index % 3 == 0) 115f else if (index % 3 == 1) 140f else 160f
                val x = cos(radians).toFloat() * orbitRadius
                val y = sin(radians).toFloat() * orbitRadius * 0.45f
                val z = sin(radians).toFloat()
                val scale = 0.7f + ((z + 1f) / 2f) * 0.4f
                val alpha = 0.35f + ((z + 1f) / 2f) * 0.65f

                // 核心视觉策略：如果它是匹配度前三高的极品同频者，赋予耀眼的特殊渐变色调与发光呼吸描边！
                val isTopMatch = topThreeIds.contains(user.id)
                val ballBrush = if (isTopMatch) {
                    Brush.linearGradient(colors = listOf(Color(0xFFFF4081), Color(0xFFFF8A80))) // 独占高耀粉橙色底衬
                } else {
                    Brush.linearGradient(colors = listOf(Color(0xFF00D0D9), Color(0xFF7E57C2))) // 标准星空蓝紫色
                }

                val borderColor = if (isTopMatch) Color(0xFFFF4081).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f)
                val borderThickness = if (isTopMatch) 2.dp else 1.2.dp

                Box(
                    modifier = Modifier.align(Alignment.Center).offset(x = x.dp, y = y.dp).size((48 * scale).dp).alpha(alpha)
                        .clip(CircleShape)
                        .background(ballBrush)
                        .border(borderThickness * scale, borderColor, CircleShape)
                        .clickable { onUserClick(user.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1),
                        color = SoulColors.textPrimary,
                        fontWeight = if (isTopMatch) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = (if (isTopMatch) 15 * scale else 13 * scale).sp
                    )
                }
            }
        }

        selectedUser?.let { user ->
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.4f), // 增加暗度以突出文字
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF00D0D9)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = SoulColors.textPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontWeight = FontWeight.Bold, color = SoulColors.textPrimary, fontSize = 15.sp)
                        Text("${user.matchRate}% Match", color = Color(0xFF00D0D9), fontSize = 11.sp)
                    }
                    IconButton(onClick = { onUserClick(user.id) }) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = SoulColors.textPrimary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { selectedUser = null }) {
                        Icon(Icons.Default.Close, null, tint = SoulColors.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedUsersRow(onUserClick: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(end = 20.dp)) {
        items(DummyData.users) { user ->
            UserMatchCard(user, onUserClick)
        }
    }
}

@Composable
fun UserMatchCard(user: User, onUserClick: (String) -> Unit) {
    Box(
        modifier = Modifier.width(150.dp).height(210.dp).clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.05f)) // 更透亮的玻璃效果
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
            .clickable { onUserClick(user.id) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape)
                    .background(Brush.linearGradient(colors = listOf(Color(0xFF00D0D9), Color(0xFF7E57C2))))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = user.name.take(1), color = SoulColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(user.name, fontWeight = FontWeight.Bold, color = SoulColors.textPrimary, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(user.teachSkills.firstOrNull() ?: "", color = Color(0xFF00D0D9), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFF00D0D9).copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)) {
                Text("${user.matchRate}% Soul", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = Color(0xFF00D0D9), fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun LearningStatusCard() {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(colors = listOf(Color(0xFF7E57C2).copy(alpha = 0.3f), Color(0xFF00D0D9).copy(alpha = 0.3f))))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Python Programming", color = SoulColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Matching with Sarah", color = SoulColors.textSecondary, fontSize = 13.sp)
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { 0.7f }, color = Color(0xFF00D0D9), strokeWidth = 5.dp, trackColor = Color.White.copy(alpha = 0.1f))
                Text("70%", color = SoulColors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SoulNavigationBar(
    strings: HomeStrings,
    onExplore: () -> Unit,
    onMatches: () -> Unit,
    onChat: () -> Unit
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
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(68.dp)
        ) {
            val navItems = listOf(
                Triple(Icons.Default.Home, strings.home, true),
                Triple(Icons.Default.Search, strings.explore, false),
                Triple(Icons.Default.Favorite, strings.matches, false),
                Triple(Icons.AutoMirrored.Filled.Chat, strings.chat, false)
            )
            navItems.forEach { (icon, label, selected) ->
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            when(label) {
                                strings.explore -> onExplore()
                                strings.matches -> onMatches()
                                strings.chat -> onChat()
                            }
                        }
                    },
                    icon = { Icon(icon, null, modifier = Modifier.size(26.dp)) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (navDark) MaterialTheme.colorScheme.primary else Color.White,
                        selectedTextColor = if (navDark) MaterialTheme.colorScheme.primary else Color(0xFFFF7A2F),
                        unselectedIconColor = SoulColors.textSecondary,
                        unselectedTextColor = SoulColors.textSecondary,
                        indicatorColor = if (navDark) Color.Transparent else Color(0xFFFF7A2F)
                    )
                )
            }
        }
    }
}

private data class HomeStrings(
    val welcome: String, val subtitle: String, val matchTitle: String, val matchRate: String,
    val home: String, val explore: String, val matches: String, val chat: String, val profile: String,
    val startMatchText: String
)


