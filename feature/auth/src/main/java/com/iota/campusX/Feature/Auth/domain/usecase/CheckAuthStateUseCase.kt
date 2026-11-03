package com.iota.campusX.Feature.Auth.domain.usecase

import com.iota.campusX.Feature.Auth.domain.model.AuthState
import com.iota.campusX.Feature.Auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class CheckAuthStateUseCase(
    private val repository: AuthRepository
) {
    val authState: StateFlow<AuthState> = repository.authState

    suspend fun checkInitialAuthState(): AuthState {
        return repository.checkInitialAuthState()
    }
}
