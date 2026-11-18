package com.example.soul_android

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.soul_android.call.CallManager
import com.example.soul_android.call.RoomManager
import com.example.soul_android.data.NotifyPrefs
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.notifications.MessageNotifier
import com.example.soul_android.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun SOULApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 持续监听登录状态：登录 / 切换账号后自动启动来电轮询
    LaunchedEffect(Unit) {
        var lastUsername = ""
        while (true) {
            val soulPrefs = context.getSharedPreferences("soul_login_prefs", Context.MODE_PRIVATE)
            val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val username = soulPrefs.getString("username", "")?.takeIf { it.isNotBlank() }
                ?: userPrefs.getString("username", "")?.takeIf { it.isNotBlank() }
                ?: ""
            if (username.isNotBlank() && username != lastUsername) {
                lastUsername = username
                CallManager.startIncomingPolling(this, username)
                RoomManager.start(username, this, context)
            }
            kotlinx.coroutines.delay(2000)
        }
    }

    // 新消息通知轮询（开关开启时才弹）
    LaunchedEffect(Unit) {
        val lastUnread = mutableMapOf<String, Int>()
        val notifApi = SoulApiService.create()
        while (true) {
            try {
                val soulPrefs = context.getSharedPreferences("soul_login_prefs", Context.MODE_PRIVATE)
                val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val me = soulPrefs.getString("username", "")?.takeIf { it.isNotBlank() }
                    ?: userPrefs.getString("username", "")?.takeIf { it.isNotBlank() }
                    ?: ""
                if (me.isNotBlank() && NotifyPrefs.isEnabled(context)) {
                    val rooms = notifApi.getChatRooms(me)
                    rooms.forEach { room ->
                        val partner = room["partnerUsername"] as? String ?: return@forEach
                        val name = room["partnerName"] as? String ?: partner
                        val unread = (room["unreadCount"] as? Number)?.toInt() ?: 0
                        val prev = lastUnread[partner]
                        if (prev != null && unread > prev) {
                            MessageNotifier.show(
                                context,
                                partner.hashCode(),
                                name,
                                room["lastMessage"] as? String ?: "新消息"
                            )
                        }
                        lastUnread[partner] = unread
                    }
                }
            } catch (_: Exception) {
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            CallManager.stopPolling()
            RoomManager.stop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
        // --- Authentication & Onboarding ---
        composable("login") {
            LoginScreen(
                onSignUpClick = { navController.navigate("signup") },
                onLoginSuccess = { 
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onDemoClick = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onBackToLogin = { navController.popBackStack() },
                onSignUpSuccess = { navController.navigate("profile_setup") }
            )
        }

        composable("profile_setup") {
            ProfileSetupScreen(
                onNextClick = { navController.navigate("skill_selection") }
            )
        }

        composable("skill_selection") {
            SkillSelectionScreen(
                onBackClick = { navController.popBackStack() },
                onDoneClick = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // --- Main Tabs ---
        composable("home") {
            HomeScreen(
                onNavigateToExplore = { navController.navigate("explore") },
                onNavigateToMatches = { navController.navigate("matches") },
                onNavigateToChat = { navController.navigate("chat_list") },
                onNavigateToProfile = { navController.navigate("profile_tab") },
                onNavigateToCustomerService = { navController.navigate("customer_service") },
                onNavigateToAiQuiz = { navController.navigate("ai_quiz") },
                onEditProfileClick = { navController.navigate("edit_profile") },
                onSettingsClick = { navController.navigate("settings") },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onUserClick = { userId -> navController.navigate("user_profile/$userId") }
            )
        }

        composable("explore") {
            ExploreScreen(
                onUserClick = { userId -> navController.navigate("user_profile/$userId") },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToMatches = { navController.navigate("matches") },
                onNavigateToChat = { navController.navigate("chat_list") },
                onNavigateToProfile = { navController.navigate("profile_tab") }
            )
        }

        composable("matches") {
            MatchesScreen(
                onUserClick = { userId -> navController.navigate("user_profile/$userId") },
                onChatClick = { userId -> navController.navigate("chat/$userId") },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToExplore = { navController.navigate("explore") },
                onNavigateToChat = { navController.navigate("chat_list") },
                onNavigateToProfile = { navController.navigate("profile_tab") },
                onReviewClick = { uid -> navController.navigate("review/$uid") }
            )
        }

        composable("chat_list") {
            ChatListScreen(
                onChatClick = { userId -> navController.navigate("chat/$userId") },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToExplore = { navController.navigate("explore") },
                onNavigateToMatches = { navController.navigate("matches") },
                onNavigateToProfile = { navController.navigate("profile_tab") }
            )
        }

        composable("profile_tab") {
            ProfileScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToExplore = { navController.navigate("explore") },
                onNavigateToMatches = { navController.navigate("matches") },
                onNavigateToChat = { navController.navigate("chat_list") },
                onEditProfileClick = { navController.navigate("edit_profile") },
                onSettingsClick = { navController.navigate("settings") },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("edit_profile") {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Details & Interaction ---
        composable("user_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserProfileScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onMatchRequestClick = { uid -> navController.navigate("match_request/$uid") },
                onChatClick = { uid -> navController.navigate("chat/$uid") }
            )
        }

        composable("match_request/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MatchRequestScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onSendRequestClick = {
                    navController.popBackStack("explore", inclusive = false)
                }
            )
        }

        composable("chat/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ChatScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onScheduleClick = { uid -> navController.navigate("schedule/$uid") },
                onCustomerServiceClick = { navController.navigate("customer_service") },
                onVoiceCallClick = {},
                onVideoCallClick = {}
            )
        }

        composable("schedule/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ScheduleScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onCustomerServiceClick = { navController.navigate("customer_service") },
                onAiQuizClick = { navController.navigate("ai_quiz") },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("customer_service") {
            CustomerServiceScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("ai_quiz") {
            AiQuizScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("review/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ReviewScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }
        }

        // 全局通话浮层（来电 / 呼出 / 通话中）
        CallOverlay()

        // 语聊房浮窗（圆形按钮 + 房间 + 邀请）
        RoomOverlay()
    }
}
