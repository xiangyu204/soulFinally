/**
 * 统一线性图标（stroke 1.5，跟随 currentColor），替代全部 emoji。
 */
const base = {
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.5,
  strokeLinecap: 'round',
  strokeLinejoin: 'round'
}

export function IconUser({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...base} className="ico">
      <circle cx="12" cy="8" r="3.4" />
      <path d="M5 20c0-3.4 3.1-5.4 7-5.4s7 2 7 5.4" />
    </svg>
  )
}

export function IconLogout({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...base} className="ico">
      <path d="M14 4H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h7" />
      <path d="M17 8l4 4-4 4" />
      <path d="M21 12H10" />
    </svg>
  )
}

export function IconChat({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...base} className="ico">
      <path d="M20 11.5a7.5 7.5 0 0 1-7.5 7.5H9l-4 3v-4.6A7.5 7.5 0 0 1 12.5 4 7.5 7.5 0 0 1 20 11.5z" />
    </svg>
  )
}

export function IconPhone({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...base} className="ico">
      <path d="M6.5 4.5l2.8 1.1-.8 2.8-2.2.6a10.5 10.5 0 0 0 6.2 6.2l.6-2.2 2.8-.8 1.1 2.8c.2.6-.2 1.2-.8 1.3-5.7 1.2-12.8-5.9-11.6-11.6.1-.6.7-1 1.3-.8z" />
    </svg>
  )
}

export function IconVideo({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...base} className="ico">
      <rect x="3" y="6.5" width="12" height="11" rx="3" />
      <path d="M15 10.5l6-3.2v9.4l-6-3.2" />
    </svg>
  )
}

export function IconStar({ size = 16, filled = false }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      {...base}
      fill={filled ? 'currentColor' : 'none'}
      className="ico"
    >
      <path d="M12 4.2l2.4 4.9 5.4.8-3.9 3.8.9 5.4-4.8-2.5-4.8 2.5.9-5.4-3.9-3.8 5.4-.8z" />
    </svg>
  )
}

export function IconSparkle({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...base} className="ico">
      <path d="M12 3l1.6 4.4L18 9l-4.4 1.6L12 15l-1.6-4.4L6 9l4.4-1.6z" />
      <path d="M18.5 15.5l.8 2.2 2.2.8-2.2.8-.8 2.2-.8-2.2-2.2-.8 2.2-.8z" />
    </svg>
  )
}

export function IconClose({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...base} className="ico">
      <path d="M6 6l12 12M18 6L6 18" />
    </svg>
  )
}

export function IconCheck({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...base} className="ico">
      <path d="M4.5 12.5l5 5L19.5 7" />
    </svg>
  )
}

export function IconRobot({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" {...base} className="ico">
      <rect x="4" y="8" width="16" height="11" rx="3" />
      <path d="M12 4v4" />
      <circle cx="9" cy="13.5" r="1.1" />
      <circle cx="15" cy="13.5" r="1.1" />
    </svg>
  )
}
