package com.aoya.televip.hooks

import android.text.TextUtils
import com.aoya.televip.TeleVip
import com.aoya.televip.data.DeletedMessage
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.getStaticIntField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
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
                val o = param.thisObject()
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
                val tgDB = getObjectField(o, resolver.getField("org.telegram.messenger.MessagesStorage", "database"))
                val ids = TextUtils.join(",", msgIds)
                val cursor =
                    if (dialogId != 0L) {
                        callMethod(
                            tgDB,
                            resolver.getMethod("org.telegram.SQLite.SQLiteDatabase", "queryFinalized"),
                            String.format(
                                Locale.US,
                                "SELECT uid, data, read_state, out, mention, mid FROM messages_v2 WHERE mid IN(%s) AND uid = %d",
                                ids,
                                dialogId,
                            ),
                            arrayOf<Any>(),
                        )
                    } else {
                        callMethod(
                            tgDB,
                            resolver.getMethod("org.telegram.SQLite.SQLiteDatabase", "queryFinalized"),
                            String.format(
                                Locale.US,
                                "SELECT uid, data, read_state, out, mention, mid FROM messages_v2 WHERE mid IN(%s) AND is_channel = 0",
                                ids,
                            ),
                            arrayOf<Any>(),
                        )
                    }

                try {
                    while (callMethod(cursor, resolver.getMethod("org.telegram.SQLite.SQLiteCursor", "next")) as Boolean) {
                        val did = callMethod(cursor, resolver.getMethod("org.telegram.SQLite.SQLiteCursor", "longValue"), 0) as Long
                        val mid = callMethod(cursor, resolver.getMethod("org.telegram.SQLite.SQLiteCursor", "intValue"), 5) as Int
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
