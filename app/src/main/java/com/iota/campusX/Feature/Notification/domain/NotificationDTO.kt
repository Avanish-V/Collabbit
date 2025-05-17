package com.iota.campusX.Feature.Notification.domain

import com.iota.campusX.Feature.Post.domain.User

data class NotificationDTO(
    var notificationId: String? = null,
    var createrId: String? = null,
    var createdAt: String? = null,
    var actionBy: User = User(), // Ensure User also has a no-arg constructor
    var reply: Reply? = null,
    var type: String = "",
    var postId: String? = null,
)

data class CreateNotificationDTO(
    var notificationId: String = "",
    var createrId: String? = null,
    var createdAt: Long = 0L,
    var actionBy: String = "",
    var type: String = "",
    var reply: Reply? = null,
    var postId: String? = null,
)

data class Reply(
    var replyId: String? = null,
    var replyContent: String? = null,
)
