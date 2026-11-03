package com.iota.campusX.Feature.Reply.data.local.mapper

import com.iota.campusX.Feature.Post.domain.models.AuthorDetails
import com.iota.campusX.Feature.Reply.data.local.entity.ReplyEntity
import com.iota.campusX.Feature.Reply.data.remote.response.MentionedUserResponse
import com.iota.campusX.Feature.Reply.data.remote.response.ReplyResponse

fun ReplyResponse.toEntity(postId: String): ReplyEntity {
    return ReplyEntity(
        id = id,
        postId = postId,
        content = content,
        imageUrl = imageUrl,
        authorId = author.authorId,
        authorName = author.authorName,
        authorProfileUrl = author.authorImage,
        authorTagline = author.authorTagline,
        mentionedUserId = mentionedUser?.uid,
        mentionedUserName = mentionedUser?.name,
        mentionedUserProfileUrl = mentionedUser?.image,
        parentReplyId = parentReplyId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        likesCount = likesCount,
        isLiked = isLiked,
        childCount = childCount,
        isAuthor = isAuthor,
        deleted = deleted
    )
}

fun ReplyEntity.toResponse(): ReplyResponse {
    return ReplyResponse(
        id = id,
        content = content,
        imageUrl = imageUrl,
        author = AuthorDetails(
            authorId = authorId,
            authorName = authorName,
            authorImage = authorProfileUrl ?: "",
            authorTagline = authorTagline,
            isCurrentUser = isAuthor
        ),
        mentionedUser = mentionedUserId?.let {
            MentionedUserResponse(
                uid = it,
                name = mentionedUserName ?: "",
                image = mentionedUserProfileUrl
            )
        },
        parentReplyId = parentReplyId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        likesCount = likesCount,
        isLiked = isLiked,
        childCount = childCount,
        isAuthor = isAuthor,
        deleted = deleted
    )
}
