package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook

class HideTyping :
    Hook(
        "hide_typing",
        "Hide typing",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate",
        ).hook("needSendTyping", HookStage.BEFORE) { param -> param.setResult(null) }
    }
}
