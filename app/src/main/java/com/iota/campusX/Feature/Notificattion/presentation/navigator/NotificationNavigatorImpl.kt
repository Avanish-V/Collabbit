package com.iota.campusX.Feature.Notificattion.presentation.navigator

import com.iota.campusX.Feature.Notificattion.presentation.effect.NotificationEffect
import com.iota.campusX.Feature.Notificattion.presentation.model.NotificationUi
import com.iotabuild.campuscircle.Notification.entity.EntityType

class NotificationNavigatorImpl : NotificationNavigator {

    override suspend fun navigate(
        notification: NotificationUi
    ): NotificationEffect {

        return when (notification.entityType) {

            EntityType.POST ->
                NotificationEffect.NavigateToPost(notification.entityId)

            EntityType.USER ->
                NotificationEffect.NavigateToProfile(notification.senderName)

            EntityType.CHAT ->{}


            else -> {}
        } as NotificationEffect
    }
}