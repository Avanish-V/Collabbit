package com.iota.campusX.Feature.Reply.data.remote.response

import com.iota.campusX.Feature.Post.domain.models.AuthorDetails
import kotlinx.serialization.Serializable

@Serializable
data class MentionedUserResponse(
    val uid: String,
    val name: String,
    val image: String? = null
)

@Serializable
data class ReplyResponse(
    val id: String,
    val content: String,
    val imageUrl: String? = null,
    val author: AuthorDetails,
    val mentionedUser: MentionedUserResponse? = null,
    val parentReplyId: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val childCount: Int = 0,
    val isAuthor: Boolean = false,
    val deleted: Boolean = false
)
