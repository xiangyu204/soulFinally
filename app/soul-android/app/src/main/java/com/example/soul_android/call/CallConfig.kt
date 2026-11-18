package com.example.soul_android.call

/**
 * 语音/视频通话配置（声网 Agora）。
 *
 * 测试前请在 https://console.agora.io 注册项目，获取 AppId 填入下方。
 *
 * - AGORA_APP_ID：必填，否则无法初始化 RtcEngine。
 * - AGORA_TOKEN：留空 "" 表示使用「App ID 无鉴权」测试模式；
 *   生产环境需在服务端生成临时 Token 后填入。
 */
object CallConfig {

    const val AGORA_APP_ID = "d24663ce4dc5456e90b2a483e95df278"

    const val AGORA_TOKEN = ""

    /** 通话频道名前缀，避免与其它项目冲突 */
    const val CHANNEL_PREFIX = "soul_call_"
}
