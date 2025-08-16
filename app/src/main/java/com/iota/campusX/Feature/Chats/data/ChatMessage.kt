package com.iota.campusX.Feature.Chats.data

import com.google.firebase.database.ServerValue
import kotlinx.serialization.Contextual
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


fun chatMessageToMap(
     messageId: String,
     senderId: String,
     text: String,
     attachmentUrl: String,
     timestamp: String,
     read: Boolean,
): Map<String, Any?> {
    return mapOf(
        "messageId" to messageId,
        "senderId" to senderId,
        "text" to text,
        "attachmentUrl" to attachmentUrl,
        "timestamp" to ServerValue.TIMESTAMP,
        "read" to read
    )
}

