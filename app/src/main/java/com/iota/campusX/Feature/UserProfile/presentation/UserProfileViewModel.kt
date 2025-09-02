package com.iota.campusX.Feature.UserProfile.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.data.Gender
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepo
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UserProfileViewModel(
    private val userProfileRepo: UserProfileRepo,
    private val userProfileRepository: UserProfileRepository
):ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val userBaseProfile: StateFlow<BaseProfileDTO?> = userProfileRepository.currentUser

    val isLoading: StateFlow<Boolean> = userProfileRepository.isLoading


    private val _universityData = MutableStateFlow<UiState<List<UniversityDTO>>>(UiState.Idle)
    val universityData: StateFlow<UiState<List<UniversityDTO>>> = _universityData.asStateFlow()

    private val _hasConnection = MutableStateFlow<UiState<Boolean?>>(UiState.Idle)
    val hasConnection: StateFlow<UiState<Boolean?>> = _hasConnection.asStateFlow()


    fun getUserProfile() = viewModelScope.launch {
        if (userBaseProfile.value != null) return@launch
        userProfileRepository.loadCurrentUser()
    }
    fun refreshProfile() = viewModelScope.launch {
        userProfileRepository.loadCurrentUser()
    }


    fun hasConnection(userId: String) = viewModelScope.launch {
        _hasConnection.value = UiState.Loading
        _hasConnection.value = userProfileRepo.hasConnection(userId).fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }

    fun onUniversityQueryChanged(query: String) {
        searchQuery.value = query
    }

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(500) // 500ms debounce delay
                .filter { it.isNotBlank() && it.length < 5 }
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    userProfileRepo.updateUniversity(query)
                }
                .onStart { _universityData.value = UiState.Loading }
                .catch { e ->
                    _universityData.value = UiState.Error("Unexpected error: ${e.localizedMessage ?: "Unknown"}")
                }
                .collect { result ->

                    _universityData.value = result
                }
        }
    }

    fun resetUniversityData() {
        _universityData.value = UiState.Idle
    }


    // Define mutation operations


}

