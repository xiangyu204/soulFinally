import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../api/client'

function Login({ text, setUser }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [result, setResult] = useState('')
  const navigate = useNavigate()

  const handleLogin = async () => {
    if (!username || !password) {
      setResult(text.fillAll || '请填写完整信息')
      return
    }

    try {
      const res = await client.post('/api/login', { username, password })
      const data = res.data

      if (data.success === true) {
        let profileData = {}
        try {
          const profileRes = await client.get(
            `/api/profile?username=${encodeURIComponent(data.username || username)}`
          )
          profileData = profileRes.data
        } catch (e) {
          console.error(e)
        }

        let avatarUrl = profileData.avatar || data.avatar || ''
        if (avatarUrl && avatarUrl.startsWith('/')) {
          avatarUrl = `${import.meta.env.VITE_API_BASE_URL || ''}${avatarUrl}`
        }

        const loginUser = {
          username: profileData.username || data.username || username,
          name: profileData.name || data.name || data.username || username,
          email: profileData.email || data.email || '',
          avatar: avatarUrl,
          role: profileData.role || data.role || 'USER'
        }

        localStorage.setItem('loginUser', JSON.stringify(loginUser))
        setUser(loginUser)
        navigate('/match')
      } else {
        setResult(data.message || '登录失败')
      }
    } catch (err) {
      console.error(err)
      setResult('请求失败')
    }
  }

  return (
    <section className="auth-page auth-textonly">
      <div className="auth-brand">
        <h1 className="auth-brand-mark">SOUL</h1>
        <p className="auth-brand-tag">Find Your Universe</p>
      </div>

      <div className="auth-form">
        <input
          type="text"
          placeholder={text.name || '用户名'}
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />

        <input
          type="password"
          placeholder={text.password || '密码'}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
        />

        <div className="auth-buttons">
          <button onClick={handleLogin}>{text.loginBtn || '登录'}</button>
          <button
            type="button"
            className="switch-btn"
            onClick={() => navigate('/register')}
          >
            {text.registerBtn || '注册'}
          </button>
        </div>

        {result && <p className="auth-result">{result}</p>}
      </div>
    </section>
  )
}

export default Login
