package com.aoya.telegami.hooks

import com.aoya.telegami.util.Hook
import com.aoya.telegami.util.HookStage

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
