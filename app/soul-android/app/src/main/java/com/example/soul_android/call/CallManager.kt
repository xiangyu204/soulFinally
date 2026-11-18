package com.example.soul_android.call

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import com.example.soul_android.data.api.CallAnswerRequest
import com.example.soul_android.data.api.CallEndRequest
import com.example.soul_android.data.api.CallStartRequest
import com.example.soul_android.data.api.CallStatusResponse
import com.example.soul_android.data.api.SoulApiService
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 语音/视频通话管理器（声网 Agora + 自建信令）。
 *
 * 单一实例，贯穿整个通话生命周期：
 * 空闲 -> 呼出/来电 -> 接通(Active) -> 结束。
 */
object CallManager {

    private const val TAG = "CALL_MANAGER"

    enum class Phase {
        IDLE, OUTGOING, INCOMING, ACTIVE, ENDED
    }

    data class State(
        val phase: Phase = Phase.IDLE,
        val callId: String? = null,
        val peerUsername: String? = null,
        val peerName: String? = null,
        val channelName: String? = null,
        val callType: String = "VOICE",
        val muted: Boolean = false,
        val cameraOff: Boolean = false,
        val remoteMuted: Boolean = false,
        val remoteJoined: Boolean = false,
        val remoteVideoOff: Boolean = false,
        val myName: String = "",
        val startTimeMs: Long? = null,
        val endReason: String? = null
    )

    private val api = SoulApiService.create()

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid: StateFlow<Int?> = _remoteUid.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var engine: RtcEngine? = null
    private var appContext: Context? = null

    private var scope: CoroutineScope? = null
    private var incomingJob: Job? = null
    private var statusJob: Job? = null

    private var me = ""
    private var myName = ""

