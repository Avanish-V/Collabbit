package com.iota.campusX.core.network.websocket.manager

import com.iota.campusX.core.network.websocket.socket.RealtimeSocketManager

interface RealtimeManager {
    fun start()
    fun stop()
}

class RealtimeManagerImpl(
    private val socketManager: RealtimeSocketManager
) : RealtimeManager {
    override fun start() {
        socketManager.connect()
    }

    override fun stop() {
        socketManager.disconnect()
    }
}
