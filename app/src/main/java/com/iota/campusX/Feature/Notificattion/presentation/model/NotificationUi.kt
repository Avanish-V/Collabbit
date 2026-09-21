package com.iota.campusX.Feature.Notificattion.presentation.model

import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import com.iota.campusX.Feature.Notificattion.domain.model.Upvoter

data class NotificationUi(
    val id: Long,
    val senderUid: String,
    val title: String,
    val body: String,
    val note: String?,
    val senderName: String,
    val senderProfileUrl: String?,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: Long,
    val entityId: String,
    val entityType: EntityType,
    val deepLink: String?,
    val isActionDone: Boolean = false,
    val postTitle: String? = null,
    val postThumbnail: String? = null,
    val upvoters: List<Upvoter> = emptyList()
)
