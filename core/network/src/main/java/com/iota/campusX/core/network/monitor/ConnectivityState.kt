package com.iota.campusX.core.network.monitor

sealed class ConnectivityState {
    object Unavailable : ConnectivityState()
    object ConnectedNoInternet : ConnectivityState()
    object CaptivePortal : ConnectivityState()
    data class Available(val isValidated: Boolean = true) : ConnectivityState()
}
