package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository

class JoinCommunityUseCase(private val repository: CommunityRepository) {
    suspend operator fun invoke(community: Community, userId: String): Result<Unit> {
        return repository.joinCommunity(community, userId)
    }
}
