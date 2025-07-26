package com.iota.campusX.Feature.Post.domain.Models

import kotlinx.serialization.Serializable

@Serializable
data class CreateReplyDTO(
    val replyId: String = "",
    val postId: String = "",
    val repliedBy: String = "",
    val content: String = "",
    val isEdited: Boolean = false,
    val visibilityMode: PostVisibilityMode = PostVisibilityMode.USER,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    val repliedAt: Long = 0L
)

@Serializable
data class GetRepliesDTO(
    val postId: String = "",
    val replyId: String = "",
    val isEdited: Boolean = false,
    val creatorDetail: CreatorDetail,
    val content: String = "",
    val visibilityMode: PostVisibilityMode = PostVisibilityMode.USER,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    val actions: PostActions,
    val repliedAt: Long = 0L
)
