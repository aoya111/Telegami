package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

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
