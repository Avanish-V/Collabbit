package com.iota.campusX.NetworkMonitor


import com.example.connectivity.network.NetworkMonitor
import kotlinx.coroutines.flow.StateFlow


class ConnectivityRepositoryImpl (
    private val networkMonitor: NetworkMonitor
) : ConnectivityRepository {
    init { networkMonitor.start() } // start monitoring. Alternatively start in Application class.
    override val connectivityState: StateFlow<ConnectivityState> = networkMonitor.state
}
