package com.iota.campusX.Feature.Auth.domain.repository

import android.content.Context
import com.iota.campusX.Feature.Auth.domain.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun signInWithGoogle(context: Context): Result<Unit>
    suspend fun checkInitialAuthState(): AuthState
    suspend fun signOut()
}
