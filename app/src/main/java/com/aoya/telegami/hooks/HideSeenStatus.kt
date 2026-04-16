package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.aoya.telegami.virt.messenger.MessagesController.ReadTask
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object HideSeenStatus : YukiBaseHooker() {
    const val MESSAGES_CONTROLLER_CN = "org.telegram.messenger.MessagesController"
    val messagesControllerClass by lazyClass(resolver.get(MESSAGES_CONTROLLER_CN))

    override fun onHook() {
        messagesControllerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(MESSAGES_CONTROLLER_CN, "completeReadTask")
            }.hook {
                before {
                    val dialogId = args[0]?.let { ReadTask(it).dialogId } ?: return@before
                    val currentUserId = Config.getCurrentUser().id

                    val shouldHide =
                        when {
                            dialogId == currentUserId -> false
                            dialogId > 0 -> Config.isFeatureEnabled("HideSeenPrivateChat")
                            else -> Config.isFeatureEnabled("HideSeenChannel")
                        }

                    if (shouldHide) {
                        resultNull()
                    }
                }
            }
    }
}
