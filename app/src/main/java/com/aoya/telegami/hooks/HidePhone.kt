package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.ui.cells.DrawerProfileCell
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class HidePhone :
    Hook(
        "hide_phone",
        "Hide 'Phone' number",
    ) {
    override fun init() {
        if (Telegami.packageName == "tw.nekomimi.nekogram") return
        findClass(
            "org.telegram.ui.Cells.DrawerProfileCell",
        ).hook(resolver.getMethod("org.telegram.ui.Cells.DrawerProfileCell", "setUser"), HookStage.AFTER) { param ->
            if (!isEnabled) return@hook
            val o = DrawerProfileCell(param.thisObject())
            val user = TLRPC.User(param.arg<Any>(0))
            o.phoneTextView.setText("@${user.username}")
        }
    }
}
