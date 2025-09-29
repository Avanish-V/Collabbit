package com.iota.campusX.Feature.UserProfile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Feature.UserProfile.domain.UserProfileInterface
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UserProfileViewModel(
    private val userProfileRepo: UserProfileInterface,
    private val userProfileRepository: UserProfileRepository
):ViewModel() {


    val userBaseProfile = userProfileRepository.currentUser.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
    )

    private val _isLoading : MutableStateFlow<UiState<Unit>> = MutableStateFlow(UiState.Idle)
    val isLoading: StateFlow<UiState<Unit>> = _isLoading.asStateFlow()


    private val _hasConnection = MutableStateFlow<UiState<Boolean?>>(UiState.Idle)
    val hasConnection: StateFlow<UiState<Boolean?>> = _hasConnection.asStateFlow()


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


    fun hasConnection(userId: String) = viewModelScope.launch {
        _hasConnection.value = UiState.Loading
        _hasConnection.value = userProfileRepo.hasConnection(userId).fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }




    // Define mutation operations


}

