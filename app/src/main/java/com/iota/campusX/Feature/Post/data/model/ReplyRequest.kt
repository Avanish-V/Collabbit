package com.iota.campusX.Feature.Post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReplyRequest(
    val postId: Long,
    val visibility: VisibilityMode,
    val text: String,
    val mediaUrl: String,
    val parentId:Long??= null
)