package com.iota.campusX.Feature.Chats.data

import com.iota.campusX.Feature.Chats.domain.ChatRequestStatus
import kotlinx.serialization.Serializable

@Serializable
data class UserChatsDTO(
    val roomId: String = "",
    val receiverId: String = "",
    val userName:String = "",
    val userImage:String? = null,
    val status: String = ChatRequestStatus.ACCEPTED.name,
    val initiatedByUid: String? = null,
    val lastMessage:LastMessage
)
@Serializable
data class LastMessage(
    val lastMessage:String = "",
    val timeStamp:Long = 0,
    val unreadCount:Int = 0,
    val isRead :Boolean = false,
    val lastMessageBy: Boolean = false
)


