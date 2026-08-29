package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.iota.campusX.Feature.Chats.data.local.ChatDatabase
import com.iota.campusX.Feature.Post.data.local.database.CampusDatabase
import com.iota.campusX.Feature.UserProfile.data.local.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class GoogleSignInViewModel(
    private val dataSource: CredentialAuthDataSource,
    private val verifyUserRepository: VerifyUserRepository,
    private val chatDatabase: ChatDatabase,
    private val campusDatabase: CampusDatabase,
    private val appDatabase: AppDatabase,
) : ViewModel() {

    private val _state: MutableStateFlow<AuthResult> = MutableStateFlow(AuthResult.Idle)
    val state: StateFlow<AuthResult> = _state.asStateFlow()

    fun signIn(context: Context) = viewModelScope.launch {
        Log.d("AuthFlow", "SignIn process started in ViewModel")
        _state.value = AuthResult.Loading

        val credential = dataSource.signIn(context)
        if (credential == null) {
            Log.e("AuthFlow", "Credential is null, sign-in aborted")
            _state.value = AuthResult.Idle
            return@launch
        }

        Log.d("AuthFlow", "Credential received from Google, authenticating with Firebase")
        try {
            val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
            val authResult = FirebaseAuth.getInstance()
                .signInWithCredential(firebaseCredential)
                .await()

            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                Log.e("AuthFlow", "Firebase user is null after sign-in")
                _state.value = AuthResult.Error("Firebase authentication failed")
                return@launch
            }

            Log.d("AuthFlow", "Firebase sign-in successful: ${firebaseUser.uid}")
            val firebaseTokenResult = firebaseUser.getIdToken(true).await()
            val firebaseIdToken = firebaseTokenResult.token

            if (firebaseIdToken == null) {
                Log.e("AuthFlow", "Firebase ID Token is null")
                _state.value = AuthResult.Error("Failed to retrieve auth token")
                return@launch
            }

            Log.d("AuthFlow", "Verifying user with backend server")
            val result = verifyUserRepository.verifyUser(firebaseIdToken)

            result.fold(
                onSuccess = {
                    Log.d("AuthFlow", "Backend verification successful, signed in!")
                    _state.value = AuthResult.SignedIn
                },
                onFailure = {
                    Log.e("AuthFlow", "Backend verification failed: ${it.message}")
                    _state.value = AuthResult.Error(it.message ?: "Sign-in failed")
                }
            )
        } catch (e: Exception) {
            Log.e("AuthFlow", "Exception during Auth flow: ${e.message}", e)
            FirebaseAuth.getInstance().signOut()
            _state.value = AuthResult.Error(e.message ?: "Sign-in failed")
        }
    }

    fun getCurrentUser(): Boolean {
        return FirebaseAuth.getInstance().currentUser?.uid != null
    }

    fun signOut() = viewModelScope.launch {
        // 1. Sign out from Firebase
        FirebaseAuth.getInstance().signOut()
        dataSource.signOut()

        // 2. Clear all local Room databases to prevent data leaking to the next user
        withContext(Dispatchers.IO) {
            try {
                chatDatabase.clearAllTables()
                campusDatabase.clearAllTables()
                appDatabase.clearAllTables()
                Log.d("GoogleSignInViewModel", "All databases cleared on sign-out")
            } catch (e: Exception) {
                Log.e("GoogleSignInViewModel", "Failed to clear databases on sign-out", e)
            }
        }

        _state.value = AuthResult.SignedOut
    }
}
