package com.aoya.televip.hooks

import com.aoya.televip.core.Config
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.virt.tgnet.TLRPC
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class HidePhone :
    Hook(
        "hide_phone",
        "Hide 'Phone' number",
    ) {
    override fun init() {
        findClass(
            "org.telegram.messenger.UserConfig",
        ).hook(resolver.getMethod("org.telegram.messenger.UserConfig", "getCurrentUser"), HookStage.AFTER) { param ->
            val user = TLRPC.User(param.getResult() ?: return@hook)
            val phone = if (isEnabled) "" else Config.getCurrentUser().phone
            user.phone = phone
        }
    }
}
