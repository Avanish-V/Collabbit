package com.iota.campusX.Feature.UserProfile.data

import SendPushNotification
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import com.iota.campusX.Feature.Notification.data.CreateNotification
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.UserProfile.OfflineSupport.UserProfileDao
import com.iota.campusX.Feature.UserProfile.domain.UserProfileInterface
import com.iota.campusX.Utils.UiState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import toDomain
import toEntity

class UserProfileImpl(
    private val sendPushNotification: SendPushNotification,
    private val notificationRepository: NotificationRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val httpClient: HttpClient,
    private val userProfileDao: UserProfileDao
) : UserProfileInterface {

    override suspend fun getUserProfile(): Flow<BaseProfileDTO?> {
        val uid = auth.currentUser?.uid ?: return flowOf(null) // no user logged in, emit null instead of crashing
        return userProfileDao.getProfile(uid).map { it?.toDomain() }
    }


    override suspend fun syncUserProfile(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(IllegalStateException("User not logged in"))

            // Fetch user profile snapshot
            val snapshot = firestore.collection("Users")
                .document(user.uid)
                .get()
                .await()

            val remote = snapshot.toObject(BaseProfileDTO::class.java)
                ?: return Result.failure(IllegalStateException("User profile not found"))

            coroutineScope {
                val connectionCount = async {
                    firestore.collection("Users")
                        .document(user.uid)
                        .collection("Connections")
                        .whereEqualTo("status", true)
                        .count()
                        .get(AggregateSource.SERVER)
                        .await()
                        .count.toInt()
                }

                val postCount = async {
                    firestore.collection("Posts")
                        .whereEqualTo("creatorId", user.uid)
                        .count()
                        .get(AggregateSource.SERVER)
                        .await()
                        .count.toInt()
                }

                val followersCount = async {
                    firestore.collection("Users")
                        .document(user.uid)
                        .collection("Followers")
                        .count()
                        .get(AggregateSource.SERVER)
                        .await()
                        .count.toInt()
                }

                val profileWithCounts = remote.copy(
                    count = Counts(
                        connections = connectionCount.await(),
                        posts = postCount.await(),
                        followers = followersCount.await()
                    )
                )

                // Save to local DB
                userProfileDao.insertProfile(profileWithCounts.toEntity())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getBaseProfile(): Result<BaseProfileDTO> {
        return try {


            val snapshot = firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                .get()
                .await()

            val connectionCount = firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                .collection("Connections")
                .whereEqualTo("status",true)
                .count()
                .get(AggregateSource.SERVER)
                .await()

            val postCount = firestore.collection("Posts")
                .whereEqualTo("creatorId", auth.currentUser!!.uid)
                .count()
                .get(AggregateSource.SERVER)
                .await()

            val replyCount = firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                .collection("Followers")
                .count()
                .get(AggregateSource.SERVER)
                .await()

            val profile = snapshot.toObject(BaseProfileDTO::class.java)
                ?.copy(
                    count = Counts(
                        connections = connectionCount.count.toInt(),
                        posts = postCount.count.toInt(),
                        followers = replyCount.count.toInt()
                    )
                )

            if (profile != null) Result.success(profile)
            else Result.failure(Exception("Profile not found"))

            } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfileById(userId: String): Result<BaseProfileDTO> {

        if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))

        return try {
            coroutineScope {
                val userDeferred = async {
                    firestore.collection("Users")
                        .document(userId)
                        .get()
                        .await()
                        .toObject(BaseProfileDTO::class.java)
                }

                val connectionsDeferred = async {
                    firestore.collection("Users")
                        .document(userId)
                        .collection("Connections")
                        .whereEqualTo("status",true)
                        .count()
                        .get(AggregateSource.SERVER)
                        .await()
                }

                val postsDeferred = async {
                    firestore.collection("Posts")
                        .whereEqualTo("creatorId", userId)
                        .count()
                        .get(AggregateSource.SERVER)
                        .await()
                }

                val followersDeferred = async {
                    firestore.collection("Users")
                        .document(userId)
                        .collection("Followers")
                        .count()
                        .get(AggregateSource.SERVER)
                        .await()
                }

                val user = userDeferred.await()
                if (user == null) {
                    Result.failure<BaseProfileDTO>(Exception("User not found"))
                } else {
                    val counts = Counts(
                        connections = connectionsDeferred.await().count.toInt(),
                        posts = postsDeferred.await().count.toInt(),
                        followers = followersDeferred.await().count.toInt()
                    )
                    Result.success(user.copy(count = counts))
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
                "senderId" to requestUserId,
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

                if (requestUserId != auth.currentUser!!.uid) {

                    notificationRepository.createNotification(
                        createNotification = CreateNotification.ConnectionRequestNotification(
                            notificationId = userId,
                            type = NotificationType.CONNECTION_REQUEST,
                            createdAt = serverTimestamp,
                            read = false,
                            actionBy = userId
                        ),
                        creatorId = requestUserId

                    )

                    sendPushNotification.messageNotification(
                        notificationReceiverId = requestUserId,
                        notificationType = "REQUEST"
                    )
                }
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
                batch.set(senderRef, mapOf("status" to true), SetOptions.merge())
                batch.set(receiverRef, mapOf("status" to true), SetOptions.merge())
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
            val snapshot = firestore.collection("Users").document(userId)
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

                val senderId = doc.getString("senderId")

                senderId?.let {
                    val userSnapshot = firestore
                        .collection("Users")
                        .document(senderId)
                        .get()
                        .await()

                    val userData = userSnapshot.toObject(BaseProfileDTO::class.java)

                    userData?.let { data ->
                        ConnectionsDTO(
                            userName = data.userName,
                            id = userData.id,
                            userImage = data.userImage,
                            isCurrentProfile = userData.id == auth.currentUser!!.uid,
                            isCurrentUser = userId == auth.currentUser!!.uid
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

    override fun updateUniversity(title: String): Flow<UiState<List<UniversityDTO>>> = flow {

        emit(UiState.Loading)

        try {

            val response: HttpResponse = httpClient.get(
                "https://autocomplete.clearbit.com/v1/companies/suggest?query=$title"
            ) {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
            }

            val bodyText = response.bodyAsText()

            val universities = Json.decodeFromString<List<UniversityDTO>>(bodyText)

            emit(UiState.Success(universities))

        } catch (e: Exception) {

            emit(UiState.Error("Parsing error: ${e.localizedMessage}"))

        }

    }

}