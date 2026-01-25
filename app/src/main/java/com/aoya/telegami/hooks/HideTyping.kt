package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class HideTyping :
    Hook(
        "hide_typing",
        "Hide typing",
    ) {
    override fun init() {
        findAndHook(
            "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate",
            "needSendTyping",
            HookStage.BEFORE,
        ) { param ->
            param.setResult(null)
        }
    }
}
