package com.iota.campusX.Feature.UserProfile.domain.useCases

import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraCheckInResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.AuraRepository

/** Awards the default daily aura points to the current user. */
class RecordCheckInUseCase(private val repository: AuraRepository) {
    suspend operator fun invoke(userId: String): Result<AuraCheckInResponse> = 
        repository.awardPoints(userId, "daily_check_in")
}
