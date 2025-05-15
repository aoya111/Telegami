package com.aoya.televip.data

import androidx.room.Entity

@Entity(primaryKeys = ["id", "dialogId"])
data class DeletedMessage(
    val id: Int,
    val dialogId: Long,
    val createdAt: Long? = System.currentTimeMillis(),
)
