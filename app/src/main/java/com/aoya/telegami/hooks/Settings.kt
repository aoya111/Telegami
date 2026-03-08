package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.aoya.telegami.core.User
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.tgnet.TLRPC

class Settings : Hook("Settings") {
    override fun init() {
        findAndHook("org.telegram.messenger.UserConfig", "setCurrentUser", HookStage.AFTER, filter = { true }) { param ->
            val tgUser = TLRPC.User(param.arg<Any>(0))
            val user = User(tgUser.id, tgUser.username)
            Config.setUser(user)
        }
    }
}
