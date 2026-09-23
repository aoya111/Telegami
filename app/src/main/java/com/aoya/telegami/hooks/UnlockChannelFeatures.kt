package com.aoya.telegami.hooks

import com.aoya.telegami.service.Config
import com.aoya.telegami.util.findMethod
import io.github.libxposed.api.XposedInterface
import android.util.Log

object UnlockChannelFeatures {
    const val MESSAGES_CONTROLLER_CN = "org.telegram.messenger.MessagesController"

    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        if (!Config.isFeatureEnabled("UnlockChannelFeatures")) return
        val method = classLoader.findMethod(MESSAGES_CONTROLLER_CN, "isChatNoForwards")
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                result = false
                hasResult = true
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${method.name}", throwable)
            }
            if (!hasResult) {
                result = chain.proceed(args.toTypedArray())
                hasResult = true
            }
            result
        }
    }
}
