package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import android.content.Intent
import android.content.IntentSender
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.Flow

interface GoogleAuthRepo {

    suspend fun signIn():IntentSender?

    suspend fun signInWithIntent(intent:Intent):SignInResult

    fun getCurrentUser():Boolean

    fun verifyUser(userId:String,userToken: String): Flow<ResultState<UserResponse>>

}