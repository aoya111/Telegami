package com.aoya.televip.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeletedMessageDao {
    @Insert
    suspend fun insert(deletedMessage: DeletedMessage)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(deletedMessages: List<DeletedMessage>)

    @Query("SELECT * FROM DeletedMessage WHERE id = :messageId AND dialogId = :dialogId")
    suspend fun get(
        messageId: Int,
        dialogId: Long,
    ): DeletedMessage?

    @Query("SELECT * FROM DeletedMessage WHERE dialogId = :dialogId")
    suspend fun getAllForDialog(dialogId: Long): List<DeletedMessage>

    @Query("SELECT * FROM DeletedMessage ORDER BY createdAt DESC")
    suspend fun getAllOrderByCreatedAt(): List<DeletedMessage>

    @Query("DELETE FROM DeletedMessage WHERE id = :messageId AND dialogId = :dialogId")
    suspend fun delete(
        messageId: Int,
        dialogId: Long,
    )

    @Query("DELETE FROM DeletedMessage WHERE dialogId = :dialogId")
    suspend fun deleteAllForDialog(dialogId: Long)

    @Query("DELETE FROM DeletedMessage")
    suspend fun deleteAll()
}
