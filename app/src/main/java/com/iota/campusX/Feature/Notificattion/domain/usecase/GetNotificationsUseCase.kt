package com.iota.campusX.Feature.Notificattion.domain.usecase

import androidx.paging.PagingData
import com.iota.campusX.Feature.Notificattion.domain.model.Notification
import com.iota.campusX.Feature.Notificattion.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(
    private val repository: NotificationRepository
) {

    operator fun invoke(): Flow<PagingData<Notification>> {
        return repository.getNotifications()
    }
}