package com.iota.campusX.NetworkMonitor

import kotlinx.coroutines.flow.StateFlow

interface ConnectivityRepository {
    val connectivityState: StateFlow<ConnectivityState>
}
