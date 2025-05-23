package com.iota.campusX.Feature.Notification.domain

import com.iota.campusX.Feature.Post.domain.User

data class NotificationDTO(
    var notificationId: String? = null,
    var createrId: String? = null,
    var createdAt: Long = 0L,
    var actionBy: User = User(), // Ensure User also has a no-arg constructor
    var content: Content? = null,
    var type: String = "",
    var postId: String? = null,
)

data class CreateNotificationDTO(
    var notificationId: String = "",
    val postId: String = "",
    var creatorId: String? = null,
    var createdAt: Long = 0L,
    var actionBy: String = "",
    val isRead: Boolean = false,
    var type: String = "",
    var contentId: String  = "",
)

data class Content(
    var contentId: String? = null,
    var text: String? = null,
    var image: String? = null,
)
