package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object FakePremium : YukiBaseHooker() {
    const val USER_CONFIG_CN = "org.telegram.messenger.UserConfig"
    val userConfigClass by lazyClass(resolver.get(USER_CONFIG_CN))

    override fun onHook() {
        if (!Config.isFeatureEnabled("FakePremium")) return
        userConfigClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(USER_CONFIG_CN, "isPremium")
            }.hook {
                replaceToTrue()
            }
    }
}
