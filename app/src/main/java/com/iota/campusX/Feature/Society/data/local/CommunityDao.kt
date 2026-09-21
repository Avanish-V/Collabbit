package com.iota.campusX.Feature.Society.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {
    @Query("SELECT * FROM communities WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllCommunities(): Flow<List<CommunityEntity>>

    @Query("SELECT * FROM communities WHERE isJoined = 1 AND isDeleted = 0 ORDER BY name ASC")
    fun getJoinedCommunities(): Flow<List<CommunityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunities(communities: List<CommunityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(community: CommunityEntity)

    @Query("SELECT * FROM communities WHERE id = :id")
    suspend fun getCommunityById(id: String): CommunityEntity?

    @Query("SELECT * FROM communities WHERE id = :id")
    fun getCommunityByIdFlow(id: String): Flow<CommunityEntity?>

    @Query("SELECT * FROM communities WHERE isSynced = 0")
    suspend fun getUnsyncedCommunities(): List<CommunityEntity>

    @Query("SELECT id FROM communities WHERE isJoined = 1")
    suspend fun getJoinedCommunityIds(): List<String>

    @Query("UPDATE communities SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM communities WHERE id = :id")
    suspend fun deleteCommunity(id: String)

    @Query("UPDATE communities SET isJoined = 0 WHERE id = :id")
    suspend fun leaveCommunity(id: String)

    @Query("UPDATE communities SET unreadCount = 0 WHERE id = :id")
    suspend fun clearUnreadCount(id: String)

    @Query("UPDATE communities SET unreadCount = unreadCount + :increment, lastMessageText = :text, lastMessageTime = :time WHERE id = :id")
    suspend fun updateLastMessage(id: String, increment: Int, text: String, time: Long)

    @Query("DELETE FROM communities")
    suspend fun clearAllCommunities()

    @Query("DELETE FROM communities WHERE id NOT IN (:ids)")
    suspend fun deleteCommunitiesNotIn(ids: List<String>)

    // --- Message Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<SocietyMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SocietyMessageEntity)

    @Query("UPDATE society_messages SET isRead = 1 WHERE societyId = :societyId")
    suspend fun markMessagesAsRead(societyId: String)

    @Query("SELECT * FROM society_messages WHERE societyId = :societyId ORDER BY timestamp ASC")
    fun getMessagesForSociety(societyId: String): Flow<List<SocietyMessageEntity>>

    @Query("SELECT * FROM society_messages WHERE id = :id")
    suspend fun getMessageById(id: String): SocietyMessageEntity?

    @Query("UPDATE society_messages SET uploadProgress = :progress WHERE id = :messageId")
    suspend fun updateMessageProgress(messageId: String, progress: Float)

    @Query("UPDATE society_messages SET isSending = :isSending, isFailed = :isFailed WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, isSending: Boolean, isFailed: Boolean)

    @Query("DELETE FROM society_messages")
    suspend fun clearAllMessages()

    @Query("DELETE FROM society_messages WHERE societyId NOT IN (:ids)")
    suspend fun deleteMessagesForSocietiesNotIn(ids: List<String>)

    @Query("DELETE FROM society_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM society_messages WHERE societyId = :societyId")
    suspend fun clearMessagesForSociety(societyId: String)
}
