package com.iotabuild.campuscircle.Notification.entity

import kotlinx.serialization.Serializable

@Serializable
enum class EntityType {
    POST,
    COMMENT,
    REPLY,
    USER,
    CHAT
}