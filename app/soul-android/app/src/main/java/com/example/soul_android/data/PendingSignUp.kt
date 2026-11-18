package com.example.soul_android.data

/**
 * 注册分两步：注册页只校验并暂存，资料设置完成后才真正写库。
 */
object PendingSignUp {
    var username: String = ""
    var email: String = ""
    var password: String = ""

    fun clear() {
        username = ""
        email = ""
        password = ""
    }

    fun isReady(): Boolean =
        username.isNotBlank() && email.isNotBlank() && password.isNotBlank()
}
