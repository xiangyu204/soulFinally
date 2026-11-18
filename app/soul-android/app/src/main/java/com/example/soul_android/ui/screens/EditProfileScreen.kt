package com.example.soul_android.ui.screens
import com.example.soul_android.ui.components.HazeBackground
import com.example.soul_android.ui.theme.SoulColors

import android.content.Context
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soul_android.data.api.ProfileResponse
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.data.LanguageState
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.ui.components.BackgroundGalaxy
import com.example.soul_android.ui.components.EditDropdownField
import com.example.soul_android.ui.viewmodels.EditProfileUiState
import com.example.soul_android.ui.viewmodels.EditProfileViewModel


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun EditProfileScreen(

    onBackClick:
        () -> Unit = {},

    viewModel:
    EditProfileViewModel =
        viewModel()

) {

    val context =
        LocalContext.current

    val language by LanguageState.language

    val epTitle = when (language) {
        AppLanguage.KOREAN -> "정보 수정"
        AppLanguage.ENGLISH -> "Edit Profile"
        AppLanguage.CHINESE -> "编辑资料"
    }
    val epRetry = when (language) {
        AppLanguage.KOREAN -> "다시 시도"
        AppLanguage.ENGLISH -> "Retry"
        AppLanguage.CHINESE -> "重试"
    }
    val epChangePhoto = when (language) {
        AppLanguage.KOREAN -> "프로필 사진 변경"
        AppLanguage.ENGLISH -> "Change Profile Photo"
        AppLanguage.CHINESE -> "更换头像"
    }
    val epName = when (language) {
        AppLanguage.KOREAN -> "성명"
        AppLanguage.ENGLISH -> "Name"
        AppLanguage.CHINESE -> "姓名"
    }
    val epAge = when (language) {
        AppLanguage.KOREAN -> "나이"
        AppLanguage.ENGLISH -> "Age"
        AppLanguage.CHINESE -> "年龄"
    }
    val epGender = when (language) {
        AppLanguage.KOREAN -> "성별"
        AppLanguage.ENGLISH -> "Gender"
        AppLanguage.CHINESE -> "性别"
    }
    val epPhone = when (language) {
        AppLanguage.KOREAN -> "전화번호"
        AppLanguage.ENGLISH -> "Phone"
        AppLanguage.CHINESE -> "电话"
    }
    val epNationality = when (language) {
        AppLanguage.KOREAN -> "국적"
        AppLanguage.ENGLISH -> "Nationality"
        AppLanguage.CHINESE -> "国籍"
    }
    val epAddress = when (language) {
        AppLanguage.KOREAN -> "주소"
        AppLanguage.ENGLISH -> "Address"
        AppLanguage.CHINESE -> "地址"
    }
    val epTime = when (language) {
        AppLanguage.KOREAN -> "학습 시간대"
        AppLanguage.ENGLISH -> "Study Time"
        AppLanguage.CHINESE -> "学习时段"
    }
    val epSave = when (language) {
        AppLanguage.KOREAN -> "저장"
        AppLanguage.ENGLISH -> "Save"
        AppLanguage.CHINESE -> "保存"
    }
    val epSaved = when (language) {
        AppLanguage.KOREAN -> "저장되었습니다."
        AppLanguage.ENGLISH -> "Saved."
        AppLanguage.CHINESE -> "已保存。"
    }
    val epAvatarChanged = when (language) {
        AppLanguage.KOREAN -> "프로필 사진이 변경되었습니다."
        AppLanguage.ENGLISH -> "Profile photo changed."
        AppLanguage.CHINESE -> "头像已更新。"
    }
    val epReadError = when (language) {
        AppLanguage.KOREAN -> "이미지를 읽을 수 없습니다."
        AppLanguage.ENGLISH -> "Cannot read image."
        AppLanguage.CHINESE -> "无法读取图片。"
    }


    // =====================================================
    // 当前登录 username
    // =====================================================

    val username =
        remember(context) {

            val soulPrefs =
                context.getSharedPreferences(
                    "soul_login_prefs",
                    Context.MODE_PRIVATE
                )


            val userPrefs =
                context.getSharedPreferences(
                    "user_prefs",
                    Context.MODE_PRIVATE
                )


            soulPrefs
                .getString(
                    "username",
                    ""
                )
                ?.takeIf {
                    it.isNotBlank()
                }

                ?: userPrefs
                    .getString(
                        "username",
                        ""
                    )
                    ?.takeIf {
                        it.isNotBlank()
                    }

                ?: ""
        }


    val uiState by
    viewModel
        .uiState
        .collectAsState()


    // =====================================================
    // Avatar upload 状态
    // =====================================================

    var avatarUploading by
    remember {

        mutableStateOf(
            false
        )
    }


    var avatarMessage by
    remember {

        mutableStateOf(
            ""
        )
    }


    // =====================================================
    // 图片选择器
    // =====================================================

    val avatarPicker =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts
                    .GetContent()

        ) { uri ->


            if (
                uri == null
            ) {

                return@rememberLauncherForActivityResult
            }


            try {

                val resolver =
                    context
                        .contentResolver


                // =================================================
                // 读取图片数据
                // =================================================

                val fileBytes =
                    resolver
                        .openInputStream(
                            uri
                        )
                        ?.use {

                            it.readBytes()
                        }


                if (
                    fileBytes == null
                ) {

                    avatarMessage =
                        epReadError

                    return@rememberLauncherForActivityResult
                }


                // =================================================
                // MIME
                // =================================================

                val mimeType =
                    resolver.getType(
                        uri
                    )
                        ?: "image/jpeg"


                // =================================================
                // 文件名
                // =================================================

                var fileName =
                    "avatar.jpg"


                resolver.query(

                    uri,

                    null,

                    null,

                    null,

                    null

                )?.use { cursor ->


                    val nameIndex =
                        cursor
                            .getColumnIndex(
                                OpenableColumns
                                    .DISPLAY_NAME
                            )


                    if (
                        nameIndex >= 0
                        &&
                        cursor.moveToFirst()
                    ) {

                        fileName =
                            cursor
                                .getString(
                                    nameIndex
                                )
                    }
                }


                avatarUploading =
                    true


                avatarMessage =
                    ""


                // =================================================
                // 上传
                // =================================================

                viewModel.uploadAvatar(

                    username =
                        username,

                    fileBytes =
                        fileBytes,

                    mimeType =
                        mimeType,

                    fileName =
                        fileName,

                    onSuccess = {

                        avatarUploading =
                            false


                        avatarMessage =
                            epAvatarChanged
                    },

                    onError = {

                        avatarUploading =
                            false


                        avatarMessage =
                            it
                    }
                )


            } catch (
                e: Exception
            ) {

                avatarUploading =
                    false


                avatarMessage =
                    e.localizedMessage
                        ?: "头像选择失败"
            }
        }


    // =====================================================
    // 加载资料
    // =====================================================

    LaunchedEffect(
        username
    ) {

        if (
            username.isNotBlank()
        ) {

            viewModel.loadProfile(
                username
            )
        }
    }


    // =====================================================
    // UI
    // =====================================================

    Box(

        modifier =
            Modifier
                .fillMaxSize()

    ) {


        HazeBackground { BackgroundGalaxy() }


        Scaffold(

            containerColor =
                Color.Transparent,


            topBar = {

                TopAppBar(

                    colors =
                        TopAppBarDefaults
                            .topAppBarColors(

                                containerColor =
                                    Color.Transparent
                            ),


                    navigationIcon = {

                        IconButton(

                            onClick =
                                onBackClick

                        ) {

                            Icon(

                                imageVector =
                                    Icons
                                        .AutoMirrored
                                        .Filled
                                        .ArrowBack,

                                contentDescription =
                                    "Back",

                                tint =
                                    SoulColors.textPrimary
                            )
                        }
                    },


                    title = {

                        Text(

                            text =
                                epTitle,

                            color =
                                Color(
                                    0xFF00C8D7
                                ),

                            fontWeight =
                                FontWeight.Bold,

                            fontSize =
                                21.sp
                        )
                    }
                )
            }

        ) { padding ->


            when (
                val state =
                    uiState
            ) {


                // =================================================
                // Loading
                // =================================================

                EditProfileUiState.Loading -> {

                    Box(

                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(
                                    padding
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator(

                            color =
                                Color(
                                    0xFF00C8D7
                                )
                        )
                    }
                }


                // =================================================
                // Error
                // =================================================

                is EditProfileUiState.Error -> {

                    Box(

                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(
                                    padding
                                )
                                .padding(
                                    30.dp
                                ),

                        contentAlignment =
                            Alignment.Center

                    ) {


                        Column(

                            horizontalAlignment =
                                Alignment.CenterHorizontally

                        ) {


                            Text(

                                text =
                                    state.message,

                                color =
                                    SoulColors.textPrimary
                            )


                            Spacer(

                                modifier =
                                    Modifier
                                        .height(
                                            16.dp
                                        )
                            )


                            Button(

                                onClick = {

                                    viewModel
                                        .loadProfile(
                                            username
                                        )
                                }

                            ) {

                                Text(
                                    epRetry
                                )
                            }
                        }
                    }
                }


                // =================================================
                // Success
                // =================================================

                is EditProfileUiState.Success -> {

                    EditProfileForm(

                        modifier =
                            Modifier.padding(
                                padding
                            ),

                        originalProfile =
                            state.profile,

                        avatarUploading =
                            avatarUploading,

                        avatarMessage =
                            avatarMessage,

                        onAvatarClick = {

                            avatarPicker
                                .launch(
                                    "image/*"
                                )
                        },

                        onSave = {
                                profile,
                                success,
                                error ->


                            viewModel
                                .saveProfile(

                                    profile =
                                        profile,

                                    onSuccess =
                                        success,

                                    onError =
                                        error
                                )
                        }
                    )
                }
            }
        }
    }
}


