package com.iota.campusX.Feature.UserProfile.data

import SendPushNotification
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.iota.campusX.Feature.Post.domain.Models.User
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepo
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.UiState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.IOException

class UserProfileImpl(
    private val sendPushNotification: SendPushNotification,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val httpClient: HttpClient
) : UserProfileRepo {


    override suspend fun getBaseProfile(): Result<BasicProfileDTO> {
        return try {
            val snapshot = firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                .get()
                .await()

            val profile = snapshot.toObject(BasicProfileDTO::class.java)

            if (profile != null) Result.success(profile)
            else Result.failure(Exception("Profile not found"))

            } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfileById(userId: String): Result<BasicProfileDTO> {
        if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))

        return try {
            coroutineScope {
                val userDeferred = async {
                    firestore.collection("Users")
                        .document(userId)
                        .get()
                        .await()
                        .toObject(BasicProfileDTO::class.java)
                }

                val userData = userDeferred.await()

                if (userData == null) {
                    Result.failure<BasicProfileDTO>(Exception("User not found"))
                } else {

                    Result.success(userData)

                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Boolean> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserName(userName: String): Result<Boolean> {
        return try {
            firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                    .update("userName", userName)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSocialAccounts(accounts: String): Result<Boolean> {
        return try {
            firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                .update("socialAccounts", accounts)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAbout(about: String): Result<Boolean> {
        return try {
            firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                    .update("userBio", about)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGender(gender: Gender): Result<Boolean> {
        return try {
            firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                    .update("userGender", gender)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
            }
        }

    override suspend fun updateInterests(interests: List<String>): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        return try {
            firestore.collection("Users").document(userId).update("interests", interests).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun updateProfileImage(imageUri: Uri): Result<Boolean> {

        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val uploadResult = suspendCancellableCoroutine<Result<Boolean>> { cont ->
                MediaManager.get().upload(imageUri)
                    .option("resource_type", "image")
                    .option("folder", "user_images")
                    .callback(object : UploadCallback {
                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            val imageUrl = resultData?.get("secure_url")?.toString()
                            if (imageUrl != null) {
                                firestore.collection("Users").document(userId)
                                    .update("userImage", imageUrl)
                                    .addOnSuccessListener { cont.resume(Result.success(true)) {} }
                                    .addOnFailureListener { cont.resume(Result.failure(it)) {} }
                            } else {
                                cont.resume(Result.failure(Exception("Image URL is null"))) {}
                            }
                        }

                        override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                            cont.resume(Result.failure(Exception(error?.description))) {}
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                        override fun onStart(requestId: String?) {}
                        override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {}
                    }).dispatch()
            }
            uploadResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCampus(campus: Campus): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        return try {
            firestore.collection("Users").document(userId).update("campus", campus).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendLinkUpRequest(requestUserId: String, currentState: Boolean?): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        if (userId == requestUserId) return Result.failure(Exception("You cannot send a request to yourself"))
        if (requestUserId.isEmpty()) return Result.failure(Exception("Invalid user ID"))

        return try {

            val senderRef = firestore.collection("Users")
                .document(userId)
                .collection("Connections")
                .document(requestUserId)

            val receiverRef = firestore.collection("Users")
                .document(requestUserId)
                .collection("Connections")
                .document(userId)

            val serverTimestamp = FieldValue.serverTimestamp()

            val senderData = mapOf(
                "receiverId" to requestUserId,
                "status" to false,
                "createdAt" to serverTimestamp
            )

            val receiverData = mapOf(
                "senderId" to userId,
                "status" to false,
                "createdAt" to serverTimestamp
            )

            if (currentState == null) {
                firestore.runBatch { batch ->
                    batch.set(senderRef, senderData)
                    batch.set(receiverRef, receiverData)
                }.await()
                sendPushNotification.messageNotification(requestUserId, "REQUEST")
            } else {
                firestore.runBatch {
                    batch ->
                    batch.delete(senderRef)
                    batch.delete(receiverRef)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptLinkUpRequest(requestUserId: String): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        return try {

            val senderRef = firestore.collection("Users")
                .document(userId)
                .collection("Connections")
                .document(requestUserId)

            val receiverRef = firestore.collection("Users")
                .document(requestUserId)
                .collection("Connections")
                .document(userId)
            firestore.runBatch {
                batch ->
                batch.update(senderRef, "status", true)
                batch.update(receiverRef, "status", true)
            }.await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectLinkUpRequest(requestUserId: String): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        return try {

            val senderRef = firestore.collection("Users")
                .document(userId)
                .collection("Connections")
                .document(requestUserId)

            val receiverRef = firestore.collection("Users")
                .document(requestUserId)
                .collection("Connections")
                .document(userId)
            firestore.runBatch {
                batch ->
                batch.delete(senderRef)
                batch.delete(receiverRef)
            }.await()

            Result.success(true)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConnectionsCount(userId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection("Users").document(auth.currentUser!!.uid)
                .collection("Connections")
                .whereEqualTo("status", true)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            Result.success(snapshot.count.toInt())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConnections(userId: String): Result<List<ConnectionsDTO>> {
        return try {
            val snapshot = firestore.collection("Users").document(userId)
                .collection("Connections")
                .whereEqualTo("status", true)

                .get().await()

            val connections = snapshot.documents.mapNotNull { doc ->
                val request = doc.toObject(LinkUpRequestDTO::class.java)
                request?.let {
                    val userSnapshot = firestore.collection("Users").document(it.senderId).get().await()
                    val userData = userSnapshot.toObject(BasicProfileDTO::class.java)
                    userData?.let { data ->
                        ConnectionsDTO(
                            user = User(
                                id = data.id,
                                userName = data.userName,
                                userImage = data.userImage
                            )
                        )
                    }
                }
            }
            Result.success(connections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasConnection(userId: String): Result<Boolean?> {
        return try {
           val deferred =  firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                .collection("Connections")
                .document(userId)
                .get()
                .await()

            if (deferred.exists()){
                val isConnected = deferred.getBoolean("status")
                Result.success(isConnected)
            }
            else{
                Result.success(null)
            }

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override fun updateUniversity(title: String): Flow<UiState<List<UniversityDTO>>> =
        flow {
        emit(UiState.Loading)

        try {
            val response: HttpResponse = httpClient.get(
                "https://autocomplete.clearbit.com/v1/companies/suggest?query=$title"
            ) {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
            }

            if (response.status.isSuccess()) {
                val universities = response.body<List<UniversityDTO>>()
                emit(UiState.Success(universities))
            } else {
                val errorBody = response.bodyAsText()
                emit(UiState.Error("HTTP ${response.status.value}: $errorBody"))
            }

        } catch (e: Exception) {
            emit(UiState.Error("Exception: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

}