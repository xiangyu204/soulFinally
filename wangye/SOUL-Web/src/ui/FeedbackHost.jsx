import { useEffect, useRef, useState } from 'react'
import { subscribe } from './feedback'
import './feedback.css'

const ICONS = {
  success: '✓',
  error: '!',
  warn: '!',
  info: 'i'
}

export default function FeedbackHost() {
  const [toasts, setToasts] = useState([])
  const [dialog, setDialog] = useState(null)
  const timers = useRef(new Map())

  useEffect(() => {
    const unsubscribe = subscribe((event) => {
      if (event.kind === 'toast') {
        setToasts((prev) => [...prev.slice(-4), event])
        const timer = setTimeout(() => {
          setToasts((prev) => prev.filter((t) => t.id !== event.id))
          timers.current.delete(event.id)
        }, event.duration || 2600)
        timers.current.set(event.id, timer)
        return
      }

      if (event.kind === 'confirm') {
        setDialog(event)
        return
      }

      if (event.kind === 'dismiss') {
        setToasts((prev) => prev.filter((t) => t.id !== event.id))
      }
    })

    return () => {
      unsubscribe()
      timers.current.forEach((t) => clearTimeout(t))
      timers.current.clear()
    }
  }, [])

  const closeDialog = (result) => {
    if (dialog?.resolve) dialog.resolve(result)
    setDialog(null)
  }

  return (
    <>
      <div className="soul-toast-stack" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`soul-toast soul-toast-${t.type}`} role="status">
            <span className="soul-toast-icon">{ICONS[t.type] || ICONS.info}</span>
            <span className="soul-toast-text">{t.message}</span>
            <button
              type="button"
              className="soul-toast-close"
              onClick={() => setToasts((prev) => prev.filter((x) => x.id !== t.id))}
              aria-label="关闭"
            >
              ×
            </button>
          </div>
        ))}
      </div>

      {dialog && (
        <div className="soul-dialog-mask" onClick={() => closeDialog(false)}>
          <div
            className="soul-dialog"
            role="dialog"
            aria-modal="true"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="soul-dialog-head">
              <span className={`soul-dialog-badge ${dialog.danger ? 'is-danger' : ''}`}>
                {dialog.danger ? '!' : 'i'}
              </span>
              <h3>{dialog.title}</h3>
            </div>

            {dialog.message && <p className="soul-dialog-text">{dialog.message}</p>}

            <div className="soul-dialog-actions">
              <button
                type="button"
                className="soul-dialog-btn ghost"
                onClick={() => closeDialog(false)}
              >
                {dialog.cancelText}
              </button>
              <button
                type="button"
                className={`soul-dialog-btn ${dialog.danger ? 'danger' : 'primary'}`}
                onClick={() => closeDialog(true)}
              >
                {dialog.confirmText}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
