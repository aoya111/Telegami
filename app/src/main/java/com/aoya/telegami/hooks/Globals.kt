package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.data.DeletedMessage
import com.aoya.telegami.utils.logd
import com.aoya.telegami.utils.logw
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

object Globals {
    private val allowMsgDelete = AtomicBoolean(false)
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val deletedMessagesCache = ConcurrentHashMap<Pair<Long, Int>, DeletedMessage>()

    fun isDeletedMessage(
        dialogId: Long,
        msgId: Int,
    ): Boolean = deletedMessagesCache.containsKey(dialogId to msgId)

    fun getDeletedMessage(
        dialogId: Long,
        msgId: Int,
    ): DeletedMessage? = deletedMessagesCache[dialogId to msgId]

    fun loadDeletedMessagesForDialog(dialogId: Long) {
        coroutineScope.launch {
            try {
                val deleted = Telegami.db.deletedMessageDao().getAllForDialog(dialogId)
                deleted.forEach { msg ->
                    deletedMessagesCache[dialogId to msg.id] = msg
                }
            } catch (e: Exception) {
                logw("Failed to load deleted messages for dialog $dialogId: ${e.message}")
            }
        }
    }

    fun clearDeletedMessageFromCache(
        dialogId: Long,
        msgIds: List<Int>,
    ) {
        msgIds.forEach { msgId ->
            deletedMessagesCache.remove(dialogId to msgId)
        }
    }

    fun addDeletedMessageToCache(msg: DeletedMessage) {
        deletedMessagesCache[msg.dialogId to msg.id] = msg
    }

    fun addDeletedMessagesToCache(messages: List<DeletedMessage>) {
        messages.forEach { msg ->
            deletedMessagesCache[msg.dialogId to msg.id] = msg
        }
    }

    fun allowNextDeletion() {
        allowMsgDelete.set(true)
    }

    fun isDeletionAllowed(): Boolean = allowMsgDelete.get()

    fun handleDeletedMessages(
        dialogId: Long,
        msgIds: List<Int>,
    ): Boolean {
        if (msgIds.isEmpty()) return false

        if (allowMsgDelete.compareAndSet(true, false)) {
            logd("Allowing deletion of ${msgIds.size} messages in dialog $dialogId")
            coroutineScope.launch {
                try {
                    Telegami.db.deletedMessageDao().deleteAllByIds(msgIds, dialogId)
                    clearDeletedMessageFromCache(dialogId, msgIds)
                } catch (e: Exception) {
                    logw("Failed to remove allowed deletions: ${e.message}")
                }
            }
            return true
        }

        logd("Storing ${msgIds.size} deleted messages in dialog $dialogId")
        coroutineScope.launch {
            try {
                val messages =
                    msgIds.map { mid ->
                        DeletedMessage(id = mid, dialogId = dialogId)
                    }
                Telegami.db.deletedMessageDao().insertAll(messages)
                addDeletedMessagesToCache(messages)
            } catch (e: Exception) {
                logw("Failed to store deleted messages: ${e.message}")
            }
        }

        return false
    }
}
