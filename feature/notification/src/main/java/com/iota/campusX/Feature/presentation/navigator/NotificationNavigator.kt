package com.iota.campusX.Feature.Notificattion.presentation.navigator

import com.iota.campusX.Feature.Notificattion.presentation.effect.NotificationEffect
import com.iota.campusX.Feature.Notificattion.presentation.model.NotificationUi

interface NotificationNavigator {

    suspend fun navigate(
        notification: NotificationUi
    ): NotificationEffect

}