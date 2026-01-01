package com.iota.campusX.Feature.UserProfile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.google.firebase.firestore.FieldValue
import com.iota.campusX.Feature.Notification.data.CreateNotification
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionResponse
import com.iota.campusX.Feature.UserProfile.domain.models.ConnectionRequestResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserConnectionsRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ConnectionRequestViewModel(
    private val userProfileRepo: UserConnectionsRepository,
    private val notificationRepository: NotificationRepository
): ViewModel() {


    private val _acceptRequestState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val acceptRequestState: StateFlow<UiState<Boolean>> = _acceptRequestState.asStateFlow()

    private val _rejectRequestState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val rejectRequestState: StateFlow<UiState<Boolean>> = _rejectRequestState.asStateFlow()

    private val _connectionState = MutableStateFlow<UiState<ConnectionRequestResponse>>(UiState.Idle)
    val connectionState: StateFlow<UiState<ConnectionRequestResponse>> = _connectionState.asStateFlow()

    private val _connections = MutableStateFlow<PagingData<ConnectionResponse>>(PagingData.empty())
    val connections: StateFlow<PagingData<ConnectionResponse>> = _connections.asStateFlow()

    private val _pendingRequests = MutableStateFlow<UiState<List<ConnectionResponse>>>(UiState.Idle)
    val pendingRequests: StateFlow<UiState<List<ConnectionResponse>>> = _pendingRequests.asStateFlow()


     fun initConnections(userId: String) {
         viewModelScope.launch {
             userProfileRepo.getConnections(userId)
                 .collectLatest { pagingData ->
                     _connections.value = pagingData
                 }
         }
    }


    fun getConnectionsForCurrentUser() {
        viewModelScope.launch {
            val result = userProfileRepo.getConnectionsForCurrentUser()
            result.collect {
                _connections.value = it
            }
        }
    }

    fun getPendingRequests() {
        viewModelScope.launch {
            _pendingRequests.value = UiState.Loading
            val result = userProfileRepo.getPendingRequests()
            _pendingRequests.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )
        }
    }

    fun request(state: ConnectionRequestState) = viewModelScope.launch {

        when (state) {
            is ConnectionRequestState.SendConnectionRequest -> {

                _connectionState.value = UiState.Loading
                _connectionState.value = userProfileRepo.sendLinkUpRequest(state.requestUserId, state.currentState).fold(
                    onSuccess = {
                        notificationRepository.createNotification(
                            createNotification = CreateNotification.ConnectionRequestNotification(
                                notificationId = UUID.randomUUID().toString(),
                                createdAt = FieldValue.serverTimestamp(),
                            ),
                            creatorId = state.requestUserId

                        )
                        UiState.Success(it)
                    },
                    onFailure = { UiState.Error(it.message ?: "Something went wrong") }
                )

            }

            is ConnectionRequestState.AcceptConnectionRequest -> {

                _acceptRequestState.value = UiState.Loading
                _acceptRequestState.value = userProfileRepo.acceptLinkUpRequest(state.requestUserId).fold(
                    onSuccess = {
                        removePendingRequestFromList(state.requestUserId.toString())
                        UiState.Success(it)
                    },
                    onFailure = { UiState.Error(it.message ?: "Something went wrong") }
                )

            }
            is ConnectionRequestState.RejectConnectionRequest -> {

                _rejectRequestState.value = UiState.Loading

                _rejectRequestState.value = userProfileRepo.rejectLinkUpRequest(state.requestUserId).fold(
                    onSuccess = {
                        removePendingRequestFromList(state.requestUserId.toString())
                        UiState.Success(true)
                    },
                    onFailure = { UiState.Error(it.message ?: "Something went wrong") }
                )

            }
            is ConnectionRequestState.DeleteConnection-> {

                _connectionState.value = UiState.Loading

                _connectionState.value = userProfileRepo.deleteConnection(state.requestUserId).fold(
                    onSuccess = {
                        removeConnectionFromList(state.requestUserId)
                        UiState.Success(it)
                    },
                    onFailure = { UiState.Error(it.message ?: "Something went wrong") }
                )

            }
        }

    }

    fun removeConnectionFromList(userId: String?){
        _connections.update { pagingData ->
            pagingData.filter { it.requestId != userId }
        }
    }

    fun removePendingRequestFromList(userId: String){
        val result =  _pendingRequests.value
        if (result is UiState.Success){
            _pendingRequests.value = UiState.Success(result.data.filter { it.requestId != userId })
        }
    }




    fun hasConnection(userId: String) = viewModelScope.launch {
        _connectionState.value = UiState.Loading
        val result = userProfileRepo.hasConnection(userId)

        result.fold(
            onSuccess = {
                _connectionState.value = UiState.Success(it)
            },
            onFailure = {
                _connectionState.value = UiState.Error(it.message ?: "Something went wrong")
            }
        )

    }

}

sealed class ConnectionRequestState {
    data class SendConnectionRequest(val requestUserId: String,val currentState: Boolean?) : ConnectionRequestState()
    data class AcceptConnectionRequest(val requestUserId: String) : ConnectionRequestState()
    data class RejectConnectionRequest(val requestUserId: String?) : ConnectionRequestState()
    data class DeleteConnection(val requestUserId: String?) : ConnectionRequestState()

}

enum class ConnectionState{Accepted,Rejected,Sent}