package com.iota.campusX.Feature.Notificattion.data.dto

import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: Long = 0,
    val entityId: String? = null,
    val deepLink: String? = null,
    val senderUid: String? = null,
    val senderName: String? = null,
    val senderImage: String? = null,
    val entityType: EntityType? = null,
    val type: NotificationType? = null,
    val isRead: Boolean? = null,
    val createdAt: String? = null,
    val title: String? = null,
    val body: String? = null
)
