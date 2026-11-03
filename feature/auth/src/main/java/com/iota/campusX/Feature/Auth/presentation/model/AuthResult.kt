package com.iota.campusX.Feature.Auth.presentation.model

sealed interface AuthResult {
    data object Idle : AuthResult
    data object Loading : AuthResult
    data object SignedIn : AuthResult
    data class Error(val message: String) : AuthResult
    data object SignedOut : AuthResult
}
