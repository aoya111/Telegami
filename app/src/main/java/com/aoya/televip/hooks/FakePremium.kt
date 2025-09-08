package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

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
