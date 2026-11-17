package com.iota.campusX.Feature.Chats.domain

import com.google.firebase.database.ValueEventListener
import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.core.common.ResultState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun sendMessage(message: String, receiverId: String, roomId: String):Result<Boolean>

    fun updateIsUserActive(isActive:Boolean,roomId: String)

    fun getIsUserActive(receiverId: String,roomId: String):Flow<Boolean>

    fun updateIsUserTyping(isActive:Boolean,roomId: String)

    fun getIsUserTyping(participantId: String,roomId: String):Flow<Boolean>

    fun receiveMessage(participantId: String,roomId: String):Flow<ResultState<List<ChatMessage>>>

    fun getChats(): Flow<ResultState<List<UserChatsDTO>>>

    suspend fun markMessagesAsReed(participantId: String, roomId: String)

    suspend fun getRoomId(participantId: String): Result<String>

    suspend fun deleteChat(chatId: String, roomId: String): Result<Boolean>

    suspend fun fetchChatRoomId(userId: String): Result<String>

    suspend fun fetchAndSaveIncomingMessage(messageId: String, roomId: String)

    fun observeUnreadCount(): Flow<Int>

    suspend fun swipeRightAction(targetUserId: String, roomId: String?, action: String, note: String? = null): Result<SwipeActionResponse>

    fun observeRoomStatus(roomId: String): Flow<String?>

    suspend fun fetchRoomStatus(roomId: String): Result<String>

}