package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import com.iota.campusX.Feature.Society.domain.repository.SocietyUser
import kotlinx.coroutines.flow.Flow

class ResolveUserUseCase(private val repository: CommunityRepository) {
    fun getCachedUser(userId: String): Flow<SocietyUser?> {
        return repository.getCachedUser(userId)
    }

    suspend fun resolveUser(userId: String): Result<SocietyUser> {
        return repository.resolveUser(userId)
    }
}
