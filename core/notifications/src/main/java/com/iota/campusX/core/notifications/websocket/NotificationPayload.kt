package com.iota.campusX.core.notifications.websocket

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPayload(
    val notificationId: Long
)
