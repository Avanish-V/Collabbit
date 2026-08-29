package com.iota.campusX.realtime.handler

import com.iota.campusX.realtime.model.SocketEvent
import com.iota.campusX.realtime.model.SocketEventType
import kotlinx.serialization.json.JsonElement

interface SocketEventHandler {

    val eventType: SocketEventType

    suspend fun handle(
        event: SocketEvent
    )
}