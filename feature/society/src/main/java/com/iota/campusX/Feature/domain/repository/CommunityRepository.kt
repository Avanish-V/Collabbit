package com.iota.campusX.Feature.Society.domain.repository

import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    suspend fun createCommunity(community: Community, logoUri: String?): Result<Unit>
    suspend fun updateCommunity(community: Community, logoUri: String?): Result<Unit>
    fun getJoinedCommunities(userId: String): Flow<List<Community>>
    fun getAllCommunities(): Flow<List<Community>>
    fun listenToCommunity(communityId: String): Flow<Community?>
    suspend fun joinCommunity(community: Community, userId: String): Result<Unit>
    suspend fun syncCommunities(userId: String): Result<Unit>
    suspend fun leaveCommunity(communityId: String, userId: String): Result<Unit>
    suspend fun deleteCommunity(communityId: String): Result<Unit>

    // --- Message Operations ---
    fun getSocietyMessages(societyId: String): Flow<List<SocietyMessage>>
    fun listenToSocietyMessages(societyId: String, currentUserId: String): Flow<List<SocietyMessage>>
    suspend fun sendSocietyMessage(message: SocietyMessage, mediaUri: String? = null): Result<Unit>
    suspend fun cancelMessageUpload(messageId: String)
    suspend fun reactToMessage(societyId: String, messageId: String, userId: String, emoji: String): Result<Unit>
    suspend fun openSnap(societyId: String, messageId: String, userId: String): Result<Unit>
    suspend fun pinMessage(societyId: String, messageId: String, messageText: String): Result<Unit>
    suspend fun unpinMessage(societyId: String): Result<Unit>
    suspend fun deleteSocietyMessage(societyId: String, messageId: String): Result<Unit>
    suspend fun syncSocietyMessages(societyId: String): Result<Unit>
    suspend fun markMessagesAsRead(societyId: String)

    // --- Presence Operations ---
    fun setPresence(societyId: String, userId: String, isOnline: Boolean)
    fun getOnlineCount(societyId: String): Flow<Int>

    // --- User Cache Operations ---
    fun getCachedUser(userId: String): Flow<SocietyUser?>
    suspend fun resolveUser(userId: String): Result<SocietyUser>
}

data class SocietyUser(
    val uid: String,
    val name: String,
    val avatarUrl: String?
)
