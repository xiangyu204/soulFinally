package com.example.soul_android.data

import android.content.Context
import com.example.soul_android.BuildConfig

/**
 * 运行时服务器地址配置（不再写死端口/IP）。
 * 用户可在登录页「服务器设置」填入公网地址（如 https://xxx.trycloudflare.com/）。
 */
object ServerConfig {

    private const val PREFS = "server_prefs"
    private const val KEY = "base_url"

    @Volatile
    var baseUrl: String = BuildConfig.API_BASE_URL

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        baseUrl = prefs.getString(KEY, BuildConfig.API_BASE_URL) ?: BuildConfig.API_BASE_URL
    }

    fun setBaseUrl(context: Context, url: String) {
        val normalized = normalize(url)
        baseUrl = normalized
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, normalized)
            .apply()
    }

    fun reset(context: Context) {
        baseUrl = BuildConfig.API_BASE_URL
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY)
            .apply()
    }

    private fun normalize(url: String): String {
        var u = url.trim()
        if (u.isEmpty()) return BuildConfig.API_BASE_URL
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "http://$u"
        }
        if (!u.endsWith("/")) u = "$u/"
        return u
    }
}
