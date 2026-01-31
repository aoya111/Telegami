package com.aoya.telegami.hooks

import android.text.TextUtils
import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.NotificationCenter

class ShowDeletedMessages :
    Hook(
        "show_deleted_messages",
        "Show 'Deleted' messages",
    ) {
    override fun init() {
        findAndHook("org.telegram.messenger.MessagesController", "deleteMessages", HookStage.BEFORE) {
            Globals.allowNextDeletion()
        }

        findAndHook(
            "org.telegram.messenger.MessagesStorage",
            "markMessagesAsDeletedInternal",
            HookStage.BEFORE,
        ) { param ->
            if (param.args().size != 5) return@findAndHook

            val (dialogId, mIds) =
                if (Telegami.packageName == "xyz.nextalone.nagram") {
                    Pair(param.arg<Long>(4), param.arg<ArrayList<Int>>(0))
                } else {
                    Pair(param.arg<Long>(0), param.arg<ArrayList<Int>>(1))
                }

            if (Globals.storeDeletedMessages(dialogId, mIds)) return@findAndHook

            param.setResult(null)
        }

        findAndHook(
            "org.telegram.messenger.MessagesController",
            "markDialogMessageAsDeleted",
            HookStage.BEFORE,
        ) { param ->
            if (!Globals.isDeletionAllowed()) param.setResult(null)
        }

        findAndHook("org.telegram.messenger.NotificationCenter", "postNotificationName", HookStage.BEFORE) { param ->
            if (Globals.isDeletionAllowed()) return@findAndHook
            if (param.arg<Int>(0) == NotificationCenter.MESSAGES_DELETED) param.setResult(null)
        }

        findAndHook(
            "org.telegram.messenger.NotificationsController",
            "removeDeletedMessagesFromNotifications",
            HookStage.BEFORE,
        ) { param ->
            param.setResult(null)
        }

        MarkDeletedMessages().init()
        // ModifyDeletedMessagesMenu().init()
    }
}
