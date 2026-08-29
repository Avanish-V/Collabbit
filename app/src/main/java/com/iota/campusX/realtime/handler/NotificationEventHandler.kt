package com.iota.campusX.realtime.handler

import com.iota.campusX.Feature.Notificattion.sync.NotificationSyncManager
import com.iota.campusX.realtime.model.NotificationPayload
import com.iota.campusX.realtime.model.SocketEvent
import com.iota.campusX.realtime.model.SocketEventType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

class NotificationEventHandler(

    private val syncManager: NotificationSyncManager,
    private val json: Json

) : SocketEventHandler {

    override val eventType = SocketEventType.NOTIFICATION_CREATED

    override suspend fun handle(
        event: SocketEvent
    ) {

        val payload = json.decodeFromJsonElement<NotificationPayload>(
            event.payload
        )

        syncManager.syncNotification(
            payload.notificationId
        )
    }
}