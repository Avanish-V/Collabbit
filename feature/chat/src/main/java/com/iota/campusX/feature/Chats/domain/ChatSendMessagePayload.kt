package com.iota.campusX.Feature.Chats.domain

import kotlinx.serialization.Serializable

@Serializable
data class ChatSendMessagePayload(
    val messageId: String,
    val roomId: String,
    val recipientId: String,
    val text: String,
    val attachmentUrl: String? = null
)