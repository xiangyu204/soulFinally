package com.example.soul_android.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.example.soul_android.models.AppLanguage

/**
 * 全局语言状态（单例）。
 * 所有屏幕共享同一份语言状态，切换后全局生效，并持久化到 SharedPreferences。
 */
object LanguageState {

    private const val PREFS = "soul_language_prefs"
    private const val KEY = "language"

    /** 当前语言，Compose 可观察（屏幕用 by 委托读取会自动重组） */
    val language = mutableStateOf(AppLanguage.KOREAN)

    private var appContext: Context? = null

    /** 在 MainActivity.onCreate 调用一次，读取已保存的语言 */
    fun init(context: Context) {
        appContext = context.applicationContext
        val saved = appContext
            ?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.getString(KEY, null)
        if (saved != null) {
            language.value = runCatching { AppLanguage.valueOf(saved) }
                .getOrDefault(AppLanguage.KOREAN)
        }
    }

    /** 切换语言并持久化 */
    fun setLanguage(lang: AppLanguage) {
        language.value = lang
        appContext
            ?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY, lang.name)
            ?.apply()
    }
}
