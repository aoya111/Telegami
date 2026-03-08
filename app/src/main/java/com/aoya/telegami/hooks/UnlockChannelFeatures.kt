package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class UnlockChannelFeatures : Hook("UnlockChannelFeatures") {
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
