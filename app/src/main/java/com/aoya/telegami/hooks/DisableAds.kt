package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class DisableAds : Hook("DisableAds") {
    override fun init() {
        findAndHook("org.telegram.ui.ChatActivity", "addSponsoredMessages", HookStage.BEFORE) { param ->
            param.setResult(null)
        }

        findAndHook("org.telegram.ui.ChatActivity", "getSponsoredMessagesCount", HookStage.BEFORE) { param ->
            param.setResult(0)
        }

        findAndHook("org.telegram.messenger.MessagesController", "getSponsoredMessages", HookStage.BEFORE) { param ->
            param.setResult(null)
        }
    }
}
