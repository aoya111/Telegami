package com.aoya.telegami.hooks

import android.content.SharedPreferences
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
            val promoCheck = classLoader.findMethod(MESSAGES_CONTROLLER_CN, "checkPromoInfoInternal", 1)
            val removePromoDialog = classLoader.findMethod(MESSAGES_CONTROLLER_CN, "removePromoDialog", 0)
            val globalSettings = classLoader.findMethod(MESSAGES_CONTROLLER_CN, "getGlobalMainSettings", 0)

            fun clearPromoPrefs() {
                runCatching {
                    (globalSettings.invoke(null) as? SharedPreferences)
                        ?.edit()
                        ?.remove("proxy_dialog")
                        ?.remove("proxyDialogAddress")
                        ?.remove("nextPromoInfoCheckTime")
                        ?.remove("promo_dialog_type")
                        ?.remove("promo_psa_message")
                        ?.remove("promo_psa_type")
                        ?.commit()
                }.onFailure {
                    Log.w("Telegami", "Failed to clear proxy promo prefs: ${it.message}")
                }
            }

            xposed.hook(promoCheck).intercept { chain ->
                runCatching { removePromoDialog.invoke(chain.thisObject) }
                    .onFailure { Log.e("Telegami", "Failed to remove proxy promo dialog", it) }
                clearPromoPrefs()
                null
            }
        } catch (throwable: Throwable) {
            Log.w("Telegami", "Unable to install proxy sponsor hook", throwable)
        }
    }
}
