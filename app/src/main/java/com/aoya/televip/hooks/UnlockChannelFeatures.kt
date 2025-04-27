package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook

class UnlockChannelFeatures :
    Hook(
        "unlock_channel_features",
        "Unlock all restricted and encrypted features for channels",
    ) {
    override fun init() {
        findClass("org.telegram.messenger.MessagesController")
            .hook("isChatNoForwards", HookStage.BEFORE) { param -> param.setResult(false) }
    }
}
