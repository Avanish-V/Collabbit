package com.iota.campusX.Feature.Society.CommunityMessages.domain

import com.iota.campusX.Feature.Society.CommunityMessages.data.model.MessageDto
import com.iota.campusX.Feature.Society.CommunityMessages.domain.models.Message
import kotlinx.coroutines.flow.Flow

interface GroupChatInterface {

    suspend fun sendMessage(groupId: String,messageDto: MessageDto): Result<Unit>
    fun observeMessages(groupId: String): Flow<Result<List<Message>>>

}