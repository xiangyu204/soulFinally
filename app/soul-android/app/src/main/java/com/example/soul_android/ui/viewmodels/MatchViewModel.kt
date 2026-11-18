package com.example.soul_android.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soul_android.data.api.BlockRequest
import com.example.soul_android.data.api.ChatSendRequest
import com.example.soul_android.data.api.FriendRequest
import com.example.soul_android.data.api.FriendRequestResponse
import com.example.soul_android.data.api.FriendRespondRequest
import com.example.soul_android.data.api.MatchUserResponse
import com.example.soul_android.data.api.RemarkRequest
import com.example.soul_android.data.api.SoulApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class MatchUiState {

    object Loading : MatchUiState()


    data class Success(

        val recommendedUsers: List<MatchUserResponse>,

        val myMatches: List<MatchUserResponse>,

        val friends: List<MatchUserResponse> = emptyList(),

        val blocks: List<MatchUserResponse> = emptyList()

    ) : MatchUiState()


    data class Error(
        val message: String
    ) : MatchUiState()
}


class MatchViewModel(

    private val apiService:
    SoulApiService =
        SoulApiService.create()

) : ViewModel() {


    companion object {

        private const val TAG =
            "MATCH_DEBUG"
    }


    private val _uiState =
        MutableStateFlow<MatchUiState>(
            MatchUiState.Loading
        )


    val uiState =
        _uiState.asStateFlow()


    private val _friendRequests =
        MutableStateFlow<List<FriendRequestResponse>>(emptyList())

    val friendRequests =
        _friendRequests.asStateFlow()


    // =====================================================
    // 获取推荐用户 + 我的匹配
    // =====================================================

    fun findMatches(

        username: String,

        have: String? = null,

        want: String? = null,

        timeSlot: String? = null,

        skillWantLevel: String? = null,

        skillOfferLevel: String? = null

    ) {




        if (username.isBlank()) {

            _uiState.value =
                MatchUiState.Error(
                    "当前登录用户为空"
                )

            return
        }


        viewModelScope.launch {


            _uiState.value =
                MatchUiState.Loading


            try {


                Log.d(
                    TAG,
                    "findMatches username=$username"
                )


                val recommended =
                    apiService.getMatches(

                        username =
                            username,

                        haveSkill =
                            have,

                        wantSkill =
                            want,

                        timeSlot =
                            timeSlot,

                        skillWantLevel =
                            skillWantLevel,

                        skillOfferLevel =
                            skillOfferLevel,

                        limit =
                            12
                    )


                val matchedUsers =
                    apiService.getMyMatches(

                        username =
                            username
                    )


                val friends =
                    apiService.listFriends(
                        username = username
                    )

                val blocks =
                    apiService.listBlocks(
                        username = username
                    )


                Log.d(
                    TAG,
                    "recommended=${recommended.size}, " +
                            "myMatches=${matchedUsers.size}, " +
                            "friends=${friends.size}, blocks=${blocks.size}"
                )


                _uiState.value =
                    MatchUiState.Success(

                        recommendedUsers =
                            recommended,

                        myMatches =
                            matchedUsers,

                        friends =
                            friends,

                        blocks =
                            blocks
                    )


            } catch (e: Exception) {


                Log.e(
                    TAG,
                    "findMatches FAILED",
                    e
                )


                _uiState.value =
                    MatchUiState.Error(

                        e.localizedMessage
                            ?: "匹配数据获取失败"
                    )
            }
        }
    }


    // =====================================================
    // 全库搜索（名字 / 用户名 / 技能模糊匹配）
    // =====================================================

    private val _searchResults =
        MutableStateFlow<List<MatchUserResponse>>(emptyList())

    val searchResults = _searchResults.asStateFlow()

    fun searchUsers(query: String, username: String) {

        if (username.isBlank()) return

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _searchResults.value = apiService.searchUsers(
                    username = username,
                    q = query
                )
            } catch (e: Exception) {
                Log.e(TAG, "searchUsers failed", e)
                _searchResults.value = emptyList()
            }
        }
    }


    // =====================================================
    // 真正建立匹配
    // =====================================================

    fun acceptMatch(

        currentUsername: String,

        targetUsername: String,

        onSuccess: () -> Unit = {},

        onError: (String) -> Unit = {}

    ) {




        if (
            currentUsername.isBlank()
            || targetUsername.isBlank()
        ) {

            onError(
                "用户名不能为空"
            )

            return
        }


        if (
            currentUsername ==
            targetUsername
        ) {

            onError(
                "不能和自己匹配"
            )

            return
        }


        viewModelScope.launch {


            try {


                Log.d(
                    TAG,
                    "acceptMatch: " +
                            "$currentUsername -> $targetUsername"
                )


                /*
                 * 当前项目：
                 *
                 * ChatRoom 就是匹配关系。
                 *
                 * 创建 ChatRoom =
                 * 真正匹配成功。
                 */
                val response =
                    apiService
                        .createDirectChatRoom(

                            ChatSendRequest(

                                senderUsername =
                                    currentUsername,

                                receiverUsername =
                                    targetUsername,

                                content =
                                    ""
                            )
                        )


                /*
                 * HTTP 层失败
                 */
                if (!response.isSuccessful) {


                    val error =
                        response
                            .errorBody()
                            ?.string()
                            ?: "HTTP ${response.code()}"


                    Log.e(
                        TAG,
                        "acceptMatch HTTP FAILED: $error"
                    )


                    onError(
                        error
                    )

                    return@launch
                }


                /*
                 * Spring Boot 现在业务错误也可能返回 HTTP 200。
                 *
                 * 所以必须继续检查：
                 *
                 * success=true/false
                 */
                val body =
                    response.body()


                val success =
                    body
                        ?.get("success")
                            as? Boolean
                        ?: false


                if (!success) {


                    val message =
                        body
                            ?.get("message")
                            ?.toString()
                            ?: "匹配失败"


                    Log.e(
                        TAG,
                        "acceptMatch business FAILED: $message"
                    )


                    onError(
                        message
                    )

                    return@launch
                }


                Log.d(
                    TAG,
                    "MATCH SUCCESS: $body"
                )


                /*
                 * 匹配成功后：
                 *
                 * 重新获取我的匹配，
                 * 同时把这个用户从推荐列表删除。
                 */
                refreshAfterMatch(

                    username =
                        currentUsername,

                    matchedUsername =
                        targetUsername
                )


                onSuccess()


            } catch (e: Exception) {


                Log.e(
                    TAG,
                    "acceptMatch EXCEPTION",
                    e
                )


                onError(

                    e.localizedMessage
                        ?: "匹配失败"
                )
            }
        }
    }


    // =====================================================
    // 当前项目没有 Reject 数据表
    //
    // 所以“跳过”只在当前推荐列表隐藏
    // =====================================================

    fun rejectLocally(
        targetUsername: String
    ) {


        val current =
            _uiState.value


        if (
            current
                    !is MatchUiState.Success
        ) {

            return
        }


        _uiState.value =
            current.copy(

                recommendedUsers =
                    current
                        .recommendedUsers
                        .filter {

                            it.username !=
                                    targetUsername
                        }
            )
    }


    // =====================================================
    // 匹配成功后的刷新
    // =====================================================

    private suspend fun refreshAfterMatch(

        username: String,

        matchedUsername: String

    ) {


        val current =
            _uiState.value


        try {


            val matchedUsers =
                apiService.getMyMatches(

                    username =
                        username
                )

            val friends =
                apiService.listFriends(username = username)

            val blocks =
                apiService.listBlocks(username = username)


            if (
                current
                        is MatchUiState.Success
            ) {


                _uiState.value =
                    MatchUiState.Success(

                        recommendedUsers =
                            current
                                .recommendedUsers
                                .filter {

                                    it.username !=
                                            matchedUsername
                                },

                        myMatches =
                            matchedUsers,

                        friends = friends,

                        blocks = blocks
                    )


            } else {


                val recommended =
                    apiService.getMatches(

                        username =
                            username
                    )


                _uiState.value =
                    MatchUiState.Success(

                        recommendedUsers =
                            recommended,

                        myMatches =
                            matchedUsers,

                        friends = friends,

                        blocks = blocks
                    )
            }


        } catch (e: Exception) {


            Log.e(
                TAG,
                "refreshAfterMatch FAILED",
                e
            )
        }
    }


    // =====================================================
    // 好友 / 黑名单操作
    // =====================================================

    fun removeFriend(username: String, friendUsername: String) {
        viewModelScope.launch {
            try {
                apiService.removeFriend(FriendRequest(username, friendUsername))
                reload(username)
            } catch (e: Exception) {
                Log.e(TAG, "removeFriend failed", e)
            }
        }
    }

    fun blockUser(username: String, blockUsername: String) {
        viewModelScope.launch {
            try {
                apiService.addBlock(BlockRequest(username, blockUsername))
                reload(username)
            } catch (e: Exception) {
                Log.e(TAG, "blockUser failed", e)
            }
        }
    }

    fun unblockUser(username: String, blockUsername: String) {
        viewModelScope.launch {
            try {
                apiService.removeBlock(BlockRequest(username, blockUsername))
                reload(username)
            } catch (e: Exception) {
                Log.e(TAG, "unblockUser failed", e)
            }
        }
    }

    private suspend fun reload(username: String) {
        try {
            val recommended = apiService.getMatches(username = username)
            val matched = apiService.getMyMatches(username = username)
            val friends = apiService.listFriends(username = username)
            val blocks = apiService.listBlocks(username = username)
            _uiState.value = MatchUiState.Success(
                recommendedUsers = recommended,
                myMatches = matched,
                friends = friends,
                blocks = blocks
            )
        } catch (e: Exception) {
            Log.e(TAG, "reload failed", e)
        }
    }

    fun loadFriendRequests(username: String) {
        if (username.isBlank()) return
        viewModelScope.launch {
            try {
                _friendRequests.value = apiService.listFriendRequests(username)
            } catch (e: Exception) {
                Log.e(TAG, "loadFriendRequests failed", e)
            }
        }
    }

    fun respondFriendRequest(requestId: Long, accept: Boolean, username: String) {
        viewModelScope.launch {
            try {
                apiService.respondFriendRequest(FriendRespondRequest(requestId, username, accept))
                reload(username)
                loadFriendRequests(username)
            } catch (e: Exception) {
                Log.e(TAG, "respondFriendRequest failed", e)
            }
        }
    }

    fun setRemark(owner: String, target: String, remark: String) {
        if (owner.isBlank() || target.isBlank()) return
        viewModelScope.launch {
            try {
                apiService.setRemark(RemarkRequest(owner, target, remark))
                reload(owner)
            } catch (e: Exception) {
                Log.e(TAG, "setRemark failed", e)
            }
        }
    }
}