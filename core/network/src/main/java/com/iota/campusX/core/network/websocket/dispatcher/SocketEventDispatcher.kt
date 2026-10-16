package com.iota.campusX.core.network.websocket.dispatcher

import android.util.Log
import com.iota.campusX.core.network.websocket.handler.SocketEventHandler
import com.iota.campusX.core.network.websocket.model.SocketEvent

interface SocketEventDispatcher {
    suspend fun dispatch(event: SocketEvent)
}

class SocketEventDispatcherImpl(handlers: List<SocketEventHandler>) : SocketEventDispatcher {
    private val handlerMap = handlers.associateBy { it.eventType }

    override suspend fun dispatch(event: SocketEvent) {
        val handler = handlerMap[event.eventType]
        if (handler == null) {
            Log.w("SocketDispatcher", "No handler registered for ${event.eventType}")
            return
        }
        handler.handle(event)
    }
}
