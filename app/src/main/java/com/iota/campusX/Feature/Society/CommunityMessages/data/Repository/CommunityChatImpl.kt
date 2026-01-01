package com.iota.campusX.Feature.Society.CommunityMessages.data.Repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.iota.campusX.Feature.Society.CommunityMessages.data.DataSource
import com.iota.campusX.Feature.Society.CommunityMessages.data.model.MessageDto
import com.iota.campusX.Feature.Society.CommunityMessages.domain.GroupChatInterface
import com.iota.campusX.Feature.Society.CommunityMessages.domain.models.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class CommunityChatImpl(private val dataSource: DataSource,private val firebaseAuth: FirebaseAuth): GroupChatInterface {

    override suspend fun sendMessage(
        groupId: String,
        messageDto: MessageDto
    ): Result<Unit> {
        return try {
            val chat = MessageDto(
                text = messageDto.text,
                replyTo = messageDto.replyTo,
                messageId = messageDto.messageId,
                senderId = firebaseAuth.currentUser?.uid ?: "",
                mediaUrl = null,
                mediaType = null,
                timestamp = FieldValue.serverTimestamp()
            )
            dataSource.sendMessage(groupId, chat)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CommunityChatImpl", "sendMessage: ", e)
            Result.failure(e)
        }
    }


    override fun observeMessages(groupId: String): Flow<Result<List<Message>>> =
        dataSource.observeMessages(groupId)
            .map { list ->
                Result.success(
                    list.map {
                        Message(
                            id = it.messageId,
                            senderId = it.senderId,
                            text = it.text,
                            timestamp = it.timestamp,
                            replyToMessageId = it.replyTo,
                            mentions = it.mentions,
                            bgColor = ""
                        )
                    }
                )
        }.catch {
            emit(Result.failure(it))
        }

}