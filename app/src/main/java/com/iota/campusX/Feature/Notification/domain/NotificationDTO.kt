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
    var createrId: String? = null,
    var createdAt: Long = 0L,
    var actionBy: String = "",
    var type: String = "",
    var content: Content? = null,
    var postId: String? = null,
)

data class Content(
    var contentId: String? = null,
    var content: String? = null,
)
