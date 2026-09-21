package com.iota.campusX.Feature.Society.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iota.campusX.Feature.Society.data.local.*
import com.iota.campusX.Feature.Society.data.remote.FirestoreCommunityDataSource
import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import com.iota.campusX.Feature.Society.domain.repository.SocietyUser
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.UUID

class CommunityRepositoryImpl(
    private val communityDao: CommunityDao,
    private val remoteDataSource: FirestoreCommunityDataSource,
    private val firebaseDatabase: FirebaseDatabase,
    private val userProfileRepository: UserProfileRepository,
    private val userCacheDao: UserCacheDao
) : CommunityRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeUploadJobs = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.Job>()

    override suspend fun createCommunity(community: Community, logoUri: String?): Result<Unit> {
        val communityId = community.id.ifBlank { UUID.randomUUID().toString() }

        return try {
            // Attempt to create in Firestore first
            val remoteCommunity = remoteDataSource.createCommunity(community.copy(id = communityId), logoUri)
            
            // Add creator to joined societies remotely
            remoteDataSource.joinCommunity(communityId, remoteCommunity.creatorId)
            
            // Save to local Room DB (with initial member count 1)
            communityDao.insertCommunity(remoteCommunity.copy(memberCount = 1).toLocal(isSynced = true, isJoined = true))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SOCIETY_ERROR", "Failed to create community: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun updateCommunity(community: Community, logoUri: String?): Result<Unit> {
        return try {
            val remoteCommunity = remoteDataSource.updateCommunity(community, logoUri)
            communityDao.insertCommunity(remoteCommunity.toLocal(isSynced = true, isJoined = true))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SOCIETY_ERROR", "Failed to update community: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override fun getJoinedCommunities(userId: String): Flow<List<Community>> {
        return communityDao.getJoinedCommunities().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllCommunities(): Flow<List<Community>> {
        return communityDao.getAllCommunities().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun listenToCommunity(communityId: String): Flow<Community?> {
        val roomFlow = communityDao.getCommunityByIdFlow(communityId)
            .map { it?.toDomain() }

        return remoteDataSource.listenToCommunity(communityId)
            .onEach { community ->
                community?.let {
                    val existing = communityDao.getCommunityById(communityId)
                    val isJoined = existing?.isJoined ?: false
                    communityDao.insertCommunity(it.toLocal(isSynced = true, isJoined = isJoined))
                }
            }
            .flatMapLatest { roomFlow }
    }

    override suspend fun joinCommunity(community: Community, userId: String): Result<Unit> {
        return try {
            remoteDataSource.joinCommunity(community.id, userId)
            communityDao.insertCommunity(community.toLocal(isSynced = true, isJoined = true))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncCommunities(userId: String): Result<Unit> {
        return try {
            val remoteCommunities = remoteDataSource.getAllCommunities()
            val remoteIds = remoteCommunities.map { it.id }
            val remoteJoinedIds = remoteDataSource.getJoinedCommunityIds(userId).toSet()

            val localEntities = remoteCommunities.map { community ->
                val isJoined = remoteJoinedIds.contains(community.id) || community.creatorId == userId
                community.toLocal(isSynced = true, isJoined = isJoined) 
            }
            
            // 1. Update/Insert all from remote
            communityDao.insertCommunities(localEntities)
            
            // 2. Delete communities that no longer exist in remote
            if (remoteIds.isEmpty()) {
                communityDao.clearAllCommunities()
                communityDao.clearAllMessages()
            } else {
                communityDao.deleteCommunitiesNotIn(remoteIds)
                communityDao.deleteMessagesForSocietiesNotIn(remoteIds)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("SOCIETY_ERROR", "Sync communities failed: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    override suspend fun leaveCommunity(communityId: String, userId: String): Result<Unit> {
        return try {
            remoteDataSource.leaveCommunity(communityId, userId)
            communityDao.leaveCommunity(communityId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCommunity(communityId: String): Result<Unit> {
        return try {
            remoteDataSource.deleteCommunity(communityId)
            communityDao.deleteCommunity(communityId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSocietyMessages(societyId: String): Flow<List<SocietyMessage>> {
        return communityDao.getMessagesForSociety(societyId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun listenToSocietyMessages(societyId: String, currentUserId: String): Flow<List<SocietyMessage>> {
        // The "Production Way": UI observes local Room database as source of truth
        val roomFlow = communityDao.getMessagesForSociety(societyId)
            .map { entities -> entities.map { it.toDomain() } }

        // We use the Firestore listener to keep Room updated in the background
        // flatMapLatest ensures that as long as this flow is collected, 
        // the remote listener stays active and Room stays updated.
        return remoteDataSource.listenToMessages(societyId)
            .onEach { remoteMessages ->
                val entities = remoteMessages.map { it.toLocal(isRead = true) }
                communityDao.insertMessages(entities)
                communityDao.clearUnreadCount(societyId)
            }
            .flatMapLatest { roomFlow }
    }

    suspend fun handleNewMessage(message: SocietyMessage, currentUserId: String) {
        if (message.senderId != currentUserId) {
            communityDao.updateLastMessage(
                id = message.societyId,
                increment = 1,
                text = message.text,
                time = message.timestamp
            )
        } else {
            communityDao.updateLastMessage(
                id = message.societyId,
                increment = 0,
                text = message.text,
                time = message.timestamp
            )
        }
    }

    override suspend fun markMessagesAsRead(societyId: String) {
        communityDao.markMessagesAsRead(societyId)
        communityDao.clearUnreadCount(societyId)
    }

    override suspend fun reactToMessage(societyId: String, messageId: String, userId: String, emoji: String): Result<Unit> {
        return try {
            // Optimistic Update in Room DB
            val localMessage = communityDao.getMessageById(messageId)
            if (localMessage != null) {
                val currentReactions = localMessage.reactions.toMutableMap()
                val users = currentReactions[emoji]?.toMutableList() ?: mutableListOf()
                
                if (users.contains(userId)) {
                    users.remove(userId)
                } else {
                    users.add(userId)
                }
                
                if (users.isEmpty()) {
                    currentReactions.remove(emoji)
                } else {
                    currentReactions[emoji] = users
                }
                
                communityDao.insertMessage(localMessage.copy(reactions = currentReactions))
            }

            // Remote Update
            remoteDataSource.reactToMessage(societyId, messageId, userId, emoji)
            Result.success(Unit)
        } catch (e: Exception) {
            // Revert is not strictly needed as Firestore listener will correct Room
            Result.failure(e)
        }
    }

    override suspend fun openSnap(societyId: String, messageId: String, userId: String): Result<Unit> {
        return try {
            remoteDataSource.openSnap(societyId, messageId, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pinMessage(societyId: String, messageId: String, messageText: String): Result<Unit> {
        return try {
            // Optimistic update locally in Room
            val localCommunity = communityDao.getCommunityById(societyId)
            localCommunity?.let {
                communityDao.insertCommunity(it.copy(
                    pinnedMessageId = messageId,
                    pinnedMessageText = messageText
                ))
            }

            remoteDataSource.pinMessage(societyId, messageId, messageText)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unpinMessage(societyId: String): Result<Unit> {
        return try {
            // Optimistic update locally in Room
            val localCommunity = communityDao.getCommunityById(societyId)
            localCommunity?.let {
                communityDao.insertCommunity(it.copy(
                    pinnedMessageId = null,
                    pinnedMessageText = null
                ))
            }

            remoteDataSource.unpinMessage(societyId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSocietyMessage(societyId: String, messageId: String): Result<Unit> {
        return try {
            // Optimistic Update in Room DB
            communityDao.deleteMessage(messageId)
            
            // Remote Update
            remoteDataSource.deleteMessage(societyId, messageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendSocietyMessage(message: SocietyMessage, mediaUri: String?): Result<Unit> {
        val job = repositoryScope.launch {
            try {
                // Optimistic Update in Room DB: Set as sending
                val optimisticMessage = message.copy(isSending = true, isFailed = false, uploadProgress = 0f)
                communityDao.insertMessage(optimisticMessage.toLocal())
                
                // Remote Update with Progress Tracking
                val remoteMessage = remoteDataSource.sendMessage(
                    message = message, 
                    mediaUri = mediaUri,
                    onProgress = { progress ->
                        // Update Room with progress
                        repositoryScope.launch {
                            communityDao.updateMessageProgress(message.id, progress)
                        }
                    }
                )
                
                // Sync the finalized message from Firestore back to Room: Set as sent
                communityDao.insertMessage(remoteMessage.toLocal(isRead = true).copy(isSending = false, isFailed = false))
            } catch (e: Exception) {
                if (e is CancellationException) {
                    // Handled by cancelMessageUpload deleting the message
                    throw e
                }
                // Update Room to failed status
                communityDao.updateMessageStatus(message.id, isSending = false, isFailed = true)
            } finally {
                activeUploadJobs.remove(message.id)
            }
        }
        
        activeUploadJobs[message.id] = job
        return Result.success(Unit) // Return success immediately for the optimistic trigger
    }

    override suspend fun cancelMessageUpload(messageId: String) {
        activeUploadJobs[messageId]?.cancel()
        activeUploadJobs.remove(messageId)
        communityDao.deleteMessage(messageId)
    }

    override suspend fun syncSocietyMessages(societyId: String): Result<Unit> {
        return try {
            val remoteMessages = remoteDataSource.getMessages(societyId)
            communityDao.clearMessagesForSociety(societyId)
            communityDao.insertMessages(remoteMessages.map { it.toLocal() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun setPresence(societyId: String, userId: String, isOnline: Boolean) {
        try {
            val presenceRef = firebaseDatabase.getReference("presence/societies/$societyId/$userId")
            if (isOnline) {
                presenceRef.setValue(true).addOnFailureListener { e ->
                    Log.e("SOCIETY_ERROR", "Failed to set presence for user $userId in society $societyId", e)
                }
                presenceRef.onDisconnect().removeValue().addOnFailureListener { e ->
                    Log.e("SOCIETY_ERROR", "Failed to set onDisconnect for user $userId in society $societyId", e)
                }
            } else {
                presenceRef.removeValue().addOnFailureListener { e ->
                    Log.e("SOCIETY_ERROR", "Failed to remove presence for user $userId in society $societyId", e)
                }
            }
        } catch (e: Exception) {
            Log.e("SOCIETY_ERROR", "Error in setPresence: ${e.localizedMessage}", e)
        }
    }

    override fun getOnlineCount(societyId: String): Flow<Int> = callbackFlow {
        val onlineRef = firebaseDatabase.getReference("presence/societies/$societyId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.childrenCount.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SOCIETY_ERROR", "Online count listener cancelled: ${error.message}")
                close(error.toException())
            }
        }
        onlineRef.addValueEventListener(listener)
        awaitClose { onlineRef.removeEventListener(listener) }
    }

    override fun getCachedUser(userId: String): Flow<SocietyUser?> {
        return userCacheDao.getUser(userId).map { entity ->
            entity?.let { SocietyUser(it.uid, it.name, it.avatarUrl) }
        }
    }

    override suspend fun resolveUser(userId: String): Result<SocietyUser> {
        return try {
            val profileResult = userProfileRepository.getUserProfileById(userId)
            profileResult.fold(
                onSuccess = { profile ->
                    val societyUser = SocietyUser(
                        uid = userId,
                        name = profile.baseProfile.name,
                        avatarUrl = profile.baseProfile.image
                    )
                    userCacheDao.insertUser(
                        UserCacheEntity(
                            uid = userId,
                            name = societyUser.name,
                            avatarUrl = societyUser.avatarUrl
                        )
                    )
                    Result.success(societyUser)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
