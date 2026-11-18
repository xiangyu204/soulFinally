import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../api/client'
import { toast, confirmDialog } from '../ui/feedback'
import defaultAvatar from '../assets/default-avatar.png'
import '../styles/friends.css'

const STR = {
  zh: {
    title: '好友',
    subtitle: '添加好友后，才能发送图片和发起语音 / 视频通话',
    addTitle: '添加好友',
    placeholder: '输入对方的用户名',
    messagePlaceholder: '验证消息（可选）',
    send: '发送申请',
    sent: '好友申请已发送',
    needName: '请输入用户名',
    self: '不能添加自己',
    requests: '待处理申请',
    noRequests: '暂无好友申请',
    accept: '接受',
    reject: '拒绝',
    accepted: '已添加为好友',
    rejected: '已拒绝',
    list: '我的好友',
    noFriends: '还没有好友，先添加一个吧',
    chat: '发消息',
    remove: '删除',
    removeConfirm: '确定删除该好友吗？',
    removed: '已删除好友',
    loginFirst: '请先登录',
    failed: '操作失败，请稍后再试'
  },
  en: {
    title: 'Friends',
    subtitle: 'Add friends to send images and start voice / video calls',
    addTitle: 'Add a friend',
    placeholder: "Enter the other person's username",
    messagePlaceholder: 'Verification message (optional)',
    send: 'Send request',
    sent: 'Friend request sent',
    needName: 'Please enter a username',
    self: 'You cannot add yourself',
    requests: 'Pending requests',
    noRequests: 'No pending requests',
    accept: 'Accept',
    reject: 'Decline',
    accepted: 'Now friends',
    rejected: 'Declined',
    list: 'My friends',
    noFriends: 'No friends yet — add one',
    chat: 'Message',
    remove: 'Remove',
    removeConfirm: 'Remove this friend?',
    removed: 'Friend removed',
    loginFirst: 'Please log in first',
    failed: 'Action failed, please try again'
  },
  ko: {
    title: '친구',
    subtitle: '친구를 추가해야 이미지 전송과 음성 / 영상 통화를 할 수 있습니다',
    addTitle: '친구 추가',
    placeholder: '상대방 아이디를 입력하세요',
    messagePlaceholder: '인사말 (선택)',
    send: '신청 보내기',
    sent: '친구 신청을 보냈습니다',
    needName: '아이디를 입력하세요',
    self: '자기 자신은 추가할 수 없습니다',
    requests: '받은 신청',
    noRequests: '받은 신청이 없습니다',
    accept: '수락',
    reject: '거절',
    accepted: '친구가 되었습니다',
    rejected: '거절했습니다',
    list: '내 친구',
    noFriends: '아직 친구가 없습니다',
    chat: '메시지',
    remove: '삭제',
    removeConfirm: '이 친구를 삭제할까요?',
    removed: '친구를 삭제했습니다',
    loginFirst: '먼저 로그인해 주세요',
    failed: '작업에 실패했습니다'
  }
}

