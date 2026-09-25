package com.iota.campusX.Feature.Auth.data.repository

import android.content.Context
import android.util.Log
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.iota.campusX.Feature.Auth.data.datasource.AuthCredentialDataSource
import com.iota.campusX.Feature.Auth.domain.repository.AuthRepository
import com.iota.campusX.Feature.Chats.data.local.ChatDatabase
import com.iota.campusX.Feature.Post.data.local.database.CampusDatabase
import com.iota.campusX.Feature.UserProfile.data.local.dao.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.local.database.AppDatabase
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.local.mapper.toEntity
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val dataSource: AuthCredentialDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val httpClient: HttpClient,
    private val userProfileDao: UserProfileDao,
    private val chatDatabase: ChatDatabase,
    private val campusDatabase: CampusDatabase,
    private val appDatabase: AppDatabase
) : AuthRepository {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(false)
    override val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            _isLoggedIn.value = false
        }

        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                _isLoggedIn.value = false
            }
        }
    }

    override suspend fun checkInitialAuthState(): Boolean {
        return withContext(Dispatchers.IO) {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val profile = userProfileDao.getProfile()
                val verified = profile != null
                _isLoggedIn.value = verified
                verified
            } else {
                _isLoggedIn.value = false
                false
            }
        }
    }

    override suspend fun getGoogleCredential(context: Context): GoogleIdTokenCredential? {
        return dataSource.signIn(context)
    }

    override suspend fun authenticateWithFirebase(credential: GoogleIdTokenCredential): Result<String> {
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Firebase user is null after sign-in"))

            val firebaseTokenResult = firebaseUser.getIdToken(true).await()
            val firebaseIdToken = firebaseTokenResult.token
                ?: return Result.failure(Exception("Failed to retrieve auth token"))

            Result.success(firebaseIdToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyUserToken(firebaseIdToken: String): Result<UserProfileEntity> {
        return try {
            val response = httpClient.get("users/me") {
                header("Authorization", "Bearer $firebaseIdToken")
            }

            when (response.status.value) {
                200 -> {
                    val profileDTO = response.body<ProfileResponse>()
                    val entity = profileDTO.toEntity()
                    userProfileDao.insertProfile(entity)
                    _isLoggedIn.value = true
                    Result.success(entity)
                }
                401, 403 -> {
                    firebaseAuth.signOut()
                    _isLoggedIn.value = false
                    Result.failure(Exception("Unauthorized: ${response.status}"))
                }
                else -> {
                    firebaseAuth.signOut()
                    _isLoggedIn.value = false
                    Result.failure(Exception("Server error: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            firebaseAuth.signOut()
            _isLoggedIn.value = false
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        dataSource.signOut()

        withContext(Dispatchers.IO) {
            try {
                chatDatabase.clearAllTables()
                campusDatabase.clearAllTables()
                appDatabase.clearAllTables()
                Log.d("AuthRepositoryImpl", "All databases cleared on sign-out")
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Failed to clear databases on sign-out", e)
            }
        }
        _isLoggedIn.value = false
    }
}
