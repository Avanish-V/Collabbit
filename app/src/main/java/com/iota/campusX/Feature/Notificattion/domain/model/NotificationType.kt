package com.iotabuild.campuscircle.Notification.entity

import kotlinx.serialization.Serializable

@Serializable
enum class NotificationType {
    LIKE,
    COMMENT,
    REPLY,
    FOLLOW,
    MESSAGE,
    MENTION,
    SYSTEM
}