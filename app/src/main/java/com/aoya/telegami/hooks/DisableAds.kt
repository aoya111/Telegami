package com.aoya.telegami.hooks

import android.util.Log
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.findMethod
import io.github.libxposed.api.XposedInterface

object DisableAds {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    const val MESSAGES_CONTROLLER_CN = "org.telegram.messenger.MessagesController"

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        if (!Config.isFeatureEnabled("DisableAds")) return
        listOf(
            classLoader.findMethod(CHAT_ACTIVITY_CN, "addSponsoredMessages") to null,
            classLoader.findMethod(MESSAGES_CONTROLLER_CN, "getSponsoredMessages") to null,
        ).forEach { (method, value) ->
            xposed.hook(method).intercept { chain ->
                val args = chain.args.toMutableList()
                var result: Any? = null
                var hasResult = false
                try {
                    result = value
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
}
