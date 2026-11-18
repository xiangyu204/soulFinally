package com.example.soul_android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soul_android.data.LanguageState
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.ui.components.SoulIconTile
import com.example.soul_android.ui.components.UserAvatar
import com.example.soul_android.ui.theme.SoulColors

/*
 * 主页右侧滑出的资料抽屉。
 * 取代底部「我的」标签：头像 / 姓名 / 用户名 + 快捷操作。
 */
@Composable
fun ProfileDrawerOverlay(
    open: Boolean,
    onClose: () -> Unit,
    avatar: String?,
    name: String,
    username: String,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onCustomerService: () -> Unit,
    onAiQuiz: () -> Unit,
    onLogout: () -> Unit
) {
    val language by LanguageState.language

    val labels = when (language) {
        AppLanguage.CHINESE -> DrawerStrings(
            edit = "编辑资料",
            settings = "设置",
            customer = "客服中心",
            aiQuiz = "AI 测评",
            logout = "退出登录"
        )
        AppLanguage.ENGLISH -> DrawerStrings(
            edit = "Edit Profile",
            settings = "Settings",
            customer = "Customer Service",
            aiQuiz = "AI Quiz",
            logout = "Log Out"
        )
        AppLanguage.KOREAN -> DrawerStrings(
            edit = "프로필 편집",
            settings = "설정",
            customer = "고객센터",
            aiQuiz = "AI 퀴즈",
            logout = "로그아웃"
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (open) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { onClose() }
            )
        }

        AnimatedVisibility(
            visible = open,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(296.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
                color = SoulColors.card
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(avatar = avatar, name = name, size = 72.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = name,
                                color = SoulColors.textPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@$username",
                                color = SoulColors.textMuted,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    DrawerActionRow(Icons.Default.Edit, labels.edit) { onEditProfile() }
                    DrawerActionRow(Icons.Default.Settings, labels.settings) { onSettings() }
                    DrawerActionRow(Icons.Default.Headset, labels.customer) { onCustomerService() }
                    DrawerActionRow(Icons.Default.Quiz, labels.aiQuiz) { onAiQuiz() }

                    Spacer(modifier = Modifier.weight(1f))

                    DrawerActionRow(
                        icon = Icons.Default.Logout,
                        label = labels.logout,
                        tint = Color(0xFFFF5A5F)
                    ) { onLogout() }
                }
            }
        }
    }
}

@Composable
private fun DrawerActionRow(
    icon: ImageVector,
    label: String,
    tint: Color = SoulColors.accent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SoulIconTile(icon = icon, size = 40.dp, tint = tint)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            color = SoulColors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = SoulColors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

private data class DrawerStrings(
    val edit: String,
    val settings: String,
    val customer: String,
    val aiQuiz: String,
    val logout: String
)
