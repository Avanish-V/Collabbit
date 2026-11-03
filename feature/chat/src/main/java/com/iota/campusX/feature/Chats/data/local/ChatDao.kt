package com.iota.campusX.Feature.Chats.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(roomId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chat_messages WHERE roomId = :roomId AND senderId = :senderId AND text = :text")
    suspend fun deletePendingMessage(roomId: String, senderId: String, text: String)

    @Query("UPDATE chat_messages SET isPending = :isPending, isFailed = :isFailed WHERE messageId = :messageId")
    suspend fun updateMessageStatus(messageId: String, isPending: Boolean, isFailed: Boolean)

    @Query("DELETE FROM chat_messages WHERE roomId = :roomId")
    suspend fun clearRoomMessages(roomId: String)

    @Query("SELECT * FROM chat_rooms ORDER BY timeStamp DESC")
    fun getAllChatRooms(): Flow<List<ChatRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRooms(rooms: List<ChatRoomEntity>)

    @Query("DELETE FROM chat_rooms")
    suspend fun clearChatRooms()

    @Query("SELECT roomId FROM chat_rooms WHERE receiverId = :receiverId LIMIT 1")
    suspend fun getRoomIdByReceiverId(receiverId: String): String?

    @Query("SELECT receiverId FROM chat_rooms WHERE roomId = :roomId LIMIT 1")
    suspend fun getReceiverIdByRoomId(roomId: String): String?
}
