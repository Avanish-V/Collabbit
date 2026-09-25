package com.iota.campusX.Feature.Auth.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Auth.domain.usecase.CheckAuthStateUseCase
import com.iota.campusX.Feature.Auth.domain.usecase.SignInWithGoogleUseCase
import com.iota.campusX.Feature.Auth.domain.usecase.SignOutUseCase
import com.iota.campusX.Feature.Auth.presentation.model.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoogleSignInViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val checkAuthStateUseCase: CheckAuthStateUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<AuthResult> = MutableStateFlow(AuthResult.Idle)
    val state: StateFlow<AuthResult> = _state.asStateFlow()

    val isLoggedIn: StateFlow<Boolean?> = checkAuthStateUseCase.isLoggedIn

    private val _pendingDeepLink = MutableStateFlow<Uri?>(null)
    val pendingDeepLink: StateFlow<Uri?> = _pendingDeepLink.asStateFlow()

    init {
        viewModelScope.launch {
            checkAuthStateUseCase.checkInitialAuthState()
        }
    }

    fun setPendingDeepLink(uri: Uri?) {
        _pendingDeepLink.value = uri
    }

    fun clearPendingDeepLink() {
        _pendingDeepLink.value = null
    }

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

    fun getCurrentUser(): Boolean {
        return isLoggedIn.value == true
    }

    fun signOut() = viewModelScope.launch {
        signOutUseCase()
        _state.value = AuthResult.SignedOut
    }
}
