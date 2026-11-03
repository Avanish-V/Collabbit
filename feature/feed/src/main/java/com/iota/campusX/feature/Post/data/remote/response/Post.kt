package com.iota.campusX.Feature.Post.data.remote.response

import com.iota.campusX.Feature.Post.domain.attachment.AttachmentDto
import com.iota.campusX.Feature.Post.domain.models.AuthorDetails
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val postId: String,
    val caption: String,
    val attachment: AttachmentDto?,
    val author: AuthorDetails,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false
)
