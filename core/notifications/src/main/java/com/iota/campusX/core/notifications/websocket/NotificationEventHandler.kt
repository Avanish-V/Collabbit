package com.iota.campusX.core.notifications.websocket

import com.iota.campusX.core.notifications.sync.NotificationSyncManager
import com.iota.campusX.core.network.websocket.handler.SocketEventHandler
import com.iota.campusX.core.network.websocket.model.SocketEvent
import com.iota.campusX.core.network.websocket.model.SocketEventType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class NotificationEventHandler(
    private val syncManager: NotificationSyncManager,
    private val json: Json
) : SocketEventHandler {

    override val eventType = SocketEventType.NOTIFICATION_CREATED

    override suspend fun handle(event: SocketEvent) {
        val payload = json.decodeFromJsonElement<NotificationPayload>(event.payload)
        syncManager.syncNotification(payload.notificationId)
    }
}
