package com.iota.campusX.Feature.Notification.domain

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.UserDetail

data class NotificationDTO(
    var notificationId: String? = null,
    var creatorId: String? = null,
    var createdAt: Long = 0L,
    var actionBy: UserDetail = UserDetail(), // Ensure User also has a no-arg constructor
    var content: Content? = null,
    var type: NotificationType = NotificationType.COMMENTED,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    var visibilityMode: PostVisibilityMode? = null,
    var postId: String = "",
)

data class CreateNotificationDTO(
    var notificationId: String = "",
    val postId: String? = null,
    var replyId: String?  = null,
    var requestId: String?  = null,
    var creatorId: String? = null,
    var createdAt: Long = 0L,
    var actionBy: String = "",
    val isRead: Boolean = false,
    var type: NotificationType = NotificationType.COMMENTED,
    var visibilityMode: PostVisibilityMode = PostVisibilityMode.USER,
    val feedMode: FeedMode = FeedMode.GLOBAL

)

data class Content(
    var contentId: String? = null,
    var text: String? = null,
    var image: String? = null,
)


enum class NotificationType{
    LIKE_REPLY,
    LIKE_POST,
    COMMENTED,
    REQUEST,
}
