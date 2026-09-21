package com.iota.campusX.Feature.Reply.data.remote.request

import kotlinx.serialization.Serializable

@Serializable
data class ReplyRequest(
    val content: String,
    val imageUrl: String? = null,
    val parentReplyId: String? = null,
    val mentionedUserId: String? = null
)