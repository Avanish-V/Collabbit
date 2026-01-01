package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.iota.campusX.Feature.UserProfile.data.local.database.UserProfileDao
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class GoogleSignInViewModel (
    private val dataSource: CredentialAuthDataSource,
    private val verifyUserRepository: VerifyUserRepository,
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    private val _state : MutableStateFlow<AuthResult> = MutableStateFlow(AuthResult.Idle)
    val state: StateFlow<AuthResult> = _state.asStateFlow()

    fun signIn() = viewModelScope.launch {
        _state.value = AuthResult.Loading

        val credential = dataSource.signIn() ?: return@launch

        try {
            val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
            val authResult = FirebaseAuth.getInstance()
                .signInWithCredential(firebaseCredential)
                .await()

            val firebaseUser = authResult.user ?: return@launch
            val firebaseTokenResult = firebaseUser.getIdToken(true).await()
            val firebaseIdToken = firebaseTokenResult.token ?: return@launch

            val result = verifyUserRepository.verifyUser(firebaseIdToken)

            result.fold(
                onSuccess = {
                    userProfileDao.insertProfile(
                        profile = it
                    )
                    _state.value = AuthResult.SignedIn
                },
                onFailure = {
                    _state.value = AuthResult.Error(it.message ?: "Sign-in failed")
                }
            )
        } catch (e: Exception) {
            Log.d("VerifyUserRepoImpl", "database error ${e.message}")
            FirebaseAuth.getInstance().signOut()
            _state.value = AuthResult.Error(e.message ?: "Sign-in failed")
        }
    }


    fun getCurrentUser(): Boolean{
        if (FirebaseAuth.getInstance().currentUser?.uid != null){
            return true
        }else{
            return false
        }
    }


    fun signOut() = viewModelScope.launch {
        dataSource.signOut()
        _state.value = AuthResult.SignedOut
    }

}

