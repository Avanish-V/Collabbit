package com.iota.campusX.realtime.socket

import com.iota.campusX.realtime.connection.SocketConnectionState
import com.iota.campusX.realtime.model.SocketEvent
import kotlinx.coroutines.flow.StateFlow

interface RealtimeSocketManager {


    fun connect()

    fun disconnect()
    suspend fun send(event: SocketEvent)

    val connectionState: StateFlow<SocketConnectionState>

    val incomingEvents: kotlinx.coroutines.flow.SharedFlow<SocketEvent>
}