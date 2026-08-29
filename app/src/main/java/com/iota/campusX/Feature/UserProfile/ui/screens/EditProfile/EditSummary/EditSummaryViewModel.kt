package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditSummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditSummaryViewModel(private val profileRepository: UserProfileRepository): ViewModel() {

    val summary = MutableStateFlow("")

    private val _summaryUiState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val summaryUiState: StateFlow<UiState<Boolean>> = _summaryUiState.asStateFlow()

    fun setSummary(summary: String){
        this.summary.value = summary
    }

    fun updateSummary(summary: String) {
        viewModelScope.launch {
            _summaryUiState.value = UiState.Loading
            val result = profileRepository.updateSummary(summary)
            result.fold(
                onSuccess = {
                    _summaryUiState.value = UiState.Success(true)
                },
                onFailure = {
                    _summaryUiState.value = UiState.Error(it.message ?: "Failed to update summary")
                }
            )
        }

    }

}