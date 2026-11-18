package com.example.soul_android.data

import android.content.Context
import com.example.soul_android.models.Message
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 聊天记录本地缓存：断网时仍可查看历史消息。
 * 图片由 Coil 磁盘缓存兜底（看过的图片离线仍可见）。
 */
object LocalMessageStore {

    private const val PREFS = "soul_chat_cache"
    private var appContext: Context? = null
    private val gson = Gson()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun key(me: String, partner: String) = "chat_${me}_$partner"

    fun save(me: String, partner: String, messages: List<Message>) {
        val ctx = appContext ?: return
        try {
            val json = gson.toJson(messages)
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(key(me, partner), json)
                .apply()
        } catch (_: Exception) {
        }
    }

    fun load(me: String, partner: String): List<Message>? {
        val ctx = appContext ?: return null
        val json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key(me, partner), null)
            ?: return null
        return try {
            val type = object : TypeToken<List<Message>>() {}.type
            gson.fromJson<List<Message>>(json, type)
        } catch (_: Exception) {
            null
        }
    }
}
