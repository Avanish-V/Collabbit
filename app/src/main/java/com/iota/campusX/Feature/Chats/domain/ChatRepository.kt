package com.iota.campusX.Feature.Chats.domain

import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun sendMessage(message: String, receiverId: String,roomId: String):Flow<ResultState<Boolean>>

    fun updateIsUserActive(isActive:Boolean,roomId: String)

    fun getIsUserActive(roomId: String,receiverId: String):Flow<Boolean>

    fun updateIsUserTyping(isActive:Boolean,roomId: String)

    fun getIsUserTyping(roomId:String,participantId: String):Flow<Boolean>

    fun receiveMessage(roomId: String):Flow<ResultState<List<ChatMessage>>>

    fun getChats():Flow<ResultState<List<UserChatsDTO>>>

    fun markMessagesAsReed(roomId: String,participantId: String)




    
}