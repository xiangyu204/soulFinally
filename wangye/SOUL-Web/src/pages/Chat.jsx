import { IconPhone, IconVideo, IconStar, IconSparkle, IconClose } from '../ui/Icons'
import { useEffect, useMemo, useRef, useState } from 'react'
import client from '../api/client'
import { cachedTranslate } from '../api/translateCache'
import chatStrings from '../i18n/chatStrings'
import { toast } from '../ui/feedback'
import { useLocation } from 'react-router-dom'
import AgoraRTC from 'agora-rtc-sdk-ng'

const AI_PREFIX = '🤖'
const TIME_GAP = 300000

function CallIcon({ name, off }) {
  const common = {
    width: 26,
    height: 26,
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.6,
    strokeLinecap: 'round',
    strokeLinejoin: 'round'
  }
  if (name === 'mic') {
    return (
      <svg {...common}>
        <path d="M12 4a2.5 2.5 0 0 0-2.5 2.5v5a2.5 2.5 0 0 0 5 0v-5A2.5 2.5 0 0 0 12 4z" />
        <path d="M6 11.5a6 6 0 0 0 12 0" />
        <path d="M12 17.5V21" />
        {off && <path d="M4 4l16 16" />}
      </svg>
    )
  }
  if (name === 'phone' || name === 'video') {
    if (name === 'phone') {
      return (
        <svg {...common}>
          <path d="M6.5 4.5l3 1.2-.9 3-2.4.6a11 11 0 0 0 6.5 6.5l.6-2.4 3-.9 1.2 3c.2.6-.2 1.2-.8 1.3-6 1.3-13.5-6.2-12.2-12.2.1-.6.7-1 1.3-.8z" />
        </svg>
      )
    }
    return (
      <svg {...common}>
        <rect x="3" y="6.5" width="12" height="11" rx="3" />
        <path d="M15 10.5l6-3.2v9.4l-6-3.2" />
      </svg>
    )
  }
  if (name === 'camera') {
    return (
      <svg {...common}>
        <rect x="3" y="6.5" width="12" height="11" rx="3" />
        <path d="M15 10.5l6-3.2v9.4l-6-3.2" />
        {off && <path d="M4 4l16 16" />}
      </svg>
    )
  }
  return (
    <svg {...common}>
      <path d="M5 15.5c3.5 2.8 10.5 2.8 14 0" />
      <path d="M5 15.5l1.4-3.6 3.2.9-.6 2" />
      <path d="M19 15.5l-1.4-3.6-3.2.9.6 2" />
    </svg>
  )
}

