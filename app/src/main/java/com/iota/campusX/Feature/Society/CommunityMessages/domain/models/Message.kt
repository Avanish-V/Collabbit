package com.iota.campusX.Feature.Society.CommunityMessages.domain.models

data class Message(
    val id: String = "",
    val senderId: String = "",
    val userName: String = "",
    val avatarUrl: String? = null,
    val bgColor: String,
    val text: String = "",
    val timestamp: Any?=null,
    val replyToMessageId: String? = null,
    val mentions: List<String> = emptyList(),
    val messageStatus: MessageStatus = MessageStatus.SENDING
)

