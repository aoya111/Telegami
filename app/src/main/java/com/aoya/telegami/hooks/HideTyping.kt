package com.aoya.telegami.hooks

import com.aoya.telegami.util.Hook
import com.aoya.telegami.util.HookStage

class HideTyping : Hook("HideTyping") {
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
