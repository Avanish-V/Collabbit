package com.iota.campusX.core.network.monitor

import android.net.Network

interface InternetPinger {
    suspend fun isInternetAvailable(network: Network? = null): Boolean
}
