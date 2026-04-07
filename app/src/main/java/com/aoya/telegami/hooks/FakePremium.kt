package com.aoya.telegami.hooks

import com.aoya.telegami.util.Hook
import com.aoya.telegami.util.HookStage

class FakePremium : Hook("FakePremium") {
    override fun init() {
        findAndHook("org.telegram.messenger.UserConfig", "isPremium", HookStage.BEFORE) { param ->
            param.setResult(true)
        }
    }
}
