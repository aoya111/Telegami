package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class HideSeenStatus :
    Hook(
        "hide_seen_status",
        "Hide 'Seen' status for messages",
    ) {
    override fun init() {
        findClass("org.telegram.messenger.MessagesController")
            .hook(resolver.getMethod("org.telegram.messenger.MessagesController", "completeReadTask"), HookStage.BEFORE) { param ->
                if (!isEnabled) return@hook
                param.setResult(null)
            }
    }
}
