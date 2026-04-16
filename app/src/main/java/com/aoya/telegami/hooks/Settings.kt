package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.aoya.telegami.core.User
import com.aoya.telegami.virt.tgnet.TLRPC
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object Settings : YukiBaseHooker() {
    const val USER_CONFIG_CN = "org.telegram.messenger.UserConfig"
    val userConfigClass by lazyClass(resolver.get(USER_CONFIG_CN))

    override fun onHook() {
        userConfigClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(USER_CONFIG_CN, "setCurrentUser")
            }.hook {
                after {
                    val tgUser = TLRPC.User(args[0]!!)
                    val user = User(tgUser.id, tgUser.username)
                    Config.setUser(user)
                }
            }
    }
}
