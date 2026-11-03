package com.iota.campusX.Feature.Notificattion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import com.iota.campusX.Feature.Notificattion.domain.model.Upvoter

@Entity(tableName = "notifications")
data class NotificationEntity(

    @PrimaryKey
    val id: Long,
    val senderUid: String,
    val senderName: String,
    val senderProfileUrl: String?,
    val type: NotificationType,
    val entityType: EntityType,
    val entityId: String,
    val title: String? = null,
    val body: String? = null,
    val note: String? = null,
    val deepLink: String?,
    val isRead: Boolean,
    val createdAt: Long,
    val isActionDone: Boolean = false,
    val postTitle: String? = null,
    val postThumbnail: String? = null,
    val upvoters: List<Upvoter> = emptyList()
)
