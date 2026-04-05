package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.logd

class NekoBlock : Hook("NekoBlock") {
    override fun init() {
        if (Telegami.packageName != "tw.nekomimi.nekogram") return
        findAndHook("uo5", "g", HookStage.BEFORE, filter = { true }) { param ->
            logd("Neko is spying")
            param.setResult(null)
        }
    }
}
