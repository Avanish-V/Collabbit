package com.iota.campusX.Feature.Notificattion.sync

import com.iota.campusX.Feature.Notificattion.domain.repository.NotificationRepository

class NotificationSyncManagerImpl(

    private val repository: NotificationRepository

) : NotificationSyncManager {

    override suspend fun syncNotification(
        notificationId: Long
    ) {
        repository.syncNotification(notificationId)
    }
}