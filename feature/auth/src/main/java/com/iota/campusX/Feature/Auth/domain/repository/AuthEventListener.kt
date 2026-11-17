package com.iota.campusX.Feature.Auth.domain.repository

interface AuthEventListener {
    suspend fun onSignInSuccess()
    suspend fun onSignOut()
}
