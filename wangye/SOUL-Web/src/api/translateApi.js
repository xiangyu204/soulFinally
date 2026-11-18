import { cachedTranslate } from './translateCache'

/**
 * Thin re-export kept for existing imports.
 * All translation now goes through the shared cache so repeated calls
 * (polling, re-renders) never spam /api/translate.
 */
export async function translateText(text, targetLang) {
  return cachedTranslate(text, targetLang)
}

export { cachedTranslate, cachedTranslateMany } from './translateCache'
