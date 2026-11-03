package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository

class LeaveCommunityUseCase(private val repository: CommunityRepository) {
    suspend operator fun invoke(communityId: String, userId: String): Result<Unit> {
        return repository.leaveCommunity(communityId, userId)
    }
}
