package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.iota.campusX.Feature.UserProfile.domain.UserProfileInterface
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.http.auth.AuthState


class GoogleSignInViewModel (
    private val dataSource: CredentialAuthDataSource,
    private val verifyUserRepository: VerifyUserRepository,
    private val userProfileRepository: UserProfileInterface
) : ViewModel() {

    private val _state : MutableStateFlow<AuthResult> = MutableStateFlow(AuthResult.Idle)
    val state: StateFlow<AuthResult> = _state.asStateFlow()

    fun signIn() = viewModelScope.launch {

        _state.value = AuthResult.Loading

        val credential = dataSource.signIn()

        if (credential == null) {
            return@launch
        }

        try {
            val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
            val authResult = FirebaseAuth.getInstance()
                .signInWithCredential(firebaseCredential)
                .await() // use kotlinx-coroutines-play-services

            val firebaseUser = authResult.user
            val uid = firebaseUser?.uid

            if (uid != null) {
                val result = verifyUserRepository.verifyUser(uid, credential.idToken)
                result.fold(
                    onSuccess = {
                        userProfileRepository.syncUserProfile()
                        _state.value = AuthResult.SignedIn
                    },
                    onFailure = {
                        _state.value = AuthResult.Error(it.message ?: "Sign-in failed")
                    }
                )
            }
        } catch (e: Exception) {
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