export default function Friends() {
  const navigate = useNavigate()
  const lang = (localStorage.getItem('lang') || 'zh').slice(0, 2)
  const t = STR[lang] || STR.zh

  const user = useMemo(() => {
    try {
      return JSON.parse(localStorage.getItem('loginUser') || 'null')
    } catch {
      return null
    }
  }, [])
  const me = user?.username || ''

  const [target, setTarget] = useState('')
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [requests, setRequests] = useState([])
  const [friends, setFriends] = useState([])
  const [loading, setLoading] = useState(true)

  const loadAll = useCallback(async () => {
    if (!me) {
      setLoading(false)
      return
    }
    try {
      const [reqRes, listRes] = await Promise.all([
        client.get('/api/friend/requests', { params: { username: me } }),
        client.get('/api/friend/list', { params: { username: me } })
      ])
      setRequests(Array.isArray(reqRes.data) ? reqRes.data : [])
      setFriends(Array.isArray(listRes.data) ? listRes.data : [])
    } catch {
      setRequests([])
      setFriends([])
    } finally {
      setLoading(false)
    }
  }, [me])

  useEffect(() => {
    loadAll()
  }, [loadAll])

  // 实时刷新：每 5 秒轮询一次，窗口重新聚焦时立即刷新
  useEffect(() => {
    if (!me) return
    const id = setInterval(() => {
      loadAll()
    }, 5000)
    const onFocus = () => loadAll()
    const onVisible = () => {
      if (document.visibilityState === 'visible') loadAll()
    }
    window.addEventListener('focus', onFocus)
    document.addEventListener('visibilitychange', onVisible)
    return () => {
      clearInterval(id)
      window.removeEventListener('focus', onFocus)
      document.removeEventListener('visibilitychange', onVisible)
    }
  }, [loadAll, me])

  const handleAdd = async () => {
    const name = target.trim()
    if (!me) {
      toast.warn(t.loginFirst)
      return
    }
    if (!name) {
      toast.warn(t.needName)
      return
    }
    if (name === me) {
      toast.warn(t.self)
      return
    }
    setSending(true)
    try {
      const res = await client.post('/api/friend/request', {
        username: me,
        friendUsername: name,
        message: message.trim()
      })
      if (res.data?.success) {
        toast.success(res.data?.message || t.sent)
        setTarget('')
        setMessage('')
        loadAll()
      } else {
        toast.error(res.data?.message || t.failed)
      }
    } catch (err) {
      toast.error(err?.response?.data?.message || t.failed)
    } finally {
      setSending(false)
    }
  }

  const handleRespond = async (request, accept) => {
    try {
      const res = await client.post('/api/friend/respond', {
        requestId: request.requestId,
        username: me,
        accept
      })
      if (res.data?.success) {
        toast.success(accept ? t.accepted : t.rejected)
      } else {
        toast.error(res.data?.message || t.failed)
      }
      loadAll()
    } catch {
      toast.error(t.failed)
    }
  }

  const handleRemove = async (friend) => {
    const ok = await confirmDialog({
      title: t.remove,
      message: t.removeConfirm,
      confirmText: t.remove,
      cancelText: lang === 'en' ? 'Cancel' : lang === 'ko' ? '취소' : '取消',
      danger: true
    })
    if (!ok) return
    try {
      await client.post('/api/friend/remove', {
        username: me,
        friendUsername: friend.username
      })
      toast.success(t.removed)
      loadAll()
    } catch {
      toast.error(t.failed)
    }
  }

  if (!me) {
    return (
      <section className="friends-page">
        <div className="friends-empty">{t.loginFirst}</div>
      </section>
    )
  }

  return (
    <section className="friends-page">
      <header className="friends-head">
        <h1>{t.title}</h1>
        <p>{t.subtitle}</p>
      </header>

      <div className="friends-grid">
        <div className="friends-col">
          <div className="friends-card">
            <h2>{t.addTitle}</h2>
            <div className="friends-form">
              <input
                value={target}
                onChange={(e) => setTarget(e.target.value)}
                placeholder={t.placeholder}
                onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
              />
              <input
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder={t.messagePlaceholder}
                onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
              />
              <button
                type="button"
                className="friends-primary"
                onClick={handleAdd}
                disabled={sending}
              >
                {sending ? '...' : t.send}
              </button>
            </div>
          </div>

          <div className="friends-card">
            <h2>
              {t.requests}
              {requests.length > 0 && (
                <span className="friends-badge">{requests.length}</span>
              )}
            </h2>
            {requests.length === 0 ? (
              <p className="friends-muted">{t.noRequests}</p>
            ) : (
              <ul className="friend-request-list">
                {requests.map((r) => (
                  <li key={r.requestId} className="friend-request-item">
                    <div className="friend-avatar">
                      {String(r.fromName || r.fromUser || '?').charAt(0)}
                    </div>
                    <div className="friend-meta">
                      <strong>{r.fromName || r.fromUser}</strong>
                      <span>{r.message || ''}</span>
                    </div>
                    <div className="friend-request-actions">
                      <button
                        type="button"
                        className="friends-accept"
                        onClick={() => handleRespond(r, true)}
                      >
                        {t.accept}
                      </button>
                      <button
                        type="button"
                        className="friends-reject"
                        onClick={() => handleRespond(r, false)}
                      >
                        {t.reject}
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        <div className="friends-col">
          <div className="friends-card friends-card-list">
            <h2>
              {t.list}
              <span className="friends-count">{friends.length}</span>
            </h2>

            {loading ? (
              <p className="friends-muted">...</p>
            ) : friends.length === 0 ? (
              <p className="friends-muted">{t.noFriends}</p>
            ) : (
              <ul className="friend-list">
                {friends.map((f) => (
                  <li key={f.username} className="friend-item">
                    <div className="friend-avatar">
                      {f.avatar ? (
                        <img
                          src={f.avatar}
                          alt={f.name || f.username}
                          onError={(e) => {
                            e.target.src = defaultAvatar
                          }}
                        />
                      ) : (
                        String(f.name || f.username || '?').charAt(0)
                      )}
                    </div>
                    <div className="friend-meta">
                      <strong>{f.remark || f.name || f.username}</strong>
                      <span>@{f.username}</span>
                    </div>
                    <div className="friend-actions">
                      <button
                        type="button"
                        className="friends-ghost"
                        onClick={() =>
                          navigate('/chat', {
                            state: {
                              peerUsername: f.username,
                              peerName: f.remark || f.name || f.username
                            }
                          })
                        }
                      >
                        {t.chat}
                      </button>
                      <button
                        type="button"
                        className="friends-danger"
                        onClick={() => handleRemove(f)}
                      >
                        {t.remove}
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}
