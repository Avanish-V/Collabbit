package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity

interface VerifyUserRepository {

    suspend fun verifyUser(userToken: String): Result<UserProfileEntity>

}