package com.iota.campusX.Feature.Post.data.local.mapper

import com.iota.campusX.Feature.Post.data.local.entity.PostEntity
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.attachment.AttachmentDto
import com.iota.campusX.Feature.Post.domain.models.AuthorDetails
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun PostEntity.toDomain(): Post {
    return Post(
        postId = postId,
        caption = caption,
        author = AuthorDetails(
            authorId = authorId,
            authorName = authorName,
            authorImage = authorProfileUrl,
            authorTagline = authorTagline,
            isCurrentUser = isCurrentUser
        ),
        attachment = attachmentJson?.let {
            json.decodeFromString<AttachmentDto>(it)
        },
        likesCount = likeCount,
        commentCount = commentCount,
        isLiked = isLiked,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
