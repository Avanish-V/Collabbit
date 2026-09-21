package com.iota.campusX.Feature.Collab.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Collab.data.model.*
import com.iota.campusX.Feature.Collab.domain.repository.CollabRepository
import com.iota.campusX.Feature.Collab.domain.usecase.CreateCollabUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class CollabViewModel(
    private val repository: CollabRepository,
    private val createCollabUseCase: CreateCollabUseCase
) : ViewModel() {

    private val _collabState = MutableStateFlow<UiState<List<CollabResponse>>>(UiState.Idle)
    val collabState = _collabState.asStateFlow()

    private val _userCollabsState = MutableStateFlow<UiState<List<CollabResponse>>>(UiState.Idle)
    val userCollabsState : StateFlow<UiState<List<CollabResponse>>> = _userCollabsState.asStateFlow()

    private val _selectedCollabState = MutableStateFlow<UiState<CollabResponse>>(UiState.Idle)
    val selectedCollabState = _selectedCollabState.asStateFlow()

    private val _createCollabState = MutableStateFlow<UiState<CollabResponse>>(UiState.Idle)
    val createCollabState = _createCollabState.asStateFlow()

    data class CollabFilter(val type: String, val query: String = "")
    private val _filterFlow = MutableStateFlow(CollabFilter("All"))

    @OptIn(ExperimentalCoroutinesApi::class)
    val collabPagingData: Flow<PagingData<CollabResponse>> = _filterFlow
        .flatMapLatest { filter ->
            repository.getCollabsPaging(filter.type, filter.query)
        }
        .cachedIn(viewModelScope)

    fun fetchCollabs(type: String? = null, query: String? = null) {
        val current = _filterFlow.value
        _filterFlow.value = CollabFilter(
            type = type ?: current.type,
            query = query ?: current.query
        )
    }

    fun fetchCollabById(collabId: String) {
        viewModelScope.launch {
            _selectedCollabState.value = UiState.Loading
            repository.getCollabById(collabId).fold(
                onSuccess = {
                    _selectedCollabState.value = UiState.Success(it)
                },
                onFailure = {
                    _selectedCollabState.value = UiState.Error(it.message ?: "Failed to fetch collaboration")
                }
            )
        }
    }

    fun getCollabsByUserId(userId: String){
        viewModelScope.launch {
            _userCollabsState.value = UiState.Loading
            repository.getCollabsByUserId(userId).fold(
                onSuccess = {
                    _userCollabsState.value = UiState.Success(it)
                },
                onFailure = {
                    _userCollabsState.value = UiState.Error(it.message ?: "Failed to fetch your collaborations")
                }
            )
        }
    }

    fun createCollab(
        title: String,
        description: String,
        type: CollabType,
        requirements: List<String>,
        needed: Int,
        deadline: Long? = null,
        projectUrl: String? = null
    ) {
        val request = CreateCollabRequest(
            title = title,
            description = description,
            collabType = type,
            requirements = requirements,
            participantsNeeded = needed,
            deadline = deadline,
            projectUrl = projectUrl
        )
        viewModelScope.launch {
            _createCollabState.value = UiState.Loading
            createCollabUseCase(request).fold(
                onSuccess = {
                    _createCollabState.value = UiState.Success(it)
                },
                onFailure = {
                    _createCollabState.value = UiState.Error(it.message ?: "Failed to create collaboration")
                }
            )
        }
    }

    private val _collabRequestsState = MutableStateFlow<UiState<List<RequestUiState>>>(UiState.Idle)
    val collabRequestsState = _collabRequestsState.asStateFlow()

    private val _connectState = MutableStateFlow<UiState<CollabRequestStatus>>(UiState.Idle)
    val connectState = _connectState.asStateFlow()

    private val _hasRequestedState = MutableStateFlow<UiState<CollabRequestStatus>>(UiState.Idle)
    val hasRequested : StateFlow<UiState<CollabRequestStatus>> = _hasRequestedState.asStateFlow()

    fun connectToCollab(collabId: String) {
        viewModelScope.launch {
            _hasRequestedState.value = UiState.Loading
            repository.requestToCollab(collabId).fold(
                onSuccess = {
                    _hasRequestedState.value = UiState.Success(data = CollabRequestStatus.PENDING)
                },
                onFailure = {
                    _hasRequestedState.value = UiState.Error(it.message ?: "Failed to connect")
                }
            )
        }
    }

    fun hasRequested(collabId: String){
        viewModelScope.launch {
            _hasRequestedState.value = UiState.Loading
            repository.hasAlreadyApplied(collabId).fold(
                onSuccess = {
                    _hasRequestedState.value = UiState.Success(it)
                },
                onFailure = {
                    _hasRequestedState.value = UiState.Error(it.message ?: "Failed to check status")
                }
            )
        }
    }

    fun resetConnectState() {
        _connectState.value = UiState.Idle
    }

    fun fetchCollabRequests(collabId: String) {
        viewModelScope.launch {
            _collabRequestsState.value = UiState.Loading
            repository.getCollabRequests(collabId).fold(
                onSuccess = { requests ->
                    val uiState = requests.map { RequestUiState(request = it) }
                    _collabRequestsState.value = UiState.Success(uiState)
                },
                onFailure = {
                    _collabRequestsState.value = UiState.Error(it.message ?: "Failed to fetch requests")
                }
            )
        }
    }

    fun updateCollabRequestStatus(
        requestId: String,
        status: CollabRequestStatus,
        collabId: String
    ) {
        viewModelScope.launch {
            val currentState = _collabRequestsState.value
            if (currentState is UiState.Success) {
                _collabRequestsState.value = UiState.Success(
                    currentState.data.map { item ->
                        if (item.request.id == requestId) {
                            when (status) {
                                CollabRequestStatus.ACCEPTED -> item.copy(isAcceptLoading = true)
                                CollabRequestStatus.DECLINED -> item.copy(isRejectLoading = true)
                                else -> item
                            }
                        } else item
                    }
                )
            }

            repository.updateCollabRequestStatus(requestId, status.name).fold(
                onSuccess = { response ->
                    val state = _collabRequestsState.value
                    if (state is UiState.Success) {
                        val updated = state.data.map { item ->
                            if (item.request.id == requestId) {
                                item.copy(
                                    request = item.request.copy(status = response.name),
                                    isAcceptLoading = false,
                                    isRejectLoading = false
                                )
                            } else item
                        }
                        
                        _collabRequestsState.value = UiState.Success(
                            data = if (response == CollabRequestStatus.DECLINED) {
                                updated.filterNot { it.request.id == requestId }
                            } else updated
                        )
                    }
                },
                onFailure = { error ->
                    val state = _collabRequestsState.value
                    if (state is UiState.Success) {
                        _collabRequestsState.value = UiState.Success(
                            state.data.map { item ->
                                if (item.request.id == requestId) {
                                    item.copy(isAcceptLoading = false, isRejectLoading = false)
                                } else item
                            }
                        )
                    }
                    _collabRequestsState.value = UiState.Error(error.message ?: "Failed to update status")
                }
            )
        }
    }

    private val _deleteCollabState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteCollabState = _deleteCollabState.asStateFlow()

    fun deleteCollab(collabId: String) {
        viewModelScope.launch {
            _deleteCollabState.value = UiState.Loading
            repository.deleteCollab(collabId).fold(
                onSuccess = {
                    _deleteCollabState.value = UiState.Success(Unit)
                },
                onFailure = {
                    _deleteCollabState.value = UiState.Error(it.message ?: "Failed to delete collaboration")
                }
            )
        }
    }

    fun resetDeleteState() {
        _deleteCollabState.value = UiState.Idle
    }

    fun resetCreateState() {
        _createCollabState.value = UiState.Idle
    }
}

data class RequestUiState(
    val isAcceptLoading: Boolean = false,
    val isRejectLoading: Boolean = false,
    val request: CollabConnectRequestResponse,
)
