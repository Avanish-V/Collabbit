package com.iota.campusX.Feature.Auth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Auth.domain.usecase.SignInWithGoogleUseCase
import com.iota.campusX.Feature.Auth.domain.usecase.SignOutUseCase
import com.iota.campusX.Feature.Auth.presentation.model.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoogleSignInViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<AuthResult> = MutableStateFlow(AuthResult.Idle)
    val state: StateFlow<AuthResult> = _state.asStateFlow()

    fun signIn(context: Context) = viewModelScope.launch {
        _state.value = AuthResult.Loading
        val result = signInWithGoogleUseCase(context)

        result.fold(
            onSuccess = {
                _state.value = AuthResult.SignedIn
            },
            onFailure = { error ->
                _state.value = AuthResult.Error(error.message ?: "Sign-in failed. Please try again.")
            }
        )
    }

    fun signOut() = viewModelScope.launch {
        signOutUseCase()
        _state.value = AuthResult.SignedOut
    }
}
