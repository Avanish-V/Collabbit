package com.iota.campusX.Feature.UserProfile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.domain.UserProfileInterface
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ViewProfileViewModel(private val userProfileRepo: UserProfileInterface):ViewModel() {

    private val _profileById = MutableStateFlow<UiState<BaseProfileDTO>>(UiState.Idle)
    val profileById: StateFlow<UiState<BaseProfileDTO>> = _profileById.asStateFlow()

    private val _hasConnection = MutableStateFlow<UiState<Boolean?>>(UiState.Idle)
    val hasConnection: StateFlow<UiState<Boolean?>> = _hasConnection.asStateFlow()


    private val _connections = MutableStateFlow<UiState<List<ConnectionsDTO>>>(UiState.Idle)
    val connections: StateFlow<UiState<List<ConnectionsDTO>>> = _connections.asStateFlow()

    private val _connectionCount = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val connectionCount: StateFlow<UiState<Int>> = _connectionCount.asStateFlow()

    private val _sendLinkUpRequestState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val sendLinkUpRequestState: StateFlow<UiState<Unit>> = _sendLinkUpRequestState.asStateFlow()


    fun getUserById(userId: String) {
        viewModelScope.launch {

            _profileById.value = UiState.Loading

            val result = userProfileRepo.getUserProfileById(userId)

           _profileById.value =  result.fold(
                onSuccess = {
                    UiState.Success(it)
                   // _profileByUserId.value = UiState.Success((profileByUserId.value as UiState.Success).data.copy(basicProfileDTO = it))
                },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )

        }
    }

    fun getConnections(userId: String) {
        viewModelScope.launch {
            _connections.value = UiState.Loading
            val result = userProfileRepo.getConnections(userId)
            _connections.value = result.fold(
                onSuccess = { UiState.Success(it) },
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

    fun sendLinkUpRequest(requestUserId: String, currentState: Boolean?) = viewModelScope.launch {
        _sendLinkUpRequestState.value = UiState.Loading
        _sendLinkUpRequestState.value = userProfileRepo.sendLinkUpRequest(requestUserId, currentState).fold(
            onSuccess = { UiState.Success(Unit) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }

    fun hasConnection(userId: String) = viewModelScope.launch {
        _hasConnection.value = UiState.Loading
        _hasConnection.value = userProfileRepo.hasConnection(userId).fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }

}

