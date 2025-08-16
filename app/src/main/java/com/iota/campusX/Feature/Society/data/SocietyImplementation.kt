package com.iota.campusX.Feature.Society.data

import android.util.Log
import com.google.api.client.util.Data.mapOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Society.AgoraTokenBuilder.generateDigitRandom
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.SetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.Status
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SocietyImplementation(private val fireStore: FirebaseFirestore,private val auth: FirebaseAuth):SocietyRepository{

    override suspend fun createSociety(createSocietyDTO: CreateSocietyDTO): Result<Unit> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))

        if (createSocietyDTO.mode == FeedMode.CAMPUS && createSocietyDTO.campusId == null) {
            return Result.failure(Exception("Campus not found!"))
        }

        val path = if (createSocietyDTO.mode == FeedMode.CAMPUS) {
            "CampusSociety"
        } else {
            "GlobalSociety"
        }

        return try {
            val roomId = UUID.randomUUID().toString()

            val data = mapOf(
                "societyName" to createSocietyDTO.societyName,
                "description" to createSocietyDTO.description,
                "createdBy" to currentUser.uid, // ✅ Safer
                "joined" to createSocietyDTO.joined,
                "mode" to createSocietyDTO.mode.name, // 🔁 Serialize enum properly
                "roomId" to roomId,
                "isActive" to false
            )

            fireStore.collection(path).document(roomId).set(data).await()

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

            val data = fireStore.collection(path).get().await()

            val societyList = data.documents.mapNotNull {doc->

                val society = doc.toObject(CreateSocietyDTO::class.java) ?: return@mapNotNull null

                val createdById = society.createdBy

                val createdBy = fireStore
                    .collection("Users")
                    .document(createdById)
                    .get()
                    .await()
                    .toObject(UserDetail::class.java)?: UserDetail()

                GetSocietyDTO(
                    societyName = society.societyName,
                    description = society.description,
                    createdBy = createdBy,
                    joined = society.joined,
                    mode = society.mode,
                    roomId = society.roomId
                )

            }

            Result.success(societyList)

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun updateRoom(roomId: String,isActive:Boolean,feedMode: FeedMode,campusId: String?): Result<Unit> {
        return try {

            val path = if (feedMode == FeedMode.CAMPUS && campusId != null){
                "CampusSociety"
            }else{
                "GlobalSociety"
            }

            val data = mapOf(
                "isActive" to isActive
            )
            fireStore.collection(path).document(roomId).update(data).await()
            Result.success(Unit)
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
                "microphone" to false,
                "speaking" to false,
                "raiseHand" to false
            )

            fireStore.collection(path).document(roomId)
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
            val path = if (feedMode == FeedMode.CAMPUS && campusId != null){
                "CampusSociety"
            }else{
                "GlobalSociety"
            }
            fireStore.collection(path).document(roomId)
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

    override suspend fun listenForApproval(roomId: String, feedMode: FeedMode, campusId: String?): Flow<List<GetJoinRequestDTO>> = callbackFlow {

        val path = resolveRoomPath(feedMode, campusId)

        val listener = fireStore.collection(path)
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

                            val user = userSnapshot.toObject(UserDetail::class.java) ?: return@mapNotNull null

                            GetJoinRequestDTO(
                                requestId = data.requestId,
                                userName = user.userName,
                                userImage = user.userImage,
                                role = data.role,
                                status = data.status,
                                speaking = data.speaking,
                                microphone = data.microphone,
                                raiseHand = data.raiseHand,
                                uid = data.uid
                            )

                        } catch (e: Exception) {
                            Log.d("JOINING_REQUESTS",e.message.toString())
                            null
                        }
                    }
                    Log.d("JOINING_REQUESTS",requests.toString())
                    trySend(requests).isSuccess
                }
            }


        awaitClose { listener.remove() }
    }

    override suspend fun stageUpParticipant(roomId: String, status: Status, requestId: String, feedMode: FeedMode, campusId: String?): Result<String> {
        return try {

            val path = resolveRoomPath(feedMode, campusId)

            fireStore.collection(path)
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

            val path = resolveRoomPath(feedMode, campusId)


            fireStore.collection(path)
                .document(roomId)
                .collection("JoinRequests")
                .document(auth.currentUser?.uid ?: "")
                .update("microphone", isMicrophone)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("Firestore", "Stage up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun isSpeaking(roomId: String, isSpeaking: Boolean, requestId: String, feedMode: FeedMode, campusId: String?): Result<Unit> {
        return try {

            val path = resolveRoomPath(feedMode, campusId)


            fireStore.collection(path)
                .document(roomId)
                .collection("JoinRequests")
                .document(requestId)
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

            fireStore.collection(path)
                .document(roomId)
                .collection("JoinRequests")
                .document(requestId)
                .update("raiseHand", isRaiseHand)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("Firestore", "Stage up failed", e)
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