// =========================================================
// Form
// =========================================================

@Composable
private fun EditProfileForm(

    modifier:
    Modifier =
        Modifier,

    originalProfile:
    ProfileResponse,

    avatarUploading:
    Boolean,

    avatarMessage:
    String,

    onAvatarClick:
        () -> Unit,

    onSave:
        (
        ProfileResponse,
        () -> Unit,
        (String) -> Unit
    ) -> Unit

) {

    val language by LanguageState.language

    val epChangePhoto = when (language) {
        AppLanguage.KOREAN -> "프로필 사진 변경"
        AppLanguage.ENGLISH -> "Change Profile Photo"
        AppLanguage.CHINESE -> "更换头像"
    }
    val epAvatarChanged = when (language) {
        AppLanguage.KOREAN -> "프로필 사진이 변경되었습니다."
        AppLanguage.ENGLISH -> "Profile photo changed."
        AppLanguage.CHINESE -> "头像已更新。"
    }
    val epName = when (language) {
        AppLanguage.KOREAN -> "성명"
        AppLanguage.ENGLISH -> "Name"
        AppLanguage.CHINESE -> "姓名"
    }
    val epAge = when (language) {
        AppLanguage.KOREAN -> "나이"
        AppLanguage.ENGLISH -> "Age"
        AppLanguage.CHINESE -> "年龄"
    }
    val epGender = when (language) {
        AppLanguage.KOREAN -> "성별"
        AppLanguage.ENGLISH -> "Gender"
        AppLanguage.CHINESE -> "性别"
    }
    val epPhone = when (language) {
        AppLanguage.KOREAN -> "전화번호"
        AppLanguage.ENGLISH -> "Phone"
        AppLanguage.CHINESE -> "电话"
    }
    val epNationality = when (language) {
        AppLanguage.KOREAN -> "국적"
        AppLanguage.ENGLISH -> "Nationality"
        AppLanguage.CHINESE -> "国籍"
    }
    val epAddress = when (language) {
        AppLanguage.KOREAN -> "주소"
        AppLanguage.ENGLISH -> "Address"
        AppLanguage.CHINESE -> "地址"
    }
    val epTime = when (language) {
        AppLanguage.KOREAN -> "학습 시간대"
        AppLanguage.ENGLISH -> "Study Time"
        AppLanguage.CHINESE -> "学习时段"
    }
    val epSave = when (language) {
        AppLanguage.KOREAN -> "저장"
        AppLanguage.ENGLISH -> "Save"
        AppLanguage.CHINESE -> "保存"
    }
    val epSaved = when (language) {
        AppLanguage.KOREAN -> "저장되었습니다."
        AppLanguage.ENGLISH -> "Saved."
        AppLanguage.CHINESE -> "已保存。"
    }


    // =====================================================
    // 输入数据
    // =====================================================

    var name by
    remember(
        originalProfile
    ) {

        mutableStateOf(

            originalProfile
                .name
                ?: ""
        )
    }


    var age by
    remember(
        originalProfile
    ) {

        mutableStateOf(

            originalProfile
                .age
                ?.toString()
                ?: ""
        )
    }


    var gender by
    remember(
        originalProfile
    ) {

        mutableStateOf(

            originalProfile
                .gender
                ?: ""
        )
    }


    var phone by
    remember(
        originalProfile
    ) {

        mutableStateOf(

            originalProfile
                .phone
                ?: ""
        )
    }


    var nationality by
    remember(
        originalProfile
    ) {

        mutableStateOf(

            originalProfile
                .nationality
                ?: ""
        )
    }


    var address by
    remember(
        originalProfile
    ) {

        mutableStateOf(

            originalProfile
                .address
                ?: ""
        )
    }


    var timeSlot by
    remember(
        originalProfile
    ) {

        mutableStateOf(

            originalProfile
                .timeSlot
                ?: ""
        )
    }


    var saving by
    remember {

        mutableStateOf(
            false
        )
    }


    var message by
    remember {

        mutableStateOf(
            ""
        )
    }


    // =====================================================
    // Avatar URL
    // =====================================================

    val avatarUrl =
        remember(
            originalProfile.avatar
        ) {

            buildAvatarUrl(
                originalProfile.avatar
            )
        }


    // =====================================================
    // 内容
    // =====================================================

    Column(

        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal =
                        24.dp
                )
                .padding(
                    bottom =
                        40.dp
                )

    ) {


        Spacer(

            modifier =
                Modifier.height(
                    16.dp
                )
        )


        // =================================================
        // Avatar
        // =================================================

        Column(

            modifier =
                Modifier
                    .fillMaxWidth(),

            horizontalAlignment =
                Alignment.CenterHorizontally

        ) {


            Box(

                modifier =
                    Modifier
                        .size(
                            110.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            Color.White.copy(
                                alpha =
                                    0.08f
                            )
                        )
                        .border(
                            width =
                                2.dp,
                            color =
                                Color(
                                    0xFF00C8D7
                                ),
                            shape =
                                CircleShape
                        )
                        .clickable(
                            enabled =
                                !avatarUploading
                        ) {

                            onAvatarClick()
                        },

                contentAlignment =
                    Alignment.Center

            ) {


                if (
                    avatarUrl != null
                ) {

                    AsyncImage(

                        model =
                            avatarUrl,

                        contentDescription =
                            "Avatar",

                        modifier =
                            Modifier
                                .fillMaxSize(),

                        contentScale =
                            ContentScale.Crop
                    )


                } else {


                    Icon(

                        imageVector =
                            Icons.Default.Person,

                        contentDescription =
                            null,

                        modifier =
                            Modifier
                                .size(
                                    55.dp
                                ),

                        tint =
                            SoulColors.textPrimary
                    )
                }


                // Camera Icon
                Box(

                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .size(
                                34.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                Color(
                                    0xFF00C8D7
                                )
                            ),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.CameraAlt,

                        contentDescription =
                            "Change Avatar",

                        tint =
                            SoulColors.textPrimary,

                        modifier =
                            Modifier
                                .size(
                                    18.dp
                                )
                    )
                }


                if (
                    avatarUploading
                ) {

                    Box(

                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Black.copy(
                                        alpha =
                                            0.45f
                                    )
                                ),

                        contentAlignment =
                            Alignment.Center

                    ) {

                        CircularProgressIndicator(

                            modifier =
                                Modifier
                                    .size(
                                        32.dp
                                    ),

                            strokeWidth =
                                3.dp,

                            color =
                                Color(
                                    0xFF00C8D7
                                )
                        )
                    }
                }
            }


            Spacer(

                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            TextButton(

                enabled =
                    !avatarUploading,

                onClick =
                    onAvatarClick

            ) {

                Text(

                    text =
                        epChangePhoto,

                    color =
                        Color(
                            0xFF00C8D7
                        ),

                    fontWeight =
                        FontWeight.Bold
                )
            }


            if (
                avatarMessage
                    .isNotBlank()
            ) {

                Text(

                    text =
                        avatarMessage,

                    color =
                        if (
                            avatarMessage ==
                                epAvatarChanged
                        ) {

                            Color(
                                0xFF00C8D7
                            )

                        } else {

                            Color(
                                0xFFFF6B6B
                            )
                        },

                    fontSize =
                        12.sp
                )
            }


            Spacer(

                modifier =
                    Modifier.height(
                        14.dp
                    )
            )
        }


        // =================================================
        // 姓名
        // =================================================

        EditField(

            title =
                epName,

            value =
                name,

            onValueChange = {

                name = it
            }
        )


        // =================================================
        // 年龄 + 性别
        // =================================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    14.dp
                )

        ) {


            Box(

                modifier =
                    Modifier
                        .weight(
                            1f
                        )

            ) {


                EditField(

                    title =
                        epAge,

                    value =
                        age,

                    onValueChange = {

                        if (
                            it.all { char ->
                                char.isDigit()
                            }
                        ) {

                            age = it
                        }
                    }
                )
            }


            Box(

                modifier =
                    Modifier
                        .weight(
                            1f
                        )

            ) {


                EditDropdownField(
                    title = epGender,
                    value = gender,
                    options = when (language) {
                        AppLanguage.CHINESE -> listOf("男性", "女性", "其他")
                        AppLanguage.ENGLISH -> listOf("Male", "Female", "Other")
                        AppLanguage.KOREAN -> listOf("남성", "여성", "기타")
                    },
                    onSelect = { gender = it }
                )
            }
        }


        // =================================================
        // 电话
        // =================================================

        EditField(

            title =
                epPhone,

            value =
                phone,

            onValueChange = {

                phone = it
            }
        )


        // =================================================
        // 国籍
        // =================================================

        EditField(

            title =
                epNationality,

            value =
                nationality,

            onValueChange = {

                nationality = it
            }
        )


        // =================================================
        // 地址
        // =================================================

        EditField(

            title =
                epAddress,

            value =
                address,

            onValueChange = {

                address = it
            }
        )


        // =================================================
        // 学习时间段
        // =================================================

        EditField(

            title =
                epTime,

            value =
                timeSlot,

            onValueChange = {

                timeSlot = it
            }
        )


        Spacer(

            modifier =
                Modifier.height(
                    20.dp
                )
        )


        // =================================================
        // Save
        // =================================================

        Button(

            enabled =
                !saving
                        &&
                        !avatarUploading,

            onClick = {


                saving =
                    true


                message =
                    ""


                val updatedProfile =
                    originalProfile.copy(

                        name =
                            name
                                .trim(),

                        age =
                            age
                                .toIntOrNull(),

                        gender =
                            gender
                                .trim(),

                        phone =
                            phone
                                .trim(),

                        nationality =
                            nationality
                                .trim(),

                        address =
                            address
                                .trim(),

                        timeSlot =
                            timeSlot
                                .trim()
                    )


                onSave(

                    updatedProfile,


                    // Success
                    {

                        saving =
                            false


                        message =
                            epSaved
                    },


                    // Error
                    { error ->

                        saving =
                            false


                        message =
                            error
                    }
                )
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        56.dp
                    ),

            shape =
                RoundedCornerShape(
                    30.dp
                ),

            colors =
                ButtonDefaults
                    .buttonColors(

                        containerColor =
                            Color(
                                0xFF06C5D4
                            ),

                        contentColor =
                            SoulColors.textPrimary,

                        disabledContainerColor =
                            Color(
                                0xFF06C5D4
                            ).copy(
                                alpha =
                                    0.4f
                            )
                    )

        ) {


            if (
                saving
            ) {


                CircularProgressIndicator(

                    modifier =
                        Modifier
                            .size(
                                22.dp
                            ),

                    strokeWidth =
                        2.dp,

                    color =
                        SoulColors.textPrimary
                )


            } else {


                Text(

                    text =
                        epSave,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize =
                        15.sp
                )
            }
        }


        // =================================================
        // 保存消息
        // =================================================

        if (
            message
                .isNotBlank()
        ) {


            Spacer(

                modifier =
                    Modifier
                        .height(
                            14.dp
                        )
            )


            Text(

                text =
                    message,

                color =
                    if (
                        message ==
                        epSaved
                    ) {

                        Color(
                            0xFF00D0D9
                        )

                    } else {

                        Color(
                            0xFFFF6B6B
                        )
                    },

                fontSize =
                    13.sp
            )
        }
    }
}


