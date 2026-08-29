package com.iota.campusX.Feature.UserProfile.domain.useCases

import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository

class UpdateFcmTokenUseCase(
    private val repository: UserProfileRepository
) {

    suspend operator fun invoke(
        token: String
    ) {
        repository.updateFcmToken(token)
    }
}