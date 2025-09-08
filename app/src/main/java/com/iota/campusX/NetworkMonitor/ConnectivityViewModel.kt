package com.iota.campusX.NetworkMonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject


class ConnectivityViewModel(
    private val repo: ConnectivityRepository
) : ViewModel() {

    // Expose a UI model that's easy for Compose to observe
    val uiState: StateFlow<ConnectivityUiState> = repo.connectivityState
        .map { connectivityState ->
            when (connectivityState) {
                is ConnectivityState.Available -> ConnectivityUiState.Online
                is ConnectivityState.Unavailable -> ConnectivityUiState.Offline
                is ConnectivityState.ConnectedNoInternet -> ConnectivityUiState.ConnectedNoInternet
                is ConnectivityState.CaptivePortal -> ConnectivityUiState.CaptivePortal
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConnectivityUiState.Offline)
}

sealed interface ConnectivityUiState {
    object Online : ConnectivityUiState
    object Offline : ConnectivityUiState
    object ConnectedNoInternet : ConnectivityUiState
    object CaptivePortal : ConnectivityUiState
}
