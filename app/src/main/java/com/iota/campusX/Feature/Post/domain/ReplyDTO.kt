package com.iota.campusX.Feature.Post.domain

import kotlinx.serialization.Serializable

@Serializable
data class ReplyDTO(
    val replyId: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val repliedAt: Long = 0L
)

@Serializable
data class GetRepliesDTO(
    val postId: String = "",
    val replyId: String = "",
    val user: User,
    val content: String = "",
    val actions: PostActions
)
