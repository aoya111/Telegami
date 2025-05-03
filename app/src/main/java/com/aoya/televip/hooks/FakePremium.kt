package com.aoya.televip.hooks

import com.aoya.televip.TeleVip
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class FakePremium :
    Hook(
        "enable_premium",
        "Enable premium features",
    ) {
    override fun init() {
        findClass(
            "org.telegram.messenger.UserConfig",
        ).hook(resolver.getMethod("org.telegram.messenger.UserConfig", "isPremium"), HookStage.BEFORE) { param ->
            param.setResult(true)
        }

        if (TeleVip.packageName == "com.iMe.android") {
            findClass("com.iMe.storage.data.locale.prefs.impl.h").hook("isPremium", HookStage.BEFORE) { param -> param.setResult(true) }
            findClass("com.iMe.utils.helper.ForkPremiumHelper").hook("isPremium", HookStage.BEFORE) { param -> param.setResult(true) }
        }

        if (TeleVip.packageName != "com.skyGram.bestt") {
            findClass(
                "org.telegram.messenger.MessagesController",
            ).hook("premiumFeaturesBlocked", HookStage.BEFORE) { param -> param.setResult(false) }
        }
    }
}
