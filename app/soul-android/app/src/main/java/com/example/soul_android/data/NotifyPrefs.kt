package com.example.soul_android.data

import android.content.Context

/** 通知开关持久化 */
object NotifyPrefs {
    private const val PREFS = "soul_notify_prefs"
    private const val KEY = "enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY, enabled)
            .apply()
    }
}
