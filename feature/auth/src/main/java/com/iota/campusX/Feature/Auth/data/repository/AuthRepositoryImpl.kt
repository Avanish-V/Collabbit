package com.iota.campusX.Feature.Auth.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.iota.campusX.Feature.Auth.data.datasource.AuthCredentialDataSource
import com.iota.campusX.Feature.Auth.domain.model.AuthState
import com.iota.campusX.Feature.Auth.domain.repository.AuthRepository
import com.iota.campusX.Feature.UserProfile.data.local.dao.UserProfileDao
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
    private val onSignInSuccess: suspend () -> Unit = {},
    private val onSignOut: suspend () -> Unit = {}
) : AuthRepository {

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
                val idToken = tokenResult.token
                if (idToken == null) {
                    signOut()
                    return@withContext AuthState.Unauthenticated
                }

                val response = httpClient.get("users/me") {
                    header("Authorization", "Bearer $idToken")
                }

                if (response.status.value == 200) {
                    val profileDTO = response.body<ProfileResponse>()
                    userProfileDao.insertProfile(profileDTO.toEntity())
                    _authState.value = AuthState.Authenticated
                    AuthState.Authenticated
                } else {
                    signOut()
                    AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Failed to check initial auth state", e)
                signOut()
                AuthState.Unauthenticated
            }
        }
    }

    override suspend fun signInWithGoogle(context: Context): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                _authState.value = AuthState.Authenticating
                val googleIdTokenCredential = dataSource.signIn(context)
                    ?: run {
                        signOut()
                        return@withContext Result.failure(Exception("Google Sign-In failed or cancelled"))
                    }

                val idToken = googleIdTokenCredential.idToken
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                    ?: run {
                        signOut()
                        return@withContext Result.failure(Exception("Firebase user is null after sign-in"))
                    }

                val firebaseTokenResult = firebaseUser.getIdToken(true).await()
                val firebaseIdToken = firebaseTokenResult.token
                    ?: run {
                        signOut()
                        return@withContext Result.failure(Exception("Failed to retrieve auth token"))
                    }

                _authState.value = AuthState.Verifying
                val response = httpClient.get("users/me") {
                    header("Authorization", "Bearer $firebaseIdToken")
                }

                when (response.status.value) {
                    200 -> {
                        val profileDTO = response.body<ProfileResponse>()
                        userProfileDao.insertProfile(profileDTO.toEntity())
                        _authState.value = AuthState.Authenticated
                        try {
                            onSignInSuccess()
                        } catch (e: Exception) {
                            Log.e("AuthRepositoryImpl", "onSignInSuccess callback failed", e)
                        }
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
                onSignOut()
                Log.d("AuthRepositoryImpl", "onSignOut callback executed")
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Error executing onSignOut callback", e)
            }

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

            _authState.value = AuthState.Unauthenticated
        }
    }
}
