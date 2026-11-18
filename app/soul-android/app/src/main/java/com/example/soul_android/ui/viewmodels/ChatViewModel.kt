package com.example.soul_android.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soul_android.data.LocalMessageStore
import android.content.Context
import android.net.Uri
import com.example.soul_android.data.api.ChatSendRequest
import com.example.soul_android.data.api.FriendRequest
import com.example.soul_android.data.api.MessageActionRequest
import com.example.soul_android.data.api.ReadRequest
import com.example.soul_android.data.api.RelationStatus
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.models.Message
import com.example.soul_android.models.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant

sealed class ChatUiState {

    object Loading : ChatUiState()

    data class Success(
        val messages: List<Message>,
        val partnerAvatar: String? = null
    ) : ChatUiState()

    data class Error(
        val message: String
    ) : ChatUiState()
}

class ChatViewModel(
    private val apiService: SoulApiService = SoulApiService.create()
) : ViewModel() {

    companion object {
        private const val TAG = "CHAT_DEBUG"
    }

    private val _uiState =
        MutableStateFlow<ChatUiState>(ChatUiState.Loading)

    val uiState = _uiState.asStateFlow()

    private val _messages =
        MutableStateFlow<List<Message>>(emptyList())

    private var activeConversationKey: String? = null
    private var isFetching = false

    private val _relationStatus = MutableStateFlow<RelationStatus?>(null)
    val relationStatus = _relationStatus.asStateFlow()

    private val _sendError = MutableStateFlow<String?>(null)
    val sendError = _sendError.asStateFlow()

    /**
     * 获取两个人之间的聊天记录
     */
    fun fetchMessages(
        otherUserId: String,
        currentUsername: String
    ) {



        if (otherUserId.isBlank()) {
            Log.e(TAG, "fetchMessages: otherUserId 为空")
            _uiState.value =
                ChatUiState.Error("聊天对象不能为空")
            return
        }

        if (currentUsername.isBlank()) {
            Log.e(TAG, "fetchMessages: currentUsername 为空")
            _uiState.value =
                ChatUiState.Error("当前登录用户为空")
            return
        }

        val conversationKey = "$currentUsername|$otherUserId"
        if (isFetching) return
        isFetching = true

        viewModelScope.launch {

            if (activeConversationKey != conversationKey) {
                activeConversationKey = conversationKey
                _messages.value = emptyList()
                _uiState.value = ChatUiState.Loading
            } else if (_messages.value.isEmpty()) {
                _uiState.value = ChatUiState.Loading
            }

            Log.d(
                TAG,
                "fetchMessages: me=$currentUsername partner=$otherUserId"
            )

            /*
             * 1. 创建 / 获取直聊聊天室
             *
             * 注意：
             * 这个接口返回 Response，所以 400 / 500 不一定抛 Exception。
             * 必须手动检查 isSuccessful。
             */
            try {

                val roomResponse =
                    apiService.createDirectChatRoom(
                        ChatSendRequest(
                            senderUsername = currentUsername,
                            receiverUsername = otherUserId,
                            content = ""
                        )
                    )

                if (roomResponse.isSuccessful) {

                    Log.d(
                        TAG,
                        "createDirectChatRoom success " +
                                "code=${roomResponse.code()} " +
                                "body=${roomResponse.body()}"
                    )

                } else {

                    val errorBody =
                        roomResponse.errorBody()?.string()

                    Log.e(
                        TAG,
                        "createDirectChatRoom failed " +
                                "code=${roomResponse.code()} " +
                                "error=$errorBody"
                    )
                }

            } catch (e: Exception) {

                /*
                 * 创建聊天室失败不直接终止。
                 *
                 * 因为聊天室可能本来就存在，
                 * 仍然尝试获取聊天记录。
                 */
                Log.e(
                    TAG,
                    "createDirectChatRoom exception",
                    e
                )
            }

            /*
             * 2. 获取聊天记录及对方头像
             */
            var partnerAvatar: String? = null
            try {
                val profileResp = apiService.getUserProfile(otherUserId)
                if (profileResp.isSuccessful) {
                    partnerAvatar = profileResp.body()?.avatar
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch partner profile for avatar", e)
            }

            try {

                val responses =
                    apiService.getChatMessages(
                        me = currentUsername,
                        partner = otherUserId
                    )

                Log.d(
                    TAG,
                    "getChatMessages success size=${responses.size}"
                )

                val mappedMessages =
                    responses.map { resp ->

                        Message(
                            id = resp.id?.toString()
                                ?: java.util.UUID
                                    .randomUUID()
                                    .toString(),

                            senderId = resp.senderUsername,

                            text = resp.content,

                            /*
                             * 你的 Message.timestamp 目前是 Long，
                             * 暂时使用当前时间。
                             *
                             * 后面如果需要按照服务器 createdAt 显示时间，
                             * 再单独转换。
                             */
                            timestamp = parseTimestamp(resp.createdAt),

                            isMe =
                                resp.senderUsername ==
                                        currentUsername,

                            type = when {
                                resp.type.equals("IMAGE", ignoreCase = true) -> MessageType.IMAGE
                                resp.type.equals("CALL", ignoreCase = true) -> MessageType.CALL
                                else -> MessageType.TEXT
                            },

                            mediaUrl = resp.mediaUrl,

                            recalled = resp.recalled,

                            serverId = resp.id
                        )
                    }

                LocalMessageStore.save(currentUsername, otherUserId, mappedMessages)

                _messages.value = mappedMessages

                _uiState.value =
                    ChatUiState.Success(
                        mappedMessages,
                        partnerAvatar
                    )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "getChatMessages failed",
                    e
                )

                if (_messages.value.isEmpty()) {
                    val cached = LocalMessageStore.load(currentUsername, otherUserId)
                    if (cached != null && cached.isNotEmpty()) {
                        _messages.value = cached
                        _uiState.value = ChatUiState.Success(cached, partnerAvatar)
                    } else {
                        _uiState.value =
                            ChatUiState.Error(
                                "网络错误，请检查网络连接"
                            )
                    }
                }
            } finally {
                isFetching = false
            }
        }
    }

    /**
     * 发送消息
     */
    fun sendMessage(
        receiverId: String,
        content: String,
        currentUsername: String
    ) {

        /*
         * 空消息不发送
         */


        if (content.isBlank()) {
            return
        }

        if (receiverId.isBlank()) {

            Log.e(
                TAG,
                "sendMessage: receiverId 为空"
            )

            return
        }

        if (currentUsername.isBlank()) {

            Log.e(
                TAG,
                "sendMessage: currentUsername 为空"
            )

            return
        }

        viewModelScope.launch {

            Log.d(
                TAG,
                "准备发送消息"
            )

            Log.d(
                TAG,
                "sender=$currentUsername"
            )

            Log.d(
                TAG,
                "receiver=$receiverId"
            )

            Log.d(
                TAG,
                "content=$content"
            )

            try {

                /*
                 * 发送消息时不要再 createDirectChatRoom。
                 *
                 * 因为进入 ChatScreen 时 fetchMessages()
                 * 已经创建/获取聊天室。
                 */
                val response =
                    apiService.sendMessage(
                        ChatSendRequest(
                            senderUsername =
                                currentUsername,

                            receiverUsername =
                                receiverId,

                            content =
                                content.trim()
                        )
                    )

                Log.d(
                    TAG,
                    "sendMessage HTTP CODE=${response.code()}"
                )

                /*
                 * Retrofit Response<T>：
                 *
                 * 400 / 404 / 500 并不一定进入 catch。
                 * 所以这里必须判断 isSuccessful。
                 */
                if (response.isSuccessful) {

                    Log.d(
                        TAG,
                        "sendMessage SUCCESS"
                    )

                    Log.d(
                        TAG,
                        "response body=${response.body()}"
                    )

                    /*
                     * 确认服务器返回成功以后，
                     * 才把消息添加到 App 页面。
                     *
                     * 这样不会再发生：
                     *
                     * App 看起来发送成功，
                     * 实际服务器根本没收到。
                     */
                    val saved = response.body()?.data
                    val newMessage =
                        Message(
                            id = saved?.id?.toString()
                                ?: java.util.UUID.randomUUID().toString(),

                            senderId = currentUsername,

                            text = saved?.content ?: content.trim(),

                            timestamp = parseTimestamp(saved?.createdAt),

                            isMe = true,

                            serverId = saved?.id
                        )

                    val updatedMessages =
                        _messages.value +
                                newMessage

                    _messages.value =
                        updatedMessages

                    val currentAvatar =
                        (_uiState.value as? ChatUiState.Success)?.partnerAvatar

                    _uiState.value =
                        ChatUiState.Success(
                            updatedMessages,
                            currentAvatar
                        )

                } else {

                    val errorBody =
                        response
                            .errorBody()
                            ?.string()

                    Log.e(
                        TAG,
                        "sendMessage FAILED " +
                                "code=${response.code()} " +
                                "error=$errorBody"
                    )

                    // 发送失败：本地加一条失败消息（红色感叹号）
                    val failedMsg = Message(
                        id = java.util.UUID.randomUUID().toString(),
                        senderId = currentUsername,
                        text = content.trim(),
                        timestamp = System.currentTimeMillis(),
                        isMe = true,
                        failed = true
                    )
                    val updatedFailed = _messages.value + failedMsg
                    _messages.value = updatedFailed
                    val avatarFailed = (_uiState.value as? ChatUiState.Success)?.partnerAvatar
                    _uiState.value = ChatUiState.Success(updatedFailed, avatarFailed)

                    _sendError.value = extractErrorMessage(errorBody)
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "sendMessage EXCEPTION",
                    e
                )
            }
        }
    }

    fun sendImage(
        context: Context,
        imageUri: Uri,
        receiverId: String,
        currentUsername: String
    ) {
        if (receiverId.isBlank() || currentUsername.isBlank()) return

        viewModelScope.launch {
            try {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(imageUri) ?: "image/jpeg"
                val bytes = resolver.openInputStream(imageUri)?.use { it.readBytes() }
                    ?: throw IllegalArgumentException("无法读取图片")
                if (bytes.size > 10 * 1024 * 1024) {
                    throw IllegalArgumentException("图片不能超过 10MB")
                }

                val fileBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    "chat_${System.currentTimeMillis()}.jpg",
                    fileBody
                )
                val textType = "text/plain".toMediaTypeOrNull()
                val response = apiService.sendImage(
                    currentUsername.toRequestBody(textType),
                    receiverId.toRequestBody(textType),
                    filePart
                )

                if (response.isSuccessful) {
                    val saved = response.body()?.data ?: return@launch
                    val newMessage = Message(
                        id = saved.id?.toString() ?: java.util.UUID.randomUUID().toString(),
                        senderId = saved.senderUsername,
                        text = saved.content,
                        timestamp = parseTimestamp(saved.createdAt),
                        isMe = true,
                        type = MessageType.IMAGE,
                        mediaUrl = saved.mediaUrl,
                        serverId = saved.id
                    )
                    val updated = _messages.value + newMessage
                    _messages.value = updated
                    val avatar = (_uiState.value as? ChatUiState.Success)?.partnerAvatar
                    _uiState.value = ChatUiState.Success(updated, avatar)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "sendImage failed code=${response.code()} error=$errorBody")
                    _sendError.value = extractErrorMessage(errorBody)
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendImage exception", e)
            }
        }
    }

    /**
     * 撤回消息（仅自己发的、2分钟内）
     */
    fun recallMessage(message: Message, currentUsername: String) {
        val serverId = message.serverId ?: return
        viewModelScope.launch {
            try {
                val resp = apiService.recallMessage(MessageActionRequest(serverId, currentUsername))
                if (resp.isSuccessful) {
                    _messages.value = _messages.value.map {
                        if (it.id == message.id) it.copy(recalled = true, text = "", mediaUrl = null) else it
                    }
                    syncSuccessState()
                } else {
                    _sendError.value = extractErrorMessage(resp.errorBody()?.string())
                }
            } catch (e: Exception) {
                Log.e(TAG, "recallMessage exception", e)
                _sendError.value = "撤回失败"
            }
        }
    }

    /**
     * 删除消息（仅自己这边消失，对方仍可见）
     */
    fun deleteMessage(message: Message, currentUsername: String) {
        val serverId = message.serverId ?: return
        viewModelScope.launch {
            try {
                val resp = apiService.deleteMessage(MessageActionRequest(serverId, currentUsername))
                if (resp.isSuccessful) {
                    _messages.value = _messages.value.filter { it.id != message.id }
                    syncSuccessState()
                } else {
                    _sendError.value = extractErrorMessage(resp.errorBody()?.string())
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteMessage exception", e)
                _sendError.value = "删除失败"
            }
        }
    }

    /**
     * 标记会话已读（进入聊天页时调用，清除未读红点）
     */
    fun markRead(currentUsername: String, partner: String) {
        if (currentUsername.isBlank() || partner.isBlank()) return
        viewModelScope.launch {
            try {
                apiService.markRead(ReadRequest(currentUsername, partner))
            } catch (e: Exception) {
                Log.e(TAG, "markRead failed", e)
            }
        }
    }

    private fun syncSuccessState() {
        val avatar = (_uiState.value as? ChatUiState.Success)?.partnerAvatar
        _uiState.value = ChatUiState.Success(_messages.value, avatar)
    }

    /**
     * 清除消息（仅本地移除，不调用后端，释放本地显示压力）
     */
    fun clearMessageLocally(message: Message) {
        _messages.value = _messages.value.filter { it.id != message.id }
        syncSuccessState()
    }

    private fun parseTimestamp(createdAt: String?): Long =
        try {
            createdAt?.let(Instant::parse)?.toEpochMilli() ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }

    fun fetchRelationStatus(currentUsername: String, other: String) {
        if (currentUsername.isBlank() || other.isBlank()) return
        viewModelScope.launch {
            try {
                _relationStatus.value = apiService.relationStatus(currentUsername, other)
            } catch (e: Exception) {
                Log.e(TAG, "relationStatus failed", e)
            }
        }
    }

    fun requestFriend(currentUsername: String, friendUsername: String, message: String? = null) {
        if (currentUsername.isBlank() || friendUsername.isBlank()) return
        viewModelScope.launch {
            try {
                val resp = apiService.requestFriend(FriendRequest(currentUsername, friendUsername, message))
                val msg = resp.body()?.get("message")?.toString() ?: "好友申请已发送"
                _sendError.value = msg
            } catch (e: Exception) {
                Log.e(TAG, "requestFriend failed", e)
                _sendError.value = "好友申请发送失败"
            }
        }
    }

    fun clearSendError() {
        _sendError.value = null
    }

    private fun extractErrorMessage(body: String?): String {
        if (body.isNullOrBlank()) return "发送失败"
        return try {
            Regex("\"message\"\\s*:\\s*\"([^\"]*)\"").find(body)
                ?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
                ?: "发送失败"
        } catch (_: Exception) {
            "发送失败"
        }
    }
}