package com.iota.campusX.Feature.Chats.domain

import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun sendMessage(message: String, messageId: String, receiverId: String):Flow<ResultState<Boolean>>

    fun updateIsUserActive(isActive:Boolean,participantId: String)

    fun getIsUserActive(receiverId: String):Flow<Boolean>

    fun updateIsUserTyping(isActive:Boolean,participantId: String)

    fun getIsUserTyping(participantId: String):Flow<Boolean>

    fun receiveMessage(participantId: String):Flow<ResultState<List<ChatMessage>>>

    fun getChats():Flow<ResultState<List<UserChatsDTO>>>

    fun markMessagesAsReed(participantId: String)

    fun getRoomId(participantId: String):Flow<ResultState<String>>




    
}