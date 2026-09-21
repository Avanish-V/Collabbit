package com.iota.campusX.Feature.Notificattion.domain.model

import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import kotlinx.serialization.Serializable

data class Notification(
    val id: Long,
    val senderUid: String,
    val senderName: String,
    val senderProfileUrl: String?,
    val title: String,
    val body: String,
    val note: String? = null,
    val type: NotificationType,
    val entityType: EntityType,
    val entityId: String,
    val deepLink: String?,
    val isRead: Boolean,
    val createdAt: Long,
    val isActionDone: Boolean = false,
    val postTitle: String? = null,
    val postThumbnail: String? = null,
    val upvoters: List<Upvoter> = emptyList()
)

@Serializable
data class Upvoter(
    val uid: String,
    val name: String,
    val image: String?,
    val about: String?,
    val tagline: String?
)
