package com.iota.campusX.realtime.dispatcher

import android.util.Log
import com.iota.campusX.realtime.handler.SocketEventHandler
import com.iota.campusX.realtime.model.SocketEvent

class SocketEventDispatcherImpl(

    handlers: List<SocketEventHandler>

) : SocketEventDispatcher {

    private val handlerMap =

        handlers.associateBy {

            it.eventType

        }

    override suspend fun dispatch(event: SocketEvent) {

        val handler = handlerMap[event.eventType]

        if (handler == null) {
            Log.w("SocketDispatcher", "No handler registered for ${event.eventType}")
            return
        }

        handler.handle(event)
    }
}