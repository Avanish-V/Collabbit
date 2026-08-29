package com.iota.campusX.realtime.dispatcher

import com.iota.campusX.realtime.model.SocketEvent

interface SocketEventDispatcher {

    suspend fun dispatch(
        event: SocketEvent
    )

}