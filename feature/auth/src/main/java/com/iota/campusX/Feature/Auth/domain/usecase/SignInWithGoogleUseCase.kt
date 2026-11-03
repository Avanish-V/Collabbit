package com.iota.campusX.Feature.Auth.domain.usecase

import android.content.Context
import com.iota.campusX.Feature.Auth.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(context: Context): Result<Unit> =
        repository.signInWithGoogle(context)
}
