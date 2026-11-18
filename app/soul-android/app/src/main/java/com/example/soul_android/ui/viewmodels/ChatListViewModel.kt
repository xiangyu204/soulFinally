package com.example.soul_android.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soul_android.data.api.SoulApiService
import com.example.soul_android.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChatListUiState {

    object Loading : ChatListUiState()

    data class Success(
        val rooms: List<User>
    ) : ChatListUiState()

    data class Error(
        val message: String
    ) : ChatListUiState()
}

class ChatListViewModel(
    private val apiService: SoulApiService =
        SoulApiService.create()
) : ViewModel() {

    companion object {
        private const val TAG = "CHAT_LIST_DEBUG"
    }

    private val _uiState =
        MutableStateFlow<ChatListUiState>(
            ChatListUiState.Loading
        )

    val uiState: StateFlow<ChatListUiState> =
        _uiState.asStateFlow()

    /**
     * 获取当前用户的聊天室
     */
    fun fetchRooms(
        username: String
    ) {



        if (username.isBlank()) {

            Log.e(
                TAG,
                "fetchRooms: username 为空"
            )

            _uiState.value =
                ChatListUiState.Error(
                    "当前登录用户为空"
                )

            return
        }

        viewModelScope.launch {

            if (_uiState.value !is ChatListUiState.Success) {
                _uiState.value =
                    ChatListUiState.Loading
            }

            try {

                Log.d(
                    TAG,
                    "fetchRooms username=$username"
                )

                val roomMaps =
                    apiService.getChatRooms(
                        username
                    )

                Log.d(
                    TAG,
                    "服务器返回 rooms=$roomMaps"
                )

                val backendUsers =
                    roomMaps.mapNotNull { map ->

                        val partnerUsername =
                            map["partnerUsername"]
                                    as? String

                        /*
                         * 没有聊天对象用户名，
                         * 这一条数据直接跳过。
                         */
                        if (
                            partnerUsername
                                .isNullOrBlank()
                        ) {

                            Log.w(
                                TAG,
                                "发现无效聊天室数据: $map"
                            )

                            return@mapNotNull null
                        }

                        val partnerName =
                            map["partnerName"]
                                    as? String
                                ?: partnerUsername

                        val lastMsg =
                            map["lastMessage"]
                                    as? String
                                ?: ""

                        val lastType =
                            map["lastMessageType"]
                                    as? String
                                ?: "TEXT"

                        val avatarUrl =
                            map["avatar"]
                                    as? String

                        val lastTime =
                            map["lastMessageTime"]
                                    as? String

                        val lastMedia =
                            map["lastMessageMedia"]
                                    as? String
                                ?: ""

                        User(
                            /*
                             * 当前聊天 API 是按用户名：
                             *
                             * me + partner
                             *
                             * 所以这里使用 partnerUsername
                             * 作为 id 是可以的。
                             */
                            id = partnerUsername,

                            name = partnerName,

                            bio = lastMsg,

                            languages =
                                emptyList(),

                            teachSkills =
                                emptyList(),

                            learnSkills =
                                emptyList(),

                            matchRate = 90,

                            isOnline = true,

                            lastMessageType = lastType,

                            avatar = avatarUrl,

                            lastMessageTime = lastTime,

                            lastMessageMedia = lastMedia
                        )
                    }
                        /*
                         * 防止同一个聊天对象重复出现
                         */
                        .distinctBy {
                            it.id
                        }

                Log.d(
                    TAG,
                    "最终聊天室数量=${backendUsers.size}"
                )

                val unreadMap = try {
                    apiService.getUnread(username)
                        .associate { m ->
                            (m["partnerUsername"] as? String ?: "") to
                                    ((m["unreadCount"] as? Number)?.toInt() ?: 0)
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "getUnread failed", e)
                    emptyMap()
                }

                val roomsWithUnread = backendUsers.map { u ->
                    u.copy(unreadCount = unreadMap[u.id] ?: 0)
                }

                _uiState.value =
                    ChatListUiState.Success(
                        roomsWithUnread
                    )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "fetchRooms FAILED",
                    e
                )

                /*
                 * 不再使用 DummyData 假装成功。
                 */
                _uiState.value =
                    ChatListUiState.Error(
                        "获取聊天列表失败：" +
                                (e.message
                                    ?: "未知错误")
                    )
            }
        }
    }
}