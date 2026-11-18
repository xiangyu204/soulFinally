import { useNavigate } from 'react-router-dom'
import '../styles/home-saas.css'

const I = { fill: 'none', stroke: 'currentColor', strokeWidth: 1.5, strokeLinecap: 'round', strokeLinejoin: 'round' }

function IconFlow({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...I}>
      <rect x="3" y="3" width="7" height="7" rx="2" />
      <rect x="14" y="14" width="7" height="7" rx="2" />
      <path d="M10 6.5h4a3 3 0 0 1 3 3v4" />
    </svg>
  )
}

function IconPulse({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...I}>
      <path d="M3 12h4l2.5-6 3.5 12 2.5-6h5" />
    </svg>
  )
}

function IconShield({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...I}>
      <path d="M12 3l7 3v6c0 4.2-2.9 7.6-7 9-4.1-1.4-7-4.8-7-9V6z" />
      <path d="M9 12.2l2.2 2.2L15.5 10" />
    </svg>
  )
}

function IconGlobe({ size = 14 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...I} strokeWidth={1.4}>
      <circle cx="12" cy="12" r="9" />
      <path d="M3 12h18" />
      <path d="M12 3c2.6 2.4 4 5.6 4 9s-1.4 6.6-4 9c-2.6-2.4-4-5.6-4-9s1.4-6.6 4-9z" />
    </svg>
  )
}

const LANGS = [
  { code: 'zh', label: '中文', short: '中' },
  { code: 'en', label: 'English', short: 'EN' },
  { code: 'ko', label: '한국어', short: '한' }
]

const COPY = {
  zh: {
    nav: [['首页', '/'], ['匹配', '/match'], ['好友', '/friends'], ['AI 测试', '/quiz'], ['聊天', '/chat'], ['告示板', '/board']],
    login: '登录',
    signup: '免费注册',
    entry: '进入匹配',
    titleA: '技能交换，',
    titleB: '互相成就。',
    langLabel: '全局翻译',
    sub: '你教中文或编程，对方教你韩语或设计。匹配、翻译与互评，都在一次对话里完成。',
    cta1: '开始匹配',
    cta2: '先测一下水平',
    trust: '语言 · 技术 · 音乐 · 设计',
    trustLine: '你能教的，都值得被交换。',
    features: {
      eyebrow: '为什么是 SOUL',
      title: '一次交换需要的一切，都在同一处',
      sub: '不用另找工具：找人、沟通、验证水平。',
      cards: [
        { t: '统一匹配流程', d: '按技能互补、语言偏好与可交换时段排序，双方都能教、也都能学，避免单方面索取。' },
        { t: '对话内翻译', d: '中文、韩文、英文在聊天中互译，原文保留、译文在下，跨语言沟通不丢信息。' },
        { t: '能力测评', d: '按技能自动出题，十道题给出等级，让双方在同一个标准上开始交换。' }
      ]
    },
    foot: '© 2026 SOUL'
  },
  en: {
    nav: [['Home', '/'], ['Match', '/match'], ['Friends', '/friends'], ['AI Test', '/quiz'], ['Chat', '/chat'], ['Board', '/board']],
    login: 'Log in',
    signup: 'Sign up',
    entry: 'Go to match',
    titleA: 'Exchange a skill.',
    titleB: 'Both of us grow.',
    langLabel: 'Global translation',
    sub: 'Teach Chinese or code, learn Korean or design. Matching, translation and review all happen in one conversation.',
    cta1: 'Start matching',
    cta2: 'Test my level',
    trust: 'Language · Tech · Music · Design',
    trustLine: 'Anything you can teach is worth trading.',
    features: {
      eyebrow: 'WHY SOUL',
      title: 'Everything an exchange needs, in one place',
      sub: 'No extra tools: find a partner, talk, verify the level.',
      cards: [
        { t: 'Unified matching', d: 'Ranked by complementary skills, language preference and available hours — both sides teach and both sides learn.' },
        { t: 'Translation in chat', d: 'Chinese, Korean and English translated inline; the original stays, the translation sits below it.' },
        { t: 'Skill assessment', d: 'Ten generated questions per skill produce a level, so both sides start from the same standard.' }
      ]
    },
    foot: '© 2026 SOUL'
  },
  ko: {
    nav: [['홈', '/'], ['매칭', '/match'], ['친구', '/friends'], ['AI 테스트', '/quiz'], ['채팅', '/chat'], ['게시판', '/board']],
    login: '로그인',
    signup: '회원가입',
    entry: '매칭으로',
    titleA: '기술을 주고받고,',
    titleB: '서로를 완성합니다.',
    langLabel: '전체 번역',
    sub: '중국어나 코딩을 가르치고, 한국어나 디자인을 배웁니다. 매칭과 번역, 평가가 한 번의 대화 안에서 끝납니다.',
    cta1: '매칭 시작',
    cta2: '내 레벨 확인',
    trust: '언어 · 기술 · 음악 · 디자인',
    trustLine: '가르칠 수 있는 것은 무엇이든 교환할 가치가 있습니다.',
    features: {
      eyebrow: 'WHY SOUL',
      title: '교환에 필요한 모든 것, 한곳에서',
      sub: '별도 도구 없이: 파트너 찾기, 대화, 레벨 검증.',
      cards: [
        { t: '통합 매칭', d: '스킬 상호 보완, 언어 선호, 가능한 시간대를 기준으로 정렬합니다. 양쪽 모두 가르치고 배웁니다.' },
        { t: '대화 내 번역', d: '중국어·한국어·영어를 채팅에서 번역합니다. 원문은 그대로, 번역은 아래에 표시합니다.' },
        { t: '역량 평가', d: '스킬별 10문항으로 레벨을 산정해, 같은 기준에서 교환을 시작합니다.' }
      ]
    },
    foot: '© 2026 SOUL'
  }
}


