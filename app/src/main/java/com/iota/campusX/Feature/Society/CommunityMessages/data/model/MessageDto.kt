// data/model/MessageDto.kt
package com.iota.campusX.Feature.Society.CommunityMessages.data.model

data class MessageDto(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val timestamp: Any? = null,
    val mentions: List<String> = emptyList(),
    val replyTo: String? = null,
)
