package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditOpenTo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.remote.response.MatchPreferenceResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditOpenToViewModel(
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    private val _selectedIds = MutableStateFlow<List<Long>>(emptyList())
    val selectedIds: StateFlow<List<Long>> = _selectedIds.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val uiState: StateFlow<UiState<Boolean>> = _uiState.asStateFlow()

    private val _matchPreferences = MutableStateFlow<UiState<List<MatchPreferenceResponse>>>(UiState.Idle)
    val matchPreferences: StateFlow<UiState<List<MatchPreferenceResponse>>> = _matchPreferences.asStateFlow()

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

    fun fetchUserPreferences() {
        viewModelScope.launch {
            profileRepository.getUserMatchPreferences()
                .fold(
                    onSuccess = { prefs ->
                        _selectedIds.value = prefs.map { it.id }
                    },
                    onFailure = { /* Handle error if needed */ }
                )
        }
    }

    fun toggleOption(id: Long) {
        val current = _selectedIds.value.toMutableList()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
    }

    fun updateOpenTo() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            profileRepository.updateMatchPreferences(_selectedIds.value)
                .fold(
                    onSuccess = {
                        _uiState.value = UiState.Success(true)
                    },
                    onFailure = {
                        _uiState.value = UiState.Error(it.message ?: "Failed to update preferences")
                    }
                )
        }
    }
}
