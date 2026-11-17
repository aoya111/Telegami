package com.aoya.telegami.hooks

import android.util.Log
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.virt.tgnet.TLRPC
import io.github.libxposed.api.XposedInterface

object UnlockChannelFeatures {
    const val MESSAGES_CONTROLLER_CN = "org.telegram.messenger.MessagesController"

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        if (!Config.isFeatureEnabled("UnlockChannelFeatures")) return

        listOf("isChatNoForwards", "isChatNoForwardsById").forEach { name ->
            runCatching {
                val method = classLoader.findMethod(MESSAGES_CONTROLLER_CN, name, 1)
                xposed.hook(method).intercept { false }
            }.onFailure {
                Log.w("Telegami", "UnlockChannelFeatures $name unavailable: ${it.message}")
            }
        }

        // Some client-side code reads chat.noforwards directly instead of
        // calling isChatNoForwards(). Clear it on the cached chat objects.
        runCatching {
            val getChat = classLoader.findMethod(MESSAGES_CONTROLLER_CN, "getChat", 1)
            xposed.hook(getChat).intercept { chain ->
                val result = chain.proceed(chain.args.toTypedArray())
                try {
                    if (result != null) {
                        TLRPC.Chat(result).noforwards = false
                    }
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Failed to clear chat noforwards", throwable)
                }
                result
            }
        }.onFailure {
            Log.w("Telegami", "UnlockChannelFeatures getChat unavailable: ${it.message}")
        }
    }
}
