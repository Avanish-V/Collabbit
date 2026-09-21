package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.UserProfile.data.local.dao.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.local.mapper.toEntity
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class VerifyUserRepoImpl(
    private val firebaseAuth: FirebaseAuth,
    private val httpClint: HttpClient,
    private val userProfileDao: UserProfileDao
) : VerifyUserRepository {

    override suspend fun verifyUser(userToken: String): Result<ProfileResponse> {
        return try {
            val response = httpClint.get("users/me") {
                header("Authorization", "Bearer $userToken")
            }

            Log.d("VerifyUserRepoImpl", "Status: ${response.status}")

            Log.d("VerifyUserRepoImpl", "Sending token: $userToken")

            when (response.status.value) {
                200 -> {
                    val profileDTO = response.body<ProfileResponse>()
                    userProfileDao.insertProfile(profileDTO.toEntity())
                    Log.d("VerifyUserRepoImpl", "ProfileDTO: $profileDTO")
                    Result.success(profileDTO)
                }
                401, 403 -> {
                    Log.d("VerifyUserRepoImpl", "Status: ${response.status}")
                    // Invalid/expired token
                    firebaseAuth.signOut()
                    Result.failure(Exception("Unauthorized: ${response.status}"))
                }
                else -> {
                    // Other server errors
                    Log.d("AuthFlow", "VerifyUserRepoImpl: Calling signOut due to server error ${response.status}")
                    firebaseAuth.signOut()
                    Result.failure(Exception("Server error: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            // Network failure, timeout, etc.
            Log.e("VerifyUserRepoImpl", "Network error caught in repo: ${e.localizedMessage}", e)
            Log.d("AuthFlow", "VerifyUserRepoImpl: Calling signOut due to error")
            firebaseAuth.signOut()
            Result.failure(e)
        }
    }


}
