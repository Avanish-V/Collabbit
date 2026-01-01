package com.iota.campusX.Feature.Post.data.model

import com.iota.campusX.Feature.Post.domain.models.AuthorDetails
import kotlinx.serialization.Serializable

@Serializable
data class ReplyResponse(
    val replyId: Long,
    val postId: Long,
    val author: AuthorDetails,
    val visibility: VisibilityMode,
    val text: String,
    val mediaUrl: String? = null,
    val isEdited: Boolean = false,
    val parentId: Long? = null,
    val updatedAt: Long,
    val createdAt: Long,
    val actions: PostActions,
    val children: List<ReplyResponse> = emptyList()
)