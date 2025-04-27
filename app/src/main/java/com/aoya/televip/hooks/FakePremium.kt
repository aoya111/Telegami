package com.aoya.televip.hooks

import com.aoya.televip.TeleVip
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook

class FakePremium :
    Hook(
        "enable_premium",
        "Enable premium features",
    ) {
    override fun init() {
        if (TeleVip.packageName == "com.iMe.android") {
            findClass("com.iMe.storage.data.locale.prefs.impl.h").hook("isPremium", HookStage.BEFORE) { param -> param.setResult(true) }
            findClass("com.iMe.utils.helper.ForkPremiumHelper").hook("isPremium", HookStage.BEFORE) { param -> param.setResult(true) }
        }
        findClass("org.telegram.messenger.UserConfig").hook("isPremium", HookStage.BEFORE) { param -> param.setResult(true) }
        if (TeleVip.packageName != "com.skyGram.bestt") {
            findClass(
                "org.telegram.messenger.MessagesController",
            ).hook("premiumFeaturesBlocked", HookStage.BEFORE) { param -> param.setResult(false) }
        }
    }
}
