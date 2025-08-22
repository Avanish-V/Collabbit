package com.iota.campusX.Screens.Post.DataModel

data class FeedContent(
    val id: ContentId,
    val text: String,
    val isOwner: Boolean,
    val type: ContentType
)

sealed class ContentId {
    data class Post(val postId: String) : ContentId()
    data class Reply(val postId: String, val replyId: String) : ContentId()
}


enum class ContentType { POST, REPLY }
