package com.example.soul_android.ui.screens
import com.example.soul_android.ui.components.HazeBackground
import com.example.soul_android.ui.theme.SoulColors

import android.content.Context
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.call.CallManager
import com.example.soul_android.data.LanguageState
import com.example.soul_android.models.AppLanguage
import com.example.soul_android.models.Message
import com.example.soul_android.models.MessageType
import com.example.soul_android.ui.components.BackgroundGalaxy
import com.example.soul_android.ui.components.LanguageSelector
import com.example.soul_android.ui.components.SoulTextField
import com.example.soul_android.ui.components.UserAvatar
import com.example.soul_android.ui.viewmodels.ChatUiState
import com.example.soul_android.ui.viewmodels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: String,
    onBackClick: () -> Unit = {},
    onScheduleClick: (String) -> Unit = {},
    onCustomerServiceClick: () -> Unit = {},
    onVoiceCallClick: (String) -> Unit = {},
    onVideoCallClick: (String) -> Unit = {},
    chatViewModel: ChatViewModel = viewModel()
) {

    val language by LanguageState.language

    val translateTargetLang = when (language) {
        AppLanguage.CHINESE -> "中文"
        AppLanguage.ENGLISH -> "English"
        AppLanguage.KOREAN -> "韩语"
    }

    var languageMenuExpanded by remember {
        mutableStateOf(false)
    }

    var messageText by remember {
        mutableStateOf("")
    }

    var showExchangeDialog by remember {
        mutableStateOf(false)
    }

    /*
     * 图片大屏预览（点击图片后进入）
     */
    var viewerImageUrl by remember {
        mutableStateOf<String?>(null)
    }

    /*
     * 好友申请弹窗（验证信息 + 匹配技能）
     */
    var showFriendRequestDialog by remember {
        mutableStateOf(false)
    }

    /*
     * 全局翻译开关
     */
    var isGlobalTranslationEnabled by remember {
        mutableStateOf(false)
    }

    /*
     * AI 翻译接口
     */
    val apiService = remember {
        SoulApiService.create()
    }

    val strings = when (language) {
        AppLanguage.KOREAN -> ChatStrings(
            onlineStatus = "온라인",
            inputPlaceholder = "메시지를 입력하세요...",
            schedule = "일정 잡기",
            exchange = "스킬 교환",
            exchangeTitle = "스킬 교환 안내",
            exchangeMsg = "상대방과 스킬 교환을 조율할 수 있습니다.",
            close = "닫기",
            typing = "입력 중..."
        )
        AppLanguage.ENGLISH -> ChatStrings(
            onlineStatus = "Online",
            inputPlaceholder = "Type a message...",
            schedule = "Schedule",
            exchange = "Skill Exchange",
            exchangeTitle = "Skill Exchange Guide",
            exchangeMsg = "You can coordinate a skill exchange with your partner.",
            close = "Close",
            typing = "Typing..."
        )
        AppLanguage.CHINESE -> ChatStrings(
            onlineStatus = "在线",
            inputPlaceholder = "输入消息...",
            schedule = "约时间",
            exchange = "技能交换",
            exchangeTitle = "技能交换说明",
            exchangeMsg = "你可以与对方协调技能交换。",
            close = "关闭",
            typing = "正在输入..."
        )
    }


    // =====================================================
    // 当前登录用户
    // =====================================================

    val context = LocalContext.current

    val currentUsername = remember(context) {

        /*
         * 登录页面正常情况下应该把 username
         * 保存到 soul_login_prefs。
         */
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

        /*
         * 非常重要：
         *
         * 不再使用
         *
         * ?: "xiangyu"
         *
         * 如果没有登录信息，就返回空字符串。
         */
        soulPrefs
            .getString("username", "")
            ?.takeIf {
                it.isNotBlank()
            }
            ?: userPrefs
                .getString("username", "")
                ?.takeIf {
                    it.isNotBlank()
                }
            ?: ""
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && currentUsername.isNotBlank() && userId.isNotBlank()) {
            chatViewModel.sendImage(
                context = context,
                imageUri = uri,
                receiverId = userId,
                currentUsername = currentUsername
            )
        }
    }

    // 聊天对象显示名（先用 username 兜底，后续从后端拉真实 name）
    var partnerName by remember(userId) {
        mutableStateOf(userId)
    }

    var pendingCallType by remember { mutableStateOf("VOICE") }

    fun startCall(caller: String, partner: String, callType: String) {
        CallManager.startCall(
            context = context,
            peerUsername = partner,
            peerName = partnerName,
            callType = callType
        )
        if (callType == "VIDEO") {
            onVideoCallClick(partner)
        } else {
            onVoiceCallClick(partner)
        }
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants.values.all { it }
        if (granted && currentUsername.isNotBlank() && userId.isNotBlank()) {
            startCall(currentUsername, userId, pendingCallType)
        } else {
            Toast.makeText(context, "需要麦克风/摄像头权限才能通话", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestCall(callType: String) {
        pendingCallType = callType
        val permissions =
            if (callType == "VIDEO") {
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                )
            } else {
                arrayOf(Manifest.permission.RECORD_AUDIO)
            }
        callPermissionLauncher.launch(permissions)
    }


    // =====================================================
    // 聊天对象资料
    // =====================================================

    /*
     * 根据 username 从后端获取聊天对象资料。
     *
     * 这样：
     * API receiverUsername = minho10
     * UI 显示 name = 서민호
     */
    LaunchedEffect(userId) {

        if (userId.isBlank()) {
            partnerName = ""
            return@LaunchedEffect
        }

        // 请求开始前先用 username 兜底，避免页面空白
        partnerName = userId

        try {

            val response =
                apiService.getUserProfile(
                    username = userId
                )

            if (response.isSuccessful) {

                val profile =
                    response.body()

                partnerName =
                    profile
                        ?.name
                        ?.takeIf { it.isNotBlank() }
                        ?: profile
                            ?.username
                            ?.takeIf { it.isNotBlank() }
                                ?: userId

                android.util.Log.d(
                    "CHAT_SCREEN",
                    "聊天对象资料加载成功: username=$userId name=$partnerName"
                )

            } else {

                android.util.Log.e(
                    "CHAT_SCREEN",
                    "聊天对象资料加载失败: username=$userId code=${response.code()} error=${response.errorBody()?.string()}"
                )
            }

        } catch (e: Exception) {

            android.util.Log.e(
                "CHAT_SCREEN",
                "获取聊天对象资料异常: username=$userId",
                e
            )

            // 网络异常时仍然使用 username，不影响聊天 API
            partnerName = userId
        }
    }


    // =====================================================
    // ViewModel
    // =====================================================

    val uiState by
    chatViewModel.uiState.collectAsState()

    val relationStatus by
        chatViewModel.relationStatus.collectAsState()

    val sendError by
        chatViewModel.sendError.collectAsState()

    val isFriend = relationStatus?.friend == true
    val isMatched = relationStatus?.matched == true

    LaunchedEffect(userId, currentUsername) {
        if (currentUsername.isNotBlank() && userId.isNotBlank()) {
            chatViewModel.fetchRelationStatus(currentUsername, userId)
        }
    }

    LaunchedEffect(sendError) {
        sendError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            chatViewModel.clearSendError()
        }
    }

    val listState =
        rememberLazyListState()


    // =====================================================
    // 进入聊天页面
    // =====================================================

    LaunchedEffect(
        userId,
        currentUsername
    ) {

        if (
            currentUsername.isNotBlank() &&
            userId.isNotBlank()
        ) {

            /*
             * 定时轮询，几秒内收到对方新消息
             */
            while (true) {

                chatViewModel.fetchMessages(
                    otherUserId = userId,
                    currentUsername = currentUsername
                )

                chatViewModel.markRead(
                    currentUsername,
                    userId
                )

                kotlinx.coroutines.delay(2500)
            }

        } else {

            android.util.Log.e(
                "CHAT_SCREEN",
                "无法获取聊天记录: " +
                        "currentUsername=$currentUsername " +
                        "userId=$userId"
            )
        }
    }


    // =====================================================
    // 消息列表
    // =====================================================

    val (messages, partnerAvatar) =
        when (val state = uiState) {

            is ChatUiState.Success -> {
                Pair(state.messages, state.partnerAvatar)
            }

            else -> {
                Pair(emptyList(), null)
            }
        }


    /*
     * 新消息出现后自动滚动到底部
     */
    LaunchedEffect(
        messages.size
    ) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.size - 1
            )
        }
    }


    // =====================================================
    // 页面
    // =====================================================

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        HazeBackground { BackgroundGalaxy(forceDark = true) }

        Scaffold(

            containerColor =
                Color.Transparent,

            // =================================================
            // 顶部栏
            // =================================================

            topBar = {

                TopAppBar(

                    colors =
                        TopAppBarDefaults
                            .topAppBarColors(
                                containerColor =
                                    Color.Transparent
                            ),

                    title = {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            /*
                             * 头像
                             */
                            UserAvatar(
                                avatar = partnerAvatar,
                                name = partnerName,
                                size = 40.dp
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(
                                        12.dp
                                    )
                            )


                            Column {

                                Text(
                                    text =
                                        partnerName,

                                    fontSize =
                                        16.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        Color.White
                                )


                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier =
                                            Modifier
                                                .size(
                                                    8.dp
                                                )
                                                .clip(
                                                    CircleShape
                                                )
                                                .background(
                                                    Color(
                                                        0xFF4CAF50
                                                    )
                                                )
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                6.dp
                                            )
                                    )

                                    Text(
                                        text =
                                            strings.onlineStatus,

                                        fontSize =
                                            12.sp,

                                        color =
                                            Color(
                                                0xFF00D0D9
                                            ).copy(
                                                alpha =
                                                    0.8f
                                            )
                                    )
                                }
                            }
                        }
                    },


                    // =================================================
                    // 返回按钮
                    // =================================================

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
                                    Color.White
                            )
                        }
                    },


                    // =================================================
                    // 顶部操作按钮
                    // =================================================

                    actions = {

                        /*
                         * AI 全局翻译
                         */
                        IconButton(

                            onClick = {

                                isGlobalTranslationEnabled =
                                    !isGlobalTranslationEnabled
                            },

                            modifier =
                                Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                    .background(

                                        if (
                                            isGlobalTranslationEnabled
                                        ) {

                                            Color(
                                                0xFF00D0D9
                                            ).copy(
                                                alpha =
                                                    0.2f
                                            )

                                        } else {

                                            Color.Transparent
                                        }
                                    )
                                    .border(

                                        width =
                                            1.dp,

                                        color =
                                            if (
                                                isGlobalTranslationEnabled
                                            ) {

                                                Color(
                                                    0xFF00D0D9
                                                )

                                            } else {

                                                Color.White.copy(
                                                    alpha =
                                                        0.2f
                                                )
                                            },

                                        shape =
                                            RoundedCornerShape(
                                                12.dp
                                            )
                                    )
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.SwapHoriz,

                                contentDescription =
                                    "AI Global Translate",

                                tint =
                                    if (
                                        isGlobalTranslationEnabled
                                    ) {

                                        Color(
                                            0xFF00D0D9
                                        )

                                    } else {

                                        Color.White
                                    }
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.width(
                                    8.dp
                                )
                        )


                        /*
                         * AI 客服
                         */
                        IconButton(
                            onClick =
                                onCustomerServiceClick
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Help,

                                contentDescription =
                                    "AI Customer Service",

                                tint =
                                    Color.White
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.width(
                                    4.dp
                                )
                        )


                        /*
                         * 加好友（仅匹配未加好友时显示）
                         */
                        if (isMatched && !isFriend) {
                            IconButton(
                                onClick = {
                                    showFriendRequestDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Add Friend",
                                    tint = Color(0xFF00D0D9)
                                )
                            }
                        }


                        /*
                         * 语言选择
                         */
                        LanguageSelector(

                            currentLanguage =
                                language,

                            expanded =
                                languageMenuExpanded,

                            onExpandedChange = {
                                languageMenuExpanded =
                                    it
                            },

                            onLanguageSelected = {
                                LanguageState.setLanguage(it)

                                languageMenuExpanded =
                                    false
                            }
                        )
                    }
                )
            }

        ) { padding ->


            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            padding
                        )
            ) {


                // =================================================
                // 聊天区域
                // =================================================

                Box(

                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .fillMaxWidth()
                ) {


                    /*
                     * 没有登录信息
                     */
                    if (
                        currentUsername.isBlank()
                    ) {

                        Box(

                            modifier =
                                Modifier
                                    .fillMaxSize(),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    if (language == AppLanguage.CHINESE) "找不到登录信息。" else if (language == AppLanguage.ENGLISH) "Login info not found." else "로그인 정보를 찾을 수 없습니다.",

                                color =
                                    Color.Red.copy(
                                        alpha =
                                            0.8f
                                    )
                            )
                        }

                    } else {

                        when (
                            val state =
                                uiState
                        ) {


                            // =========================================
                            // Loading
                            // =========================================

                            ChatUiState.Loading -> {

                                Box(

                                    modifier =
                                        Modifier
                                            .fillMaxSize(),

                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    CircularProgressIndicator(
                                        color =
                                            Color(
                                                0xFF00D0D9
                                            )
                                    )
                                }
                            }


                            // =========================================
                            // Error
                            // =========================================

                            is ChatUiState.Error -> {

                                Box(

                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .padding(
                                                24.dp
                                            ),

                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Text(
                                        text =
                                            state.message,

                                        color =
                                            Color.Red.copy(
                                                alpha =
                                                    0.8f
                                            ),

                                        fontSize =
                                            14.sp
                                    )
                                }
                            }


                            // =========================================
                            // Success
                            // =========================================

                            is ChatUiState.Success -> {

                                LazyColumn(

                                    state =
                                        listState,

                                    modifier =
                                        Modifier
                                            .fillMaxSize(),

                                    contentPadding =
                                        PaddingValues(
                                            16.dp
                                        ),

                                    verticalArrangement =
                                        Arrangement.spacedBy(
                                            16.dp
                                        )
                                ) {

                                    items(
                                        items =
                                            state.messages,

                                        key = {
                                            it.id
                                        }
                                    ) { message ->

                                        ChatBubbleComponent(

                                            message =
                                                message,

                                            otherUserName =
                                                partnerName,

                                            partnerAvatar =
                                                partnerAvatar,

                                            isGlobalTranslationEnabled =
                                                isGlobalTranslationEnabled,

                                            apiService =
                                                apiService,

                                            targetLangName =
                                                translateTargetLang,

                                            language =
                                                language,

                                            onImageClick = {
                                                viewerImageUrl =
                                                    buildAvatarUrl(it.mediaUrl)
                                            },

                                            onRecall = {
                                                chatViewModel.recallMessage(
                                                    it,
                                                    currentUsername
                                                )
                                            },

                                            onDelete = {
                                                chatViewModel.deleteMessage(
                                                    it,
                                                    currentUsername
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                // =================================================
                // 正在输入
                // =================================================

                AnimatedVisibility(

                    visible =
                        false,

                    enter =
                        fadeIn() +
                                expandVertically(),

                    exit =
                        fadeOut() +
                                shrinkVertically()
                ) {

                    Text(

                        text =
                            strings.typing,

                        color =
                            Color.White.copy(
                                alpha =
                                    0.4f
                            ),

                        fontSize =
                            12.sp,

                        modifier =
                            Modifier.padding(
                                horizontal =
                                    24.dp,

                                vertical =
                                    4.dp
                            )
                    )
                }


                // =================================================
                // 快捷按钮
                // =================================================

                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal =
                                    16.dp,

                                vertical =
                                    8.dp
                            ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {


                    AssistChip(

                        onClick = {
                            onScheduleClick(
                                userId
                            )
                        },

                        label = {

                            Text(
                                text =
                                    strings.schedule,

                                color =
                                    Color.White,

                                fontSize =
                                    12.sp
                            )
                        },

                        leadingIcon = {

                            Icon(
                                imageVector =
                                    Icons
                                        .Default
                                        .CalendarMonth,

                                contentDescription =
                                    null,

                                tint =
                                    Color(
                                        0xFF00D0D9
                                    ),

                                modifier =
                                    Modifier.size(
                                        16.dp
                                    )
                            )
                        },

                        shape =
                            RoundedCornerShape(
                                20.dp
                            ),

                        border =
                            BorderStroke(

                                width =
                                    1.dp,

                                color =
                                    Color.White.copy(
                                        alpha =
                                            0.12f
                                    )
                            ),

                        colors =
                            AssistChipDefaults
                                .assistChipColors(

                                    containerColor =
                                        Color.White.copy(
                                            alpha =
                                                0.05f
                                        )
                                )
                    )


                    AssistChip(

                        onClick = {
                            showExchangeDialog =
                                true
                        },

                        label = {

                            Text(
                                text =
                                    strings.exchange,

                                color =
                                    Color.White,

                                fontSize =
                                    12.sp
                            )
                        },

                        leadingIcon = {

                            Icon(
                                imageVector =
                                    Icons.Default.SwapHoriz,

                                contentDescription =
                                    null,

                                tint =
                                    Color(
                                        0xFF00D0D9
                                    ),

                                modifier =
                                    Modifier.size(
                                        16.dp
                                    )
                            )
                        },

                        shape =
                            RoundedCornerShape(
                                20.dp
                            ),

                        border =
                            BorderStroke(

                                width =
                                    1.dp,

                                color =
                                    Color.White.copy(
                                        alpha =
                                            0.12f
                                    )
                            ),

                        colors =
                            AssistChipDefaults
                                .assistChipColors(

                                    containerColor =
                                        Color.White.copy(
                                            alpha =
                                                0.05f
                                        )
                                )
                    )
                }


                // =================================================
                // 输入区域
                // =================================================

                Surface(

                    modifier =
                        Modifier
                            .fillMaxWidth(),

                    color =
                        Color.White.copy(
                            alpha =
                                0.03f
                        ),

                    border =
                        BorderStroke(

                            width =
                                1.dp,

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.1f
                                )
                        )
                ) {

                    Column(
                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            )
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ChatActionButton(
                                icon = Icons.Default.Image,
                                label = when (language) {
                                    AppLanguage.CHINESE -> "图片"
                                    AppLanguage.ENGLISH -> "Image"
                                    AppLanguage.KOREAN -> "사진"
                                },
                                enabled = currentUsername.isNotBlank() && userId.isNotBlank() && isFriend,
                                onClick = { imagePicker.launch("image/*") }
                            )
                            ChatActionButton(
                                icon = Icons.Default.Mic,
                                label = when (language) {
                                    AppLanguage.CHINESE -> "语音"
                                    AppLanguage.ENGLISH -> "Voice"
                                    AppLanguage.KOREAN -> "음성"
                                },
                                enabled = currentUsername.isNotBlank() && userId.isNotBlank() && isFriend,
                                onClick = { requestCall("VOICE") }
                            )
                            ChatActionButton(
                                icon = Icons.Default.Videocam,
                                label = when (language) {
                                    AppLanguage.CHINESE -> "视频"
                                    AppLanguage.ENGLISH -> "Video"
                                    AppLanguage.KOREAN -> "영상"
                                },
                                enabled = currentUsername.isNotBlank() && userId.isNotBlank() && isFriend,
                                onClick = { requestCall("VIDEO") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {


                        SoulTextField(

                            value =
                                messageText,

                            onValueChange = {
                                messageText =
                                    it
                            },

                            placeholder =
                                strings.inputPlaceholder,

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )


                        Spacer(
                            modifier =
                                Modifier.width(
                                    12.dp
                                )
                        )


                        // =================================================
                        // 发送按钮
                        // =================================================

                        val canSend =
                            messageText.isNotBlank() &&
                                    currentUsername.isNotBlank() &&
                                    userId.isNotBlank()


                        val sendButtonScale by
                        animateFloatAsState(

                            targetValue =
                                if (
                                    canSend
                                ) {
                                    1.1f
                                } else {
                                    1f
                                },

                            label =
                                "sendButtonScale"
                        )


                        IconButton(

                            onClick = {

                                if (
                                    canSend
                                ) {

                                    /*
                                     * 保存内容。
                                     *
                                     * 避免先 clear 后拿不到原消息。
                                     */
                                    val messageToSend =
                                        messageText.trim()


                                    android.util.Log.d(
                                        "CHAT_SCREEN",
                                        "点击发送: " +
                                                "sender=$currentUsername " +
                                                "receiver=$userId " +
                                                "content=$messageToSend"
                                    )


                                    /*
                                     * 非常重要：
                                     *
                                     * 现在必须传三个参数：
                                     *
                                     * receiverId
                                     * content
                                     * currentUsername
                                     */
                                    chatViewModel.sendMessage(

                                        receiverId =
                                            userId,

                                        content =
                                            messageToSend,

                                        currentUsername =
                                            currentUsername
                                    )


                                    /*
                                     * 清空输入框。
                                     *
                                     * 如果以后需要实现
                                     * “发送失败保留内容”，
                                     * 可以再把这个逻辑移到 ViewModel。
                                     */
                                    messageText =
                                        ""
                                }
                            },

                            enabled =
                                canSend,

                            modifier =
                                Modifier
                                    .size(
                                        48.dp
                                    )
                                    .scale(
                                        sendButtonScale
                                    )
                                    .clip(
                                        CircleShape
                                    )
                                    .background(

                                        if (
                                            canSend
                                        ) {

                                            Brush.linearGradient(

                                                colors =
                                                    listOf(
                                                        Color(
                                                            0xFF00D0D9
                                                        ),
                                                        Color(
                                                            0xFF7E57C2
                                                        )
                                                    )
                                            )

                                        } else {

                                            Brush.linearGradient(

                                                colors =
                                                    listOf(
                                                        Color.White.copy(
                                                            alpha =
                                                                0.1f
                                                        ),
                                                        Color.White.copy(
                                                            alpha =
                                                                0.1f
                                                        )
                                                    )
                                            )
                                        }
                                    )
                        ) {

                            Icon(

                                imageVector =
                                    Icons
                                        .AutoMirrored
                                        .Filled
                                        .Send,

                                contentDescription =
                                    "Send",

                                tint =
                                    Color.White
                            )
                        }
                    }
                    }
                }
            }
        }
    }


    // =====================================================
    // Skill Exchange Dialog
    // =====================================================

    if (
        showExchangeDialog
    ) {

        AlertDialog(

            onDismissRequest = {
                showExchangeDialog =
                    false
            },

            containerColor =
                Color(0xFF1C1F26),

            titleContentColor =
                Color.White,

            textContentColor =
                Color.White.copy(
                    alpha =
                        0.7f
                ),

            title = {

                Text(
                    text =
                        strings.exchangeTitle,

                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                Text(
                    text =
                        strings.exchangeMsg
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {
                        showExchangeDialog =
                            false
                    }
                ) {

                    Text(

                        text =
                            strings.close,

                        color =
                            Color(
                                0xFF00D0D9
                            ),

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        )
    }


    /*
     * 图片大屏预览（点击图片后进入）
     */
    viewerImageUrl?.let { url ->
        ImageViewerScreen(
            imageUrl = url,
            onClose = { viewerImageUrl = null }
        )
    }

    /*
     * 好友申请弹窗（验证信息 + 匹配技能）
     */
    if (showFriendRequestDialog) {
        FriendRequestDialog(
            targetUsername = userId,
            onDismiss = { showFriendRequestDialog = false },
            onSend = { message ->
                chatViewModel.requestFriend(currentUsername, userId, message)
                showFriendRequestDialog = false
            }
        )
    }
}


// =========================================================
// Chat Actions
// =========================================================

@Composable
private fun ChatActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = if (enabled) Color(0xFF00D0D9).copy(alpha = 0.10f)
        else Color.White.copy(alpha = 0.035f),
        border = BorderStroke(
            1.dp,
            if (enabled) Color(0xFF00D0D9).copy(alpha = 0.35f)
            else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) Color(0xFF00D0D9) else Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.35f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// =========================================================
// Chat Bubble
// =========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatBubbleComponent(

    message: Message,

    otherUserName: String,

    partnerAvatar: String?,

    isGlobalTranslationEnabled: Boolean,

    apiService: SoulApiService,

    targetLangName: String,

    language: AppLanguage,

    onImageClick: (Message) -> Unit,

    onRecall: (Message) -> Unit,

    onDelete: (Message) -> Unit
) {


    val timeFormat =
        remember {

            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            )
        }


    val timeStr =
        timeFormat.format(
            Date(
                message.timestamp
            )
        )


    var translatedText by
    remember(
        message.id
    ) {
        mutableStateOf("")
    }


    var isTranslating by
    remember(
        message.id
    ) {
        mutableStateOf(false)
    }


    /*
     * 长按操作弹窗状态
     */
    var showActions by
    remember(message.id) {
        mutableStateOf(false)
    }

    /*
     * 删除确认弹窗
     */
    var showDeleteConfirm by
    remember(message.id) {
        mutableStateOf(false)
    }


    /*
     * 撤回时效：仅自己发送、未撤回、2 分钟内
     */
    val recallable =
        message.isMe &&
                !message.recalled &&
                (System.currentTimeMillis() - message.timestamp) <= 2 * 60 * 1000L


    val actionStrings =
        when (language) {
            AppLanguage.KOREAN -> MessageActionStrings(
                recall = "회수",
                delete = "삭제",
                clear = "지우기",
                recalledByMe = "메시지를 회수했습니다",
                recalledByOther = "상대방이 메시지를 회수했습니다",
                actionsTitle = "메시지 작업"
            )
            AppLanguage.ENGLISH -> MessageActionStrings(
                recall = "Recall",
                delete = "Delete",
                clear = "Clear",
                recalledByMe = "You recalled a message",
                recalledByOther = "The other party recalled a message",
                actionsTitle = "Message actions"
            )
            AppLanguage.CHINESE -> MessageActionStrings(
                recall = "撤回",
                delete = "删除",
                clear = "清除",
                recalledByMe = "你撤回了一条消息",
                recalledByOther = "对方撤回了一条消息",
                actionsTitle = "消息操作"
            )
        }


    // =====================================================
    // AI 自动翻译
    // =====================================================

    LaunchedEffect(
        isGlobalTranslationEnabled,
        message.id,
        targetLangName
    ) {

        if (
            isGlobalTranslationEnabled &&
            !message.isMe &&
            message.type == MessageType.TEXT &&
            translatedText.isEmpty()
        ) {

            isTranslating =
                true

            try {

                val resp =
                    apiService.translateText(

                        text =
                            message.text,

                        targetLang =
                            targetLangName
                    )


                if (
                    resp.isSuccessful &&
                    resp.body() != null
                ) {

                    translatedText =
                        resp.body()!!
                            .advice

                } else {

                    translatedText =
                        "[翻译失败]"
                }

            } catch (
                e: Exception
            ) {

                translatedText =
                    "[翻译失败]"
            }


            isTranslating =
                false
        }
    }


    // =====================================================
    // Message UI
    // =====================================================

    Column(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalAlignment =
            if (
                message.isMe
            ) {
                Alignment.End
            } else {
                Alignment.Start
            }
    ) {

        if (message.recalled) {

            /*
             * 撤回后的占位提示（双方都只能看到这个）
             */
            Text(

                text =
                    if (message.isMe) {
                        actionStrings.recalledByMe
                    } else {
                        actionStrings.recalledByOther
                    },

                color =
                    Color.White.copy(
                        alpha = 0.35f
                    ),

                fontSize =
                    13.sp
            )


            Spacer(
                modifier = Modifier.height(2.dp)
            )


            Text(

                text = timeStr,

                fontSize = 10.sp,

                color = Color.White.copy(
                    alpha = 0.25f
                )
            )

        } else if (message.type == MessageType.CALL) {

            /*
             * 通话记录（图标 + 状态颜色 + 时长）
             */
            CallRecordBubble(
                content = message.text,
                mediaUrl = message.mediaUrl,
                timeStr = timeStr,
                language = language
            )

        } else {

            Row(

                verticalAlignment =
                    Alignment.Bottom,

                horizontalArrangement =
                    if (
                        message.isMe
                    ) {
                        Arrangement.End
                    } else {
                        Arrangement.Start
                    },

                modifier =
                    Modifier.fillMaxWidth()
            ) {


                /*
                 * 对方头像
                 */
                if (
                    !message.isMe
                ) {
                    val avatarUrl = buildAvatarUrl(partnerAvatar)

                    Box(

                        modifier =
                            Modifier
                                .size(
                                    32.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(

                                    Brush.linearGradient(

                                        colors =
                                            listOf(
                                                Color(
                                                    0xFF00D0D9
                                                ),
                                                Color(
                                                    0xFF7E57C2
                                                )
                                            )
                                    )
                                )
                                .align(
                                    Alignment.Top
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Partner Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(

                                text =
                                    otherUserName
                                        .take(1),

                                color =
                                    Color.White,

                                fontSize =
                                    12.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.width(
                                8.dp
                            )
                    )
                }


                // 发送失败：红色感叹号
                if (message.isMe && message.failed) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "发送失败",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }


                Column(

                    horizontalAlignment =
                        if (
                            message.isMe
                        ) {
                            Alignment.End
                        } else {
                            Alignment.Start
                        }
                ) {


                    Surface(

                        color =
                            if (
                                message.isMe
                            ) {

                                Color.Transparent

                            } else {

                                Color.White.copy(
                                    alpha =
                                        0.08f
                                )
                            },

                        shape =
                            RoundedCornerShape(

                                topStart =
                                    if (
                                        message.isMe
                                    ) {
                                        20.dp
                                    } else {
                                        4.dp
                                    },

                                topEnd =
                                    if (
                                        message.isMe
                                    ) {
                                        4.dp
                                    } else {
                                        20.dp
                                    },

                                bottomStart =
                                    20.dp,

                                bottomEnd =
                                    20.dp
                            ),

                        border =
                            if (
                                message.isMe
                            ) {

                                null

                            } else {

                                BorderStroke(

                                    width =
                                        1.dp,

                                    color =
                                        Color.White.copy(
                                            alpha =
                                                0.08f
                                        )
                                )
                            },

                        modifier =
                            if (
                                message.isMe
                            ) {

                                Modifier.background(

                                    brush =
                                        Brush.linearGradient(

                                            colors =
                                                listOf(Color(0xFF00E5D0), Color(0xFF4C6FFF), Color(0xFF7C5CFF))
                                        ),

                                    shape =
                                        RoundedCornerShape(
                                            topStart =
                                                20.dp,

                                            topEnd =
                                                4.dp,

                                            bottomEnd =
                                                20.dp,

                                            bottomStart =
                                                20.dp
                                        )
                                )

                            } else {

                                Modifier
                            }
                            .combinedClickable(
                                onClick = {
                                    if (message.type == MessageType.IMAGE) {
                                        onImageClick(message)
                                    }
                                },
                                onLongClick = {
                                    showActions = true
                                }
                            )
                    ) {
                        if (message.type == MessageType.IMAGE && !message.mediaUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = buildAvatarUrl(message.mediaUrl),
                                contentDescription = "Chat image",
                                modifier = Modifier
                                    .widthIn(max = 240.dp)
                                    .heightIn(min = 120.dp, max = 300.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            SelectionContainer {
                                Text(
                                    text = message.text,
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 10.dp
                                    ),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }


                    // =================================================
                    // 翻译结果
                    // =================================================

                    if (
                        isGlobalTranslationEnabled &&
                        !message.isMe &&
                        message.type == MessageType.TEXT
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    4.dp
                                )
                        )


                        Text(

                            text =
                                when {

                                    isTranslating -> {
                                        "翻译中..."
                                    }

                                    translatedText
                                        .isNotEmpty() -> {

                                        "✨ $translatedText"
                                    }

                                    else -> {
                                        ""
                                    }
                                },

                            color =
                                Color(
                                    0xFF00D0D9
                                ),

                            fontSize =
                                12.sp,

                            fontWeight =
                                FontWeight.Medium,

                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        6.dp
                                )
                        )
                    }


                    /*
                     * 消息时间（放在消息下方）
                     */
                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(

                        text = timeStr,

                        fontSize = 10.sp,

                        color = Color.White.copy(
                            alpha = 0.3f
                        ),

                        modifier = Modifier.padding(
                            horizontal = 4.dp
                        )
                    )
                }
            }
        }
    }


    /*
     * 长按操作弹窗（撤回 / 删除）
     */
    if (showActions) {

        ModalBottomSheet(

            onDismissRequest = {
                showActions = false
            },

            containerColor =
                Color(0xFF1C1F26)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp)
            ) {

                Text(
                    text = actionStrings.actionsTitle,
                    color = Color(0xFFA8B0C2),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (message.isMe && recallable) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                showActions = false
                                onRecall(message)
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actionStrings.recall,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            showActions = false
                            showDeleteConfirm = true
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = actionStrings.delete,
                        color = Color(0xFFFF5A5A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    /*
     * 删除前询问是否确定
     */
    if (showDeleteConfirm) {
        val title = when (language) {
            AppLanguage.CHINESE -> "删除消息"
            AppLanguage.ENGLISH -> "Delete message"
            AppLanguage.KOREAN -> "메시지 삭제"
        }
        val desc = when (language) {
            AppLanguage.CHINESE -> "确定要删除这条消息吗？删除后仅自己不可见。"
            AppLanguage.ENGLISH -> "Delete this message? It will be removed only for you."
            AppLanguage.KOREAN -> "이 메시지를 삭제하시겠습니까? 나에게서만 삭제됩니다."
        }
        val cancelLabel = when (language) {
            AppLanguage.CHINESE -> "取消"
            AppLanguage.ENGLISH -> "Cancel"
            AppLanguage.KOREAN -> "취소"
        }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1C1F26),
            title = {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = desc,
                    color = Color(0xFFA8B0C2),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(message)
                    }
                ) {
                    Text(
                        text = actionStrings.delete,
                        color = Color(0xFFFF5A5A),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        text = cancelLabel,
                        color = Color(0xFFA8B0C2)
                    )
                }
            }
        )
    }
}


// =========================================================
// Strings
// =========================================================

@Composable
private fun FriendRequestDialog(
    targetUsername: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    val api = remember { SoulApiService.create() }
    var message by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(targetUsername) {
        try {
            val p = api.getUserProfile(targetUsername).body()
            val list = mutableListOf<String>()
            p?.skillOffer?.split(",", "，", "、", "/")?.forEach { s ->
                val t = s.trim()
                if (t.isNotBlank()) list.add(t)
            }
            p?.skillWant?.split(",", "，", "、", "/")?.forEach { s ->
                val t = s.trim()
                if (t.isNotBlank()) list.add(t)
            }
            skills = list.distinct()
        } catch (_: Exception) {
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1F26),
        title = { Text("添加好友", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("填写验证信息", color = Color(0xFF6E7789)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = Color(0xFF00D0D9),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("匹配技能", color = Color(0xFFA8B0C2), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (skills.isEmpty()) "暂无" else skills.joinToString(" · "),
                    color = Color(0xFF00D0D9),
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSend(message) }) {
                Text("发送", color = Color(0xFF00D0D9), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color(0xFFA8B0C2))
            }
        }
    )
}


private data class ChatStrings(

    val onlineStatus: String,

    val inputPlaceholder: String,

    val schedule: String,

    val exchange: String,

    val exchangeTitle: String,

    val exchangeMsg: String,

    val close: String,

    val typing: String
)


private data class MessageActionStrings(

    val recall: String,

    val delete: String,

    val clear: String,

    val recalledByMe: String,

    val recalledByOther: String,

    val actionsTitle: String
)

private fun buildAvatarUrl(avatar: String?): String? {
    if (avatar.isNullOrBlank()) return null
    if (avatar.startsWith("http://") || avatar.startsWith("https://")) return avatar
    return SoulApiService.BASE_URL.trimEnd('/') + "/" + avatar.trimStart('/')
}

@Composable
private fun CallRecordBubble(
    content: String,
    mediaUrl: String?,
    timeStr: String,
    language: AppLanguage
) {
    val isVideo =
        mediaUrl.equals(
            "VIDEO",
            ignoreCase = true
        )
    val ended =
        content.startsWith("ended:")
    val canceled =
        content.equals("canceled", true) || content.equals("cancelled", true)
    val missed =
        content.equals("missed", true)
    val rejected =
        content.equals("rejected", true)
    val durationSec =
        if (ended) content.substringAfter(":").toIntOrNull() ?: 0 else 0

    val accent =
        when {
            missed || rejected -> Color(0xFFFF6B6B)
            canceled -> Color.White.copy(alpha = 0.55f)
            else -> Color(0xFF00D0D9)
        }

    val callLabel =
        when (language) {
            AppLanguage.CHINESE -> if (isVideo) "视频通话" else "语音通话"
            AppLanguage.ENGLISH -> if (isVideo) "Video call" else "Voice call"
            AppLanguage.KOREAN -> if (isVideo) "영상통화" else "음성통화"
        }

    val statusLabel =
        when {
            missed -> when (language) {
                AppLanguage.CHINESE -> "未接听"
                AppLanguage.ENGLISH -> "Missed"
                AppLanguage.KOREAN -> "부재중"
            }
            rejected -> when (language) {
                AppLanguage.CHINESE -> "对方已拒绝"
                AppLanguage.ENGLISH -> "Declined"
                AppLanguage.KOREAN -> "상대방이 거절함"
            }
            canceled -> when (language) {
                AppLanguage.CHINESE -> "已取消"
                AppLanguage.ENGLISH -> "Canceled"
                AppLanguage.KOREAN -> "취소됨"
            }
            else -> formatCallDuration(durationSec)
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    if (isVideo) Icons.Filled.Videocam else Icons.Filled.Call,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column {

                Text(
                    text = callLabel,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = statusLabel,
                    color = accent,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = timeStr,
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 10.sp
        )
    }
}

private fun callRecordText(content: String, mediaUrl: String?, language: AppLanguage): String {
    val isVideo = mediaUrl.equals("VIDEO", ignoreCase = true)
    val typeLabel = when (language) {
        AppLanguage.CHINESE -> if (isVideo) "视频通话" else "语音通话"
        AppLanguage.ENGLISH -> if (isVideo) "Video call" else "Voice call"
        AppLanguage.KOREAN -> if (isVideo) "영상 통화" else "음성 통화"
    }
    return if (content.startsWith("ended:")) {
        val sec = content.substringAfter(":").toIntOrNull() ?: 0
        "$typeLabel ${formatCallDuration(sec)}"
    } else {
        when (language) {
            AppLanguage.CHINESE -> "未接听$typeLabel"
            AppLanguage.ENGLISH -> "Missed $typeLabel"
            AppLanguage.KOREAN -> "부재중 $typeLabel"
        }
    }
}

private fun formatCallDuration(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}
