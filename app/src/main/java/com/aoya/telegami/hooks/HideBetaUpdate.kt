package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class HideBetaUpdate :
    Hook(
        "hide_beta_update",
        "Hide Telegram Beta Update",
    ) {
    override fun init() {
        if (Telegami.packageName != "org.telegram.messenger.beta") return
        findAndHook("org.telegram.messenger.ApplicationLoaderImpl", "isCustomUpdate", HookStage.BEFORE, filter = { true }) { param ->
            param.setResult(false)
        }
    }
}
