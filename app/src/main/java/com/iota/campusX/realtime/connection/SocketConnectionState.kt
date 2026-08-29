package com.iota.campusX.realtime.connection

sealed interface SocketConnectionState {

    /**
     * No active connection.
     */
    data object Disconnected : SocketConnectionState

    /**
     * Connection attempt is in progress.
     */
    data object Connecting : SocketConnectionState

    /**
     * Socket is connected and receiving events.
     */
    data object Connected : SocketConnectionState

    /**
     * Lost connection and retrying.
     */
    data class Reconnecting(
        val attempt: Int
    ) : SocketConnectionState

    /**
     * Connection failed.
     */
    data class Error(
        val throwable: Throwable
    ) : SocketConnectionState
}