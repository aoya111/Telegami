package com.aoya.telegami.hooks

import android.text.TextUtils
import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.MessagesStorage
import com.aoya.telegami.virt.messenger.NotificationCenter

class ShowDeletedMessages :
    Hook(
        "show_deleted_messages",
        "Show 'Deleted' messages",
    ) {
    override fun init() {
        findAndHook("org.telegram.messenger.MessagesController", "deleteMessages", HookStage.BEFORE) { param ->
            if (param.args().size != 12) return@findAndHook
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

            val db = MessagesStorage(param.thisObject()).database

            val idStr = TextUtils.join(",", mIds)
            val cursor =
                if (dialogId != 0L) {
                    db.queryFinalized("SELECT uid, mid FROM messages_v2 WHERE mid IN ($idStr) AND uid = $dialogId")
                } else {
                    db.queryFinalized("SELECT uid, mid FROM messages_v2 WHERE mid IN ($idStr) AND is_channel = 0")
                } ?: return@findAndHook

            val map = mutableMapOf<Long, MutableList<Int>>()
            while (cursor.next()) {
                val dId = cursor.longValue(0)
                val mId = cursor.intValue(1)
                map.getOrPut(dId) { mutableListOf() }.add(mId)
            }

            var shouldDelete = true
            map.forEach { (dId, mIds) ->
                shouldDelete = shouldDelete && Globals.handleDeletedMessages(dId, mIds)
            }

            if (shouldDelete) return@findAndHook

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

        // ModifyDeletedMessagesMenu().init()
    }
}
