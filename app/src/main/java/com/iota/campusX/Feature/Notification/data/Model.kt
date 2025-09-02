package com.iota.campusX.Feature.Notification.data

import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Post.data.model.VisibilityMode

sealed class CreateNotification {
    abstract val notificationId: String
    abstract val type: NotificationType
    abstract val createdAt: Any?
    abstract val read: Boolean

    data class LikeNotification(
        override val notificationId: String = "",
        override val type: NotificationType = NotificationType.LIKE,
        override val createdAt: Any? = null,
        override val read: Boolean = false,
        val likes: List<String> = emptyList(),
        val postId: String = ""
    ) : CreateNotification()

    data class CommentNotification(
        override val notificationId: String = "",
        override val type: NotificationType = NotificationType.COMMENT,
        override val createdAt: Any? = null,
        override val read: Boolean = false,
        val postId: String = "",
        val visibilityMode: VisibilityMode = VisibilityMode.USER,
        val commentContent: List<CommentContent> = emptyList()
    ) : CreateNotification()

    data class ConnectionRequestNotification(
        override val notificationId: String = "",
        override val type: NotificationType = NotificationType.CONNECTION_REQUEST,
        override val createdAt: Any? = null,
        override val read: Boolean = false,
        val actionBy: String = ""
    ) : CreateNotification()
}
data class CommentContent(
    val visibilityMode: VisibilityMode = VisibilityMode.USER,
    val repliedBy: String = "",
    val replyId: String = ""
)

data class PostContent(
    val text: String = "",
    val image: String = ""
)
