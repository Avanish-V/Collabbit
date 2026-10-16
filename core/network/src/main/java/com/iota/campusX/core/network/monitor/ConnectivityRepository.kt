package com.iota.campusX.core.network.monitor

import kotlinx.coroutines.flow.StateFlow

interface ConnectivityRepository {
    val connectivityState: StateFlow<ConnectivityState>
}

class ConnectivityRepositoryImpl(
    private val networkMonitor: NetworkMonitor
) : ConnectivityRepository {
    init {
        networkMonitor.start()
    }
    override val connectivityState: StateFlow<ConnectivityState> = networkMonitor.state
}
