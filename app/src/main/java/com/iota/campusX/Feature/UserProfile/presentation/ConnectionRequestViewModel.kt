package com.iota.campusX.Feature.UserProfile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepo
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class ConnectionRequestViewModel(private val userProfileRepo: UserProfileRepo): ViewModel() {

    private val _acceptRequestState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val acceptRequestState: StateFlow<UiState<Boolean>> = _acceptRequestState.asStateFlow()

    private val _rejectRequestState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val rejectRequestState: StateFlow<UiState<Boolean>> = _rejectRequestState.asStateFlow()

    private val _sentRequestState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val sentRequestState: StateFlow<UiState<Boolean>> = _sentRequestState.asStateFlow()

    private val _connections = MutableStateFlow<UiState<List<ConnectionsDTO>>>(UiState.Idle)
    val connections: StateFlow<UiState<List<ConnectionsDTO>>> = _connections.asStateFlow()

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

    fun request(state: ConnectionRequestState) = viewModelScope.launch {

        when (state) {
            is ConnectionRequestState.SendConnectionRequest -> {

                _sentRequestState.value = UiState.Loading
                _sentRequestState.value = userProfileRepo.sendLinkUpRequest(state.requestUserId, state.currentState).fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Something went wrong") }
                )

            }

            is ConnectionRequestState.AcceptConnectionRequest -> {

                _acceptRequestState.value = UiState.Loading
                _acceptRequestState.value = userProfileRepo.acceptLinkUpRequest(state.requestUserId).fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Something went wrong") }
                )

            }
            is ConnectionRequestState.RejectConnectionRequest -> {

                _rejectRequestState.value = UiState.Loading
                _rejectRequestState.value = userProfileRepo.rejectLinkUpRequest(state.requestUserId).fold(
                    onSuccess = {
                        removeConnectionFromList(state.requestUserId)
                        UiState.Success(it)
                    },
                    onFailure = { UiState.Error(it.message ?: "Something went wrong") }
                )

            }
        }

    }

    fun removeConnectionFromList(userId: String){
        if (connections.value is UiState.Idle || connections.value is UiState.Loading) return
        _connections.value = UiState.Success((connections.value as UiState.Success).data.filter { it.user.id != userId })
    }

}

sealed class ConnectionRequestState {
    data class SendConnectionRequest(val requestUserId: String,val currentState: Boolean?) : ConnectionRequestState()
    data class AcceptConnectionRequest(val requestUserId: String) : ConnectionRequestState()
    data class RejectConnectionRequest(val requestUserId: String) : ConnectionRequestState()

}

enum class ConnectionState{Accepted,Rejected,Sent}