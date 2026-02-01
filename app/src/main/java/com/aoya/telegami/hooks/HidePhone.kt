package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.ui.cells.DrawerProfileCell

class HidePhone :
    Hook(
        "hide_phone",
        "Hide 'Phone' number",
    ) {
    override fun init() {
        if (Telegami.packageName in listOf("it.octogram.android", "tw.nekomimi.nekogram", "xyz.nextalone.nagram")) return
        findAndHook("org.telegram.ui.Cells.DrawerProfileCell", "setUser", HookStage.AFTER) { param ->
            val o = DrawerProfileCell(param.thisObject())
            val user = TLRPC.User(param.arg<Any>(0))
            o.phoneTextView.setText("@${user.username}")
        }
    }
}
