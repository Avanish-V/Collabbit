package com.iota.campusX.realtime.manager

import com.iota.campusX.realtime.socket.RealtimeSocketManager

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