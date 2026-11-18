import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../api/client'
import defaultAvatar from '../assets/default-avatar.png'
import '../styles/setup.css'

const COUNTRIES = ["대한민국 (South Korea)", "中国", "日本", "United States", "United Kingdom", "Canada", "Australia", "New Zealand", "Deutschland", "France", "España", "Italia", "Nederland", "België", "Schweiz", "Österreich", "Sverige", "Norge", "Danmark", "Suomi", "Polska", "Česko", "Slovensko", "Magyarország", "România", "България", "Ελλάδα", "Türkiye", "Россия", "Україна", "Қазақстан", "ישראל", "العربية السعودية", "الإمارات العربية المتحدة", "مصر", "India", "Pakistan", "Bangladesh", "Việt Nam", "ไทย", "Indonesia", "Malaysia", "Singapore", "Philippines", "Монгол", "Brasil", "México", "Argentina", "Chile", "Colombia", "Perú", "South Africa", "Nigeria", "Kenya", "Maroc", "Ireland", "Portugal"]
const GENDERS = ['남성 / Male', '여성 / Female', '기타 / Other']
const TIME_SLOTS = ['09:00 - 12:00', '12:00 - 18:00', '18:00 - 22:00', '주말 / Weekend']

function ProfileSetup({ text, lang, setUser }) {
  const navigate = useNavigate()
  const pending = useMemo(() => {
    try {
      return JSON.parse(sessionStorage.getItem('pendingRegistration') || 'null')
    } catch {
      return null
    }
  }, [])

  const [form, setForm] = useState({
    age: '',
    gender: '',
    phone: '',
    nationality: '',
    address: '',
    timeSlot: ''
  })
  const [avatar, setAvatar] = useState('')
  const [uploading, setUploading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState('')

  useEffect(() => {
    if (!pending) navigate('/register')
  }, [pending, navigate])

  const update = (key, value) => setForm((prev) => ({ ...prev, [key]: value }))

  const avatarPreview = avatar
    ? (avatar.startsWith('http')
        ? avatar
        : `${import.meta.env.VITE_API_BASE_URL || ''}${avatar}`)
    : defaultAvatar

  const handleAvatar = async (file) => {
    if (!file) return
    if (!file.type.startsWith('image/')) {
      setResult('请选择图片文件')
      return
    }
    const fd = new FormData()
    fd.append('file', file)
    setUploading(true)
    setResult('')
    try {
      // 不要手动设置 Content-Type：交给浏览器/axios 自动带 boundary
      const res = await client.post('/api/upload', fd)
      const url = res.data?.url || ''
      if (url) {
        setAvatar(url)
      } else {
        setResult('头像上传失败')
      }
    } catch (e) {
      console.error(e)
      setResult('头像上传失败')
    } finally {
      setUploading(false)
    }
  }

  const submit = async () => {
    if (!pending) {
      navigate('/register')
      return
    }
    const missing = []
    if (!form.age) missing.push('年龄')
    if (!form.gender) missing.push('性别')
    if (!form.phone.trim()) missing.push('电话')
    if (!form.nationality) missing.push('国籍')
    if (missing.length) {
      setResult('请填写必填项：' + missing.join('、'))
      return
    }
    const ageNum = Number(form.age)
    if (!Number.isFinite(ageNum) || ageNum < 10 || ageNum > 100) {
      setResult('年龄需在 10-100 之间')
      return
    }

    setSubmitting(true)
    setResult('')
    try {
      const res = await client.post('/api/register', {
        username: pending.username,
        password: pending.password,
        email: pending.email,
        age: ageNum,
        gender: form.gender,
        phone: form.phone.trim(),
        nationality: form.nationality,
        address: form.address.trim(),
        timeSlot: form.timeSlot,
        avatar
      })
      const data = res.data || {}
      if (data.success) {
        sessionStorage.removeItem('pendingRegistration')
        const loginUser = {
          username: data.username || pending.username,
          name: data.name || pending.username,
          email: data.email || pending.email,
          avatar: avatar
            ? (avatar.startsWith('http')
                ? avatar
                : `${import.meta.env.VITE_API_BASE_URL || ''}${avatar}`)
            : '',
          role: data.role || 'USER'
        }
        localStorage.removeItem('loginUser')
        localStorage.setItem('loginUser', JSON.stringify(loginUser))
        setUser(loginUser)
        navigate('/')
      } else {
        setResult(data.message || '注册失败')
      }
    } catch (e) {
      console.error(e)
      setResult('注册失败，请稍后重试')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="setup-page">
      <header className="setup-head">
        <h1>完善个人资料</h1>
        <p>完成以下信息后即注册成功</p>
      </header>

      <div className="setup-card">
        <div className="setup-avatar">
          <img src={avatarPreview} alt="avatar" />
          <label className="setup-avatar-btn">
            {uploading ? '上传中...' : '更换头像'}
            <input
              type="file"
              accept="image/*"
              onChange={(e) => handleAvatar(e.target.files?.[0])}
              disabled={uploading}
            />
          </label>
        </div>

        <div className="setup-grid">
          <label className="setup-field">
            <span>年龄 <i>*</i></span>
            <input
              type="number"
              min="10"
              max="100"
              value={form.age}
              onChange={(e) => update('age', e.target.value)}
              placeholder="例如 22"
            />
          </label>

          <label className="setup-field">
            <span>性别 <i>*</i></span>
            <select value={form.gender} onChange={(e) => update('gender', e.target.value)}>
              <option value="">请选择</option>
              {GENDERS.map((g) => <option key={g} value={g}>{g}</option>)}
            </select>
          </label>

          <label className="setup-field">
            <span>电话 <i>*</i></span>
            <input
              type="tel"
              value={form.phone}
              onChange={(e) => update('phone', e.target.value)}
              placeholder="010-0000-0000"
            />
          </label>

          <label className="setup-field">
            <span>国籍 <i>*</i></span>
            <select value={form.nationality} onChange={(e) => update('nationality', e.target.value)}>
              <option value="">请选择国籍</option>
              {COUNTRIES.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </label>

          <label className="setup-field">
            <span>地址 <em>(选填)</em></span>
            <input
              type="text"
              value={form.address}
              onChange={(e) => update('address', e.target.value)}
              placeholder="城市 / 住址"
            />
          </label>

          <label className="setup-field">
            <span>学习时段 <em>(选填)</em></span>
            <select value={form.timeSlot} onChange={(e) => update('timeSlot', e.target.value)}>
              <option value="">请选择</option>
              {TIME_SLOTS.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </label>
        </div>

        {result && <p className="setup-result">{result}</p>}

        <div className="setup-actions">
          <button
            type="button"
            className="setup-back"
            onClick={() => navigate('/register')}
          >
            返回
          </button>
          <button
            type="button"
            className="setup-submit"
            onClick={submit}
            disabled={submitting || uploading}
          >
            {submitting ? '注册中...' : '完成注册'}
          </button>
        </div>
      </div>
    </section>
  )
}

export default ProfileSetup
