package com.iota.campusX.core.notifications.sync

interface NotificationSyncManager {
    suspend fun syncNotification(notificationId: Long)
}
