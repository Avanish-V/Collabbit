package com.iota.campusX.realtime.socket

import com.iota.campusX.realtime.connection.SocketConnectionState
import com.iota.campusX.realtime.model.SocketEvent
import kotlinx.coroutines.flow.StateFlow

interface RealtimeSocketManager {

    /**
     * Start realtime connection.
     */
    fun connect()

    /**
     * Close realtime connection.
     */
    fun disconnect()

    /**
     * Send message to server.
     */
    suspend fun send(event: SocketEvent)

    /**
     * Observe connection state.
     */
    val connectionState: StateFlow<SocketConnectionState>

    /**
     * Observe incoming socket events.
     */
    val incomingEvents: kotlinx.coroutines.flow.SharedFlow<SocketEvent>
}