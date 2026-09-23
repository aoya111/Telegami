package com.aoya.telegami.hooks

import com.aoya.telegami.service.Config
import com.aoya.telegami.util.findMethod
import io.github.libxposed.api.XposedInterface
import android.util.Log

object FakePremium {
    const val USER_CONFIG_CN = "org.telegram.messenger.UserConfig"
    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        if (!Config.isFeatureEnabled("FakePremium")) return
        val method = classLoader.findMethod(USER_CONFIG_CN, "isPremium")
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                result = true
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
