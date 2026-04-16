package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object UnlockChannelFeatures : YukiBaseHooker() {
    const val MESSAGES_CONTROLLER_CN = "org.telegram.messenger.MessagesController"

    val messagesControllerClass by lazyClass(resolver.get(MESSAGES_CONTROLLER_CN))

    override fun onHook() {
        if (!Config.isFeatureEnabled("UnlockChannelFeatures")) return
        messagesControllerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(MESSAGES_CONTROLLER_CN, "isChatNoForwards")
            }.hook {
                replaceToFalse()
            }
    }
}
