package com.iota.campusX.Feature.Auth.domain.usecase

import com.iota.campusX.Feature.Auth.domain.repository.AuthRepository

class SignOutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.signOut()
    }
}
