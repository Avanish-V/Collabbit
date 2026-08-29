package com.iota.campusX.Feature.Notificattion.domain.usecase

import com.iota.campusX.Feature.Notificattion.domain.repository.NotificationRepository

class GetUnreadCountUseCase(

    private val repository: NotificationRepository

) {

    suspend operator fun invoke(): Long {

        return repository.unreadCount()

    }
}