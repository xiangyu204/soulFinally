import client from './client'

/**
 * Cached translation helper.
 *
 * Why: pages like Board / Chat / Match call translate on every render or poll
 * (Chat polls messages every 2s). Without a cache each poll fired dozens of
 * POST /api/translate requests, which left pages stuck on "loading".
 *
 * This module keeps:
 *  - a memory cache keyed by `lang::text`
 *  - an in-flight map so concurrent identical requests share one promise
 *  - a small localStorage layer so a reload does not re-translate everything
 */

const MEM = new Map()
const INFLIGHT = new Map()
const LS_PREFIX = 'trc2:'
const LS_MAX_ENTRIES = 800
const LS_DROP_COUNT = 400

let lsKeys = null

function lsAllKeys() {
  if (lsKeys) return lsKeys
  try {
    lsKeys = Object.keys(localStorage).filter((k) => k.startsWith(LS_PREFIX))
  } catch (e) {
    lsKeys = []
  }
  return lsKeys
}

function lsGet(key) {
  try {
    const raw = localStorage.getItem(LS_PREFIX + key)
    if (raw == null) return undefined
    return JSON.parse(raw)
  } catch (e) {
    return undefined
  }
}

function lsSet(key, value) {
  try {
    const keys = lsAllKeys()
    if (keys.length >= LS_MAX_ENTRIES) {
      // drop the oldest batch to keep storage bounded
      keys.slice(0, LS_DROP_COUNT).forEach((k) => {
        localStorage.removeItem(k)
      })
      lsKeys = null
    }
    localStorage.setItem(LS_PREFIX + key, JSON.stringify(value))
    if (Array.isArray(lsKeys)) lsKeys.push(LS_PREFIX + key)
  } catch (e) {
    /* storage full or disabled: memory cache still works */
  }
}

const SKIP = /^[\s\d\p{P}\p{S}]*$/u

function keyOf(text, lang) {
  return lang + '::' + text
}

function pickTranslation(data) {
  if (typeof data === 'string') return data
  if (!data || typeof data !== 'object') return ''
  return (
    data.translatedText ||
    data.translation ||
    data.text ||
    data.result ||
    data.data ||
    ''
  )
}

async function requestTranslation(text, lang) {
  const res = await client.post('/api/translate', { text, targetLang: lang })
  return pickTranslation(res.data)
}

/**
 * The backend uses an LLM, which occasionally replies with a question about
 * the request (e.g. "请问您需要翻译成什么目标语言呢？") instead of a
 * translation. Treat those meta replies as failures so the UI keeps the
 * original text instead of showing the prompt.
 */
const META_REPLY = /目标语言|需要翻译成|请提供|请问您|translate into|target language/i

function isUsable(input, output) {
  if (!output || typeof output !== 'string') return false
  const out = output.trim()
  if (!out) return false
  if (out === input.trim()) return false
  if (META_REPLY.test(out) && !META_REPLY.test(input)) return false
  return true
}

/**
 * Translate `text` into `lang`. Never throws: returns the original text on
 * failure so the UI keeps rendering.
 */
export async function cachedTranslate(text, lang) {
  if (!text || typeof text !== 'string') return text
  if (!lang) return text
  if (text.length > 2000) return text
  if (SKIP.test(text)) return text

  const key = keyOf(text, lang)

  const hitMem = MEM.get(key)
  if (hitMem !== undefined) return hitMem

  const hitLs = lsGet(key)
  if (hitLs !== undefined && hitLs !== null) {
    MEM.set(key, hitLs)
    return hitLs
  }

  const inflight = INFLIGHT.get(key)
  if (inflight) return inflight

  const p = requestTranslation(text, lang)
    .then((out) => {
      const value = isUsable(text, out) ? out.trim() : text
      MEM.set(key, value)
      lsSet(key, value)
      return value
    })
    .catch(() => text)
    .finally(() => {
      INFLIGHT.delete(key)
    })

  INFLIGHT.set(key, p)
  return p
}

/** Translate many strings, de-duplicated, in parallel but bounded. */
export async function cachedTranslateMany(texts, lang) {
  const list = [...new Set(texts.filter(Boolean))]
  const out = {}
  await Promise.all(
    list.map(async (t) => {
      out[t] = await cachedTranslate(t, lang)
    })
  )
  return out
}

export function clearTranslationCache() {
  MEM.clear()
  INFLIGHT.clear()
  try {
    lsAllKeys().forEach((k) => localStorage.removeItem(k))
    lsKeys = null
  } catch (e) {
    /* ignore */
  }
}
