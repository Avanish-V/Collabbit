package com.iota.campusX.realtime.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SocketEvent(

    val eventType: SocketEventType,

    val payload: JsonElement

)