package com.iota.campusX.Feature.Society.CommunityMessages.domain.UseCase

import com.iota.campusX.Feature.Society.CommunityMessages.domain.GroupChatInterface
import com.iota.campusX.Feature.Society.CommunityMessages.domain.models.Message
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveEnrichedMessagesUseCase(
    private val messageRepo: GroupChatInterface,
    private val userRepo: UserProfileRepository
) {

    operator fun invoke(groupId: String): Flow<Result<List<Message>>> =
        messageRepo.observeMessages(groupId).map { result ->

            result.fold(
                onSuccess = { messages ->
                    val enriched = messages.map { msg ->
                        asyncProfile(msg)
                    }
                    Result.success(enriched)
                },
                onFailure = { e ->
                    Result.failure(e)
                }
            )
        }

    private suspend fun asyncProfile(msg: Message): Message {
        val user = userRepo.getUserProfileById(msg.senderId).getOrNull()

        return msg.copy(
            userName = user?.name ?: "Unknown",
            avatarUrl = user?.image ?: null,
            bgColor = user?.bgColor ?: ""
        )
    }
}
