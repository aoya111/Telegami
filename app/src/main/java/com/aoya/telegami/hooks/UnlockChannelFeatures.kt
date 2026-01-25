package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class UnlockChannelFeatures :
    Hook(
        "unlock_channel_features",
        "Unlock all restricted and encrypted features for channels",
    ) {
    override fun init() {
        findAndHook(
            "org.telegram.messenger.MessagesController",
            "isChatNoForwards",
            HookStage.BEFORE,
        ) { param ->
            param.setResult(false)
        }
    }
}
