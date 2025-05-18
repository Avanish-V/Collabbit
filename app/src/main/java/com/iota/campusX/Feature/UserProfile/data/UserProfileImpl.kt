package com.iota.campusX.Feature.UserProfile.data

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationDTO
import com.iota.campusX.Feature.Post.domain.UploadResponse
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepo
import com.iota.campusX.Utils.ResultState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException

class UserProfileImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val httpClient: HttpClient
) : UserProfileRepo {

    override suspend fun getBaseProfile(): Flow<ResultState<UserBasicProfileDTO>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {

                firestore.collection("Users").document(auth.currentUser?.uid ?: "").get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            val profileData = it.toObject(UserBasicProfileDTO::class.java)
                            if (profileData != null)
                                trySend(ResultState.Success(profileData))
                        }

                    }
                    .addOnFailureListener {
                        trySend(ResultState.Error(it.message.toString()))
                    }

            } catch (e: IOException) {
                trySend(ResultState.Error("Network error: ${e.message}"))
            } catch (e: Exception) {
                trySend(ResultState.Error("An error occurred: ${e.message}"))
            } finally {
                awaitClose { Log.d("getBaseProfile", "Flow closed") }
            }

        }
    }

    override suspend fun getUserProfileById(userId: String): Flow<ResultState<UserBasicProfileDTO>> {
        return callbackFlow {
            trySend(ResultState.Loading)

            try {

                val result = coroutineScope {

                    async {

                        val userDeferred = async {
                            firestore.collection("Users")
                                .document(userId)
                                .get()
                                .await()
                                .toObject(UserBasicProfileDTO::class.java)
                        }

                        val connRequestDeferred = async {
                            firestore.collection("Users")
                                .document(userId)
                                .collection("LinkUpRequests")
                                .document(auth.currentUser!!.uid)
                                .get()
                                .await()

                        }

                        val userData = userDeferred.await()
                        val connRequest = connRequestDeferred.await()

                        val isConnected = if (connRequest.exists()) {
                            connRequest.getBoolean("status") ?: false
                        } else {
                            null
                        }

                        UserBasicProfileDTO(

                            _id = userData?._id ?: "",
                            userName = userData?.userName ?: "",
                            userImage = userData?.userImage ?: "",
                            userEmail = userData?.userEmail ?: "",
                            userBio = userData?.userBio ?: "",
                            userGender = userData?.userGender ?: "",
                            social = userData?.social ?: "",
                            metaData = userData?.metaData ?: MetaData(),
                            campus = userData?.campus,
                            isRequestSent = isConnected
                        )


                    }

                }

                trySend(ResultState.Success(result.await()))

            } catch (e: IOException) {
                trySend(ResultState.Error("Network error: ${e.message}"))
            } catch (e: Exception) {
                trySend(ResultState.Error("An error occurred: ${e.message}"))
            } finally {
                awaitClose { Log.d("getBaseProfile", "Flow closed") }
            }

        }
    }

    override fun deleteAccount(): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            awaitClose()


        }
    }

    override fun updateSocialAccounts(accounts: String): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)


            awaitClose()

        }
    }

    override fun updateUserName(userName: String): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {

                firestore.collection("Users").document(auth.currentUser!!.uid)
                    .update("userName", userName)
                    .addOnSuccessListener {
                        trySend(ResultState.Success(true))
                    }
                    .addOnFailureListener {
                        trySend(ResultState.Error(it.message.toString()))
                    }

            } catch (e: IOException) {
                trySend(ResultState.Error("Check Your Network"))
            }

            awaitClose()

        }
    }

    override fun updateAbout(about: String): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {
                firestore.collection("Users").document(auth.currentUser!!.uid)
                    .update("userBio", about)
                    .addOnSuccessListener {
                        trySend(ResultState.Success(true))
                    }
                    .addOnFailureListener {
                        trySend(ResultState.Error(it.message.toString()))
                    }

            } catch (e: IOException) {
                trySend(ResultState.Error("Check Your Network"))
            }


            awaitClose()
        }
    }

    override fun updateGender(gender: String): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {
                firestore.collection("Users").document(auth.currentUser!!.uid)
                    .update("userGender", gender)
                    .addOnSuccessListener {
                        trySend(ResultState.Success(true))
                    }
                    .addOnFailureListener {
                        trySend(ResultState.Error(it.message.toString()))
                    }
            } catch (e: IOException) {

                trySend(ResultState.Error("Check Your Network"))

            }

            awaitClose()

        }
    }

    override fun updateInterests(interests: List<String>): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)


            awaitClose()


        }
    }

    override fun updateProfileImage(imageUri: Uri): Flow<ResultState<Boolean>> {

        return callbackFlow {

            trySend(ResultState.Loading)

            try {

                MediaManager.get().upload(imageUri)
                    .option("resource_type", "image")
                    .option("folder", "user_images")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {
                            // Optional: handle start
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                            val progress = ((bytes.toFloat() / totalBytes.toFloat()) * 100).toInt()
                            // Optional: update UI with progress
                        }

                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            val imageUrl = resultData?.get("secure_url")?.toString()

                            firestore.collection("Users").document(auth.currentUser!!.uid)
                                .update("userImage", imageUrl)
                                .addOnSuccessListener {
                                    trySend(ResultState.Success(true))
                                }
                                .addOnFailureListener {
                                    trySend(ResultState.Error(it.message.toString()))
                                }
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            trySend(ResultState.Error(error?.description ?: "Image upload failed"))
                            close()
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            // Optional: handle retry if needed
                        }
                    })
                    .dispatch()

            } catch (e: Exception) {

                trySend(ResultState.Error(e.message.toString()))

            }
            awaitClose()


        }
    }

    override fun updateUniversity(title: String): Flow<ResultState<List<UniversityDTO>>> =
        callbackFlow {
            trySend(ResultState.Loading)

            try {
                val response: HttpResponse =
                    httpClient.get("https://autocomplete.clearbit.com/v1/companies/suggest?query=$title") {
                        headers {
                            //append(HttpHeaders.Authorization, "Bearer sk_fDa5SoqiTjiOrJrhKDo-tA")
                            append(HttpHeaders.Accept, "application/json")
                        }
                    }

                if (response.status.isSuccess()) {
                    val universities = response.body<List<UniversityDTO>>()
                    trySend(ResultState.Success(universities))
                } else {
                    val errorBody = response.bodyAsText()
                    trySend(ResultState.Error("HTTP ${response.status.value}: $errorBody"))
                }

            } catch (e: Exception) {
                trySend(ResultState.Error("Exception: ${e.localizedMessage ?: "Unknown error"}"))
            }

            awaitClose { /* Optionally handle closing if needed */ }
        }

    override fun updateCampus(campus: Campus): Flow<ResultState<Boolean>> = callbackFlow {

        trySend(ResultState.Loading)

        try {

            firestore.collection("Users").document(auth.currentUser!!.uid)
                .update("campus", campus)
                .addOnSuccessListener {
                    trySend(ResultState.Success(true))
                }
                .addOnFailureListener {
                    trySend(ResultState.Error(it.message.toString()))
                }

        } catch (e: IOException) {
            trySend(ResultState.Error("Check Your Network"))
        }

        awaitClose()

    }

    override fun sendLinkUpRequest(
        requestUserId: String,
        currentState: Boolean?
    ): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {

                if (currentState == null) {

                    firestore.collection("Users").document(requestUserId)
                        .collection("LinkUpRequests")
                        .document(auth.currentUser!!.uid)
                        .set(
                            mapOf(
                                "senderId" to auth.currentUser!!.uid,
                                "status" to false,
                                "createdAt" to System.currentTimeMillis(),
                            )
                        )
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                        }
                        .addOnFailureListener {
                            trySend(ResultState.Error(it.message.toString()))

                        }

                } else

                    firestore.collection("Users").document(requestUserId)
                        .collection("LinkUpRequests")
                        .document(auth.currentUser!!.uid)
                        .delete()
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                        }
                        .addOnFailureListener {
                            trySend(ResultState.Error(it.message.toString()))

                        }


            } catch (e: Exception) {
                trySend(ResultState.Error(e.message.toString()))
            }
            awaitClose()

        }
    }

    override fun acceptLinkUpRequest(requestUserId: String): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {


                firestore.collection("Users").document(auth.currentUser!!.uid)
                    .collection("LinkUpRequests")
                    .document(requestUserId)
                    .update("status", true)
                    .addOnSuccessListener {
                        trySend(ResultState.Success(true))
                    }
                    .addOnFailureListener {
                        trySend(ResultState.Error(it.message.toString()))

                    }

            } catch (e: Exception) {
                trySend(ResultState.Error(e.message.toString()))
            }
            awaitClose()

        }
    }

    override fun rejectLinkUpRequest(requestUserId: String): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {

                firestore.collection("Users").document(auth.currentUser!!.uid)
                    .collection("LinkUpRequests")
                    .document(requestUserId)
                    .delete()
                    .addOnSuccessListener {
                        trySend(ResultState.Success(true))
                    }
                    .addOnFailureListener {
                        trySend(ResultState.Error(it.message.toString()))

                    }

            } catch (e: Exception) {
                trySend(ResultState.Error(e.message.toString()))
            }
            awaitClose()

        }
    }


}