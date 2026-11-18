package com.example.soul_android.call

import android.content.Context
import android.util.Log
import com.example.soul_android.data.api.RoomCreateRequest
import com.example.soul_android.data.api.RoomInviteRequest
import com.example.soul_android.data.api.RoomMessageRequest
import com.example.soul_android.data.api.RoomMessageResponse
import com.example.soul_android.data.api.RoomParticipant
import com.example.soul_android.data.api.RoomRespondRequest
import com.example.soul_android.data.api.RoomStatusResponse
import com.example.soul_android.data.api.SoulApiService
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 语聊房管理器（多人语音 + 文字）。
 * 单一实例，贯穿整个房间生命周期。
 */
object RoomManager {

    private const val TAG = "ROOM_MANAGER"

    data class RoomState(
        val roomId: String? = null,
        val roomName: String? = null,
        val owner: String? = null,
        val channelName: String? = null,
        val participants: List<String> = emptyList(),
        val participantInfos: List<RoomParticipant> = emptyList(),
        val muted: Boolean = false,
        val speakerMuted: Boolean = false
    )

    private val api = SoulApiService.create()

    private val _state = MutableStateFlow(RoomState())
    val state: StateFlow<RoomState> = _state.asStateFlow()

    private val _messages = MutableStateFlow<List<RoomMessageResponse>>(emptyList())
    val messages: StateFlow<List<RoomMessageResponse>> = _messages.asStateFlow()

    private val _incomingInvites = MutableStateFlow<List<RoomStatusResponse>>(emptyList())
    val incomingInvites: StateFlow<List<RoomStatusResponse>> = _incomingInvites.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var engine: RtcEngine? = null
    private var appContext: Context? = null
    private var scope: CoroutineScope? = null
    private var messageJob: Job? = null
    private var inviteJob: Job? = null

    private var me = ""

    fun start(username: String, scope: CoroutineScope, context: Context) {
        this.me = username
        this.scope = scope
        this.appContext = context.applicationContext
        startIncomingPolling()
    }

    fun stop() {
        messageJob?.cancel()
        inviteJob?.cancel()
        leaveVoiceChannel()
        _state.value = RoomState()
    }

    // =====================================================
    // 创建 / 加入房间
    // =====================================================

    fun createRoom(roomName: String, onSuccess: (RoomStatusResponse) -> Unit = {}) {
        val s = scope ?: return
        s.launch {
            try {
                val resp = api.createRoom(RoomCreateRequest(me, roomName))
                applyRoom(resp)
                joinVoiceChannel(resp.channelName ?: "")
                startMessagePolling(resp.roomId ?: "")
                onSuccess(resp)
            } catch (e: Exception) {
                Log.e(TAG, "createRoom failed", e)
                _error.value = "创建房间失败：${e.message}"
            }
        }
    }

    fun respondToInvite(room: RoomStatusResponse, accept: Boolean) {
        val s = scope ?: return
        val roomId = room.roomId ?: return
        s.launch {
            try {
                val resp = api.respondToInvite(RoomRespondRequest(roomId, me, accept))
                if (accept) {
                    applyRoom(resp)
                    joinVoiceChannel(resp.channelName ?: "")
                    startMessagePolling(roomId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "respondToInvite failed", e)
                _error.value = "加入房间失败：${e.message}"
            }
        }
    }

    fun leaveRoom() {
        val s = scope ?: return
        val roomId = _state.value.roomId ?: return
        s.launch {
            try {
                api.leaveRoom(RoomRespondRequest(roomId, me, false))
            } catch (e: Exception) {
                Log.e(TAG, "leaveRoom failed", e)
            }
            messageJob?.cancel()
            leaveVoiceChannel()
            _state.value = RoomState()
            _messages.value = emptyList()
        }
    }

    fun inviteFriend(friendUsername: String) {
        val s = scope ?: return
        val roomId = _state.value.roomId ?: return
        s.launch {
            try {
                val resp = api.inviteToRoom(RoomInviteRequest(roomId, me, friendUsername))
                _state.value = _state.value.copy(participants = resp.participants)
            } catch (e: Exception) {
                Log.e(TAG, "inviteFriend failed", e)
                _error.value = "邀请失败：${e.message}"
            }
        }
    }

    fun sendMessage(content: String) {
        val s = scope ?: return
        val roomId = _state.value.roomId ?: return
        if (content.isBlank()) return
        s.launch {
            try {
                api.sendRoomMessage(RoomMessageRequest(roomId, me, content.trim()))
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage failed", e)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun applyRoom(resp: RoomStatusResponse) {
        _state.value = RoomState(
            roomId = resp.roomId,
            roomName = resp.roomName,
            owner = resp.owner,
            channelName = resp.channelName,
            participants = resp.participants,
            participantInfos = resp.participantInfos,
            muted = false
        )
    }

    // =====================================================
    // 轮询：文字消息 + 邀请
    // =====================================================

    private fun startMessagePolling(roomId: String) {
        val s = scope ?: return
        messageJob?.cancel()
        messageJob = s.launch {
            while (isActive) {
                try {
                    _messages.value = api.roomMessages(roomId)
                    // 实时刷新参与人（有人进出时即时更新）
                    val status = api.roomStatus(roomId)
                    _state.value = _state.value.copy(
                        participants = status.participants,
                        participantInfos = status.participantInfos
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "message poll failed", e)
                }
                delay(2000)
            }
        }
    }

    private fun startIncomingPolling() {
        val s = scope ?: return
        inviteJob?.cancel()
        inviteJob = s.launch {
            while (isActive) {
                try {
                    if (_state.value.roomId == null) {
                        _incomingInvites.value = api.incomingRooms(me)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "invite poll failed", e)
                }
                delay(3000)
            }
        }
    }

    // =====================================================
    // 声网语音
    // =====================================================

    private fun ensureEngine(context: Context): RtcEngine? {
        engine?.let { return it }
        if (CallConfig.AGORA_APP_ID.isBlank()) return null
        return try {
            appContext = context.applicationContext
            val config = RtcEngineConfig()
            config.mContext = context.applicationContext
            config.mAppId = CallConfig.AGORA_APP_ID
            val e = RtcEngine.create(config)
            e.enableAudio()
            engine = e
            e
        } catch (ex: Exception) {
            Log.e(TAG, "ensureEngine failed", ex)
            null
        }
    }

    private suspend fun joinVoiceChannel(channel: String) {
        val context = appContext ?: return
        val e = ensureEngine(context) ?: return
        val token = try {
            api.getCallToken(channel, 0).token ?: ""
        } catch (ex: Exception) {
            Log.e(TAG, "getCallToken failed", ex)
            ""
        }
        val options = ChannelMediaOptions()
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        options.autoSubscribeAudio = true
        options.publishMicrophoneTrack = true
        val ret = e.joinChannel(token, channel, 0, options)
        Log.d(TAG, "joinVoiceChannel ret=$ret channel=$channel")
    }

    fun toggleMute() {
        val e = engine ?: return
        val next = !_state.value.muted
        e.muteLocalAudioStream(next)
        _state.value = _state.value.copy(muted = next)
    }

    fun toggleSpeaker() {
        val e = engine ?: return
        val next = !_state.value.speakerMuted
        e.muteAllRemoteAudioStreams(next)
        _state.value = _state.value.copy(speakerMuted = next)
    }

    private fun leaveVoiceChannel() {
        try {
            engine?.leaveChannel()
        } catch (e: Exception) {
            Log.e(TAG, "leaveVoiceChannel failed", e)
        }
    }
}
