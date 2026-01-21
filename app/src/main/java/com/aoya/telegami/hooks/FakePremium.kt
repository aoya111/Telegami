package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class FakePremium :
    Hook(
        "fake_premium",
        "Enable premium features",
    ) {
    override fun init() {
        findClass(
            "org.telegram.messenger.UserConfig",
        ).hook(resolver.getMethod("org.telegram.messenger.UserConfig", "isPremium"), HookStage.BEFORE) { param ->
            param.setResult(true)
        }
    }
}
