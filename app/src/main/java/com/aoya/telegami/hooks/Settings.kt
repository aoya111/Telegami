package com.aoya.telegami.hooks

import com.aoya.telegami.util.findMethod
import com.aoya.telegami.service.User
import com.aoya.telegami.service.UserConfig
import com.aoya.telegami.util.loge
import com.aoya.telegami.util.logi
import com.aoya.telegami.virt.tgnet.TLRPC
import io.github.libxposed.api.XposedInterface

object Settings {
    const val USER_CONFIG_CN = "org.telegram.messenger.UserConfig"

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        xposed.hook(classLoader.findMethod(USER_CONFIG_CN, "setCurrentUser", 1)).intercept { chain ->
            val result = chain.proceed()
            try {
                val tgUser = TLRPC.User(chain.args[0]!!)
                UserConfig.setUser(User(tgUser.id, tgUser.username))
                logi("Captured current Telegram user")
            } catch (throwable: Throwable) {
                loge("Failed to capture current Telegram user", throwable)
            }
            result
        }
        logi("Installed setCurrentUser hook")
    }
}
