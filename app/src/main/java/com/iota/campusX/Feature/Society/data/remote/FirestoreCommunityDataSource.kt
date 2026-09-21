package com.iota.campusX.Feature.Society.data.remote

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import com.iota.campusX.Feature.Society.domain.model.MessageType
import com.iota.campusX.Koin.AppConstants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreCommunityDataSource(
    private val firestore: FirebaseFirestore,
    private val s3Uploader: S3Uploader
) {
    private val communityCollection = if(AppConstants.IS_PRODUCTION){
        firestore.collection("communities")
    }else{
        firestore.collection("communities_test")
    }
    private val userCollection = if(AppConstants.IS_PRODUCTION){
        "users"
    }else{
        "users_test"
    }

    suspend fun createCommunity(community: Community, logoUri: String?): Community {
        val id = community.id.ifBlank { UUID.randomUUID().toString() }
        var finalLogoUrl = community.logoUrl

        if (logoUri != null) {
            val uploadedUrl = s3Uploader.UploadImageToS3(Uri.parse(logoUri), folder = "community_logos")
            if (uploadedUrl != null) {
                finalLogoUrl = uploadedUrl
            }
        }

        val communityWithId = community.copy(id = id, logoUrl = finalLogoUrl)
        communityCollection.document(id).set(communityWithId).await()
        return communityWithId
    }

    suspend fun updateCommunity(community: Community, logoUri: String?): Community {
        var finalLogoUrl = community.logoUrl

        if (logoUri != null && !logoUri.startsWith("http")) {
            val uploadedUrl = s3Uploader.UploadImageToS3(Uri.parse(logoUri), folder = "community_logos")
            if (uploadedUrl != null) {
                finalLogoUrl = uploadedUrl
            }
        }

        val communityWithUrl = community.copy(logoUrl = finalLogoUrl)
        communityCollection.document(community.id).set(communityWithUrl).await()
        return communityWithUrl
    }

    suspend fun getAllCommunities(): List<Community> {
        val snapshot = communityCollection.get().await()
        return snapshot.toObjects(Community::class.java).filter { !it.isDeleted }
    }

    suspend fun deleteCommunity(id: String) {
        communityCollection.document(id).update("isDeleted", true).await()
    }

    // --- Membership Operations ---

    suspend fun joinCommunity(communityId: String, userId: String) {
        firestore.runBatch { batch ->
            val userJoinRef = firestore.collection(userCollection)
                .document(userId)
                .collection("joined_societies")
                .document(communityId)
            
            batch.set(userJoinRef, mapOf("joinedAt" to System.currentTimeMillis()))
            
            val communityRef = communityCollection.document(communityId)
            batch.update(communityRef, "memberCount", FieldValue.increment(1))
        }.await()
    }

    suspend fun leaveCommunity(communityId: String, userId: String) {
        firestore.runBatch { batch ->
            val userJoinRef = firestore.collection(userCollection)
                .document(userId)
                .collection("joined_societies")
                .document(communityId)
            
            batch.delete(userJoinRef)
            
            val communityRef = communityCollection.document(communityId)
            batch.update(communityRef, "memberCount", FieldValue.increment(-1))
        }.await()
    }

    suspend fun getJoinedCommunityIds(userId: String): List<String> {
        val snapshot = firestore.collection(userCollection)
            .document(userId)
            .collection("joined_societies")
            .get()
            .await()
        return snapshot.documents.map { it.id }
    }

    fun listenToCommunity(communityId: String): Flow<Community?> = callbackFlow {
        val registration = communityCollection.document(communityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(Community::class.java))
                }
            }
        awaitClose { registration.remove() }
    }

    // --- Message Operations ---

    fun listenToMessages(societyId: String): Flow<List<SocietyMessage>> = callbackFlow {
        val registration = communityCollection.document(societyId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(SocietyMessage::class.java)
                    trySend(messages)
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun getMessages(societyId: String): List<SocietyMessage> {
        val snapshot = communityCollection.document(societyId)
            .collection("messages")
            .orderBy("timestamp")
            .get().await()
        return snapshot.toObjects(SocietyMessage::class.java)
    }

    suspend fun sendMessage(
        message: SocietyMessage, 
        mediaUri: String? = null,
        onProgress: ((Float) -> Unit)? = null
    ): SocietyMessage {
        val id = message.id.ifBlank { UUID.randomUUID().toString() }
        var finalMediaUrl = message.mediaUrl
        var finalThumbnailUrl = message.thumbnailUrl

        if (mediaUri != null) {
            val folder = when(message.type) {
                MessageType.IMAGE -> "society_messages/${message.societyId}/images"
                MessageType.SNAP -> "society_messages/${message.societyId}/snaps"
                MessageType.VIDEO -> "society_messages/${message.societyId}/videos"
                MessageType.FILE -> "society_messages/${message.societyId}/files"
                else -> "society_messages/${message.societyId}/others"
            }
            
            if (message.type == MessageType.VIDEO) {
                val (videoUrl, thumbUrl) = s3Uploader.uploadVideoWithThumbnail(Uri.parse(mediaUri), folder = folder, onProgress = onProgress)
                if (videoUrl == null) {
                    throw Exception("Failed to upload video attachment")
                }
                finalMediaUrl = videoUrl
                finalThumbnailUrl = thumbUrl
            } else {
                val shouldCompress = message.type == MessageType.IMAGE || message.type == MessageType.SNAP
                val uploadedUrl = s3Uploader.UploadImageToS3(
                    uri = Uri.parse(mediaUri), 
                    folder = folder, 
                    shouldCompress = shouldCompress,
                    onProgress = onProgress
                )
                if (uploadedUrl == null) {
                    throw Exception("Failed to upload attachment")
                }
                finalMediaUrl = uploadedUrl
            }
        }

        val messageWithId = message.copy(
            id = id, 
            mediaUrl = finalMediaUrl,
            thumbnailUrl = finalThumbnailUrl,
            timestamp = System.currentTimeMillis(),
            reactions = emptyMap()
        )
        communityCollection.document(message.societyId)
            .collection("messages")
            .document(id)
            .set(messageWithId)
            .await()
        return messageWithId
    }

    suspend fun reactToMessage(societyId: String, messageId: String, userId: String, emoji: String) {
        val messageRef = communityCollection.document(societyId)
            .collection("messages")
            .document(messageId)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(messageRef)
            
            // Safer way to extract reactions map from Firestore snapshot
            val rawReactions = snapshot.get("reactions") as? Map<*, *>
            val newReactions = mutableMapOf<String, List<String>>()
            
            rawReactions?.forEach { (key, value) ->
                if (key is String && value is List<*>) {
                    newReactions[key] = value.filterIsInstance<String>()
                }
            }
            
            val users = newReactions[emoji]?.toMutableList() ?: mutableListOf()
            if (users.contains(userId)) {
                users.remove(userId)
            } else {
                users.add(userId)
            }
            
            if (users.isEmpty()) {
                newReactions.remove(emoji)
            } else {
                newReactions[emoji] = users
            }
            
            transaction.update(messageRef, "reactions", newReactions)
        }.await()
    }

    suspend fun openSnap(societyId: String, messageId: String, userId: String) {
        val messageRef = communityCollection.document(societyId)
            .collection("messages")
            .document(messageId)
        
        messageRef.update("openedBy.$userId", true).await()
    }

    suspend fun deleteMessage(societyId: String, messageId: String) {
        communityCollection.document(societyId)
            .collection("messages")
            .document(messageId)
            .delete()
            .await()
    }

    suspend fun pinMessage(societyId: String, messageId: String, messageText: String) {
        communityCollection.document(societyId).update(
            mapOf(
                "pinnedMessageId" to messageId,
                "pinnedMessageText" to messageText
            )
        ).await()
    }

    suspend fun unpinMessage(societyId: String) {
        communityCollection.document(societyId).update(
            mapOf(
                "pinnedMessageId" to null,
                "pinnedMessageText" to null
            )
        ).await()
    }
}
