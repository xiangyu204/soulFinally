package com.example.soul_android.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soul_android.data.LanguageState
import com.example.soul_android.data.PendingSignUp
import com.example.soul_android.data.ServerConfig
import com.example.soul_android.data.api.RegisterRequest
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.ui.components.*
import com.example.soul_android.ui.theme.SoulColors
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private val COUNTRIES = listOf(
    "대한민국",
    "中国",
    "日本",
    "United States",
    "United Kingdom",
    "Canada",
    "Australia",
    "New Zealand",
    "Deutschland",
    "France",
    "España",
    "Italia",
    "Nederland",
    "België",
    "Schweiz",
    "Österreich",
    "Sverige",
    "Norge",
    "Danmark",
    "Suomi",
    "Polska",
    "Česko",
    "Slovensko",
    "Magyarország",
    "România",
    "България",
    "Ελλάδα",
    "Türkiye",
    "Россия",
    "Україна",
    "Қазақстан",
    "ישראל",
    "العربية السعودية",
    "الإمارات العربية المتحدة",
    "مصر",
    "India",
    "Pakistan",
    "Bangladesh",
    "Việt Nam",
    "ไทย",
    "Indonesia",
    "Malaysia",
    "Singapore",
    "Philippines",
    "Монгол",
    "Brasil",
    "México",
    "Argentina",
    "Chile",
    "Colombia",
    "Perú",
    "South Africa",
    "Nigeria",
    "Kenya",
    "Maroc",
    "Ireland",
    "Portugal"
)

private data class SetupStrings(
    val title: String,
    val subtitle: String,
    val photoHint: String,
    val uploading: String,
    val age: String,
    val gender: String,
    val phone: String,
    val nationality: String,
    val address: String,
    val studyTime: String,
    val optional: String,
    val select: String,
    val submit: String,
    val submitting: String,
    val errRequired: String,
    val errAge: String,
    val errSession: String,
    val errUpload: String,
    val errNetwork: String,
    val errRead: String,
    val genders: List<String>,
    val timeSlots: List<String>
)

private fun stringsFor(lang: AppLanguage): SetupStrings = when (lang) {
    AppLanguage.KOREAN -> SetupStrings(
        title = "프로필 설정",
        subtitle = "정보를 완성하면 가입이 끝납니다",
        photoHint = "사진을 눌러 변경",
        uploading = "업로드 중...",
        age = "나이",
        gender = "성별",
        phone = "전화번호",
        nationality = "국적",
        address = "주소",
        studyTime = "학습 시간대",
        optional = "선택",
        select = "선택하세요",
        submit = "가입 완료",
        submitting = "가입 중...",
        errRequired = "필수 항목을 입력하세요: 나이, 성별, 전화번호, 국적",
        errAge = "나이는 10-100 사이여야 합니다",
        errSession = "가입 정보가 만료되었습니다. 다시 가입해 주세요",
        errUpload = "사진 업로드에 실패했습니다",
        errNetwork = "네트워크 오류입니다. 잠시 후 다시 시도하세요",
        errRead = "이미지를 읽을 수 없습니다",
        genders = listOf("남성", "여성", "기타"),
        timeSlots = listOf("09:00 - 12:00", "12:00 - 18:00", "18:00 - 22:00", "주말")
    )

    AppLanguage.ENGLISH -> SetupStrings(
        title = "Profile setup",
        subtitle = "Finish this to complete sign-up",
        photoHint = "Tap to change photo",
        uploading = "Uploading...",
        age = "Age",
        gender = "Gender",
        phone = "Phone",
        nationality = "Nationality",
        address = "Address",
        studyTime = "Study time",
        optional = "optional",
        select = "Select",
        submit = "Complete sign-up",
        submitting = "Signing up...",
        errRequired = "Please fill in: age, gender, phone, nationality",
        errAge = "Age must be between 10 and 100",
        errSession = "Sign-up session expired. Please register again",
        errUpload = "Photo upload failed",
        errNetwork = "Network error. Please try again",
        errRead = "Could not read the image",
        genders = listOf("Male", "Female", "Other"),
        timeSlots = listOf("09:00 - 12:00", "12:00 - 18:00", "18:00 - 22:00", "Weekend")
    )

    AppLanguage.CHINESE -> SetupStrings(
        title = "个人资料设置",
        subtitle = "填写完成后即注册成功",
        photoHint = "点击更换头像",
        uploading = "上传中...",
        age = "年龄",
        gender = "性别",
        phone = "电话",
        nationality = "国籍",
        address = "地址",
        studyTime = "学习时段",
        optional = "选填",
        select = "请选择",
        submit = "完成注册",
        submitting = "注册中...",
        errRequired = "请填写必填项：年龄、性别、电话、国籍",
        errAge = "年龄需在 10-100 之间",
        errSession = "注册信息已失效，请重新注册",
        errUpload = "头像上传失败",
        errNetwork = "网络错误，请稍后重试",
        errRead = "读取图片失败",
        genders = listOf("男", "女", "其他"),
        timeSlots = listOf("09:00 - 12:00", "12:00 - 18:00", "18:00 - 22:00", "周末")
    )
}

