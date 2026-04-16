package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.aoya.telegami.virt.messenger.LocaleController
import com.aoya.telegami.virt.messenger.MessageObject
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object ModifyDeletedMessagesMenu : YukiBaseHooker() {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    val chatActivityClass by lazyClass(resolver.get(CHAT_ACTIVITY_CN))

    fun getResource(
        name: String,
        type: String,
    ): Int = appContext?.resources?.getIdentifier(name, type, packageName) ?: 0

    override fun onHook() {
        chatActivityClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CHAT_ACTIVITY_CN, "fillMessageMenu")
            }.hook {
                before {
                    val msgObj = args[0]?.let { MessageObject(it) } ?: return@before
                    val arrayList = args[1] as? ArrayList<Int> ?: return@before
                    val arrayList2 = args[2] as? ArrayList<String> ?: return@before
                    val arrayList3 = args[3] as? ArrayList<Int> ?: return@before

                    if (Globals.isDeletedMessage(msgObj.getDialogId(), msgObj.getId())) {
                        arrayList2.add(LocaleController.getString(getResource("Copy", "string")))
                        arrayList2.add(LocaleController.getString(getResource("Delete", "string")))
                        arrayList3.add(3)
                        arrayList3.add(1)
                        arrayList.add(getResource("msg_copy", "drawable"))
                        arrayList.add(getResource("msg_delete", "drawable"))
                        resultNull()
                    }
                }
            }
    }
}
