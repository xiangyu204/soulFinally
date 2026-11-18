/**
 * 全局反馈系统（toast + 确认弹窗）
 * 用法：
 *   import { toast, confirmDialog } from '../ui/feedback'
 *   toast('已保存', 'success')
 *   const ok = await confirmDialog({ title: '删除帖子', message: '删除后不可恢复', danger: true })
 */
const listeners = new Set()
let seq = 0

export function subscribe(fn) {
  listeners.add(fn)
  return () => listeners.delete(fn)
}

function emit(event) {
  listeners.forEach((fn) => fn(event))
}

export function toast(message, type = 'info', duration = 2600) {
  if (!message) return
  emit({ kind: 'toast', id: ++seq, message, type, duration })
}

toast.success = (m, d) => toast(m, 'success', d)
toast.error = (m, d) => toast(m, 'error', d)
toast.info = (m, d) => toast(m, 'info', d)
toast.warn = (m, d) => toast(m, 'warn', d)

export function confirmDialog(options = {}) {
  const {
    title = '确认操作',
    message = '',
    confirmText = '确认',
    cancelText = '取消',
    danger = false
  } = options

  return new Promise((resolve) => {
    emit({
      kind: 'confirm',
      id: ++seq,
      title,
      message,
      confirmText,
      cancelText,
      danger,
      resolve
    })
  })
}

export function dismiss(id) {
  emit({ kind: 'dismiss', id })
}
