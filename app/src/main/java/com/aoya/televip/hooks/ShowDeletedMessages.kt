package com.aoya.televip.hooks

import android.text.TextUtils
import com.aoya.televip.TeleVip
import com.aoya.televip.data.DeletedMessage
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.virt.messenger.MessagesStorage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.getStaticIntField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class ShowDeletedMessages :
    Hook(
        "show_deleted_messages",
        "Show 'Deleted' messages",
    ) {
    var allowMsgDelete = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun init() {
        findClass("org.telegram.messenger.MessagesController")
            .hook(resolver.getMethod("org.telegram.messenger.MessagesController", "deleteMessages"), HookStage.BEFORE) {
                allowMsgDelete = true
            }

        findClass("org.telegram.messenger.MessagesStorage")
            .hook(
                resolver.getMethod("org.telegram.messenger.MessagesStorage", "markMessagesAsDeletedInternal"),
                HookStage.BEFORE,
            ) { param ->
                val msgsStore = MessagesStorage(param.thisObject())
                val dialogId = param.arg<Long>(0)

                if (param.args().size != 5) return@hook

                val msgIds = param.arg<ArrayList<Int>>(1)
                if (msgIds.isEmpty()) return@hook

                if (allowMsgDelete) {
                    coroutineScope.launch {
                        for (mid in msgIds) {
                            db.deletedMessageDao().delete(mid, dialogId)
                        }
                    }
                    return@hook
                }

                val msgs = mutableListOf<DeletedMessage>()
                val ids = TextUtils.join(",", msgIds)
                val cursor =
                    if (dialogId != 0L) {
                        msgsStore.database.queryFinalized(
                            "SELECT uid, data, read_state, out, mention, mid FROM messages_v2 WHERE mid IN ($ids) AND uid = $dialogId",
                            arrayOf<Any>(),
                        )
                    } else {
                        msgsStore.database.queryFinalized(
                            "SELECT uid, data, read_state, out, mention, mid FROM messages_v2 WHERE mid IN ($ids) AND is_channel = 0",
                            arrayOf<Any>(),
                        )
                    }

                try {
                    while (cursor.next()) {
                        val did = cursor.longValue(0)
                        val mid = cursor.intValue(5)
                        msgs.add(DeletedMessage(id = mid, dialogId = did))
                    }
                } catch (e: Exception) {
                    XposedBridge.log("(${TeleVip.packageName})[ShowDeletedMessages::markMessagesAsDeletedInternal] error: ${e.message}")
                }

                coroutineScope.launch {
                    db.deletedMessageDao().insertAll(msgs)
                }

                param.setResult(null)
            }

        findClass("org.telegram.messenger.MessagesStorage")
            .hook(
                resolver.getMethod("org.telegram.messenger.MessagesStorage", "updateDialogsWithDeletedMessagesInternal"),
                HookStage.BEFORE,
            ) { param ->
                if (allowMsgDelete) {
                    allowMsgDelete = false
                    return@hook
                }
                param.setResult(null)
            }

        findClass("org.telegram.messenger.MessagesController")
            .hook(
                resolver.getMethod("org.telegram.messenger.MessagesController", "markDialogMessageAsDeleted"),
                HookStage.BEFORE,
            ) { param ->
                if (!allowMsgDelete) {
                    param.setResult(null)
                }
            }

        findClass(
            "org.telegram.messenger.NotificationCenter",
        ).hook(resolver.getMethod("org.telegram.messenger.NotificationCenter", "postNotificationName"), HookStage.BEFORE) { param ->
            if (allowMsgDelete) return@hook
            val messagesDeleted =
                getStaticIntField(
                    findClass("org.telegram.messenger.NotificationCenter"),
                    resolver.getField("org.telegram.messenger.NotificationCenter", "messagesDeleted"),
                )
            if (param.arg<Int>(0) == messagesDeleted) param.setResult(null)
        }

        findClass(
            "org.telegram.messenger.NotificationsController",
        ).hook(
            resolver.getMethod("org.telegram.messenger.NotificationsController", "removeDeletedMessagesFromNotifications"),
            HookStage.BEFORE,
        ) { param ->
            param.setResult(null)
        }

        MarkDeletedMessages().init()
        ModifyDeletedMessagesMenu().init()
    }
}
