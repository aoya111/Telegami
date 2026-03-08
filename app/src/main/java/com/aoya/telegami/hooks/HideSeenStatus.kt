package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class HideSeenStatus : Hook("HideSeenStatus") {
    override fun init() {
        findAndHook("org.telegram.messenger.MessagesController", "completeReadTask", HookStage.BEFORE) { param ->
            param.setResult(null)
        }
    }
}
