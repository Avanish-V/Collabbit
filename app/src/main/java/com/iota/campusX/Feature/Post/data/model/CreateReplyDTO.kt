package com.iota.campusX.Feature.Post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateReplyDTO(
    val replyId: String = "",
    val postId: String = "",
    val repliedBy: String = "",
    val content: String = "",
    val isEdited: Boolean = false,
    val visibility: VisibilityMode = VisibilityMode.USER,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    val repliedAt: Long = 0L
)

@Serializable
data class GetRepliesDTO(
    val postId: String = "",
    val replyId: String = "",
    val edited: Boolean = false,
    val creatorDetail: CreatorDetail = CreatorDetail(),
    val content: String = "",
    val visibility: VisibilityMode = VisibilityMode.USER,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    val actions: PostActions = PostActions(),
    val repliedAt: Long = 0L
)


@Serializable
data class UserReplyDTO(
    val post: GetPostDTO,
    val reply: GetRepliesDTO
)