package com.iota.campusX.Feature.Notificattion.presentation.model

import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType

data class NotificationUi(
    val id: Long,
    val senderUid: String,
    val title: String,
    val body: String,
    val senderName: String,
    val senderProfileUrl: String?,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: Long,
    val entityId: String,
    val entityType: EntityType,
    val deepLink: String?
)
