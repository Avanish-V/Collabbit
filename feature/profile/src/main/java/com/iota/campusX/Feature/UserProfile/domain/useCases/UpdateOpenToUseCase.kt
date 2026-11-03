package com.iota.campusX.Feature.UserProfile.domain.useCases

import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository

class UpdateOpenToUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(openTo: List<String>): Result<Boolean> {
        return repository.updateOpenTo(openTo)
    }
}
