package com.iota.campusX.Feature.Notificattion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_sync")
data class NotificationSyncEntity(

    @PrimaryKey
    val id: Int = 1,

    val currentPage: Int,

    val lastSyncTime: Long
)