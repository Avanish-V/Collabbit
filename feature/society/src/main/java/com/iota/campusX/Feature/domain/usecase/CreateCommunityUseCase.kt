package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository

class CreateCommunityUseCase(private val repository: CommunityRepository) {
    suspend operator fun invoke(community: Community, logoUri: String?): Result<Unit> {
        return repository.createCommunity(community, logoUri)
    }
}
