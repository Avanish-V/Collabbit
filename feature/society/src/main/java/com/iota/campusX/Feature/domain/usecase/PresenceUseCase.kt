package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow

class PresenceUseCase(private val repository: CommunityRepository) {
    fun setPresence(societyId: String, userId: String, isOnline: Boolean) {
        repository.setPresence(societyId, userId, isOnline)
    }

    fun getOnlineCount(societyId: String): Flow<Int> {
        return repository.getOnlineCount(societyId)
    }
}
