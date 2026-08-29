package com.iota.campusX.Feature.UserProfile.data.remote.repository


import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.UserProfile.data.local.dao.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.local.mapper.toDomain
import com.iota.campusX.Feature.UserProfile.data.local.mapper.toEntity
import com.iota.campusX.Feature.UserProfile.data.remote.Request.BasicDetailsRequest
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserProfileImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val httpClient: HttpClient,
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    private val userCache = mutableMapOf<String, ProfileResponse>()

    override fun observeProfile(): Flow<ProfileResponse> {
        return userProfileDao.observeProfile()
            .filterNotNull()
            .map { it.toDomain() }

    }

    override suspend fun syncUserProfile(): Result<ProfileResponse> {
        return try {
            val response = httpClient.get("users/me")

            if (response.status.value in 200..299){
                val profile = response.body<ProfileResponse>()
                userProfileDao.insertProfile(profile.toEntity())
                Result.success(profile)
            }
            else{
                Result.failure(Exception("Something went wrong!"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfileById(userId: String): Result<ProfileResponse>{

        userCache[userId]?.let { return Result.success(it) }

        return try {

            if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))

            val response = httpClient.get("users/$userId")

            if (response.status.value == 200){
                val profileDTO = response.body<ProfileResponse>()
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

    override suspend fun updateSkills(skills: List<SkillResponse>): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        return try {
            val response = httpClient.patch("users/me/skills") {
                setBody(skills)
                contentType(ContentType.Application.Json)
            }
            if (response.status.value in 200..299) {
                syncUserProfile()
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update skills: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSummary(summary: String): Result<Boolean> {
        try {
            val response = httpClient.patch("users/me/summary") {
                setBody(summary)
                contentType(ContentType.Application.Json)
            }
            if (response.status.value in 200..299) {
                syncUserProfile()
                return Result.success(true)
            } else {
                return Result.failure(Exception("Failed to update summary: ${response.status}"))
            }

        }catch (e: Exception){
            return Result.failure(e)
        }
    }

    override suspend fun updateCampus(campus: Education): Result<Boolean> {
        return try {
            val response = httpClient.patch("users/me/campus") {
                setBody(campus)
                contentType(ContentType.Application.Json)
            }

            if (response.status.value in 200..299) {
                syncUserProfile()
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update campus: ${response.status}"))
            }

        } catch (e: Exception) {
            Log.d("UpdateCampus", "updateCampus error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(basicDetailsRequest: BasicDetailsRequest): Result<Unit> {
        return try {

            val response = httpClient.patch("users/me/profile") {
                setBody(basicDetailsRequest)
                contentType(ContentType.Application.Json)
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

    override suspend fun updateFcmToken(token: String): Result<Unit> {


        return try {
            val response = httpClient.patch("users/me/fcm-token") {
                setBody(token)
                contentType(ContentType.Application.Json)
            }
            Log.d("UpdateFcmToken", "updateFcmToken: ${response.status}")
            if (response.status.value in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update FCM token: ${response.status}"))
            }
        }catch (e: Exception) {
            Result.failure(e)
        }

    }

}