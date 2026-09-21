package com.iota.campusX.Feature.Notificattion.presentation.event

import com.iota.campusX.Feature.Notificattion.domain.model.Notification

sealed interface NotificationUiEvent {

    data object Refresh : NotificationUiEvent

    data object MarkAllRead : NotificationUiEvent

    data class NotificationClicked(
        val notification: Notification
    ) : NotificationUiEvent

    data class DeleteNotification(
        val notificationId: Long
    ) : NotificationUiEvent

    data class Retry(
        val notificationId: Long
    ) : NotificationUiEvent

    data class AcceptConnectRequest(
        val notification: Notification,
        val message: String
    ) : NotificationUiEvent

    data class RejectConnectRequest(
        val notification: Notification
    ) : NotificationUiEvent

    data object DismissReplySheet : NotificationUiEvent
}
