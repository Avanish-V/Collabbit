package com.iota.campusX.NetworkMonitor

sealed class ConnectivityState {
    object Unavailable : ConnectivityState()
    object ConnectedNoInternet : ConnectivityState() // Wi-Fi connected but no internet/captive
    object CaptivePortal : ConnectivityState() // optional: if you detect portal by response
    data class Available(val isValidated: Boolean = true) : ConnectivityState()
}
