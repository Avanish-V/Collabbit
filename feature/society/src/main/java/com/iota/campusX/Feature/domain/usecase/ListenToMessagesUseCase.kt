package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow

class ListenToMessagesUseCase(private val repository: CommunityRepository) {
    operator fun invoke(societyId: String, currentUserId: String): Flow<List<SocietyMessage>> {
        return repository.listenToSocietyMessages(societyId, currentUserId)
    }
}
