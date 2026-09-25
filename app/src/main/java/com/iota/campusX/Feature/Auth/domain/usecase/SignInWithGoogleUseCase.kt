package com.iota.campusX.Feature.Auth.domain.usecase

import android.content.Context
import com.iota.campusX.Feature.Auth.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(context: Context): Result<Unit> {
        val credential = repository.getGoogleCredential(context)
            ?: return Result.failure(Exception("Failed to retrieve Google credentials"))

        val firebaseTokenResult = repository.authenticateWithFirebase(credential)
        val firebaseIdToken = firebaseTokenResult.getOrElse {
            repository.signOut()
            return Result.failure(it)
        }

        val verificationResult = repository.verifyUserToken(firebaseIdToken)
        return verificationResult.map { }
    }
}
