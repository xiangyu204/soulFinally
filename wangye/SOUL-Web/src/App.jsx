// import './App.css'   // 原来的样式（已不用）

import './styles/global.css'

import './styles/home.css'
import './styles/auth.css'
import './styles/profile.css'
import './styles/match.css'
import './styles/chat.css'
import './styles/board.css'
import './styles/boardDetail.css'
import './styles/responsive.css'
import './styles/settings.css'
import './styles/quiz.css'

import './styles/navbar.css'
import './styles/theme.css'
import './styles/premium.css'
import './styles/ui-refine.css'
import './styles/ui-unify.css'
import './styles/app-theme.css'
import './styles/swiss.css'
import './styles/chat-layout.css'  // 规范层（间距/字阶/交互统一）；要回滚删掉这一行

import { useState, useEffect } from 'react'
import { NavLink, Routes, Route, useLocation, useNavigate } from 'react-router-dom'
import logoImg from './assets/logo-soul.png'
import { confirmDialog } from './ui/feedback'
import { IconUser, IconLogout } from './ui/Icons'

import defaultAvatar from './assets/default-avatar.png'

// 페이지 컴포넌트
import Home from './pages/Home'
import Match from './pages/Match'
import Chat from './pages/Chat'
import Login from './pages/Login'
import Register from './pages/Register'
import ProfileSetup from './pages/ProfileSetup'
import Profile from './pages/Profile'
import Board from './pages/Board'
import BoardDetail from './pages/BoardDetail'
import Friends from './pages/Friends'
import Settings from './pages/Settings'
import Quiz from './pages/Quiz'
import QuizTest from './pages/QuizTest'
import QuizResult from './pages/QuizResult'


// 다국어
import messages from './i18n/messages'
import FeedbackHost from './ui/FeedbackHost'

