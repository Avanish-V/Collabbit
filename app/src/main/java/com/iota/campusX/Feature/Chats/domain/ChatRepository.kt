package com.iota.campusX.Feature.Chats.domain

import com.google.firebase.database.ValueEventListener
import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun sendMessage(
        message: String,
        messageId: String,
        timestamp: Long,
        receiverId: String,
        roomId: String
    ):Flow<ResultState<Boolean>>

    fun updateIsUserActive(isActive:Boolean,roomId: String)

    fun getIsUserActive(receiverId: String,roomId: String):Flow<Boolean>

    fun updateIsUserTyping(isActive:Boolean,roomId: String)

    fun getIsUserTyping(participantId: String,roomId: String):Flow<Boolean>

    fun receiveMessage(participantId: String,roomId: String):Flow<ResultState<List<ChatMessage>>>

    suspend fun getChats(): Result<List<UserChatsDTO>>

    fun markMessagesAsReed(participantId: String,roomId: String): Flow<Unit>

    fun getRoomId(participantId: String):Flow<ResultState<String>>

    fun deleteChat(chatId:String,roomId: String):Flow<ResultState<Boolean>>

    fun fetchChatRoomId(userId: String): Flow<ResultState<String>>

}