    private val eventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.d(TAG, "onJoinChannelSuccess channel=$channel uid=$uid")
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "onUserJoined uid=$uid")
            _remoteUid.value = uid
            update { it.copy(remoteJoined = true) }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "onUserOffline uid=$uid reason=$reason")
            _remoteUid.value = null
            update { it.copy(remoteJoined = false, remoteVideoOff = false) }
        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            Log.d(TAG, "onRemoteVideoStateChanged uid=$uid state=$state reason=$reason")
            // 对方关闭摄像头时 state 为 REMOTE_VIDEO_STATE_STOPPED
            update {
                it.copy(remoteVideoOff = state == Constants.REMOTE_VIDEO_STATE_STOPPED)
            }
        }

        override fun onLeaveChannel(stats: IRtcEngineEventHandler.RtcStats) {
            Log.d(TAG, "onLeaveChannel")
        }

        override fun onError(err: Int) {
            Log.e(TAG, "onError err=$err")
            _error.value = "通话引擎错误 ($err)"
        }
    }

    private fun update(transform: (State) -> State) {
        _state.value = transform(_state.value)
    }

    // =====================================================
    // 呼入监听
    // =====================================================

    /** 登录后调用，定时轮询是否有来电 */
    fun startIncomingPolling(scope: CoroutineScope, username: String) {
        this.scope = scope
        this.me = username
        _state.value = _state.value.copy(myName = username)
        if (incomingJob?.isActive == true) return
        incomingJob = scope.launch {
            while (isActive) {
                try {
                    val currentUser = me
                    when (_state.value.phase) {
                        Phase.IDLE -> {
                            if (currentUser.isNotBlank()) {
                                val resp = api.incomingCall(currentUser)
                                if (resp.status == "RINGING") {
                                    applyIncoming(resp)
                                }
                            }
                        }
                        Phase.INCOMING, Phase.ACTIVE -> {
                            val callId = _state.value.callId
                            if (callId != null) {
                                val resp = api.callStatus(callId)
                                if (resp.status == "ENDED" || resp.status == "REJECTED") {
                                    val reason = if (resp.status == "REJECTED") "对方已拒绝" else "对方已挂断"
                                    update { it.copy(phase = Phase.ENDED, endReason = reason) }
                                    delay(1500)
                                    resetToIdle()
                                }
                            }
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "incoming poll failed", e)
                }
                delay(2000)
            }
        }
    }

    private fun applyIncoming(resp: CallStatusResponse) {
        if (_state.value.phase != Phase.IDLE) return
        update {
            it.copy(
                phase = Phase.INCOMING,
                callId = resp.callId,
                peerUsername = resp.caller,
                peerName = resp.callerName ?: resp.caller,
                channelName = resp.channelName,
                callType = resp.callType ?: "VOICE"
            )
        }
        _error.value = null
    }

    fun stopPolling() {
        incomingJob?.cancel()
        statusJob?.cancel()
    }

    // =====================================================
    // 呼出
    // =====================================================

    fun startCall(
        context: Context,
        peerUsername: String,
        peerName: String,
        callType: String
    ) {
        if (_state.value.phase != Phase.IDLE) return
        val scope = this.scope ?: return
        this.myName = me

        update {
            it.copy(
                phase = Phase.OUTGOING,
                peerUsername = peerUsername,
                peerName = peerName,
                callType = callType,
                startTimeMs = System.currentTimeMillis()
            )
        }
        _error.value = null

        scope.launch {
            try {
                val resp = api.startCall(
                    CallStartRequest(
                        caller = me,
                        callee = peerUsername,
                        callType = callType
                    )
                )
                if (resp.callId != null) {
                    update {
                        it.copy(
                            callId = resp.callId,
                            channelName = resp.channelName
                        )
                    }
                    pollOutgoingStatus(resp.callId, context, peerUsername, peerName, callType)
                } else {
                    resetToIdle()
                    _error.value = "发起通话失败"
                }
            } catch (e: Exception) {
                Log.e(TAG, "startCall failed", e)
                resetToIdle()
                _error.value = "发起通话失败：${e.message}"
            }
        }
    }

    private fun pollOutgoingStatus(
        callId: String,
        context: Context,
        peerUsername: String,
        peerName: String,
        callType: String
    ) {
        val scope = this.scope ?: return
        statusJob?.cancel()
        statusJob = scope.launch {
            var joined = false
            while (isActive) {
                try {
                    val resp = api.callStatus(callId)
                    when (resp.status) {
                        "ACTIVE" -> {
                            if (!joined) {
                                joined = true
                                joinChannel(context, resp.channelName ?: "", peerUsername, peerName, callType)
                                update {
                                    it.copy(
                                        phase = Phase.ACTIVE,
                                        channelName = resp.channelName,
                                        startTimeMs = System.currentTimeMillis()
                                    )
                                }
                            }
                        }
                        "REJECTED", "ENDED" -> {
                            val reason = if (resp.status == "REJECTED") "对方已拒绝" else "对方已挂断"
                            update { it.copy(phase = Phase.ENDED, endReason = reason) }
                            delay(1500)
                            resetToIdle()
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "status poll failed", e)
                }
                delay(1500)
            }
        }
    }

    // =====================================================
    // 来电应答
    // =====================================================

    fun acceptCall(context: Context) {
        val s = _state.value
        if (s.phase != Phase.INCOMING || s.callId == null) return
        val scope = this.scope ?: return
        val callId = s.callId
        val peer = s.peerUsername ?: ""
        val channel = s.channelName ?: ""
        val callType = s.callType

        scope.launch {
            try {
                api.answerCall(
                    CallAnswerRequest(callId = callId, callee = me, accept = true)
                )
                joinChannel(context, channel, peer, peer, callType)
                update {
                    it.copy(
                        phase = Phase.ACTIVE,
                        startTimeMs = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "acceptCall failed", e)
                resetToIdle()
                _error.value = "接听失败：${e.message}"
            }
        }
    }

    fun rejectCall() {
        val s = _state.value
        if (s.callId == null) return
        val callId = s.callId
        val scope = this.scope ?: return
        scope.launch {
            try {
                api.answerCall(
                    CallAnswerRequest(callId = callId, callee = me, accept = false)
                )
            } catch (e: Exception) {
                Log.e(TAG, "rejectCall failed", e)
            }
            resetToIdle()
        }
    }

    fun cancelOutgoing() {
        val s = _state.value
        val callId = s.callId
        val scope = this.scope
        if (callId != null && scope != null) {
            scope.launch {
                try {
                    api.endCall(CallEndRequest(callId = callId, caller = me))
                } catch (e: Exception) {
                    Log.e(TAG, "cancelOutgoing failed", e)
                }
            }
        }
        resetToIdle()
    }

    fun endCall() {
        val s = _state.value
        val callId = s.callId
        val scope = this.scope
        if (callId != null && scope != null) {
            scope.launch {
                try {
                    api.endCall(CallEndRequest(callId = callId, caller = me))
                } catch (e: Exception) {
                    Log.e(TAG, "endCall failed", e)
                }
            }
        }
        leaveChannel()
        resetToIdle()
    }

    // =====================================================
    // Agora 引擎
    // =====================================================

    private fun ensureEngine(context: Context): RtcEngine? {
        engine?.let { return it }
        if (CallConfig.AGORA_APP_ID.isBlank()) {
            _error.value = "未配置声网 AppId（见 CallConfig.kt）"
            return null
        }
        return try {
            appContext = context.applicationContext
            val config = RtcEngineConfig()
            config.mContext = context.applicationContext
            config.mAppId = CallConfig.AGORA_APP_ID
            config.mEventHandler = eventHandler
            val e = RtcEngine.create(config)
            e.enableAudio()
            engine = e
            e
        } catch (ex: Exception) {
            Log.e(TAG, "ensureEngine failed", ex)
            _error.value = "通话引擎初始化失败：${ex.message}"
            null
        }
    }

    private suspend fun joinChannel(
        context: Context,
        channel: String,
        peerUsername: String,
        peerName: String,
        callType: String
    ) {
        val e = ensureEngine(context) ?: run {
            resetToIdle()
            return
        }
        update {
            it.copy(
                peerUsername = peerUsername,
                peerName = peerName,
                channelName = channel,
                callType = callType
            )
        }
        val isVideo = callType == "VIDEO"
        if (isVideo) {
            e.enableVideo()
        }
        val fullChannel = CallConfig.CHANNEL_PREFIX + channel

        // 从后端获取临时 Token（App 证书服务端生成，生产级安全）
        val token = try {
            api.getCallToken(fullChannel, 0).token ?: ""
        } catch (ex: Exception) {
            Log.e(TAG, "getCallToken failed", ex)
            ""
        }

        val options = ChannelMediaOptions()
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        options.autoSubscribeAudio = true
        options.autoSubscribeVideo = true
        options.publishMicrophoneTrack = true
        options.publishCameraTrack = isVideo
        val ret = e.joinChannel(token, fullChannel, 0, options)
        Log.d(TAG, "joinChannel ret=$ret channel=$fullChannel tokenLen=${token.length}")
    }

    fun setupLocalVideo(view: SurfaceView) {
        val e = engine ?: return
        e.startPreview()
        val canvas = VideoCanvas(view)
        canvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN
        e.setupLocalVideo(canvas)
    }

    fun setupRemoteVideo(view: SurfaceView) {
        val e = engine ?: return
        val uid = _remoteUid.value ?: return
        val canvas = VideoCanvas(view)
        canvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN
        canvas.uid = uid
        e.setupRemoteVideo(canvas)
    }

    fun toggleMute() {
        val e = engine ?: return
        val next = !_state.value.muted
        e.muteLocalAudioStream(next)
        update { it.copy(muted = next) }
    }

    fun toggleCamera() {
        val e = engine ?: return
        val next = !_state.value.cameraOff
        e.muteLocalVideoStream(next)
        update { it.copy(cameraOff = next) }
    }

    fun switchCamera() {
        engine?.switchCamera()
    }

    fun toggleRemoteMute() {
        val e = engine ?: return
        val uid = _remoteUid.value ?: return
        val next = !_state.value.remoteMuted
        e.muteRemoteAudioStream(uid, next)
        update { it.copy(remoteMuted = next) }
    }

    private fun leaveChannel() {
        try {
            engine?.leaveChannel()
        } catch (e: Exception) {
            Log.e(TAG, "leaveChannel failed", e)
        }
    }

    private fun resetToIdle() {
        statusJob?.cancel()
        leaveChannel()
        try {
            engine?.let { RtcEngine.destroy() }
        } catch (e: Exception) {
            Log.e(TAG, "destroy failed", e)
        }
        engine = null
        _remoteUid.value = null
        _state.value = State(phase = Phase.IDLE)
    }
}
