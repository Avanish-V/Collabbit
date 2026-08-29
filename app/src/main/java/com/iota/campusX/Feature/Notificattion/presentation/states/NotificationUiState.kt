package com.iota.campusX.Feature.Notificattion.presentation.states

data class NotificationUiState(
    val unreadCount: Long = 0,
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)