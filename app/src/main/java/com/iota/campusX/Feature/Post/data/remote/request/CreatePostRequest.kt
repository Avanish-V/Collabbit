package com.iota.campusX.Feature.Post.data.remote.request

import com.iota.campusX.Feature.Post.data.remote.mapper.PostRes
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.attachment.AttachmentDto
import com.iota.campusX.Feature.Post.domain.models.AuthorDetails
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val caption: String,
    val attachment: AttachmentDto? = null
)





fun PostRes.toDomain(): Post {
    return Post(
        postId = postId,
        caption = caption,
        attachment = attachment,
        author = author,
        createdAt = createdAt,
        updatedAt = updatedAt,
        likesCount = likesCount,
        commentCount = commentCount,
        isLiked = isLiked
    )
}


@Serializable
enum class AttachmentType{
    IMAGE,VIDEO,POLL
}


val posts = emptyList<Post>()