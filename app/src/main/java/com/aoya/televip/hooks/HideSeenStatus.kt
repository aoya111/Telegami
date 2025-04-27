package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook

class HideSeenStatus :
    Hook(
        "hide_seen_status",
        "Hide 'Seen' status for messages",
    ) {
    override fun init() {
        findClass("org.telegram.messenger.MessagesController")
            .hook("completeReadTask", HookStage.BEFORE) { param ->
                param.setResult(null)
            }
    }
}
