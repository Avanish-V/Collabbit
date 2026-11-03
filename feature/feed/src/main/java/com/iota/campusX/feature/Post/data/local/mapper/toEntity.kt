package com.iota.campusX.Feature.Post.data.local.mapper

import com.iota.campusX.Feature.Post.data.local.entity.PostEntity
import com.iota.campusX.Feature.Post.data.remote.mapper.PostRes
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun PostRes.toEntity(): PostEntity {
    return PostEntity(
        postId = postId,
        caption = caption,
        authorId = author.authorId,
        authorName = author.authorName,
        authorProfileUrl = author.authorImage,
        authorTagline = author.authorTagline,
        isCurrentUser = author.isCurrentUser,
        isLiked = isLiked,
        attachmentJson = attachment?.let {
            json.encodeToString(it)
        },
        likeCount = likesCount,
        commentCount = commentCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
