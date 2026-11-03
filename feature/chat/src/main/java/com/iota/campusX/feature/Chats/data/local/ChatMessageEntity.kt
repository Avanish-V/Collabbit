package com.iota.campusX.Feature.Chats.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val messageId: String,
    val roomId: String,
    val senderId: String,
    val text: String,
    val attachmentUrl: String?,
    val timestamp: Long,
    val read: Boolean,
    val isPending: Boolean,
    val isFailed: Boolean
)