function Home({ text, lang = 'zh', user, setLang }) {
  const navigate = useNavigate()
  const C = COPY[lang] || COPY.zh
  const icons = [<IconFlow key="f" />, <IconPulse key="p" />, <IconShield key="s" />]

  return (
    <div className="saas-outer">
      <div className="saas-shell">

        <section className="saas-hero">
          <div className="saas-hero-bg" aria-hidden="true" />
          <div className="saas-hero-veil" aria-hidden="true" />

          <nav className="saas-nav saas-nav-min">
            <div className="saas-nav-brand">SOUL</div>
            <div className="saas-nav-actions">
              <div className="saas-langbar" role="group" aria-label={C.langLabel}>
                <span className="saas-langbar-label">
                  <IconGlobe />
                  <span className="saas-langbar-label-text">{C.langLabel}</span>
                </span>
                <span className="saas-langbar-divider" aria-hidden="true" />
                {LANGS.map((item) => (
                  <button
                    key={item.code}
                    type="button"
                    className={`saas-langbar-btn ${lang === item.code ? 'is-active' : ''}`}
                    aria-pressed={lang === item.code}
                    onClick={() => setLang && setLang(item.code)}
                  >
                    <span className="saas-langbar-lbl-full">{item.label}</span>
                    <span className="saas-langbar-lbl-short">{item.short}</span>
                  </button>
                ))}
              </div>
              {user ? (
                <button type="button" className="saas-btn-solid" onClick={() => navigate('/match')}>{C.entry}</button>
              ) : (
                <>
                  <button type="button" className="saas-link" onClick={() => navigate('/login')}>{C.login}</button>
                  <button type="button" className="saas-btn-solid" onClick={() => navigate('/register')}>{C.signup}</button>
                </>
              )}
            </div>
          </nav>

          <div className="saas-hero-copy">
            <h1>
              {C.titleA}
              <br />
              <span className="saas-grad">{C.titleB}</span>
            </h1>
            <p className="saas-sub">{C.sub}</p>
            <div className="saas-cta">
              <button type="button" className="saas-btn-solid saas-btn-lg" onClick={() => navigate('/match')}>
                {C.cta1}
              </button>
              <button type="button" className="saas-btn-ghost saas-btn-lg" onClick={() => navigate('/quiz')}>
                {C.cta2}
              </button>
            </div>
          </div>

          <div className="saas-trust">
            <p className="saas-trust-label">{C.trust}</p>
            <div className="saas-trust-row">
              {['中文', '한국어', '日本語', 'English', 'Python', 'Java', 'UI 设计', '摄影', '吉他'].map((it) => (
                <span className="saas-trust-item" key={it}>{it}</span>
              ))}
            </div>
          </div>
        </section>

        <section className="saas-features">
          <header className="saas-features-head">
            <p className="saas-eyebrow">{C.features.eyebrow}</p>
            <h2>{C.features.title}</h2>
            <p className="saas-features-sub">{C.features.sub}</p>
          </header>

          <div className="saas-grid">
            {C.features.cards.map((card, i) => (
              <article className="saas-card" key={card.t}>
                <span className="saas-card-icon">{icons[i]}</span>
                <h3>{card.t}</h3>
                <p>{card.d}</p>
              </article>
            ))}
          </div>
        </section>

        <footer className="saas-foot">{C.foot}</footer>
      </div>
    </div>
  )
}

export default Home
