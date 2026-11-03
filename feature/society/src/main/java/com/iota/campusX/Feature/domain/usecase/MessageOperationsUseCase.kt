package com.iota.campusX.Feature.Society.domain.usecase

import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository

class MessageOperationsUseCase(private val repository: CommunityRepository) {
    suspend fun reactToMessage(societyId: String, messageId: String, userId: String, emoji: String): Result<Unit> {
        return repository.reactToMessage(societyId, messageId, userId, emoji)
    }

    suspend fun openSnap(societyId: String, messageId: String, userId: String): Result<Unit> {
        return repository.openSnap(societyId, messageId, userId)
    }

    suspend fun cancelMessageUpload(messageId: String) {
        repository.cancelMessageUpload(messageId)
    }

    suspend fun deleteSocietyMessage(societyId: String, messageId: String): Result<Unit> {
        return repository.deleteSocietyMessage(societyId, messageId)
    }

    suspend fun pinMessage(societyId: String, messageId: String, previewText: String): Result<Unit> {
        return repository.pinMessage(societyId, messageId, previewText)
    }

    suspend fun unpinMessage(societyId: String): Result<Unit> {
        return repository.unpinMessage(societyId)
    }

    suspend fun markMessagesAsRead(societyId: String) {
        repository.markMessagesAsRead(societyId)
    }
}
