package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditLookingFor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.core.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditLookingForViewModel(private val profileRepository: UserProfileRepository) : ViewModel() {

    val lookingFor = MutableStateFlow("")

    private val _lookingForUiState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val lookingForUiState: StateFlow<UiState<Boolean>> = _lookingForUiState.asStateFlow()

    fun setLookingFor(lookingFor: String) {
        this.lookingFor.value = lookingFor
    }

    fun updateLookingFor(lookingFor: String) {
        viewModelScope.launch {
            _lookingForUiState.value = UiState.Loading
            val result = profileRepository.updateLookingFor(lookingFor)
            result.fold(
                onSuccess = {
                    _lookingForUiState.value = UiState.Success(true)
                },
                onFailure = {
                    _lookingForUiState.value = UiState.Error(it.message ?: "Failed to update looking for")
                }
            )
        }
    }
}
