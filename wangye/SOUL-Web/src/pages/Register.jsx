import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../api/client'

const EMAIL_DOMAINS = [
  'gmail.com', 'naver.com', 'daum.net', 'hanmail.net', 'nate.com',
  'outlook.com', 'hotmail.com', 'qq.com', '163.com', '126.com',
  'icloud.com', 'yahoo.com'
]

function Register({ text }) {
  const [form, setForm] = useState({
    username: '',
    password: '',
    emailLocal: '',
    emailDomain: 'gmail.com'
  })
  const [result, setResult] = useState('')
  const [checking, setChecking] = useState(false)
  const navigate = useNavigate()

  const email = form.emailLocal
    ? `${form.emailLocal}@${form.emailDomain}`
    : ''

  const update = (key, value) =>
    setForm((prev) => ({ ...prev, [key]: value }))

  const handleRegister = async () => {
    const username = form.username.trim()

    if (!username || !form.password || !form.emailLocal) {
      setResult(text.fillAll || '请填写完整信息')
      return
    }
    if (!/^[A-Za-z0-9_]{3,20}$/.test(username)) {
      setResult('用户名需 3-20 位字母、数字或下划线')
      return
    }
    if (form.password.length < 6) {
      setResult('密码至少 6 位')
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setResult(text.emailInvalid || '邮箱格式不正确')
      return
    }

    setChecking(true)
    try {
      const res = await client.get('/api/register/check', {
        params: { username }
      })
      if (!res.data?.available) {
        setResult(res.data?.message || '用户名不可用')
        return
      }
      // 先不写数据库，资料填完再真正注册
      sessionStorage.setItem(
        'pendingRegistration',
        JSON.stringify({ username, password: form.password, email })
      )
      navigate('/profile-setup')
    } catch (e) {
      console.error(e)
      setResult('检查用户名失败，请稍后重试')
    } finally {
      setChecking(false)
    }
  }

  return (
    <section className="auth-page auth-textonly">
      <div className="auth-brand">
        <h1 className="auth-brand-mark">SOUL</h1>
        <p className="auth-brand-tag">Create your account</p>
      </div>

      <div className="auth-form">
        <input
          name="username"
          placeholder="用户名 / Username"
          value={form.username}
          onChange={(e) => update('username', e.target.value)}
          autoComplete="username"
        />

        <input
          name="password"
          type="password"
          placeholder={text.password || '密码'}
          value={form.password}
          onChange={(e) => update('password', e.target.value)}
          autoComplete="new-password"
        />

        <div className="email-row">
          <input
            name="emailLocal"
            placeholder="邮箱 / Email"
            value={form.emailLocal}
            onChange={(e) => update('emailLocal', e.target.value.replace(/@.*/, ''))}
            autoComplete="email"
          />
          <select
            value={form.emailDomain}
            onChange={(e) => update('emailDomain', e.target.value)}
          >
            {EMAIL_DOMAINS.map((d) => (
              <option key={d} value={d}>{'@' + d}</option>
            ))}
          </select>
        </div>

        <p className="email-preview">{email || ' '}</p>

        <div className="auth-buttons">
          <button onClick={handleRegister} disabled={checking}>
            {checking ? '...' : (text.registerBtn || '注册')}
          </button>
          <button
            type="button"
            className="switch-btn"
            onClick={() => navigate('/login')}
          >
            {text.loginBtn || '登录'}
          </button>
        </div>

        {result && <p className="auth-result">{result}</p>}
      </div>
    </section>
  )
}

export default Register
