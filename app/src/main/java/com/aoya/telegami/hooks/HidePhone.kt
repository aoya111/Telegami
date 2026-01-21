package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class HidePhone :
    Hook(
        "hide_phone",
        "Hide 'Phone' number",
    ) {
    override fun init() {
        if (Telegami.packageName == "tw.nekomimi.nekogram") return
        findClass(
            "org.telegram.messenger.UserConfig",
        ).hook(resolver.getMethod("org.telegram.messenger.UserConfig", "getCurrentUser"), HookStage.AFTER) { param ->
            val user = TLRPC.User(param.getResult() ?: return@hook)
            val phone = if (isEnabled) "" else Config.getCurrentUser().phone
            user.phone = phone
        }
    }
}
