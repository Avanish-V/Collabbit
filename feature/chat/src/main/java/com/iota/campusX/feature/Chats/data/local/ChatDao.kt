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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertIncomingMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chat_messages WHERE roomId = :roomId AND senderId = :senderId AND text = :text")
    suspend fun deletePendingMessage(roomId: String, senderId: String, text: String)

    @Query("UPDATE chat_messages SET isPending = :isPending, isFailed = :isFailed WHERE messageId = :messageId")
    suspend fun updateMessageStatus(messageId: String, isPending: Boolean, isFailed: Boolean)

    @Query("DELETE FROM chat_messages WHERE roomId = :roomId")
    suspend fun clearRoomMessages(roomId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    @Query("SELECT * FROM chat_rooms ORDER BY timeStamp DESC")
    fun getAllChatRooms(): Flow<List<ChatRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRooms(rooms: List<ChatRoomEntity>)

    @Query("DELETE FROM chat_rooms")
    suspend fun clearChatRooms()

    @Query("SELECT * FROM chat_rooms WHERE receiverId = :receiverId LIMIT 1")
    suspend fun getRoomByReceiverId(receiverId: String): ChatRoomEntity?

    @Query("SELECT status FROM chat_rooms WHERE roomId = :roomId LIMIT 1")
    fun observeRoomStatus(roomId: String): Flow<String?>

    @Query("SELECT status FROM chat_rooms WHERE roomId = :roomId LIMIT 1")
    suspend fun getRoomStatusDirect(roomId: String): String?

    @Query("UPDATE chat_rooms SET status = :status WHERE roomId = :roomId")
    suspend fun updateRoomStatus(roomId: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(room: ChatRoomEntity)

    @Query("SELECT roomId FROM chat_rooms WHERE receiverId = :receiverId LIMIT 1")
    suspend fun getRoomIdByReceiverId(receiverId: String): String?

    @Query("SELECT receiverId FROM chat_rooms WHERE roomId = :roomId LIMIT 1")
    suspend fun getReceiverIdByRoomId(roomId: String): String?
    @Query("""
    UPDATE chat_messages
    SET read = 1
    WHERE messageId IN (:messageIds)
""")
    suspend fun markMessagesAsRead(messageIds: List<String>)

    @Query("UPDATE chat_rooms SET unreadCount = 0 WHERE roomId = :roomId")
    suspend fun clearUnreadCountForRoom(roomId: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE read = 0 AND senderId != :currentUserId")
    fun observeUnreadMessagesCount(currentUserId: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(unreadCount), 0) FROM chat_rooms")
    fun observeRoomsUnreadCount(): Flow<Int>

    /**
     * Mark all messages in a room as read locally (called when user opens the chat screen).
     * Excludes messages sent by currentUser (they are already "read" on their side).
     */
    @Query("UPDATE chat_messages SET read = 1 WHERE roomId = :roomId AND senderId != :currentUserId")
    suspend fun markRoomMessagesAsRead(roomId: String, currentUserId: String)

    /**
     * Increment unread count for a chat room when a new message arrives globally (while screen is closed).
     */
    @Query("UPDATE chat_rooms SET unreadCount = unreadCount + 1 WHERE roomId = :roomId")
    suspend fun incrementUnreadCount(roomId: String)
}
