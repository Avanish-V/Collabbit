package com.iota.campusX.Feature.Society.data

import android.net.Uri
import android.util.Log
import com.google.api.client.util.Data.mapOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Society.AgoraTokenBuilder.generateDigitRandom
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.SetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.Status
import com.iota.campusX.Feature.Society.domain.models.GetChatMessage
import com.iota.campusX.Feature.Society.domain.models.SetChatMessage
import com.iota.campusX.Feature.Society.domain.repository.SocietyInterface
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SocietyImplementation(
    private val fireStore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val fireStorage: FirebaseStorage
):SocietyInterface{

    override suspend fun createSociety(createSocietyDTO: CreateSocietyDTO,imageUri: Uri?): Result<Unit> {

        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))

        return try {

            imageUri?.let {
                val fileName = "${createSocietyDTO.roomId}_${System.currentTimeMillis()}.jpg"

               val upload =  fireStorage.reference.child("SocietyImages/$fileName").putFile(it).await()

               val downloadUrl = upload.storage.downloadUrl.await()

                fireStore.collection("Society")
                    .document(createSocietyDTO.roomId)
                    .set(createSocietyDTO.copy(imageUrl = downloadUrl.toString()))
                    .await()

            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchSocieties(feedMode: FeedMode,campusId: String?): Result<List<GetSocietyDTO>> {
        return try {

            val path = if (feedMode == FeedMode.CAMPUS && campusId != null){
                "CampusSociety"
            }else{
                "GlobalSociety"
            }

            val data = fireStore.collection("Society")
                .whereEqualTo("campusId",campusId)
                .get()
                .await()

            val societyList = data.documents.mapNotNull {doc->

                val society = doc.toObject(CreateSocietyDTO::class.java) ?: return@mapNotNull null

                val createdById = society.createdBy
                val isCurrentUser = createdById == auth.currentUser?.uid

                val createdBy = fireStore
                    .collection("Users")
                    .document(createdById)
                    .get()
                    .await()
                    .toObject(UserBasicDetail::class.java)?: UserBasicDetail()



                GetSocietyDTO(
                    societyName = society.societyName,
                    description = society.description,
                    createdBy = createdBy,
                    joined = society.joined,
                    mode = society.mode,
                    roomId = society.roomId,
                    isCurrentUser = isCurrentUser,
                    active = society.active,
                    imageUrl = society.imageUrl
                )

            }

            Result.success(societyList)

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun fetchUserSocieties(userId: String): Result<List<GetSocietyDTO>> {
        return try {

            val data = fireStore.collection("Society")
                .whereEqualTo("createdBy",userId)
                .get()
                .await()

            val societyList = data.documents.mapNotNull {doc->

                val society = doc.toObject(CreateSocietyDTO::class.java) ?: return@mapNotNull null

                val createdById = society.createdBy
                val isCurrentUser = createdById == auth.currentUser?.uid

                val createdBy = fireStore
                    .collection("Users")
                    .document(createdById)
                    .get()
                    .await()
                    .toObject(UserBasicDetail::class.java)?: UserBasicDetail()



                GetSocietyDTO(
                    societyName = society.societyName,
                    description = society.description,
                    createdBy = createdBy,
                    joined = society.joined,
                    mode = society.mode,
                    roomId = society.roomId,
                    isCurrentUser = isCurrentUser,
                    imageUrl = society.imageUrl
                )

            }

            Result.success(societyList)

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun requestToJoin(roomId: String, role: String, status: Status, feedMode: FeedMode, campusId: String?): Result<Int> {

        return try {

            if (auth.currentUser == null) return Result.failure(Exception("User not authenticated"))

            Log.d("JOIN_REQUEST_ERROR",roomId)

            val path = if (feedMode == FeedMode.CAMPUS && campusId != null){
                "CampusSociety"
            }else{
                "GlobalSociety"
            }

            val uid = generateDigitRandom()

            val data= mapOf(
                "requestId" to auth.currentUser?.uid,
                "role" to role,
                "status" to status,
                "uid" to uid,
                "muted" to true,
                "speaking" to false,
                "raiseHand" to false
            )

            fireStore.collection("Society").document(roomId)
                .collection("JoinRequests")
                .document(auth.currentUser?.uid ?: "")
                .set(data)
                .await()
            Result.success(uid)

        }catch (e:Exception){
            Result.failure(e)
        }


    }

    override suspend fun deleteJoinRequest(roomId: String, feedMode: FeedMode, campusId: String?): Result<Unit> {
        return try {

            fireStore.collection("Society").document(roomId)
                .collection("JoinRequests")
                .document(auth.currentUser?.uid ?: "")
                .delete()
                .await()
            Result.success(Unit)
        }catch (e: Exception){
            Log.d("JOIN_REQUEST_ERROR",e.message.toString())
            Result.failure(e)
        }
    }

    override suspend fun clearAudioRoom(roomId: String, feedMode: FeedMode, campusId: String?): Result<Unit> {
        return try {

            fireStore.collection("Society")
                .document(roomId)
                .collection("JoinRequests")
                .get()
                .await()
                .documents.forEach {
                    it.reference.delete()
                }
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun listenForApproval(roomId: String, feedMode: FeedMode, campusId: String?): Flow<Result<List<GetJoinRequestDTO>>> = callbackFlow {

        val listener = fireStore.collection("Society")
            .document(roomId)
            .collection("JoinRequests")
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener

                // Launch a coroutine inside the flow scope
                launch {
                    val requests = value.documents.mapNotNull { doc ->

                        val data = doc.toObject(SetJoinRequestDTO::class.java) ?: return@mapNotNull null

                        try {

                            val userSnapshot = fireStore.collection(
                                "Users")
                                .document(data.requestId)
                                .get()
                                .await()

                            val user = userSnapshot.toObject(UserBasicDetail::class.java) ?: return@mapNotNull null

                            GetJoinRequestDTO(
                                requestId = data.requestId,
                                userName = user.userName,
                                userImage = user.userImage,
                                role = data.role,
                                status = data.status,
                                speaking = data.speaking,
                                muted = data.muted,
                                raiseHand = data.raiseHand,
                                uid = data.uid
                            )

                        } catch (e: Exception) {
                            trySend(Result.failure(e))
                            null
                        }
                    }
                    trySend(Result.success((requests)))
                }
            }


        awaitClose { listener.remove() }
    }

    override suspend fun stageUpParticipant(roomId: String, status: Status, requestId: String, feedMode: FeedMode, campusId: String?): Result<String> {
        return try {

            val path = resolveRoomPath(feedMode, campusId)

            fireStore.collection("Society")
                .document(roomId)
                .collection("JoinRequests")
                .document(requestId)
                .update("status", status)
                .await()

            Result.success(requestId)

        } catch (e: Exception) {
            Log.e("Firestore", "Stage up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun isMicrophoneEnabled(roomId: String, isMicrophone: Boolean, requestId: String, feedMode: FeedMode, campusId: String?): Result<Unit> {
        return try {


            fireStore.collection("Society")
                .document(roomId)
                .collection("JoinRequests")
                .document(auth.currentUser?.uid ?: "")
                .update("muted", isMicrophone)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isSpeaking(roomId: String, isSpeaking: Boolean, requestId: String, feedMode: FeedMode, campusId: String?): Result<Unit> {
        return try {

            val path = resolveRoomPath(feedMode, campusId)


            fireStore.collection("Society")
                .document(roomId)
                .collection("JoinRequests")
                .document(auth.currentUser?.uid ?: "")
                .update("speaking", isSpeaking)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("Firestore", "Stage up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun askToSpeak(roomId: String, isRaiseHand: Boolean, requestId: String, feedMode: FeedMode, campusId: String?): Result<Unit> {
        return try {

            val path = resolveRoomPath(feedMode, campusId)

            fireStore.collection("Society")
                .document(roomId)
                .collection("JoinRequests")
                .document(auth.currentUser?.uid ?: "")
                .update("raiseHand", isRaiseHand)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("Firestore", "Stage up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteRoom(roomId: String): Result<Unit> {

        return try {

            fireStore.collection("Society")
                .document(roomId)
                .delete()
                .await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun audioRoomStatus(isActive: Boolean,roomId: String): Result<Unit> {
        return try {

            fireStore.collection("Society")
                .document(roomId)
                .update("active",isActive)
                .await()
            Result.success(Unit)

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun isRoomActive(roomId: String): Result<Boolean> {
        return try {
            fireStore.collection("Society")
                .document(roomId)
                .get()
                .await()
                .getBoolean("active")
                ?.let { Result.success(it) }
                ?: Result.failure(Exception("Room not found"))

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(roomId: String, message: SetChatMessage): Result<Unit> {

        return try {

            fireStore.collection("Society")
                .document(roomId)
                .collection("Messages")
                .add(message)
                .await()
            Result.success(Unit)

        }catch (e: Exception){
            Result.failure(e)
        }

    }

    override suspend fun listenForMessages(roomId: String): Flow<Result<List<GetChatMessage>>> {
        return callbackFlow {
            try {

                val listener = fireStore.collection("Society").document(roomId).collection("Messages")
                    .addSnapshotListener { value, error ->
                        if (error != null || value == null) return@addSnapshotListener
                        launch {
                            val messages = value.documents.mapNotNull { doc ->
                                doc.toObject(GetChatMessage::class.java)
                            }
                            trySend(Result.success(messages))
                        }
                    }

                awaitClose {
                    listener.remove()
                }

            }catch (e: Exception){
                trySend(Result.failure(e))
            }
        }
    }

    override suspend fun deleteMessageRoom(roomId: String): Result<Unit> {
        return try {

            fireStore.collection("Society")
                .document(roomId)
                .collection("Messages")
                .get()
                .await()
                .documents.forEach {
                    it.reference.delete()
                }
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getChatsCount(roomId: String): Flow<Result<Int>> {
        return callbackFlow {
            try {
                fireStore.collection("Society")
                    .document(roomId)
                    .collection("Messages")
                    .where(Filter.notEqualTo("senderId",auth.currentUser?.uid ?: ""))
                    .addSnapshotListener { value, error ->
                        if (error != null || value == null) return@addSnapshotListener
                        launch {
                            trySend(Result.success(value.documents.size))
                        }

                    }
            }catch (e: Exception){
                trySend(Result.failure(e))
            }
            awaitClose {
                close()
            }
        }
    }

    override suspend fun updateUserChatsCount(roomId: String,chatCount: Int): Result<Unit> {
        try {

            fireStore.collection("Society")
                .document(roomId)
                .collection("JoinRequests")
                .document(auth.currentUser?.uid ?: "")
                .update("chatsCount", chatCount)
                .await()
            return Result.success(Unit)

        }catch (e: Exception){
            return Result.failure(e)
        }
    }

    override suspend fun getUserChatCount(roomId: String,currentMessageCount: Int): Result<Int> {
        try {
            val data = fireStore.collection("Society")
                .document(roomId)
                .collection("JoinRequests")
                .document(auth.currentUser?.uid ?: "")
                .get()
                .await()

            val count = data.getLong("chatsCount")?.toInt() ?: 0


            return Result.success(currentMessageCount-count)
        }catch (e: Exception){
            return Result.failure(e)
        }
    }

    override suspend fun subscribeRoom(roomId: String): Result<Unit> {
        return try {
            fireStore.collection("Society")
                .document(roomId)
                .update(
                    "subscribers", FieldValue.arrayUnion(auth.currentUser?.uid) // add into array list
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unsubscribeRoom(roomId: String): Result<Unit> {
        return try {
            fireStore.collection("Society")
                .document(roomId)
                .update(
                    "subscribers", FieldValue.arrayRemove(auth.currentUser?.uid) // remove from array list
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun subscribers(roomId: String): Result<List<String>> {
        return try {
            val snapshot = fireStore.collection("Society")
                .document(roomId)
                .get()
                .await()

            val ids = snapshot.get("subscribers") as? List<*> ?: emptyList<Any>()

            val tokens = ids.mapNotNull { id ->
                id as? String
            }.mapNotNull { uid ->
                fireStore.collection("Users")
                    .document(uid)
                    .get()
                    .await()
                    .getString("token")
            }

            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun hasSubscribed(roomId: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val snapshot = fireStore.collection("Society")
                .document(roomId)
                .get()
                .await()

            val ids = snapshot.get("subscribers") as? List<*> ?: emptyList<Any>()
            Result.success(ids.contains(uid))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}

private fun resolveRoomPath(feedMode: FeedMode, campusId: String?): String {
    return if (feedMode == FeedMode.CAMPUS && campusId != null) {
        "CampusSociety"
    } else {
        "GlobalSociety"
    }
}
