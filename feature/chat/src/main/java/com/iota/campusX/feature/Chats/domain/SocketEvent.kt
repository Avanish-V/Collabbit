package com.iota.campusX.Feature.Chats.domain

import com.iota.campusX.core.network.websocket.model.SocketEventType
import kotlinx.serialization.Serializable

@Serializable
data class SocketEvent<T>(
    val eventType: SocketEventType,
    val payload: T
)