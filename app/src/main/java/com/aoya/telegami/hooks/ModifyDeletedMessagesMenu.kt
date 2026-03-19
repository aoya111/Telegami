package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.LocaleController
import com.aoya.telegami.virt.messenger.MessageObject

class ModifyDeletedMessagesMenu : Hook("ModifyDeletedMessagesMenu") {
    override fun init() {
        findAndHook(
            "org.telegram.ui.ChatActivity",
            "fillMessageMenu",
            HookStage.BEFORE,
            filter = {
                Config.isFeatureEnabled("ShowDeletedMessages")
            },
        ) { param ->
            val msgObj = MessageObject(param.arg<Any>(0))
            val arrayList = param.arg<ArrayList<Int>>(1)
            val arrayList2 = param.arg<ArrayList<String>>(2)
            val arrayList3 = param.arg<ArrayList<Int>>(3)

            if (Globals.isDeletedMessage(msgObj.dialogId, msgObj.id)) {
                arrayList2.add(LocaleController.getString(getResource("Copy", "string")))
                arrayList2.add(LocaleController.getString(getResource("Delete", "string")))
                arrayList3.add(3)
                arrayList3.add(1)
                arrayList.add(getResource("msg_copy", "drawable"))
                arrayList.add(getResource("msg_delete", "drawable"))
                param.setResult(null)
            }
        }
    }
}
