package com.iota.campusX.Feature.Auth.domain.repository

import android.content.Context
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.iota.campusX.Feature.Auth.domain.model.AuthState
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    
    suspend fun getGoogleCredential(context: Context): GoogleIdTokenCredential?
    suspend fun authenticateWithFirebase(credential: GoogleIdTokenCredential): Result<String>
    suspend fun verifyUserToken(firebaseIdToken: String): Result<UserProfileEntity>
    suspend fun checkInitialAuthState(): AuthState
    suspend fun signOut()
}
