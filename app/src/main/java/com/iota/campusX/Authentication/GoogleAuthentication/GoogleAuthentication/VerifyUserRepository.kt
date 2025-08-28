package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

interface VerifyUserRepository {

    suspend fun verifyUser(userId:String,userToken: String): Result<Unit>

}