function Chat({ text, lang }) {
  const L = chatStrings[lang] || chatStrings.zh
  const location = useLocation()
  const currentUser = useMemo(() => {
    try {
      return JSON.parse(
        localStorage.getItem('loginUser') || '{}'
      )
    } catch {
      return {}
    }
  }, [])
  const currentUsername = currentUser?.username || ''
  const currentLang = localStorage.getItem('lang') || 'ko'
  const [rooms, setRooms] = useState([])
  const [selectedRoom, setSelectedRoom] = useState(null)
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [isAiLoading, setIsAiLoading] = useState(false)
  const [
    translatedMessages,
    setTranslatedMessages
  ] = useState({})
  const [aiError, setAiError] = useState('')
  // 新增
  const [myRating, setMyRating] = useState(0)

  // 图片发送
  const fileInputRef = useRef(null)
  const [uploading, setUploading] = useState(false)

  // 通话
  const [callState, setCallState] = useState(null)
  const [incomingCall, setIncomingCall] = useState(null)
  const [callSeconds, setCallSeconds] = useState(0)
  const [muted, setMuted] = useState(false)
  const [cameraOff, setCameraOff] = useState(false)
  const agoraRef = useRef(null)
  const localTracksRef = useRef({ audio: null, video: null })
  const localVideoRef = useRef(null)
  const remoteVideoRef = useRef(null)
  const messagesEndRef = useRef(null)
  const scrollToBottom = () =>
    requestAnimationFrame(() =>
      messagesEndRef.current?.scrollIntoView({
        behavior: 'smooth'
      })
    )
  const detectLanguage = (text) => {
    if (!text) return 'en'
    if (/[\uac00-\ud7af]/.test(text))
      return 'ko'
    if (/[\u4e00-\u9fff]/.test(text))
      return 'zh'
    if (/[\u3040-\u30ff]/.test(text))
      return 'ja'
    if (/[\u0600-\u06FF]/.test(text))
      return 'ar'
    return 'en'
  }
  const translateText = async (
    content
  ) => {
    if (
      !content ||
      detectLanguage(content) === currentLang
    ) {return null}
    try {
      const out = await cachedTranslate(
        content,
        currentLang
      )
      if (!out) return null
      return out.trim() &&
        out !== content
        ? out
        : null
    } catch {return null}
  }
  const formatWeChatTime = (
    timeStr
  ) => {
    if (!timeStr) return ''
    try {
      const date =
        new Date(timeStr)
      if (isNaN(date.getTime()))
        return ''
      const now = new Date()
      let hours =
        date.getHours()
      const minutes =
        String(
          date.getMinutes()
        ).padStart(2, '0')
      const ampm =
        hours >= 12
          ? '오후'
          : '오전'
      hours = hours % 12 || 12
      const time =
        `${ampm} ${hours}:${minutes}`
      if (
        date.toDateString() ===
        now.toDateString()
      ) {return `오늘 ${time}`}
      const yesterday =
        new Date(now)
      yesterday.setDate(now.getDate() - 1)
      if (
        date.toDateString() ===
        yesterday.toDateString()
      ) {return `어제 ${time}`}
      if (
        date.getFullYear() ===
        now.getFullYear()
      ) {return `${date.getMonth() + 1}월 ${date.getDate()}일 ${time}`}
      return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일 ${time}`
    } catch {return ''}
  }
  const fetchRooms = async () => {
    try {
      const res =
        await client.get(
          '/api/chat/rooms',
          {
            params: {
              username:
                currentUsername
            }
          }
        )
      const data = res.data
      if (!Array.isArray(data)) {
        setRooms([])
        return
      }
      setRooms(data)
      if (
        !selectedRoom &&
        data.length
      ) {
        setSelectedRoom(data[0])
        scrollToBottom()
        return
      }
    } catch {setRooms([])}
  }
  // 房间列表轮询：对方发来消息后，无需刷新即可看到新会话
  useEffect(() => {
    if (!currentUsername) return

    const refresh = async () => {
      try {
        const res = await client.get('/api/chat/rooms', {
          params: { username: currentUsername }
        })
        if (Array.isArray(res.data)) {
          setRooms(res.data)
        }
      } catch (e) {
        /* 轮询失败静默重试 */
      }
    }

    const id = setInterval(refresh, 5000)
    window.addEventListener('focus', refresh)
    document.addEventListener('visibilitychange', refresh)

    return () => {
      clearInterval(id)
      window.removeEventListener('focus', refresh)
      document.removeEventListener('visibilitychange', refresh)
    }
  }, [currentUsername])

  const fetchMessages = async (scroll = false) => {
    if (!selectedRoom) return
    try {
      const res =
        await client.get(
          '/api/chat/messages',
          {
            params: {
              me:
                currentUsername,
              partner:
                selectedRoom.partnerUsername
            }
          }
        )
      const data = res.data
      if (
        !Array.isArray(data)
      ) return
      setMessages(data)
      if (scroll) {
        scrollToBottom()
      }
    } catch {}
  }
  // 评分：作业版统一写死 5 分（不走 /api/chat/rating）
  const fetchMyRating = async () => {
    if (!selectedRoom) return
    setMyRating(5)
  }
  const handleRating = async () => {
    setMyRating(5)
  }
  const autoTranslateMessages = async () => {
    const cache = {}
    for (const msg of messages) {
      if (
        msg.content?.startsWith(
          AI_PREFIX
        )
      ) continue
      if (
        translatedMessages[msg.id]
      ) {
        cache[msg.id] =
          translatedMessages[msg.id]
        continue
      }
      const translated =
        await translateText(
          msg.content
        )
      if (translated) {
        cache[msg.id] = translated
      }
    }
    setTranslatedMessages(
      cache
    )
  }
  const handleSendMessage = async () => {
    if (
      !newMessage.trim() ||
      !selectedRoom
    ) return
    try {
      const res =
        await client.post(
          '/api/chat/send',
          {
            senderUsername:
              currentUsername,
            receiverUsername:
              selectedRoom.partnerUsername,
            content:
              newMessage
          }
        )
      const data = res.data
      if (!data.success) {
        toast.error(data.message)
        return
      }
      setNewMessage('')
      setAiError('')
      fetchMessages(true)
      fetchRooms()
    } catch (err) {
      console.error(
        '发送消息失败:',
        err
      )
    }
  }

  // ===== 图片发送 =====
  const handlePickImage = () => {
    if (!selectedRoom) return
    fileInputRef.current?.click()
  }

  const handleImageSelected = async (e) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file || !selectedRoom) return
    if (!file.type.startsWith('image/')) {
      toast.warn(L.imageOnly)
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      toast.warn(L.imageTooLarge)
      return
    }
    setUploading(true)
    try {
      const fd = new FormData()
      fd.append('senderUsername', currentUsername)
      fd.append('receiverUsername', selectedRoom.partnerUsername)
      fd.append('file', file)
      const res = await client.post('/api/chat/image', fd, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      if (!res.data?.success) {
        toast.error(res.data?.message || L.imageFailed)
        return
      }
      fetchMessages(true)
      fetchRooms()
    } catch (err) {
      const status = err?.response?.status
      toast.error(
        status === 403
          ? L.imageNeedFriend
          : err?.response?.data?.message || L.imageFailed
      )
    } finally {
      setUploading(false)
    }
  }

  // 从「好友-发消息」进入时，直接打开与对方的会话
  useEffect(() => {
    const peer = location.state?.peerUsername
    if (!peer || !currentUsername) return
    let cancelled = false
    const open = async () => {
      try {
        await client.post('/api/chat/direct', {
          senderUsername: currentUsername,
          receiverUsername: peer,
          content: '',
          type: 'TEXT',
          mediaUrl: null
        })
        if (cancelled) return
        const res = await client.get('/api/chat/rooms', {
          params: { username: currentUsername }
        })
        const list = Array.isArray(res.data) ? res.data : []
        setRooms(list)
        const room = list.find((r) => r.partnerUsername === peer)
        if (room) {
          setSelectedRoom(room)
        } else {
          toast.error(L.chatOpenFailed || '无法打开与该好友的会话')
        }
      } catch (e) {
        console.error('open direct chat failed', e)
        toast.error(L.chatOpenFailed || '无法打开与该好友的会话')
      }
    }
    open()
    return () => { cancelled = true }
  }, [location.key, location.state?.peerUsername, currentUsername])

  // ===== 通话 =====
  const [permState, setPermState] = useState('unknown')
  const [callDiag, setCallDiag] = useState('')
  const [remoteOn, setRemoteOn] = useState(false)

  const describeMediaError = (err) => {
    const name = (err && err.name) || ''
    if (!window.isSecureContext) {
      return L.mediaInsecure || '当前地址不是 HTTPS 或 localhost，浏览器不会授权摄像头/麦克风'
    }
    if (name === 'NotAllowedError' || name === 'SecurityError') {
      return L.mediaDeniedHint || '权限被拒绝：点地址栏左侧的锁/图标 → 摄像头、麦克风改为“允许” → 刷新页面'
    }
    if (name === 'NotFoundError' || name === 'DevicesNotFoundError') {
      return L.mediaNoDevice || '没有检测到摄像头或麦克风设备'
    }
    if (name === 'NotReadableError' || name === 'TrackStartError') {
      return L.mediaBusy || '设备被其他程序占用，请关闭其他视频/会议软件后重试'
    }
    if (name === 'OverconstrainedError') {
      return L.mediaConstraint || '摄像头不支持当前分辨率，请换一个设备'
    }
    return (L.mediaFailed || '无法打开麦克风/摄像头') + (name ? ` (${name})` : '')
  }

  // 通话前主动申请一次权限：成功会写入浏览器站点授权，后面 Agora 可直接复用
  const requestMediaPermission = async (needVideo) => {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      throw new Error('unsupported')
    }
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: true,
      video: needVideo ? { width: 640, height: 480 } : false
    })
    stream.getTracks().forEach((t) => t.stop())
    setPermState('granted')
    return true
  }

  const refreshPerm = async () => {
    try {
      if (!navigator.permissions || !navigator.permissions.query) return
      const [cam, mic] = await Promise.all([
        navigator.permissions.query({ name: 'camera' }),
        navigator.permissions.query({ name: 'microphone' })
      ])
      const denied = cam.state === 'denied' || mic.state === 'denied'
      const granted = cam.state === 'granted' && mic.state === 'granted'
      setPermState(denied ? 'denied' : granted ? 'granted' : 'prompt')
    } catch (e) {
      setPermState('unknown')
    }
  }

  useEffect(() => {
    refreshPerm()
  }, [])

  const startCall = async (callType) => {
    if (!selectedRoom || callState) return

    // 先要权限，再发起呼叫，避免对方响铃后才失败
    try {
      await requestMediaPermission(callType === 'VIDEO')
    } catch (e) {
      toast.error(describeMediaError(e))
      refreshPerm()
      return
    }

    try {
      const res = await client.post('/api/call/start', {
        caller: currentUsername,
        callee: selectedRoom.partnerUsername,
        callType
      })
      const d = res.data
      setCallState({
        callId: d.callId,
        channelName: d.channelName,
        callType: d.callType || callType,
        status: d.status || 'RINGING',
        peer: selectedRoom.partnerName || selectedRoom.partnerUsername,
        peerAvatar: selectedRoom.partnerAvatar,
        isCaller: true,
        startedAt: d.status === 'ACTIVE' ? Date.now() : 0
      })
      setMuted(false)
      setCameraOff(false)
    } catch (err) {
      const status = err?.response?.status
      toast.error(
        status === 403
          ? L.callNeedFriend
          : status
            ? L.callFailed
            : (L.serverUnreachable ||
              '连不上服务器：请确认后端/本地开发服务正在运行')
      )
    }
  }

  const answerIncoming = async (accept) => {
    if (!incomingCall) return
    const call = incomingCall
    setIncomingCall(null)

    if (accept) {
      try {
        await requestMediaPermission(call.callType === 'VIDEO')
      } catch (e) {
        toast.error(describeMediaError(e))
        refreshPerm()
        return
      }
    }

    try {
      const res = await client.post('/api/call/answer', {
        callId: call.callId,
        callee: currentUsername,
        accept
      })
      const d = res.data
      if (!accept) return
      setCallState({
        callId: call.callId,
        channelName: d.channelName || call.channelName,
        callType: d.callType || call.callType,
        status: 'ACTIVE',
        peer: call.caller,
        isCaller: false,
        startedAt: Date.now()
      })
      setMuted(false)
      setCameraOff(false)
    } catch {}
  }

  const endCall = async () => {
    const current = callState
    setCallState(null)
    setRemoteOn(false)
    setCallDiag('')
    setCallSeconds(0)
    if (!current) return
    try {
      await client.post('/api/call/end', {
        callId: current.callId,
        caller: currentUsername
      })
    } catch {}
    fetchRooms()
  }

  // ===== 真实音视频（Agora RTC） =====
  useEffect(() => {
    let cancelled = false

    const leave = async () => {
      const c = agoraRef.current
      agoraRef.current = null
      const tr = localTracksRef.current
      if (tr.audio) { try { tr.audio.stop(); tr.audio.close() } catch {} tr.audio = null }
      if (tr.video) { try { tr.video.stop(); tr.video.close() } catch {} tr.video = null }
      if (c) { try { await c.leave() } catch {} }
    }

    const join = async () => {
      const channelName = callState?.channelName
      if (!callState || callState.status !== 'ACTIVE' || !channelName) return
      try {
        // 每个用户一个稳定且唯一的 uid，避免同频道 uid 冲突互踢
        let myUid = 1
        const seed = String(currentUsername || 'user')
        for (let i = 0; i < seed.length; i++) {
          myUid = (myUid * 31 + seed.charCodeAt(i)) % 100000
        }
        myUid = myUid + 1

        const res = await client.get('/api/call/token', {
          params: { channelName, uid: myUid }
        })
        if (cancelled) return
        const { token, appId, uid } = res.data || {}
        if (!appId || !token) throw new Error('agora token missing')

        const rtc = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' })
        agoraRef.current = rtc
        setCallDiag('')

        rtc.on('connection-state-change', (cur, prev, reason) => {
          setCallDiag('RTC: ' + prev + '→' + cur + (reason ? ' (' + reason + ')' : ''))
        })
        rtc.on('exception', (e) => {
          const code = e?.code ?? e?.reason ?? '?'
          setCallDiag('RTC 异常: ' + code)
        })

        rtc.on('user-joined', (u) => {
          setCallDiag('对方已入会: ' + (u?.uid ?? ''))
        })

        rtc.on('user-published', async (user, mediaType) => {
          try {
            await rtc.subscribe(user, mediaType)
            if (mediaType === 'video') {
              if (remoteVideoRef.current) {
                user.videoTrack?.play(remoteVideoRef.current)
                setRemoteOn(true)
                setCallDiag('远端画面已接收')
              } else {
                setCallDiag('远端画面元素未就绪')
              }
            }
            if (mediaType === 'audio') {
              user.audioTrack?.play()
            }
          } catch (e) {
            console.error(e)
            setCallDiag('订阅失败: ' + (e?.code ?? e?.name ?? '') )
          }
        })

        rtc.on('user-unpublished', (user, mediaType) => {
          if (mediaType === 'video') {
            try { user.videoTrack?.stop() } catch {}
            setRemoteOn(false)
          }
        })

        rtc.on('user-left', () => {
          setRemoteOn(false)
          setCallDiag('对方已离开频道')
        })

        try {
          await rtc.join(appId, channelName, token, uid ?? myUid)
        } catch (joinErr) {
          const code = joinErr?.code ?? joinErr?.name ?? ''
          setCallDiag('入会失败: ' + code + ' ' + (joinErr?.message || ''))
          toast.error('加入频道失败: ' + code)
          throw joinErr
        }

        let audioTrack = null
        let videoTrack = null
        try {
          if (callState.callType === 'VIDEO') {
            const tracks = await AgoraRTC.createMicrophoneAndCameraTracks()
            audioTrack = tracks[0]
            videoTrack = tracks[1]
          } else {
            audioTrack = await AgoraRTC.createMicrophoneAudioTrack()
          }
        } catch (permErr) {
          console.error('media permission', permErr)
          toast.error(describeMediaError(permErr))
          throw permErr
        }

        if (cancelled) { audioTrack.close(); videoTrack?.close(); return }

        localTracksRef.current.audio = audioTrack
        localTracksRef.current.video = videoTrack
        audioTrack.setEnabled(!muted)
        if (videoTrack) {
          videoTrack.setEnabled(!cameraOff)
          if (localVideoRef.current) videoTrack.play(localVideoRef.current)
        }
        try {
          await rtc.publish(videoTrack ? [audioTrack, videoTrack] : [audioTrack])
          setCallDiag('已入会并推流')
        } catch (pubErr) {
          const code = pubErr?.code ?? pubErr?.name ?? ''
          setCallDiag('推流失败: ' + code + ' ' + (pubErr?.message || ''))
          toast.error('推流失败: ' + code)
          throw pubErr
        }
      } catch (err) {
        console.error('Agora join failed', err)
      }
    }

    join()
    return () => { cancelled = true; leave() }
  }, [callState?.status, callState?.channelName, callState?.callType])

  // 静音 / 摄像头 开关同步到真实轨道
  useEffect(() => {
    const t = localTracksRef.current.audio
    if (t) t.setEnabled(!muted)
  }, [muted])

  useEffect(() => {
    const t = localTracksRef.current.video
    if (t) t.setEnabled(!cameraOff)
  }, [cameraOff])

  // 来电轮询（无通话时每 3 秒）
  useEffect(() => {
    if (!currentUsername || callState) return
    let stopped = false
    const poll = async () => {
      try {
        const res = await client.get('/api/call/incoming', {
          params: { username: currentUsername }
        })
        if (stopped) return
        const d = res.data
        if (
          d &&
          d.callId &&
          d.status === 'RINGING' &&
          d.caller !== currentUsername
        ) {
          setIncomingCall(d)
        } else {
          setIncomingCall(null)
        }
      } catch {}
    }
    poll()
    const id = setInterval(poll, 3000)
    return () => {
      stopped = true
      clearInterval(id)
    }
  }, [currentUsername, callState])

  // 主叫方轮询是否被接听
  useEffect(() => {
    if (!callState?.isCaller || callState.status === 'ACTIVE') return
    const id = setInterval(async () => {
      try {
        const res = await client.get('/api/call/status', {
          params: { callId: callState.callId }
        })
        const d = res.data
        if (d.status === 'ACTIVE') {
          setCallState((prev) =>
            prev ? { ...prev, status: 'ACTIVE', startedAt: Date.now() } : prev
          )
        } else if (d.status === 'REJECTED' || d.status === 'ENDED') {
          setCallState(null)
          toast.info(d.status === 'REJECTED' ? L.callRejected : L.callEnded)
        }
      } catch {
        setCallState(null)
      }
    }, 2000)
    return () => clearInterval(id)
  }, [callState?.callId, callState?.isCaller, callState?.status])

  // 通话计时
  useEffect(() => {
    if (!callState || callState.status !== 'ACTIVE') {
      setCallSeconds(0)
      return
    }
    const id = setInterval(() => setCallSeconds((s) => s + 1), 1000)
    return () => clearInterval(id)
  }, [callState?.callId, callState?.status])

  const formatCallTime = (sec) => {
    const m = Math.floor(sec / 60)
    const s = sec % 60
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }

  // 通话记录消息（移动端样式的系统气泡）
  const callMessageLabel = (msg) => {
    const raw = (msg.content || '').trim()
    if (raw.startsWith('ended:')) {
      const sec = Number(raw.split(':')[1] || 0)
      const mm = Math.floor(sec / 60)
      const ss = sec % 60
      const dur = mm > 0 ? `${mm}:${String(ss).padStart(2, '0')}` : `${ss}${L.seconds}`
      return `${L.callEnded} · ${dur}`
    }
    if (raw === 'canceled') return L.callCanceled
    if (raw === 'missed') return L.callMissed
    if (raw === 'rejected') return L.callRejected
    return L.callEnded
  }

  const callMessageIcon = (msg) =>
    (msg.mediaUrl === 'VIDEO' ? <IconVideo size={14} /> : <IconPhone size={14} />)
  const handleAiSuggest = async () => {
    if (!selectedRoom)
      return
    setIsAiLoading(true)
    setAiError('')
    try {
      const context =messages
        .slice(-8)
        .map(msg => msg.content)
        .join('\n')
        .slice(0, 300)
      const question =
        newMessage.trim()
        ||
        messages[messages.length - 1]?.content
        ||
        '최근 채팅 내용에서 이해하기 어려운 지식 포인트를 설명해주세요.'
      const res = await client.post(
        '/api/chat/ai-help',
        {
          message:
            question,
          partner:
            selectedRoom.partnerUsername,
          context,
          targetLang:
            currentLang
        }
      )
      const data =res.data
        if (
          !data.success
        ) {
          setAiError('AI 학습 도움 생성에 실패했습니다.')
          return
        }
        const aiMessage =
          `${AI_PREFIX}\n${data.answer}`
        const sendRes =
          await client.post(
            '/api/chat/send',
            {
              senderUsername:
                currentUsername,
              receiverUsername:
                selectedRoom.partnerUsername,
              content:
                aiMessage
            }
          )
        const sendData =
          sendRes.data
        if (
          !sendData.success
        ) {
          setAiError('AI 메시지 저장에 실패했습니다.')
          return
        }
        setNewMessage('')
        fetchMessages(true)
        fetchRooms()
        scrollToBottom()
      } catch (err) {
        console.error(
          'AI 요청失败:',
          err
        )
        setAiError(
          'AI 서버 연결에 실패했습니다.'
        )
      } finally {
        setIsAiLoading(
          false
        )
      }
    }
  useEffect(() => {
    if (
      currentUsername
    ) {
      fetchRooms()
    }
  }, [currentUsername])
  useEffect(() => {
    if (
      !selectedRoom ||
      !currentUsername
    ) {
      return
    }
    fetchMessages(true)
    // 新增
    fetchMyRating()
    const interval =
      setInterval(
        () =>
          fetchMessages(
            false
          ),
        2000
      )
    return () =>
      clearInterval(
        interval
      )
  }, [
    selectedRoom,
    currentUsername
  ])
  useEffect(() => {
    if (
      !messages.length
    ) {
      setTranslatedMessages(
        {}
      )
      return
    }
    autoTranslateMessages()
  }, [
    messages,
    currentLang
  ])
  if (
    !currentUsername
  ) {
    return (
      <div
        style={{
          color:
            'white',
          padding:
            '40px'
        }}
      >
        {L.loginFirst}
      </div>
    )
  }
  if (
    !rooms.length
  ) {
    return (
      <div
        style={{
          color:
            'white',
          padding:
            '40px'
        }}
      >
        {L.noRooms}
      </div>
    )
  }
  return (
    <div className="chat-page">
      <div className="chat-sidebar">
        <div className="chat-sidebar-top">
          <h2 className="chat-title">
            {text.chat}
          </h2>
          <input
            type="text"
            placeholder={L.searchPlaceholder}
            className="chat-search"
          />
        </div>
        <div className="chat-user-list">
          {rooms.map(
            room => (
            <div
              key={
                room.roomId
              }
              className={`chat-user-card ${
                selectedRoom?.roomId ===
                room.roomId
                  ? 'active-chat'
                  : ''
              }`}
              onClick={() => {
                setSelectedRoom(
                  room
                )
                setAiError('')
                scrollToBottom()
              }}
            >
              <div className="chat-avatar">
                {room.partnerAvatar ? (
                  <img
                    src={room.partnerAvatar}
                    alt={room.partnerName}
                    style={{
                      width: '100%',
                      height: '100%',
                      objectFit: 'cover',
                      borderRadius: '50%'
                    }}
                  />
                ) : (
                  room.partnerName?.charAt(0)
                )}
              </div>
              <div className="chat-user-info">
                <h4>
                  {room.partnerName}
                  <span
                    style={{
                      marginLeft:
                        '8px',
                      color:
                        '#f5b50a',
                      fontSize:
                        '13px'
                    }}
                  >
                    <IconStar size={15} /> {
                      5
                    }
                  </span>
                </h4>
                <p>
                  {
                    room.lastMessage ||
                    L.startChat
                  }
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
            <div className="chat-main">
        <div className="chat-header">
          <div className="chat-header-info">
            <h3>
              {selectedRoom?.partnerName}
              <span
                style={{
                  marginLeft: '10px',
                  color: '#f5b50a',
                  fontSize: '15px'
                }}
              >
                <IconStar size={15} /> {
                  5
                }
              </span>
            </h3>
            {/* 当前用户评分 */}
            <div>
              {[1,2,3,4,5].map(star => (
                <span
                  style={{
                    display: 'flex',
                    gap: '6px',
                    marginTop: '8px',
                    padding: '8px 10px',
                    borderRadius: '14px',
                    background: 'rgba(255,255,255,0.03)',
                    border: '1px solid rgba(255,255,255,0.05)',
                    backdropFilter: 'blur(12px)',
                    alignItems: 'center'
                  }}
                  key={star}
                  onClick={() => handleRating(star)}
                  style={{
                    cursor: 'pointer',
                    fontSize: '22px',
                    transition: 'all 0.2s ease',
                    transform: star <= myRating ? 'scale(1.08)' : 'scale(1)',
                    filter: star <= myRating
                      ? 'drop-shadow(0 0 4px rgba(255,215,0,0.6))'
                      : 'none',
                    opacity: star <= myRating ? 1 : 0.45,
                    userSelect: 'none'
                  }}
                >
                  {star <= myRating ? <IconStar size={20} filled /> : <IconStar size={20} />}
                </span>
              ))}
            </div>
          </div>
        </div>
        {permState !== 'granted' && (
          <button
            type="button"
            className="media-perm-pill"
            title={L.enableDevices || '点击授权麦克风与摄像头'}
            onClick={async () => {
              try {
                await requestMediaPermission(true)
                toast.success(L.mediaReady || '设备权限已就绪，可以发起通话')
              } catch (e) {
                toast.error(describeMediaError(e))
                refreshPerm()
              }
            }}
          >
            <CallIcon name="mic" />
            <span>{L.enableDevices || '授权设备'}</span>
          </button>
        )}

        <div className="chat-call-actions">
          <button
            type="button"
            className="call-action-btn"
            title={L.voiceCall}
            onClick={() => startCall('VOICE')}
            disabled={!selectedRoom}
          >
            <span className="call-action-icon"><CallIcon name="phone" /></span>
            <span className="call-action-label">{L.voiceCall}</span>
          </button>
          <button
            type="button"
            className="call-action-btn call-action-video"
            title={L.videoCall}
            onClick={() => startCall('VIDEO')}
            disabled={!selectedRoom}
          >
            <span className="call-action-icon"><CallIcon name="video" /></span>
            <span className="call-action-label">{L.videoCall}</span>
          </button>
        </div>
        <div className="chat-messages">
          {messages.map((msg, index) => {
            const isMe =
              msg.senderUsername ===
              currentUsername
            const isAi =
              msg.content?.startsWith(
                AI_PREFIX
              )

            // AI 没有上下文时会把「看不到历史记录」这类废话当成消息发出来，直接不渲染
            if (
              isAi &&
              /没有更早的聊天记录|no earlier chat|이전 대화 기록|이전 채팅 기록/.test(
                msg.content
              )
            ) {
              return null
            }
            let showTimeBubble =
              index === 0
            let hideAvatar =
              false
            if (index > 0) {
              const prev =
                messages[index - 1]
              const nowTime =
                new Date(
                  msg.createdAt ||
                  msg.time
                )
              const prevTime =
                new Date(
                  prev.createdAt ||
                  prev.time
                )
              if (
                !isNaN(nowTime) &&
                !isNaN(prevTime)
              ) {
                showTimeBubble =
                  nowTime -
                  prevTime >
                  TIME_GAP
              }
            }
            if (
              index <
              messages.length - 1
            ) {
              const next =
                messages[index + 1]
              const nowTime =
                new Date(
                  msg.createdAt ||
                  msg.time
                )
              const nextTime =
                new Date(
                  next.createdAt ||
                  next.time
                )
              if (
                next.senderUsername ===
                msg.senderUsername &&
                nextTime - nowTime <=
                TIME_GAP &&
                !next.content?.startsWith(
                  AI_PREFIX
                )
              ) {
                hideAvatar = true
              }
            }
            return (
              <div
                key={
                  msg.id || index
                }
                style={{
                  display:
                    'contents'
                }}
              >                {showTimeBubble && (

                  <div className="chat-time-bubble">

                    {formatWeChatTime(
                      msg.createdAt ||
                      msg.time
                    )}
                  </div>
                )}
                {msg.type === 'CALL' ? (
                  <div className="chat-system-bubble">
                    <span className="chat-system-icon">
                      {callMessageIcon(msg)}
                    </span>
                    <span>{callMessageLabel(msg)}</span>
                  </div>
                ) : isAi ? (
                  <div className="ai-suggestion-box">
                    <div className="ai-suggest-title">
                      {AI_PREFIX}
                    </div>
                    <div className="ai-suggest-content">
                      {msg.content.replace(
                        `${AI_PREFIX}\n`,
                        ''
                      )}
                    </div>
                  </div>
                ) : (
                  <div
                    className={`message-row ${
                      isMe
                        ? 'row-sent'
                        : 'row-received'
                    } ${
                      hideAvatar
                        ? 'avatar-hidden'
                        : ''
                    }`}
                  >
                    {!isMe && (
                      <div className="message-avatar">
                      {!hideAvatar && (
                        selectedRoom?.partnerAvatar ? (
                          <img
                            src={selectedRoom.partnerAvatar}
                            alt={selectedRoom.partnerName}
                            style={{
                              width: '100%',
                              height: '100%',
                              objectFit: 'cover',
                              borderRadius: '50%'
                            }}
                          />
                        ) : (
                          selectedRoom?.partnerName?.charAt(0)
                        )
                      )}
                    </div>
                    )}
                    <div
                      className={`message ${
                        isMe
                          ? 'sent'
                          : 'received'
                      }`}
                    >
                      {msg.recalled ? (
                        <div className="message-text message-recalled">
                          {L.recalled}
                        </div>
                      ) : msg.type === 'IMAGE' && msg.mediaUrl ? (
                        <a
                          className="message-image-link"
                          href={msg.mediaUrl}
                          target="_blank"
                          rel="noreferrer"
                        >
                          <img
                            className="message-image"
                            src={msg.mediaUrl}
                            alt="image"
                            loading="lazy"
                          />
                        </a>
                      ) : (
                        <div className="message-text">
                          {msg.content}
                        </div>
                      )}
                      {translatedMessages[msg.id] && (
                        <div className="translated-message">
                          {
                            translatedMessages[msg.id]
                          }
                        </div>
                      )}
                    </div>
                    {isMe && (
                      <>
                        <div className="message-avatar">
                          {!hideAvatar &&
                            currentUsername?.charAt(
                              0
                            )}
                        </div>
                        <span
                          className={`msg-status ${
                            msg.isRead
                              ? 'status-read'
                              : 'status-unread'
                          }`}
                        >
                          {msg.isRead
                            ? L.read
                            : L.unread}
                        </span>
                      </>
                    )}
                  </div>
                )}
              </div>
            )
          })}
          {isAiLoading && (
            <div className="message-row row-received">
              <div className="message-avatar">
                <IconSparkle size={16} />
              </div>
              <div className="message received ai-typing-bubble">
                <div className="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>
        <div className="chat-input-area">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="chat-file-input"
            onChange={handleImageSelected}
          />
          <button
            type="button"
            className="plus-btn image-pick-btn"
            title={L.pickImage}
            onClick={handlePickImage}
            disabled={uploading || !selectedRoom}
          >
            {uploading ? '…' : '＋'}
          </button>
          <input
            type="text"
            className="chat-input"
            value={newMessage}
            onChange={(e) =>
              setNewMessage(
                e.target.value
              )
            }
            onKeyDown={(e) =>
              e.key === 'Enter' &&
              handleSendMessage()
            }
            placeholder={L.inputPlaceholder}
          />
          <button
            className="ai-btn"
            onClick={
              handleAiSuggest
            }
            disabled={
              isAiLoading
            }
          >
            {
              isAiLoading
                ? '...'
                : <><IconSparkle size={14} /> AI</>
            }
          </button>
          <button
            className="send-btn"
            onClick={
              handleSendMessage
            }
          >
            {L.send}
          </button>
        </div>
      </div>
      {/* 来电弹窗 */}
      {incomingCall && (
        <div className="call-incoming-overlay">
          <div className="call-incoming-card">
            <div className="call-incoming-avatar">
              {String(incomingCall.caller || '?').charAt(0)}
            </div>
            <div className="call-incoming-name">
              {incomingCall.callerName || incomingCall.caller}
            </div>
            <div className="call-incoming-type">
              {incomingCall.callType === 'VIDEO'
                ? L.incomingVideo
                : L.incomingVoice}
            </div>
            <div className="call-incoming-actions">
              <button
                type="button"
                className="call-decline-btn"
                onClick={() => answerIncoming(false)}
              >
                <span><IconClose size={14} /></span>
                {L.decline}
              </button>
              <button
                type="button"
                className="call-accept-btn"
                onClick={() => answerIncoming(true)}
              >
                <span><IconPhone size={18} /></span>
                {L.accept}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 通话中浮层 */}
      {callState && (
        <div
          className={`call-overlay ${
            callState.callType === 'VIDEO' ? 'call-overlay-video' : ''
          }`}
        >
          <div className="call-stage">
            {callState.callType === 'VIDEO' ? (
              <div className="call-video-stage">
                <div className="call-video-remote">
                  <video
                    ref={remoteVideoRef}
                    className="call-remote-video"
                    autoPlay
                    playsInline
                  />
                  {!remoteOn && (
                    <>
                      <div className="call-peer-avatar">
                        {callState.peerAvatar ? (
                          <img
                            src={callState.peerAvatar}
                            alt={callState.peer}
                          />
                        ) : (
                          String(callState.peer || '?').charAt(0)
                        )}
                      </div>
                      <span className="call-video-hint">
                        {L.videoPlaceholder}
                      </span>
                    </>
                  )}
                </div>
                <div
                  className={`call-video-local ${
                    cameraOff ? 'camera-off' : ''
                  }`}
                >
                  <video
                    ref={localVideoRef}
                    className="call-local-video"
                    autoPlay
                    playsInline
                    muted
                  />
                  {cameraOff && (
                    <span className="call-local-hint">
                      {L.cameraOff || 'Camera off'}
                    </span>
                  )}
                </div>
              </div>
            ) : (
              <div className="call-audio-stage">
                <div className="call-peer-avatar call-peer-avatar-lg">
                  {callState.peerAvatar ? (
                    <img src={callState.peerAvatar} alt={callState.peer} />
                  ) : (
                    String(callState.peer || '?').charAt(0)
                  )}
                </div>
              </div>
            )}

            <div className="call-topbar">
              <div className="call-peer-name">{callState.peer}</div>
              <div className="call-status-text">
                {callState.status === 'ACTIVE'
                  ? `${L.inCall} · ${formatCallTime(callSeconds)}`
                  : callState.isCaller
                    ? L.calling
                    : L.ringing}
              </div>
              {callDiag && (
                <div className="call-diag">{callDiag}</div>
              )}
            </div>

            <div className="call-controls">
              <button
                type="button"
                className={`call-ctrl ${muted ? 'call-ctrl-off' : ''}`}
                onClick={() => setMuted((v) => !v)}
                title={L.mic}
              >
                <span className="call-ctrl-icon"><CallIcon name="mic" off={muted} /></span>
                <span className="call-ctrl-label">{L.mic}</span>
              </button>
              {callState.callType === 'VIDEO' && (
                <button
                  type="button"
                  className={`call-ctrl ${
                    cameraOff ? 'call-ctrl-off' : ''
                  }`}
                  onClick={() => setCameraOff((v) => !v)}
                  title={L.camera}
                >
                  <span className="call-ctrl-icon"><CallIcon name="camera" off={cameraOff} /></span>
                  <span className="call-ctrl-label">{L.camera}</span>
                </button>
              )}
              <button
                type="button"
                className="call-ctrl call-hangup"
                onClick={endCall}
                title={L.hangUp}
              >
                <span className="call-ctrl-icon"><CallIcon name="hangup" /></span>
                <span className="call-ctrl-label">{L.hangUp}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
export default Chat