// =========================================================
// 输入框
// =========================================================

@Composable
private fun EditField(

    title:
    String,

    value:
    String,

    onValueChange:
        (String) -> Unit

) {


    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    bottom =
                        14.dp
                )

    ) {


        Text(

            text =
                title,

            color =
                Color.White.copy(
                    alpha =
                        0.75f
                ),

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier
                    .padding(
                        start =
                            4.dp,
                        bottom =
                            7.dp
                    )
        )


        OutlinedTextField(

            value =
                value,

            onValueChange =
                onValueChange,

            singleLine =
                true,

            modifier =
                Modifier
                    .fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    20.dp
                ),

            colors =
                OutlinedTextFieldDefaults
                    .colors(

                        focusedTextColor =
                            SoulColors.textPrimary,

                        unfocusedTextColor =
                            SoulColors.textPrimary,

                        focusedBorderColor =
                            Color(
                                0xFF00C8D7
                            ),

                        unfocusedBorderColor =
                            Color.White.copy(
                                alpha =
                                    0.18f
                            ),

                        cursorColor =
                            Color(
                                0xFF00C8D7
                            ),

                        focusedContainerColor =
                            Color.White.copy(
                                alpha =
                                    0.04f
                            ),

                        unfocusedContainerColor =
                            Color.White.copy(
                                alpha =
                                    0.04f
                            )
                    )
        )
    }
}


// =========================================================
// 数据库 avatar -> Android 可访问 URL
// =========================================================

private fun buildAvatarUrl(
    avatar: String?
): String? {


    if (
        avatar.isNullOrBlank()
    ) {

        return null
    }


    // 已经是完整 URL
    if (
        avatar.startsWith(
            "http://"
        )
        ||
        avatar.startsWith(
            "https://"
        )
    ) {

        return avatar
    }


    // 数据库存的是：
    //
    // /uploads/avatars/xxx.jpg
    //
    // 转换成：
    //
    // http://10.0.2.2:8080/uploads/avatars/xxx.jpg

    return SoulApiService
        .BASE_URL
        .trimEnd(
            '/'
        )

    "/"

    avatar
        .trimStart(
            '/'
        )
}