package com.iota.campusX.Feature.Chats.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val attachmentUrl: String? = null,
    val timestamp: Long = 0L,
    val read: Boolean = false
)


