package com.aoya.telegami.hooks

import android.content.SharedPreferences
import android.util.Log
import com.aoya.telegami.core.obfuscate.ResolverManager
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
        installSponsoredHook(xposed, "addSponsoredMessages") {
            classLoader.findMethod(CHAT_ACTIVITY_CN, "addSponsoredMessages")
        }
        installSponsoredHook(xposed, "getSponsoredMessages") {
            classLoader.findMethod(MESSAGES_CONTROLLER_CN, "getSponsoredMessages")
        }
        installProxySponsorHook(xposed, classLoader)
    }

    private fun installSponsoredHook(
        xposed: XposedInterface,
        name: String,
        resolve: () -> java.lang.reflect.Method,
    ) {
        try {
            xposed.hook(resolve()).intercept { null }
        } catch (throwable: Throwable) {
            Log.w("Telegami", "Unable to install $name", throwable)
        }
    }

    private fun installProxySponsorHook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        try {
            val controllerClass = classLoader.loadClass(ResolverManager.get(MESSAGES_CONTROLLER_CN))
            val promoCheck =
                controllerClass.declaredMethods.firstOrNull {
                    it.name.startsWith("lambda\$checkPromoInfo") && it.parameterCount == 1
                } ?: return
            val removePromoDialog =
                controllerClass.declaredMethods.firstOrNull {
                    it.name == "removePromoDialog" && it.parameterCount == 0
                } ?: return
            val globalSettings =
                controllerClass.declaredMethods.firstOrNull {
                    it.name == "getGlobalMainSettings" && it.parameterCount == 0
                } ?: return

            promoCheck.isAccessible = true
            removePromoDialog.isAccessible = true
            globalSettings.isAccessible = true
            xposed.hook(promoCheck).intercept { chain ->
                val result = chain.proceed()
                try {
                    removePromoDialog.invoke(chain.thisObject)
                    (globalSettings.invoke(null) as? SharedPreferences)
                        ?.edit()
                        ?.remove("proxy_dialog")
                        ?.remove("proxyDialogAddress")
                        ?.remove("nextPromoInfoCheckTime")
                        ?.commit()
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Failed to hide proxy sponsor", throwable)
                }
                result
            }
        } catch (throwable: Throwable) {
            Log.w("Telegami", "Unable to install proxy sponsor hook", throwable)
        }
    }
}
