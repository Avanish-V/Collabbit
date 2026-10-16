package com.iota.campusX.core.network.websocket.handler

import com.iota.campusX.core.network.websocket.model.SocketEvent
import com.iota.campusX.core.network.websocket.model.SocketEventType

interface SocketEventHandler {
    val eventType: SocketEventType
    suspend fun handle(event: SocketEvent)
}
