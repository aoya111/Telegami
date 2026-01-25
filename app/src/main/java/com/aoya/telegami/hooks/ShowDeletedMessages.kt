package com.aoya.telegami.hooks

import android.text.TextUtils
import com.aoya.telegami.Telegami
import com.aoya.telegami.data.DeletedMessage
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.NotificationCenter
import kotlinx.coroutines.launch

class ShowDeletedMessages :
    Hook(
        "show_deleted_messages",
        "Show 'Deleted' messages",
    ) {
    override fun init() {
        findAndHook("org.telegram.messenger.MessagesController", "deleteMessages", HookStage.BEFORE) {
            Globals.allowMsgDelete.set(true)
        }

        findAndHook(
            "org.telegram.messenger.MessagesStorage",
            "markMessagesAsDeletedInternal",
            HookStage.BEFORE,
        ) { param ->
            val dialogId = param.arg<Long>(0)

            if (param.args().size != 5) return@findAndHook

            val mIds = param.arg<ArrayList<Int>>(1)
            if (mIds.isEmpty()) return@findAndHook

            if (Globals.allowMsgDelete.compareAndSet(true, false)) {
                Globals.coroutineScope.launch {
                    db.deletedMessageDao().deleteAllByIds(mIds, dialogId)
                }
                return@findAndHook
            }

            Globals.coroutineScope.launch {
                db.deletedMessageDao().insertAll(
                    mIds.map { mid ->
                        DeletedMessage(id = mid, dialogId = dialogId)
                    },
                )
            }

            param.setResult(null)
        }

        findAndHook(
            "org.telegram.messenger.MessagesController",
            "markDialogMessageAsDeleted",
            HookStage.BEFORE,
        ) { param ->
            if (!Globals.allowMsgDelete.get()) param.setResult(null)
        }

        findAndHook("org.telegram.messenger.NotificationCenter", "postNotificationName", HookStage.BEFORE) { param ->
            if (Globals.allowMsgDelete.get()) return@findAndHook
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
