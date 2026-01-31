package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.data.DeletedMessage
import com.aoya.telegami.utils.logd
import com.aoya.telegami.utils.logw
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object Globals {
    private val allowMsgDelete = AtomicBoolean(false)
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Mark next deletion as allowed (won't be stored as deleted message)
     * Used when user explicitly deletes a message
     */
    fun allowNextDeletion() {
        allowMsgDelete.set(true)
    }

    /**
     * Check if deletion is allowed
     */
    fun isDeletionAllowed(): Boolean = allowMsgDelete.get()

    /**
     * Store deleted messages to database, or remove from deleted list if allowed
     */
    fun storeDeletedMessages(
        dialogId: Long,
        msgIds: List<Int>,
    ): Boolean {
        if (msgIds.isEmpty()) return false

        if (allowMsgDelete.compareAndSet(true, false)) {
            logd("Allowing deletion of ${msgIds.size} messages in dialog $dialogId")
            coroutineScope.launch {
                try {
                    Telegami.db.deletedMessageDao().deleteAllByIds(msgIds, dialogId)
                } catch (e: Exception) {
                    logw("Failed to remove allowed deletions: ${e.message}")
                }
            }
            return true
        }

        logd("Storing ${msgIds.size} deleted messages in dialog $dialogId")
        coroutineScope.launch {
            try {
                Telegami.db.deletedMessageDao().insertAll(
                    msgIds.map { mid ->
                        DeletedMessage(id = mid, dialogId = dialogId)
                    },
                )
            } catch (e: Exception) {
                logw("Failed to store deleted messages: ${e.message}")
            }
        }

        return false
    }
}
