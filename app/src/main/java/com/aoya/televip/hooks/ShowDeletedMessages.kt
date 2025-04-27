package com.aoya.televip.hooks

import com.aoya.televip.core.Config
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedHelpers.getStaticIntField

class ShowDeletedMessages :
    Hook(
        "show_deleted_messages",
        "Show 'Deleted' messages",
    ) {
    override fun init() {
        var allowDelete = false

        findClass("org.telegram.messenger.MessagesController")
            .hook("deleteMessages", HookStage.BEFORE) {
                allowDelete = true
            }

        findClass("org.telegram.messenger.MessagesStorage")
            .hook("markMessagesAsDeleted", HookStage.BEFORE) { param ->
                if (allowDelete) return@hook
                val messages = param.argNullable<List<Int>>(1) ?: return@hook

                val dialogId = param.arg<Long>(0)
                if (dialogId == 0L || messages.isEmpty()) return@hook

                Config.updateDeletedMessages(dialogId, messages)

                param.setResult(null)
            }

        findClass("org.telegram.messenger.MessagesStorage")
            .hook("updateDialogsWithDeletedMessages", HookStage.BEFORE) { param ->
                if (!allowDelete) {
                    param.setResult(null)
                }
            }

        findClass("org.telegram.messenger.MessagesController")
            .hook("markDialogMessageAsDeleted", HookStage.BEFORE) { param ->
                if (!allowDelete) {
                    param.setResult(null)
                }
            }

        findClass("org.telegram.messenger.NotificationCenter").hook("postNotificationName", HookStage.BEFORE) { param ->
            if (allowDelete) return@hook
            val messagesDeleted = getStaticIntField(findClass("org.telegram.messenger.NotificationCenter"), "messagesDeleted")
            if (param.arg<Int>(0) == messagesDeleted) param.setResult(null)
        }

        findClass("org.telegram.messenger.NotificationCenter").hook("postNotificationName", HookStage.AFTER) {
            allowDelete = false
        }

        findClass(
            "org.telegram.messenger.NotificationsController",
        ).hook("removeDeletedMessagesFromNotifications", HookStage.BEFORE) { param ->
            param.setResult(null)
        }
    }
}
