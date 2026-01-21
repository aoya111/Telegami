package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class HideTyping :
    Hook(
        "hide_typing",
        "Hide typing",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate",
        ).hook(
            resolver.getMethod("org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate", "needSendTyping"),
            HookStage.BEFORE,
        ) { param -> param.setResult(null) }
    }
}
