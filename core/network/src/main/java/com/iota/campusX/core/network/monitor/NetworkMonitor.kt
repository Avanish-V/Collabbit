package com.iota.campusX.core.network.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NetworkMonitor(
    context: Context,
    private val pinger: InternetPinger,
    private val scope: CoroutineScope
) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _state = MutableStateFlow<ConnectivityState>(ConnectivityState.Unavailable)
    val state: StateFlow<ConnectivityState> = _state.asStateFlow()

    private var callback: ConnectivityManager.NetworkCallback? = null

    @android.annotation.SuppressLint("MissingPermission")
    fun start() {
        if (callback != null) return

        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch { evaluateNetwork(network) }
            }

            override fun onLost(network: Network) {
                scope.launch { _state.emit(ConnectivityState.Unavailable) }
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                scope.launch { evaluateCapabilities(network, caps) }
            }
        }

        callback = cb
        cm.registerDefaultNetworkCallback(cb)

        scope.launch { evaluateCurrentNetwork() }
    }

    fun stop() {
        callback?.let { cm.unregisterNetworkCallback(it) }
        callback = null
    }

    private suspend fun evaluateCurrentNetwork() {
        val active = cm.activeNetwork ?: return _state.emit(ConnectivityState.Unavailable)
        val caps = cm.getNetworkCapabilities(active)
        if (caps != null) {
            evaluateCapabilities(active, caps)
        } else {
            _state.emit(ConnectivityState.Unavailable)
        }
    }

    private suspend fun evaluateCapabilities(network: Network, caps: NetworkCapabilities) {
        when {
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> {
                _state.emit(ConnectivityState.Available(true))
            }

            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> {
                _state.emit(ConnectivityState.ConnectedNoInternet)

                scope.launch {
                    val ok = pinger.isInternetAvailable(network)
                    if (ok) _state.emit(ConnectivityState.Available(true))
                }
            }

            else -> {
                _state.emit(ConnectivityState.Unavailable)
            }
        }
    }

    private suspend fun evaluateNetwork(network: Network) {
        val caps = cm.getNetworkCapabilities(network)
        if (caps != null) {
            evaluateCapabilities(network, caps)
        } else {
            _state.emit(ConnectivityState.Unavailable)
        }
    }
}
