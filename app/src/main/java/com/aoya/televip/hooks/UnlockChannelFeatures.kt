package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class UnlockChannelFeatures :
    Hook(
        "unlock_channel_features",
        "Unlock all restricted and encrypted features for channels",
    ) {
    override fun init() {
        findClass("org.telegram.messenger.MessagesController")
            .hook(
                resolver.getMethod("org.telegram.messenger.MessagesController", "isChatNoForwards"),
                HookStage.BEFORE,
            ) { param -> param.setResult(false) }
    }
}
