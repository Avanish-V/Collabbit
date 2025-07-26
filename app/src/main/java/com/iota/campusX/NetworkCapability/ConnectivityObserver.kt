package com.iota.campusX.NetworkCapability

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    val isConnected:Flow<Boolean>
}