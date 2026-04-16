package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object HideTyping : YukiBaseHooker() {
    const val CHAT_ACTIVITY_ENTER_VIEW_DELEGATE_CN = "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate"
    val chatActivityEnterViewDelegateClass by lazyClass(resolver.get(CHAT_ACTIVITY_ENTER_VIEW_DELEGATE_CN))

    override fun onHook() {
        if (!Config.isFeatureEnabled("HideTyping")) return

        chatActivityEnterViewDelegateClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CHAT_ACTIVITY_ENTER_VIEW_DELEGATE_CN, "needSendTyping")
            }.hook {
                replaceUnit {}
            }
    }
}
