package com.iota.campusX.Feature.Notificattion.domain.model

import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType

data class Notification(
    val id: Long,
    val senderUid: String,
    val senderName: String,
    val senderProfileUrl: String?,
    val title: String,
    val body: String,
    val type: NotificationType,
    val entityType: EntityType,
    val entityId: String,
    val deepLink: String?,
    val isRead: Boolean,
    val createdAt: Long
)
