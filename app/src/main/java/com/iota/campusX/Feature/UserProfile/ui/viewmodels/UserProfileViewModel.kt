package com.iota.campusX.Feature.UserProfile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.repository.UserProfileRepositoryData
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UserProfileViewModel(
    private val userProfileRepo: UserProfileRepository,
    private val userProfileRepository: UserProfileRepositoryData
): ViewModel() {


    val userBaseProfile = userProfileRepository.currentUser.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.Lazily,
            initialValue = null
    )

    private val _isLoading : MutableStateFlow<UiState<Unit>> = MutableStateFlow(UiState.Idle)
    val isLoading: StateFlow<UiState<Unit>> = _isLoading.asStateFlow()



    fun getUserProfile() = viewModelScope.launch {
        if (userBaseProfile.value != null) return@launch
        userProfileRepository.loadCurrentUser()
    }
    fun refreshProfile() = viewModelScope.launch {

        _isLoading.value = UiState.Loading

       val result =  userProfileRepo.syncUserProfile()

        result.fold(
            onSuccess = {

                _isLoading.value = UiState.Success(Unit)
            },
            onFailure = {
                _isLoading.value = UiState.Error(it.message ?: "Something went wrong")
            }
        )

    }

}