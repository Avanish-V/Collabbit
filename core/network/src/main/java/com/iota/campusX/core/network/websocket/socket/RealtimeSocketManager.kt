package com.iota.campusX.core.network.websocket.socket

import com.iota.campusX.core.network.websocket.connection.SocketConnectionState
import com.iota.campusX.core.network.websocket.model.SocketEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface RealtimeSocketManager {
    fun connect()
    fun disconnect()
    suspend fun send(event: SocketEvent)
    val connectionState: StateFlow<SocketConnectionState>
    val incomingEvents: SharedFlow<SocketEvent>
}
