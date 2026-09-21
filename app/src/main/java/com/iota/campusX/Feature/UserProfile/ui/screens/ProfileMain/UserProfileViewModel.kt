package com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Feature.UserProfile.domain.useCases.GetProfileUseCase
import com.iota.campusX.Feature.UserProfile.domain.useCases.ObserveProfileUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.iota.campusX.Feature.UserProfile.data.remote.response.MatchPreferenceResponse
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UserProfileViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private var observeJob: Job? = null

    // ── Profile loading ───────────────────────────────────────────────────────

    fun load(userId: String?) {
        observeJob?.cancel()
        if (userId == null) observeCurrentUser() else loadOtherUser(userId)
    }

    private fun observeCurrentUser() {
        observeJob = observeProfile()
            .onEach { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false, error = null) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadOtherUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            profileRepository.getUserProfileById(userId)
                .fold(
                    onSuccess = { profile ->
                        _uiState.update { it.copy(profile = profile, isLoading = false, error = null) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                )
        }
    }

    private val _syncState: MutableStateFlow<UiState<Unit>> = MutableStateFlow(UiState.Idle)
    val syncState: StateFlow<UiState<Unit>> = _syncState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refreshProfile(userId: String?) {
        viewModelScope.launch {
            _isRefreshing.value = true
            if (userId == null) {
                getProfileUseCase(force = true)
            } else {
                profileRepository.getUserProfileById(userId)
            }
            _isRefreshing.value = false
        }
    }

    fun getUserProfile() {
        viewModelScope.launch {
            _syncState.value = UiState.Loading
            getProfileUseCase()
                .fold(
                    onSuccess = { _syncState.value = UiState.Success(Unit) },
                    onFailure = { _syncState.value = UiState.Error(it.message.toString()) }
                )
        }
    }

    suspend fun syncProfileSuspending(): Result<ProfileResponse> {
        _syncState.value = UiState.Loading
        val result = getProfileUseCase()
        result.fold(
            onSuccess = { _syncState.value = UiState.Success(Unit) },
            onFailure = { _syncState.value = UiState.Error(it.message.toString()) }
        )
        return result
    }

    private val _matchPreferences = MutableStateFlow<UiState<List<MatchPreferenceResponse>>>(UiState.Idle)
    val matchPreferences = _matchPreferences.asStateFlow()

    fun fetchMatchPreferences() {
        viewModelScope.launch {
            _matchPreferences.value = UiState.Loading
            profileRepository.getMatchPreferences()
                .fold(
                    onSuccess = { _matchPreferences.value = UiState.Success(it) },
                    onFailure = { _matchPreferences.value = UiState.Error(it.message ?: "Unknown error") }
                )
        }
    }

    fun updateOpenTo(selectedCodes: List<String>) {
        viewModelScope.launch {
            profileRepository.updateOpenTo(selectedCodes)
                .fold(
                    onSuccess = { getUserProfile() },
                    onFailure = { Log.e("UserProfileViewModel", "updateOpenTo: ${it.message}") }
                )
        }
    }

    // ── UI state ──────────────────────────────────────────────────────────────

    data class ProfileUiState(
        val profile: ProfileResponse? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
