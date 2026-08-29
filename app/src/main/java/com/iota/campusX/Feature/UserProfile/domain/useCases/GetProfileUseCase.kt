package com.iota.campusX.Feature.UserProfile.domain.useCases

import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

class GetProfileUseCase(
    private val repository: UserProfileRepository
) {
     suspend operator fun invoke(): Result<ProfileResponse> {
        return repository.syncUserProfile()
    }
}
