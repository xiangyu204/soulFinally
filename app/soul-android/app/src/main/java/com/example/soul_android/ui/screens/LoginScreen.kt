package com.example.soul_android.ui.screens
import com.example.soul_android.ui.theme.SoulColors

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soul_android.R
import com.example.soul_android.data.LanguageState
import com.example.soul_android.data.ServerConfig
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.ui.components.*
import com.example.soul_android.ui.viewmodels.AuthViewModel
import com.example.soul_android.ui.viewmodels.LoginResult

private data class LoginStrings(
    val login: String,
    val email: String,
    val password: String,
    val forgot: String,
    val signUp: String,
    val orLoginWith: String,
    val rememberMe: String
)

@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onDemoClick: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // 登录信息统一保存在 soul_login_prefs
    val prefs = remember {
        context.getSharedPreferences(
            "soul_login_prefs",
            Context.MODE_PRIVATE
        )
    }

    var email by remember {
        mutableStateOf(
            prefs.getString("saved_email", "") ?: ""
        )
    }

    var password by remember {
        mutableStateOf(
            prefs.getString("saved_password", "") ?: ""
        )
    }

    var rememberMe by remember {
        mutableStateOf(
            prefs.getBoolean("remember_me", false)
        )
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var showServerDialog by remember {
        mutableStateOf(false)
    }

    val language by LanguageState.language

    var languageMenuExpanded by remember {
        mutableStateOf(false)
    }

    val loginResult by viewModel.loginResult.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val strings = when (language) {
        AppLanguage.KOREAN -> LoginStrings(
            "로그인",
            "이메일 또는 사용자 이름",
            "비밀번호",
            "비밀번호를 잊으셨나요?",
            "계정이 없으신가요? 회원가입",
            "또는 다음으로 로그인",
            "아이디/비밀번호 기억"
        )

        AppLanguage.ENGLISH -> LoginStrings(
            "Login",
            "Email or Username",
            "Password",
            "Forgot your password?",
            "Don't have an account? Sign Up",
            "Or login with",
            "Remember me"
        )

        AppLanguage.CHINESE -> LoginStrings(
            "登录",
            "邮箱或用户名",
            "密码",
            "忘记密码？",
            "还没有账号？注册",
            "或其他方式登录",
            "记住账号密码"
        )
    }

    LaunchedEffect(loginResult) {

        when (val result = loginResult) {

            is LoginResult.Success -> {

                // 保存当前登录用户
                prefs.edit()
                    .putString(
                        "username",
                        result.username
                    )
                    .putBoolean(
                        "is_logged_in",
                        true
                    )
                    .apply()

                // 是否记住账号密码
                if (rememberMe) {

                    prefs.edit()
                        .putString(
                            "saved_email",
                            email
                        )
                        .putString(
                            "saved_password",
                            password
                        )
                        .putBoolean(
                            "remember_me",
                            true
                        )
                        .apply()

                } else {

                    // 不要 clear()
                    // 否则 username 和 is_logged_in 也会被删掉
                    prefs.edit()
                        .remove("saved_email")
                        .remove("saved_password")
                        .putBoolean(
                            "remember_me",
                            false
                        )
                        .apply()
                }

                onLoginSuccess()
            }

            is LoginResult.Error -> {

                snackbarHostState.showSnackbar(
                    result.message
                )
            }

            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        HazeBackground { BackgroundGalaxy() }

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {

                    LanguageSelector(
                        currentLanguage = language,
                        expanded = languageMenuExpanded,
                        onExpandedChange = {
                            languageMenuExpanded = it
                        },
                        onLanguageSelected = {
                            LanguageState.setLanguage(it)
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.weight(0.3f)
                )

                BrandingSection()

                Spacer(
                    modifier = Modifier.weight(0.5f)
                )

                GlassLoginPanel(
                    email = email,
                    password = password,
                    passwordVisible = passwordVisible,
                    rememberMe = rememberMe,

                    onEmailChange = {
                        email = it
                    },

                    onPasswordChange = {
                        password = it
                    },

                    onTogglePassword = {
                        passwordVisible = !passwordVisible
                    },

                    onRememberMeChange = {
                        rememberMe = it
                    },

                    strings = strings,

                    onLoginClick = {

                        if (
                            email.isNotBlank()
                            &&
                            password.isNotBlank()
                        ) {

                            viewModel.login(
                                email,
                                password
                            )
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                SocialLoginSection(
                    strings
                )

                Spacer(
                    modifier = Modifier.weight(0.2f)
                )

                TextButton(
                    onClick = onSignUpClick
                ) {

                    Text(
                        text = strings.signUp,
                        color = Color.White.copy(
                            alpha = 0.8f
                        ),
                        style = TextStyle(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Light
                        )
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }
        }

        if (showServerDialog) {
            ServerSettingsDialog(
                context = context,
                onDismiss = { showServerDialog = false }
            )
        }

        if (
            loginResult is LoginResult.Loading
        ) {

            PlanetLoadingOverlay()
        }
    }
}


@Composable
private fun GlassLoginPanel(
    email: String,
    password: String,
    passwordVisible: Boolean,
    rememberMe: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    strings: LoginStrings,
    onLoginClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(32.dp)
            )
            .border(
                1.dp,
                Color.White.copy(
                    alpha = 0.1f
                ),
                RoundedCornerShape(32.dp)
            ),
        color = Color.White.copy(
            alpha = 0.07f
        )
    ) {

        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SoulTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = strings.email
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            SoulTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = strings.password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = onTogglePassword
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        onRememberMeChange(
                            !rememberMe
                        )
                    }
                ) {

                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = onRememberMeChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = SoulGreen
                        )
                    )

                    Text(
                        text = strings.rememberMe,
                        color = Color.White.copy(
                            alpha = 0.7f
                        ),
                        fontSize = 12.sp
                    )
                }

                TextButton(
                    onClick = {}
                ) {

                    Text(
                        text = strings.forgot,
                        color = Color.White.copy(
                            alpha = 0.5f
                        ),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            SoulButton(
                text = strings.login,
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
private fun SocialLoginSection(
    strings: LoginStrings
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = strings.orLoginWith,
            color = Color.White.copy(
                alpha = 0.3f
            ),
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(
                24.dp
            )
        ) {

            SocialCircleIcon(
                R.drawable.soul_logo,
                SoulColors.textPrimary
            )

            SocialCircleIcon(
                R.drawable.soul_logo,
                Color(0xFF07C160)
            )
        }
    }
}

@Composable
private fun ServerSettingsDialog(context: Context, onDismiss: () -> Unit) {
    var url by remember { mutableStateOf(ServerConfig.baseUrl) }
    var saved by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SoulColors.popupSurface,
        titleContentColor = SoulColors.textPrimary,
        textContentColor = SoulColors.textPrimary,
        title = { Text("服务器地址") },
        text = {
            Column {
                Text(
                    "演示/异地测试时，填入后端公网地址（如 https://xxx.trycloudflare.com/）",
                    fontSize = 12.sp,
                    color = SoulColors.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it; saved = false },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("http://10.0.2.2:8080/") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SoulColors.textPrimary,
                        unfocusedTextColor = SoulColors.textPrimary,
                        focusedBorderColor = Color(0xFF00D0D9),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                if (saved) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "已保存（如已登录请重启 App 生效）",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                ServerConfig.setBaseUrl(context, url)
                saved = true
            }) {
                Text("保存", color = Color(0xFF00D0D9), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    ServerConfig.reset(context)
                    url = ServerConfig.baseUrl
                    saved = true
                }) {
                    Text("恢复默认", color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp)
                }
                TextButton(onClick = onDismiss) {
                    Text("关闭", color = SoulColors.textSecondary)
                }
            }
        }
    )
}