package com.iota.campusX.Feature.Auth.domain.model

sealed interface AuthState {
    data object Unknown : AuthState
    data object Unauthenticated : AuthState
    data object Authenticating : AuthState
    data object Verifying : AuthState
    data object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}
