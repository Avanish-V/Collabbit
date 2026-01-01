// domain/usecase/SendMessageUseCase.kt
package com.iota.campusX.Feature.Society.CommunityMessages.domain.UseCase

import com.iota.campusX.Feature.Society.CommunityMessages.data.model.MessageDto
import com.iota.campusX.Feature.Society.CommunityMessages.domain.GroupChatInterface

class SendMessageUseCase(private val repo: GroupChatInterface) {
    suspend operator fun invoke(groupId: String, message: MessageDto): Result<Unit>{
        // validation / analytics / throttling can live here
        if (message.text.isBlank()) throw IllegalArgumentException("Message empty")
        return repo.sendMessage(groupId, message)
    }
}
