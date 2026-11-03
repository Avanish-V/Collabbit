package com.iota.campusX.Feature.Reply.domain.model

data class MentionBuilder(
    val mentionUserName: String,
    val parentId: String,
    val postId: String,
    val mentionedUserId: String
)
