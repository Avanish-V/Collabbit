package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val googleAuthRepo: GoogleAuthRepo):ViewModel() {


    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()


   fun currentUser() = googleAuthRepo.getCurrentUser()

    fun onSignInResult(intent: Intent){

        viewModelScope.launch {

            val signInResult = googleAuthRepo.signInWithIntent(intent)

            signInResult.let {result->
                _state.update {
                    it.copy(
                        userId = result.userId,
                        userToken = result.userToken,
                        isSignInSuccessful = result.status != null,
                        signInError = result.errorMessage
                    )
                }
            }

        }

    }

    suspend fun startSignIn() = googleAuthRepo.signIn()

    fun verifyUser(userId:String,userToken: String) = googleAuthRepo.verifyUser(userId,userToken)

    fun userId(): String{
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

}