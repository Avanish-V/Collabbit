package com.iota.campusX.Feature.Chats.domain

import kotlinx.serialization.Serializable

@Serializable
data class MessagesReadPayload(
    val roomId: String,
    val readerId: String,
    val messageIds: List<String>
)