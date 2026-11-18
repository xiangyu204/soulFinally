package com.example.soul_android.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * 全局深浅主题状态（单例），与 LanguageState 同款：
 * 切换后全局生效，并持久化到 SharedPreferences。
 */
object ThemeState {

    private const val PREFS = "soul_theme_prefs"
    private const val KEY = "dark"

    /** 当前是否深色主题，Compose 可观察 */
    val isDark = mutableStateOf(true)

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val saved = appContext
            ?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.getBoolean(KEY, true)
        isDark.value = saved ?: true
    }

    fun toggle() {
        isDark.value = !isDark.value
        appContext
            ?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean(KEY, isDark.value)
            ?.apply()
    }
}
