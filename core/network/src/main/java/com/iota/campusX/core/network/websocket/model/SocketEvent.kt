package com.iota.campusX.core.network.websocket.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SocketEvent(
    val eventType: SocketEventType,
    val payload: JsonElement
)
