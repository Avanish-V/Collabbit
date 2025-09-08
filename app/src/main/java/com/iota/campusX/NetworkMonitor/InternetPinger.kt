package com.iota.campusX.NetworkMonitor

import android.net.Network

interface InternetPinger {
    /**
     * Returns true if we can reach the validation endpoint using the provided network.
     * The Network can be null when no explicit binding is needed.
     */
    suspend fun isInternetAvailable(network: Network? = null): Boolean
}
