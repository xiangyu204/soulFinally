package com.example.soul_android.ui.screens
import com.example.soul_android.ui.theme.SoulColors

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soul_android.data.LanguageState
import com.example.soul_android.data.PendingSignUp
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.ui.components.*
import kotlinx.coroutines.launch

private val EMAIL_DOMAINS = listOf(
    "gmail.com", "naver.com", "daum.net", "hanmail.net", "nate.com",
    "outlook.com", "hotmail.com", "qq.com", "163.com", "126.com",
    "icloud.com", "yahoo.com"
)

private fun isValidEmail(email: String): Boolean =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)

private data class SignUpStrings(
    val title: String,
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val signUpBtn: String,
    val loginLink: String
)

private fun usernameRule(lang: AppLanguage): String = when (lang) {
    AppLanguage.KOREAN -> "사용자 이름은 3-20자의 영문, 숫자 또는 밑줄이어야 합니다"
    AppLanguage.ENGLISH -> "Username must be 3-20 letters, numbers or underscores"
    AppLanguage.CHINESE -> "用户名需 3-20 位字母、数字或下划线"
}

private fun networkErrorText(lang: AppLanguage): String = when (lang) {
    AppLanguage.KOREAN -> "네트워크 오류가 발생했습니다"
    AppLanguage.ENGLISH -> "Network error. Please check your connection"
    AppLanguage.CHINESE -> "网络错误，请检查网络连接"
}

private fun checkingText(lang: AppLanguage): String = when (lang) {
    AppLanguage.KOREAN -> "SOUL ID 확인 중..."
    AppLanguage.ENGLISH -> "Checking your soul ID..."
    AppLanguage.CHINESE -> "正在校验用户名..."
}

@Composable
fun SignUpScreen(
    onBackToLogin: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var emailLocal by remember { mutableStateOf("") }
    var emailDomain by remember { mutableStateOf(EMAIL_DOMAINS.first()) }
    var domainExpanded by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val api = remember { SoulApiService.create() }

    val language by LanguageState.language
    var languageMenuExpanded by remember { mutableStateOf(false) }

    val strings = when (language) {
        AppLanguage.KOREAN -> SignUpStrings("회원가입", "사용자 이름", "이메일", "비밀번호", "비밀번호 확인", "회원가입", "이미 계정이 있으신가요? 로그인")
        AppLanguage.ENGLISH -> SignUpStrings("Sign Up", "Username", "Email", "Password", "Confirm Password", "Sign Up", "Already have an account? Log In")
        AppLanguage.CHINESE -> SignUpStrings("注册", "用户名", "邮箱", "密码", "确认密码", "注册", "已经有账号？登录")
    }

    val email = if (emailLocal.isBlank()) "" else "$emailLocal@$emailDomain"

    val submit: () -> Unit = {
        val u = username.trim()
        when {
            u.isBlank() || emailLocal.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                registerError = when (language) {
                    AppLanguage.CHINESE -> "请填写完整信息"
                    AppLanguage.ENGLISH -> "Please fill in all fields"
                    AppLanguage.KOREAN -> "모든 항목을 입력하세요"
                }
            }
            !Regex("^[A-Za-z0-9_]{3,20}$").matches(u) -> {
                registerError = usernameRule(language)
            }
            password.length < 6 -> {
                registerError = when (language) {
                    AppLanguage.CHINESE -> "密码至少 6 位"
                    AppLanguage.ENGLISH -> "Password must be at least 6 characters"
                    AppLanguage.KOREAN -> "비밀번호는 6자 이상이어야 합니다"
                }
            }
            password != confirmPassword -> {
                registerError = when (language) {
                    AppLanguage.CHINESE -> "两次密码不一致"
                    AppLanguage.ENGLISH -> "Passwords do not match"
                    AppLanguage.KOREAN -> "비밀번호가 일치하지 않습니다"
                }
            }
            !isValidEmail(email) -> {
                registerError = when (language) {
                    AppLanguage.CHINESE -> "邮箱格式不正确"
                    AppLanguage.ENGLISH -> "Invalid email format"
                    AppLanguage.KOREAN -> "잘못된 이메일 형식"
                }
            }
            else -> {
                registerError = null
                isLoading = true
                scope.launch {
                    try {
                        val resp = api.checkUsername(u)
                        val body = resp.body()
                        if (resp.isSuccessful && body?.available == true) {
                            PendingSignUp.username = u
                            PendingSignUp.email = email
                            PendingSignUp.password = password
                            isLoading = false
                            onSignUpSuccess()
                        } else {
                            registerError = body?.message ?: "用户名不可用"
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        registerError = networkErrorText(language)
                        isLoading = false
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HazeBackground { BackgroundGalaxy() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                LanguageSelector(
                    currentLanguage = language,
                    expanded = languageMenuExpanded,
                    onExpandedChange = { languageMenuExpanded = it },
                    onLanguageSelected = { LanguageState.setLanguage(it) }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            BrandingSection(title = "SOUL", subtitle = "Create your account")
            Spacer(modifier = Modifier.height(36.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp)),
                color = Color.White.copy(alpha = 0.07f)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    SoulTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = strings.username
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Box {
                        SoulTextField(
                            value = emailLocal,
                            onValueChange = { emailLocal = it },
                            placeholder = strings.email,
                            trailingIcon = {
                                Box {
                                    TextButton(onClick = { domainExpanded = true }) {
                                        Text(
                                            text = "@$emailDomain",
                                            color = SoulColors.textSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = domainExpanded,
                                        onDismissRequest = { domainExpanded = false }
                                    ) {
                                        EMAIL_DOMAINS.forEach { d ->
                                            DropdownMenuItem(
                                                text = { Text(d) },
                                                onClick = {
                                                    emailDomain = d
                                                    domainExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = email,
                        color = SoulColors.textSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SoulTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = strings.password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    SoulTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = strings.confirmPassword,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible }
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    SoulButton(
                        text = strings.signUpBtn,
                        onClick = submit,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (registerError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = registerError ?: "",
                            color = Color(0xFFFF5A5F),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            TextButton(onClick = onBackToLogin) {
                Text(
                    text = strings.loginLink,
                    color = SoulColors.textPrimary,
                    style = TextStyle(letterSpacing = 1.sp, fontWeight = FontWeight.Light)
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (isLoading) {
            PlanetLoadingOverlay(checkingText(language))
        }
    }
}
