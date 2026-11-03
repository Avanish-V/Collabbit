package com.iota.campusX.Feature.Post.data.remote.mapper

import com.iota.campusX.Feature.Post.domain.attachment.AttachmentDto
import com.iota.campusX.Feature.Post.domain.models.AuthorDetails
import kotlinx.serialization.Serializable

@Serializable
data class PostRes(
    val postId: String,
    val caption: String,
    val attachment: AttachmentDto? = null,
    val author: AuthorDetails,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false
)