function App() {
  const location = useLocation()
  const navigate = useNavigate()


  const [lang, setLang] = useState(() => {
    return localStorage.getItem('lang') || 'zh'
  })

  // 현재 로그인 사용자
  const [user, setUser] = useState(() => {

    const savedUser =
      localStorage.getItem('loginUser')

    return savedUser
      ? JSON.parse(savedUser)
      : null
  })

  const [avatarMenuOpen, setAvatarMenuOpen] = useState(false)

  // 未登录时只允许访问登录 / 注册页
  const isAuthPage =
    location.pathname === '/login' || location.pathname === '/register'
  const isPublicPage = isAuthPage || location.pathname === '/'

  useEffect(() => {
    if (!user && !isPublicPage) {
      navigate('/login', { replace: true })
    }
  }, [user, isPublicPage, navigate])

  // 登录后访问登录页则回首页
  useEffect(() => {
    if (user && isAuthPage) {
      navigate('/match', { replace: true })
    }
  }, [user, isAuthPage, navigate])


  // 当前主题：dark 深色 / light 浅色
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('theme') || 'dark'
  })

  const text =
    messages[lang] || messages.zh

  // 语言切换器（登录后放在头像菜单里，未登录放在右侧）
  const langSwitcher = (
    <div className="lang-switch" role="group" aria-label="Language">
      {[
        { code: 'zh', label: '中' },
        { code: 'en', label: 'EN' },
        { code: 'ko', label: '한' }
      ].map((item) => (
        <button
          key={item.code}
          type="button"
          className={`lang-btn ${
            lang === item.code ? 'lang-btn-active' : ''
          }`}
          onClick={() => setLang(item.code)}
        >
          {item.label}
        </button>
      ))}
    </div>
  )

  // 언어 저장
  useEffect(() => {
    localStorage.setItem('lang', lang)
  }, [lang])

  // 主题保存 + 给页面加 data-theme 属性
  useEffect(() => {
    localStorage.setItem('theme', theme)
    document.documentElement.setAttribute('data-theme', theme)
  }, [theme])

  // 点击头像菜单以外的区域，关闭头像菜单
  useEffect(() => {
    if (!avatarMenuOpen) return

    const closeAvatarMenu = (e) => {
      if (!e.target.closest('.user-box')) {
        setAvatarMenuOpen(false)
      }
    }

    document.addEventListener('click', closeAvatarMenu)

    return () => {
      document.removeEventListener('click', closeAvatarMenu)
    }
  }, [avatarMenuOpen])

  // 退出登录（二次确认）
  const handleLogout = async () => {

    const ok = await confirmDialog({
      title: text.logout || '退出登录',
      message:
        lang === 'ko'
          ? '로그아웃 하시겠습니까?'
          : lang === 'en'
            ? 'Sign out of your account?'
            : '确定要退出登录吗？',
      confirmText: text.logout || '退出登录',
      cancelText:
        lang === 'ko' ? '취소' : lang === 'en' ? 'Cancel' : '取消',
      danger: true
    })

    if (!ok) return

    localStorage.removeItem('loginUser')

    setAvatarMenuOpen(false)

    setUser(null)
  }

  // 首页是独立营销页，不进入 App 外壳
  if (location.pathname === '/') {
    return (
      <div className="marketing-root">
        <FeedbackHost />
        <Home text={text} lang={lang} user={user} setLang={setLang} />
      </div>
    )
  }

  return (

    <div className="container">

      <FeedbackHost />

      {/* NAVBAR */}
      {user && (
      <nav className="navbar">

        {/* LOGO */}
        <div className="logo-title">
          <img className="logo-img" src={logoImg} alt="SOUL" />
        </div>

        {/* RIGHT */}
        <div className="nav-right">

          {/* NAVIGATION */}
          <div className="nav-links">


            <NavLink to="/match">
              {text.match}
            </NavLink>

            <NavLink to="/friends">
              {text.friends || (lang === 'ko' ? '친구' : lang === 'en' ? 'Friends' : '好友')}
            </NavLink>

            <NavLink to="/quiz">
              {text.quiz || 'AI测试'}
            </NavLink>

            <NavLink to="/chat">
              {text.chat}
            </NavLink>

            <NavLink to="/board">
              {text.board || '告示板'}
            </NavLink>

          </div>

          {/* USER */}
          {user ? (

            <div className="user-box">

              {/* SETTINGS OUTSIDE */}
              <NavLink
                to="/settings"
                className="profile-link settings-outside-link"
                onClick={() =>
                  setAvatarMenuOpen(false)
                }
              >
                {text.settings || '设置'}
              </NavLink>

              {/* AVATAR MENU BUTTON */}
              <button
                type="button"
                className="avatar-menu-button"
                onClick={() =>
                  setAvatarMenuOpen(!avatarMenuOpen)
                }
              >

                <img
                  src={
                    user.avatar &&
                    user.avatar.trim()
                      ? user.avatar
                      : defaultAvatar
                  }
                  alt="avatar"
                  className="user-avatar"
                  onError={(e) => {
                    e.target.src = defaultAvatar
                  }}
                />
              </button>

              {/* USERNAME */}
              <span className="profile-link username-link">
                {user.name || user.username}
              </span>

              {/* AVATAR MENU */}
              {avatarMenuOpen && (

                <div className="avatar-menu">

                  <div className="avatar-menu-top">

                    <img
                      src={
                        user.avatar &&
                        user.avatar.trim()
                          ? user.avatar
                          : defaultAvatar
                      }
                      alt="avatar"
                      className="avatar-menu-img"
                      onError={(e) => {
                        e.target.src = defaultAvatar
                      }}
                    />

                    <strong>
                      {user.name || user.username}
                    </strong>

                    <span>
                      {user.email || ''}
                    </span>

                  </div>

                  {/* 语言切换：放在头像下面 */}
                  <div className="avatar-lang">
                    <span className="avatar-lang-label">
                      {lang === 'zh' ? '语言' : lang === 'ko' ? '언어' : 'Language'}
                    </span>
                    {langSwitcher}
                  </div>

                  <NavLink
                    to="/profile"
                    className="avatar-menu-item"
                    onClick={() =>
                      setAvatarMenuOpen(false)
                    }
                  >
                    <IconUser /> {text.profile || '个人资料'}
                  </NavLink>

                  <button
                    type="button"
                    className="avatar-menu-item avatar-menu-logout"
                    onClick={handleLogout}
                  >
                    <IconLogout /> {text.logout || '退出登录'}
                  </button>

                </div>

              )}

            </div>

          ) : (

            <div className="auth-links">

              <div className="auth-lang">{langSwitcher}</div>

              <NavLink to="/settings">
                {text.settings || '设置'}
              </NavLink>

              <NavLink to="/login">
                {text.login}
              </NavLink>

            </div>

          )}

        </div>

      </nav>
      )}

      {/* ROUTES */}
      <Routes>

        <Route
          path="/"
          element={
            <Home
              text={text}
              lang={lang}
              setLang={setLang}
            />
          }
        />

        <Route
          path="/match"
          element={
            <Match
              text={text}
              user={user}
              lang={lang}
            />
          }
        />

        <Route
          path="/quiz"
          element={
            <Quiz
              text={text}
              user={user}
              lang={lang}
            />
          }
        />

        <Route
          path="/quiz/test"
          element={
            <QuizTest
              lang={lang}
          />
        }
       />

        <Route
         path="/quiz/result"
         element={
           <QuizResult
             lang={lang}
         />
      }
      />

        <Route
          path="/chat"
          element={
            <Chat
              text={text}
              user={user}
              lang={lang}
            />
          }
        />

        <Route
          path="/friends"
          element={<Friends />}
        />

        <Route
          path="/board"
          element={<Board />}
        />

        <Route
          path="/board/:id"
          element={<BoardDetail />}
        />

        <Route
          path="/login"
          element={
            <Login
              text={text}
              setUser={setUser}
            />
          }
        />

        <Route
          path="/register"
          element={
            <Register text={text} />
          }
        />

        <Route
          path="/profile-setup"
          element={
            <ProfileSetup
              text={text}
              lang={lang}
              setUser={setUser}
            />
          }
        />

        <Route
          path="/profile"
          element={
            <Profile
              key={`${user?.username || 'guest'}-${lang}`}
              text={text}
              user={user}
              setUser={setUser}
              lang={lang}
            />
          }
        />

        <Route
          path="/settings"
          element={
            <Settings
              text={text}
              user={user}
              lang={lang}
              setLang={setLang}
              theme={theme}
              setTheme={setTheme}
            />
          }
        />

      </Routes>

    </div>
  )
}

export default App