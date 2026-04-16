package com.aoya.telegami.hooks

import android.text.TextUtils
import com.aoya.telegami.virt.messenger.MessagesStorage
import com.aoya.telegami.virt.messenger.NotificationCenter
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object ShowDeletedMessages : YukiBaseHooker() {
    const val MESSAGES_CONTROLLER_CN = "org.telegram.messenger.MessagesController"
    const val MESSAGES_STORAGE_CN = "org.telegram.messenger.MessagesStorage"
    const val NOTIFICATION_CENTER_CN = "org.telegram.messenger.NotificationCenter"
    const val NOTIFICATIONS_CONTROLLER_CN = "org.telegram.messenger.NotificationsController"
    val messagesControllerClass by lazyClass(resolver.get(MESSAGES_CONTROLLER_CN))
    val messagesStorageClass by lazyClass(resolver.get(MESSAGES_STORAGE_CN))
    val notificationCenterClass by lazyClass(resolver.get(NOTIFICATION_CENTER_CN))
    val notificationsControllerClass by lazyClass(resolver.get(NOTIFICATIONS_CONTROLLER_CN))

    override fun onHook() {
        messagesControllerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(MESSAGES_CONTROLLER_CN, "deleteMessages")
                parameterCount = 12
            }.hook {
                before {
                    Globals.allowNextDeletion()
                }
            }

        messagesStorageClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(MESSAGES_STORAGE_CN, "markMessagesAsDeletedInternal")
                parameterCount = 5
            }.hook {
                before {
                    val (dialogId, mIds) =
                        if (packageName == "xyz.nextalone.nagram") {
                            Pair(args[4] as Long, args[0] as ArrayList<Int>)
                        } else {
                            Pair(args[0] as Long, args[1] as ArrayList<Int>)
                        }

                    val db = MessagesStorage(instance!!).database

                    val idStr = TextUtils.join(",", mIds)
                    val cursor =
                        if (dialogId != 0L) {
                            db.queryFinalized("SELECT uid, mid FROM messages_v2 WHERE mid IN ($idStr) AND uid = $dialogId")
                        } else {
                            db.queryFinalized("SELECT uid, mid FROM messages_v2 WHERE mid IN ($idStr) AND is_channel = 0")
                        } ?: return@before

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

                    if (shouldDelete) return@before

                    resultNull()
                }
            }

        messagesControllerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(MESSAGES_CONTROLLER_CN, "markDialogMessageAsDeleted")
            }.hook {
                if (!Globals.isDeletionAllowed()) replaceUnit {}
            }

        notificationCenterClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(NOTIFICATION_CENTER_CN, "postNotificationName")
            }.hook {
                before {
                    if (Globals.isDeletionAllowed()) return@before
                    val id = args[0] as? Int ?: 0
                    if (id == NotificationCenter.MESSAGES_DELETED) resultNull()
                }
            }

        notificationsControllerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(NOTIFICATIONS_CONTROLLER_CN, "removeDeletedMessagesFromNotifications")
            }.hook {
                replaceUnit {}
            }

        loadHooker(ModifyDeletedMessagesMenu)
    }
}
