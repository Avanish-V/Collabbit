package com.iota.campusX.Feature.Notificattion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType

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
    val title: String?=null,
    val body: String?=null,
    val deepLink: String?,
    val isRead: Boolean,
    val createdAt: Long
)