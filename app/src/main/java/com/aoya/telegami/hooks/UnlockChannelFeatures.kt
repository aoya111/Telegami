package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

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
            ) { param ->
                if (!isEnabled) return@hook
                param.setResult(false)
            }
    }
}
