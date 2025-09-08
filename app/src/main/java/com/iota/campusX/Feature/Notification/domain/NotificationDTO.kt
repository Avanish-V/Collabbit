package com.iota.campusX.Feature.Notification.domain

import com.google.firebase.Timestamp
import com.iota.campusX.Feature.Notification.data.PostContent
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.VisibilityMode


sealed class GetNotification {
    abstract val notificationId: String
    abstract val type: NotificationType
    abstract val createdAt: Timestamp?
    abstract val isRead: Boolean

    data class LikeNotification(
        override val notificationId: String = "",
        override val type: NotificationType = NotificationType.LIKE,
        override val createdAt: Timestamp? = null,
        override val isRead: Boolean = false,
        val postId: String = "",
        val postContent: PostContent? = PostContent(),
        val likes: List<UserPayload> = emptyList(),// List of user IDs who liked the post
        val likesCount: Int
    // the post they liked
    ) : GetNotification()

    data class CommentNotification(
        override val notificationId: String = "",
        override val type: NotificationType = NotificationType.COMMENT,
        override val createdAt: Timestamp? = null,
        override val isRead: Boolean = false,
        val postId: String = "",
        val visibilityMode: VisibilityMode = VisibilityMode.USER,
        val postContent: PostContent? = null,
        val replyUsers : List<UserPayload> = emptyList()
        // the post being commented
    ) : GetNotification()

    data class ConnectionRequestNotification(
        override val notificationId: String = "",
        override val type: NotificationType = NotificationType.CONNECTION_REQUEST,
        override val createdAt: Timestamp? = null,
        override val isRead: Boolean = false,
        val actionBy: CreatorDetail = CreatorDetail()   // who sent the request
    ) : GetNotification()
}

enum class NotificationType {
    LIKE,
    COMMENT,
    CONNECTION_REQUEST,
    SYSTEM
}


data class UserPayload(
    val visibilityMode: VisibilityMode? = null,
    val userName: String = "",
    val userImage : String = ""
)





