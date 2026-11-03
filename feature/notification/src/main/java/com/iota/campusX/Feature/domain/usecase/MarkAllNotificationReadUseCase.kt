package com.iota.campusX.Feature.Notificattion.domain.usecase

import com.iota.campusX.Feature.Notificattion.domain.repository.NotificationRepository

class MarkAllNotificationReadUseCase(

    private val repository: NotificationRepository

) {

    suspend operator fun invoke() {

        repository.markAllRead()

    }

}