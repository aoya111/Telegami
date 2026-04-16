package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object DisableAds : YukiBaseHooker() {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    const val MESSAGES_CONTROLLER_CN = "org.telegram.messenger.MessagesController"

    val chatActivityClass by lazyClass(resolver.get(CHAT_ACTIVITY_CN))
    val messagesControllerClass by lazyClass(resolver.get(MESSAGES_CONTROLLER_CN))

    override fun onHook() {
        if (!Config.isFeatureEnabled("DisableAds")) return
        chatActivityClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CHAT_ACTIVITY_CN, "addSponsoredMessages")
            }.hook {
                before {
                    resultNull()
                }
            }
        chatActivityClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CHAT_ACTIVITY_CN, "getSponsoredMessagesCount")
            }.hook {
                before {
                    result = 0
                }
            }
        messagesControllerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(MESSAGES_CONTROLLER_CN, "getSponsoredMessages")
            }.hook {
                before {
                    resultNull()
                }
            }
    }
}
