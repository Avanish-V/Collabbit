package com.iota.campusX.Feature.Auth.domain.usecase

import com.iota.campusX.Feature.Auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class CheckAuthStateUseCase(
    private val repository: AuthRepository
) {
    val isLoggedIn: StateFlow<Boolean?> = repository.isLoggedIn

    suspend fun checkInitialAuthState(): Boolean {
        return repository.checkInitialAuthState()
    }
}
