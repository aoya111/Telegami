package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook

class ShowDeleteMessageBtn :
    Hook(
        "show_delete_msg_button",
        "Show 'Delete Messages' button",
    ) {
    override fun init() {
        findClass("org.telegram.messenger.MessageObject")
            .hook("canDeleteMessage", HookStage.BEFORE) { param -> param.setResult(true) }
    }
}