@Composable
fun ProfileSetupScreen(
    onNextClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { SoulApiService.create() }

    val language by LanguageState.language
    val S = stringsFor(language)
    var languageMenuExpanded by remember { mutableStateOf(false) }

    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("") }

    var avatarUrl by remember { mutableStateOf("") }
    var uploading by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            error = null
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    error = S.errRead
                } else {
                    val part = MultipartBody.Part.createFormData(
                        "file",
                        "avatar.jpg",
                        bytes.toRequestBody("image/*".toMediaType())
                    )
                    val res = api.uploadFile(part)
                    val url: String = res.body()?.url ?: ""
                    if (res.isSuccessful && url.isNotBlank()) {
                        avatarUrl = url
                    } else {
                        error = S.errUpload
                    }
                }
            } catch (e: Exception) {
                error = S.errUpload
            } finally {
                uploading = false
            }
        }
    }

    val previewModel = when {
        avatarUrl.isBlank() -> null
        avatarUrl.startsWith("http") -> avatarUrl
        else -> ServerConfig.baseUrl.trimEnd('/') + avatarUrl
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                LanguageSelector(
                    currentLanguage = language,
                    expanded = languageMenuExpanded,
                    onExpandedChange = { languageMenuExpanded = it },
                    onLanguageSelected = { LanguageState.setLanguage(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            BrandingSection(title = "SOUL", subtitle = S.subtitle, titleSize = 26)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = S.title,
                color = SoulColors.textPrimary,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(22.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (previewModel == null) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = SoulColors.textSecondary
                        )
                    } else {
                        AsyncImage(
                            model = previewModel,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (uploading) S.uploading else S.photoHint,
                color = SoulColors.textSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            GlassPanel {
                LabeledInput("${S.age} *") {
                    SoulTextField(
                        value = age,
                        onValueChange = { v -> age = v.filter { it.isDigit() }.take(3) },
                        placeholder = "22"
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                SelectField(
                    label = "${S.gender} *",
                    value = gender,
                    placeholder = S.select,
                    options = S.genders,
                    onSelect = { gender = it }
                )
                Spacer(modifier = Modifier.height(14.dp))

                LabeledInput("${S.phone} *") {
                    SoulTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "010-0000-0000"
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                SelectField(
                    label = "${S.nationality} *",
                    value = nationality,
                    placeholder = S.select,
                    options = COUNTRIES,
                    onSelect = { nationality = it }
                )
                Spacer(modifier = Modifier.height(14.dp))

                LabeledInput("${S.address} (${S.optional})") {
                    SoulTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = S.address
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                SelectField(
                    label = "${S.studyTime} (${S.optional})",
                    value = timeSlot,
                    placeholder = S.select,
                    options = S.timeSlots,
                    onSelect = { timeSlot = it }
                )

                Spacer(modifier = Modifier.height(26.dp))

                SoulButton(
                    text = if (submitting) S.submitting else S.submit,
                    onClick = {
                        if (submitting) return@SoulButton
                        val ageNum = age.toIntOrNull()
                        when {
                            age.isBlank() || gender.isBlank() || phone.isBlank() || nationality.isBlank() -> {
                                error = S.errRequired
                            }
                            ageNum == null || ageNum < 10 || ageNum > 100 -> {
                                error = S.errAge
                            }
                            !PendingSignUp.isReady() -> {
                                error = S.errSession
                            }
                            else -> {
                                error = null
                                submitting = true
                                scope.launch {
                                    try {
                                        val resp = api.register(
                                            RegisterRequest(
                                                username = PendingSignUp.username,
                                                name = PendingSignUp.username,
                                                email = PendingSignUp.email,
                                                password = PendingSignUp.password,
                                                age = ageNum,
                                                gender = gender,
                                                phone = phone.trim(),
                                                nationality = nationality,
                                                address = address.trim(),
                                                timeSlot = timeSlot,
                                                avatar = avatarUrl
                                            )
                                        )
                                        if (resp.isSuccessful && resp.body()?.success == true) {
                                            val prefs = context.getSharedPreferences(
                                                "soul_login_prefs",
                                                Context.MODE_PRIVATE
                                            )
                                            prefs.edit()
                                                .putString("username", PendingSignUp.username)
                                                .putBoolean("is_logged_in", true)
                                                .apply()
                                            PendingSignUp.clear()
                                            onNextClick()
                                        } else {
                                            error = resp.body()?.message ?: S.errNetwork
                                        }
                                    } catch (e: Exception) {
                                        error = S.errNetwork
                                    } finally {
                                        submitting = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error ?: "",
                        color = Color(0xFFFF5A5F),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LabeledInput(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = SoulColors.textSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun SelectField(
    label: String,
    value: String,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = SoulColors.textSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Box {
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                color = Color.White.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                onClick = { expanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value.ifBlank { placeholder },
                        color = if (value.isBlank()) SoulColors.textSecondary else SoulColors.textPrimary,
                        fontSize = 14.sp
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = SoulColors.textSecondary
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 320.dp)
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            onSelect(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
