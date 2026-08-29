package com.iota.campusX.Feature.Notificattion.sync

interface NotificationSyncManager {

    suspend fun syncNotification(
        notificationId: Long
    )

}