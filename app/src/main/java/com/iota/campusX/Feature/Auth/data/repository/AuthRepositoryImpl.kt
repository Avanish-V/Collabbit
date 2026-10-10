package com.iota.campusX.Feature.Auth.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.iota.campusX.Feature.Auth.data.datasource.AuthCredentialDataSource
import com.iota.campusX.Feature.Auth.domain.model.AuthState
import com.iota.campusX.Feature.Auth.domain.repository.AuthRepository
import com.iota.campusX.Feature.Chats.data.local.ChatDatabase
import com.iota.campusX.Feature.Post.data.local.database.CampusDatabase
import com.iota.campusX.Feature.UserProfile.data.local.dao.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.local.database.AppDatabase
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
    private val chatDatabase: ChatDatabase,
    private val campusDatabase: CampusDatabase,
    private val appDatabase: AppDatabase
) : AuthRepository {

    private val userProfileDao: UserProfileDao = appDatabase.userProfileDao()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            _authState.value = AuthState.Unauthenticated
        }

        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    override suspend fun checkInitialAuthState(): AuthState {
        return withContext(Dispatchers.IO) {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                _authState.value = AuthState.Unauthenticated
                return@withContext AuthState.Unauthenticated
            }

            try {
                val tokenResult = firebaseUser.getIdToken(false).await()
                val token = tokenResult.token
                if (token == null) {
                    signOut()
                    return@withContext AuthState.Unauthenticated
                }

                // Attempt to reach backend to verify token validity and sync profile
                val response = try {
                    httpClient.get("users/me") {
                        header("Authorization", "Bearer $token")
                    }
                } catch (networkEx: Exception) {
                    Log.w("AuthRepositoryImpl", "Network check failed on launch; checking local cache", networkEx)
                    null
                }

                if (response != null) {
                    when (response.status.value) {
                        200 -> {
                            val profileDTO = response.body<ProfileResponse>()
                            userProfileDao.insertProfile(profileDTO.toEntity())
                            _authState.value = AuthState.Authenticated
                            AuthState.Authenticated
                        }
                        401, 403 -> {
                            Log.e("AuthRepositoryImpl", "Session revoked by backend: ${response.status}")
                            signOut()
                            AuthState.Unauthenticated
                        }
                        else -> {
                            // Backend 5xx or temporary issue, check if local cached profile exists
                            val cachedProfile = userProfileDao.getProfile()
                            if (cachedProfile != null) {
                                Log.w("AuthRepositoryImpl", "Backend error (${response.status}), falling back to cached profile")
                                _authState.value = AuthState.Authenticated
                                AuthState.Authenticated
                            } else {
                                signOut()
                                AuthState.Unauthenticated
                            }
                        }
                    }
                } else {
                    // Network was unreachable: do NOT sign out if Firebase user is logged in
                    Log.i("AuthRepositoryImpl", "Offline launch with active Firebase session - staying authenticated")
                    _authState.value = AuthState.Authenticated
                    AuthState.Authenticated
                }
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Error checking initial auth state", e)
                val cachedProfile = userProfileDao.getProfile()
                if (cachedProfile != null && firebaseAuth.currentUser != null) {
                    _authState.value = AuthState.Authenticated
                    AuthState.Authenticated
                } else {
                    signOut()
                    AuthState.Unauthenticated
                }
            }
        }
    }

    override suspend fun signInWithGoogle(context: Context): Result<Unit> {
        return withContext(Dispatchers.IO) {
            _authState.value = AuthState.Authenticating
            try {
                // 1. Get Google Credentials from CredentialManager
                val credential = dataSource.signIn(context)
                    ?: run {
                        _authState.value = AuthState.Unauthenticated
                        return@withContext Result.failure(Exception("Google Sign-In was cancelled or failed."))
                    }

                // 2. Authenticate with Firebase
                val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user
                    ?: run {
                        signOut()
                        return@withContext Result.failure(Exception("Firebase user is null after sign-in"))
                    }

                // 3. Obtain Firebase ID Token
                val firebaseTokenResult = firebaseUser.getIdToken(true).await()
                val firebaseIdToken = firebaseTokenResult.token
                    ?: run {
                        signOut()
                        return@withContext Result.failure(Exception("Failed to retrieve auth token"))
                    }

                // 4. Verify & sync with backend
                _authState.value = AuthState.Verifying
                val response = httpClient.get("users/me") {
                    header("Authorization", "Bearer $firebaseIdToken")
                }

                when (response.status.value) {
                    200 -> {
                        val profileDTO = response.body<ProfileResponse>()
                        userProfileDao.insertProfile(profileDTO.toEntity())
                        _authState.value = AuthState.Authenticated
                        Result.success(Unit)
                    }
                    401, 403 -> {
                        signOut()
                        Result.failure(Exception("Unauthorized: ${response.status}"))
                    }
                    else -> {
                        signOut()
                        Result.failure(Exception("Server verification failed with status: ${response.status}"))
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Sign-in failed", e)
                signOut()
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
                Result.failure(e)
            }
        }
    }

    override suspend fun signOut() {
        withContext(Dispatchers.IO) {
            try {
                firebaseAuth.signOut()
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Error signing out from Firebase", e)
            }
            try {
                dataSource.signOut()
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Error signing out from CredentialManager", e)
            }

            try {
                chatDatabase.clearAllTables()
                campusDatabase.clearAllTables()
                appDatabase.clearAllTables()
                Log.d("AuthRepositoryImpl", "All databases cleared on sign-out")
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Failed to clear databases on sign-out", e)
            }
            _authState.value = AuthState.Unauthenticated
        }
    }
}
