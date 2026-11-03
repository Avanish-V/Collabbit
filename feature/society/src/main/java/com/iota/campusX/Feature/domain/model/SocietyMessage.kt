package com.iota.campusX.Feature.Society.domain.model

enum class MessageType {
    TEXT, IMAGE, VIDEO, FILE, SNAP, COURSE
}

data class SocietyMessage(
    var id: String = "",
    var societyId: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var senderAvatarUrl: String? = null,
    var text: String = "",
    var type: MessageType = MessageType.TEXT,
    var mediaUrl: String? = null,
    var thumbnailUrl: String? = null,
    var width: Int = 0,
    var height: Int = 0,
    var aspectRatio: Float = 0f,
    var fileName: String? = null,
    var timestamp: Long = System.currentTimeMillis(),
    var replyToId: String? = null,
    var replyToText: String? = null,
    var replyToName: String? = null,
    var reactions: Map<String, List<String>> = emptyMap(),
    var openedBy: Map<String, Boolean> = emptyMap(),
    var isSending: Boolean = false,
    var isFailed: Boolean = false,
    var uploadProgress: Float = 0f
)
