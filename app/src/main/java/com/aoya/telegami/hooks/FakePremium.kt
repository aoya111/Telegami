package com.aoya.telegami.hooks

import android.util.Log
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.virt.tgnet.TLRPC
import io.github.libxposed.api.XposedInterface

object FakePremium {
    const val USER_CONFIG_CN = "org.telegram.messenger.UserConfig"

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        if (!Config.isFeatureEnabled("FakePremium")) return

        runCatching {
            val isPremium = classLoader.findMethod(USER_CONFIG_CN, "isPremium", 0)
            xposed.hook(isPremium).intercept { true }
        }.onFailure {
            Log.w("Telegami", "FakePremium isPremium unavailable: ${it.message}")
        }

        // isPremium() only reads currentUser.premium; other UI paths read the
        // field or MessagesController.isPremiumUser(user) directly.
        runCatching {
            val getCurrentUser = classLoader.findMethod(USER_CONFIG_CN, "getCurrentUser", 0)
            xposed.hook(getCurrentUser).intercept { chain ->
                val result = chain.proceed(chain.args.toTypedArray())
                try {
                    if (result != null) {
                        TLRPC.User(result).premium = true
                    }
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Failed to set premium on current user", throwable)
                }
                result
            }
        }.onFailure {
            Log.w("Telegami", "FakePremium getCurrentUser unavailable: ${it.message}")
        }
    }
}
