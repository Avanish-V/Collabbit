package com.iota.campusX.Feature.Chats.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_rooms")
data class ChatRoomEntity(
    @PrimaryKey val roomId: String,
    val receiverId: String,
    val userName: String,
    val userImage: String?,
    @Embedded val lastMessage: LastMessageEntity
)

data class LastMessageEntity(
    val lastMessageText: String,
    val timeStamp: Long,
    val unreadCount: Int,
    val isRead: Boolean,
    val lastMessageBy: Boolean
)
