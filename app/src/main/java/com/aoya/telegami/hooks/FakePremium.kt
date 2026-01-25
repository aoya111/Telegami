package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class FakePremium :
    Hook(
        "fake_premium",
        "Enable premium features",
    ) {
    override fun init() {
        findAndHook("org.telegram.messenger.UserConfig", "isPremium", HookStage.BEFORE, filter = { true }) { param ->
            param.setResult(true)
        }
    }
}
