package com.aoya.telegami.hooks

import android.text.TextUtils
import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.messenger.MessagesStorage
import com.aoya.telegami.virt.messenger.NotificationCenter
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver
import io.github.libxposed.api.XposedInterface
import android.util.Log

object ShowDeletedMessages {
    const val MESSAGES_CONTROLLER_CN = "org.telegram.messenger.MessagesController"
    const val MESSAGES_STORAGE_CN = "org.telegram.messenger.MessagesStorage"
    const val NOTIFICATION_CENTER_CN = "org.telegram.messenger.NotificationCenter"
    const val NOTIFICATIONS_CONTROLLER_CN = "org.telegram.messenger.NotificationsController"
    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        val deleteMessages = classLoader.findMethod(MESSAGES_CONTROLLER_CN, "deleteMessages", 12)
        xposed.hook(deleteMessages).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                    Globals.allowNextDeletion()
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${deleteMessages.name}", throwable)
            }
            if (!hasResult) result = chain.proceed(args.toTypedArray())
            result
        }

        val markMessages = classLoader.findMethod(MESSAGES_STORAGE_CN, "markMessagesAsDeletedInternal", 5)
        xposed.hook(markMessages).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                    val invocation = chain
                    val (dialogId, mIds) =
                        if (Telegami.packageName == "xyz.nextalone.nagram") {
                            Pair(args[4] as Long, args[0] as ArrayList<Int>)
                        } else {
                            Pair(args[0] as Long, args[1] as ArrayList<Int>)
                        }

                    val db = MessagesStorage(chain.thisObject!!).database

                    val idStr = TextUtils.join(",", mIds)
                    val cursor =
                        if (dialogId != 0L) {
                            db.queryFinalized("SELECT uid, mid FROM messages_v2 WHERE mid IN ($idStr) AND uid = $dialogId")
                        } else {
                            db.queryFinalized("SELECT uid, mid FROM messages_v2 WHERE mid IN ($idStr) AND is_channel = 0")
                    } ?: return@intercept null

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

                    if (shouldDelete) return@intercept chain.proceed(args.toTypedArray())

                    hasResult = true
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${markMessages.name}", throwable)
            }
            if (!hasResult) result = chain.proceed(args.toTypedArray())
            result
        }

        if (!Globals.isDeletionAllowed()) {
            val method = classLoader.findMethod(MESSAGES_CONTROLLER_CN, "markDialogMessageAsDeleted")
            xposed.hook(method).intercept { chain ->
                val args = chain.args.toMutableList()
                var result: Any? = Unit
                var hasResult = true
                try { result = Unit } catch (throwable: Throwable) {
                    hasResult = false
                    Log.e("Telegami", "Hook callback failed before ${method.name}", throwable)
                }
                if (!hasResult) result = chain.proceed(args.toTypedArray())
                result
            }
        }

        val postNotification = classLoader.findMethod(NOTIFICATION_CENTER_CN, "postNotificationName")
        xposed.hook(postNotification).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                    if (Globals.isDeletionAllowed()) return@intercept chain.proceed(args.toTypedArray())
                    val id = args[0] as? Int ?: 0
                    if (id == NotificationCenter.MESSAGES_DELETED) hasResult = true
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${postNotification.name}", throwable)
            }
            if (!hasResult) result = chain.proceed(args.toTypedArray())
            result
        }

        val removeDeleted = classLoader.findMethod(NOTIFICATIONS_CONTROLLER_CN, "removeDeletedMessagesFromNotifications")
        xposed.hook(removeDeleted).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = Unit
            var hasResult = true
            try { result = Unit } catch (throwable: Throwable) {
                hasResult = false
                Log.e("Telegami", "Hook callback failed before ${removeDeleted.name}", throwable)
            }
            if (!hasResult) result = chain.proceed(args.toTypedArray())
            result
        }
        ModifyDeletedMessagesMenu.install(xposed, classLoader)
    }
}
