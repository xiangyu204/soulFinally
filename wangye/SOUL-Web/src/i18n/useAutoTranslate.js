import { useCallback, useEffect, useRef, useState } from 'react'
import { cachedTranslate } from '../api/translateCache'

/**
 * 全局自动翻译 hooks —— 所有组件共用同一套翻译通道。
 *
 * 数据流：组件调用 t('原文') → 命中缓存直接返回 →
 * 未命中则请求后端 /api/translate（内部走 DeepSeek）→ 回来后触发一次重渲染。
 *
 * 特点：
 *  - 同一条文本全站只请求一次（translateCache 负责去重与持久化）
 *  - 文本本来就是目标语言时直接返回，不发请求
 *  - 翻译失败自动回退原文，不会白屏
 */

const detectLang = (text) => {
  if (!text) return 'en'
  if (/[\uac00-\ud7af]/.test(text)) return 'ko'
  if (/[\u3040-\u30ff]/.test(text)) return 'ja'
  if (/[\u4e00-\u9fff]/.test(text)) return 'zh'
  if (/[\u0600-\u06ff]/.test(text)) return 'ar'
  return 'en'
}

export default function useAutoTranslate(lang) {
  const [, bump] = useState(0)
  const cache = useRef(new Map())
  const inflight = useRef(new Set())

  // 切换语言时清空本地渲染缓存（持久缓存仍在 translateCache 里）
  useEffect(() => {
    cache.current.clear()
    inflight.current.clear()
    bump((v) => v + 1)
  }, [lang])

  return useCallback(
    (text) => {
      if (!text || typeof text !== 'string') return text
      if (!lang) return text
      if (detectLang(text) === lang) return text

      const key = lang + '::' + text
      if (cache.current.has(key)) return cache.current.get(key)

      if (!inflight.current.has(key)) {
        inflight.current.add(key)
        cachedTranslate(text, lang)
          .then((out) => {
            inflight.current.delete(key)
            if (out && out !== text) {
              cache.current.set(key, out)
              bump((v) => v + 1)
            } else {
              cache.current.set(key, text)
            }
          })
          .catch(() => {
            inflight.current.delete(key)
          })
      }

      return text
    },
    [lang]
  )
}
