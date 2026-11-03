package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository

class DeleteCommunityUseCase(private val repository: CommunityRepository) {
    suspend operator fun invoke(communityId: String): Result<Unit> {
        return repository.deleteCommunity(communityId)
    }
}
