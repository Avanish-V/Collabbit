package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse

interface VerifyUserRepository {

    suspend fun verifyUser(userToken: String): Result<ProfileResponse>

}