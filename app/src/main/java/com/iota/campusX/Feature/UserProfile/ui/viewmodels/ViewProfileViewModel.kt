package com.iota.campusX.Feature.UserProfile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.domain.repository.UserConnectionsRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewProfileViewModel(
    private val userProfileRepo: UserConnectionsRepository,
    private val userProfileRepository: UserProfileRepository
): ViewModel() {

    private val _profileById = MutableStateFlow<UiState<BaseProfileDTO>>(UiState.Idle)
    val profileById: StateFlow<UiState<BaseProfileDTO>> = _profileById.asStateFlow()

    private val _hasConnection = MutableStateFlow<UiState<Boolean?>>(UiState.Idle)
    val hasConnection: StateFlow<UiState<Boolean?>> = _hasConnection.asStateFlow()


    private val _connections = MutableStateFlow<UiState<List<ConnectionsDTO>>>(UiState.Idle)
    val connections: StateFlow<UiState<List<ConnectionsDTO>>> = _connections.asStateFlow()

    private val _connectionCount = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val connectionCount: StateFlow<UiState<Int>> = _connectionCount.asStateFlow()



    fun getUserById(userId: String) {
        viewModelScope.launch {

            _profileById.value = UiState.Loading

            val result = userProfileRepository.getUserProfileById(userId)

           _profileById.value =  result.fold(
                onSuccess = {
                    UiState.Success(it)
                   // _profileByUserId.value = UiState.Success((profileByUserId.value as UiState.Success).data.copy(basicProfileDTO = it))
                },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )

        }
    }

    fun getConnectionCount(userId: String) {

        if (connectionCount.value is UiState.Success) return

        viewModelScope.launch {

            _connectionCount.value = UiState.Loading

            val result = userProfileRepo.getConnectionsCount(userId)

            _connectionCount.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )
        }
    }

}