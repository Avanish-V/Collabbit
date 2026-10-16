package com.iota.campusX.core.network.websocket.connection

sealed interface SocketConnectionState {
    data object Disconnected : SocketConnectionState
    data object Connecting : SocketConnectionState
    data object Connected : SocketConnectionState
    data class Reconnecting(val attempt: Int) : SocketConnectionState
    data class Error(val throwable: Throwable) : SocketConnectionState
}
