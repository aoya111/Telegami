package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.util.Hook
import com.aoya.telegami.util.HookStage

class HideBetaUpdate : Hook("HideBetaUpdate") {
    override fun init() {
        if (Telegami.packageName != "org.telegram.messenger.beta") return
        findAndHook("org.telegram.messenger.ApplicationLoaderImpl", "isCustomUpdate", HookStage.BEFORE, filter = { true }) { param ->
            param.setResult(false)
        }
    }
}
