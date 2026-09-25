package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository

class SendSocietyMessageUseCase(private val repository: CommunityRepository) {
    suspend operator fun invoke(message: SocietyMessage, mediaUri: String? = null): Result<Unit> {
        return repository.sendSocietyMessage(message, mediaUri)
    }
}
