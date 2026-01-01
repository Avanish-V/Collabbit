package com.iota.campusX.Feature.UserProfile.data.repository

import SendPushNotification
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.UserProfile.data.local.database.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Campus
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Counts
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UpdateProfileDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UserProfileResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Koin.END_POINT
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import toDomain

class UserProfileImpl(
    private val sendPushNotification: SendPushNotification,
    private val notificationRepositoryProvider: () -> NotificationRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val httpClient: HttpClient,
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    private val userCache = mutableMapOf<String, BaseProfileDTO>()

    override  fun getUserProfile(): Flow<BaseProfileDTO?> {
        val uid = auth.currentUser?.uid ?: return flowOf(null) // no user logged in, emit null instead of crashing
        return userProfileDao.getProfile(uid).map { it?.toDomain() }
    }

    override suspend fun syncUserProfile(): Result<Unit> {
        return try {

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            val response = httpClient.get("http://192.168.29.180:8080/users/me") {
                header("Authorization", "Bearer $token")
            }

            Log.d("SyncUserProfile", "Status: ${response.status}")
            Log.d("SyncUserProfile", "Status: ${response.bodyAsText()}")

            if (response.status.value == 200){
                val profileDTO = Gson().fromJson(response.bodyAsText(), UserProfileEntity::class.java)
                userProfileDao.insertProfile(profileDTO)
                Result.success(Unit)
            }
            else{
                Result.failure(Exception("Something went wrong!"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBaseProfile(): Result<BaseProfileDTO> {
        return try {

            Result.success(BaseProfileDTO())

            } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfileById(userId: String): Result<BaseProfileDTO>{

        userCache[userId]?.let { return Result.success(it) }

        return try {
            if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            val response = httpClient.get("$END_POINT/users/$userId") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.value == 200){
                val profileDTO = Gson().fromJson(response.bodyAsText(), BaseProfileDTO::class.java)
                profileDTO.let { userCache[userId] = it }
                Result.success(profileDTO)
            }
            else{
                Result.failure(Exception("Something went wrong!"))
            }
        }catch (e: Exception){

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

    override suspend fun updateInterests(interests: List<String>): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        return try {
            firestore.collection("Users").document(userId).update("interests", interests).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCampus(campus: Campus): Result<Boolean> {

        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            val body = Json.encodeToString(campus)

            Log.d("UpdateCampus", "updateCampus: $body")

            val response = httpClient.patch("http://192.168.29.180:8080/users/me/campus") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(body)
            }

            Log.d("UpdateCampus", "Status: ${response.status}")
            Log.d("UpdateCampus", "Status: ${response.bodyAsText()}")

            if (response.status.value in 200..299){
                return Result.success(true)
            }else{
                return Result.failure(Exception("Failed to update campus"))
            }

        } catch (e: Exception) {
            Log.d("UpdateCampus", "updateCampus: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(updateProfileDTO: UpdateProfileDTO): Result<Unit> {
        return try {

            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                return Result.failure(Exception("User not signed in"))
            }

            val tokenResult = currentUser.getIdToken(true).await()

            val token = tokenResult.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            Log.d("UpdateProfile", "updateProfile: $updateProfileDTO")

            val data = Json.encodeToString(updateProfileDTO)

            val response = httpClient.patch("$END_POINT/users/me/profile") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(data) // Ktor + ContentNegotiation can serialize automatically
            }

            if (response.status.value in 200..299){
                Result.success(Unit)
            }
            else{
                Result.failure(Exception("Something went wrong!"))
            }

        } catch (e: Exception) {
            Log.d("UpdateProfile", "updateProfile: ${e.message}")
            Result.failure(e)
        }
    }

}