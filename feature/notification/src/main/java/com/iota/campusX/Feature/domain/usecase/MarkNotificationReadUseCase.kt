package com.iota.campusX.Feature.Notificattion.domain.usecase

import com.iota.campusX.Feature.Notificattion.domain.repository.NotificationRepository

class MarkNotificationReadUseCase(

    private val repository: NotificationRepository

) {

    suspend operator fun invoke(id: Long) {

        repository.markRead(id)

    }

}