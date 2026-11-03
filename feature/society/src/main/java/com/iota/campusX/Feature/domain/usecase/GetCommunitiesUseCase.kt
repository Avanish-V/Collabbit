package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow

class GetCommunitiesUseCase(private val repository: CommunityRepository) {
    fun getAll(): Flow<List<Community>> = repository.getAllCommunities()
    fun getJoined(userId: String): Flow<List<Community>> = repository.getJoinedCommunities(userId)
}
