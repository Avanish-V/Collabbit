package com.iota.campusX.Feature.UserProfile.domain.useCases

import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveProfileUseCase(
    private val repository: UserProfileRepository
) {
     operator fun invoke(): Flow<ProfileResponse> {
        return repository.observeProfile()
    }
}
