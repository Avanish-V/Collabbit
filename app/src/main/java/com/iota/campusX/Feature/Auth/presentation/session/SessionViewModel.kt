package com.iota.campusX.Feature.Auth.presentation.session

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Auth.domain.model.AuthState
import com.iota.campusX.Feature.Auth.domain.usecase.CheckAuthStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(
    private val checkAuthStateUseCase: CheckAuthStateUseCase
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = checkAuthStateUseCase.authState
        .map { authState ->
            when (authState) {
                AuthState.Authenticated -> SessionState.Authenticated
                AuthState.Unauthenticated, is AuthState.Error -> SessionState.Unauthenticated
                AuthState.Unknown, AuthState.Authenticating, AuthState.Verifying -> SessionState.Loading
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionState.Loading
        )

    private val _pendingDeepLink = MutableStateFlow<Uri?>(null)
    val pendingDeepLink: StateFlow<Uri?> = _pendingDeepLink.asStateFlow()

    init {
        resolveSession()
    }

    fun resolveSession() {
        viewModelScope.launch {
            checkAuthStateUseCase.checkInitialAuthState()
        }
    }

    fun setPendingDeepLink(uri: Uri?) {
        _pendingDeepLink.value = uri
    }

    fun consumePendingDeepLink(): Uri? {
        val uri = _pendingDeepLink.value
        _pendingDeepLink.value = null
        return uri
    }
}
