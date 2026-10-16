package com.iota.campusX.Feature.Collab.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Collab.data.model.*
import com.iota.campusX.Feature.Collab.domain.repository.CollabRepository
import com.iota.campusX.Feature.Collab.domain.usecase.CreateCollabUseCase
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import com.iota.campusX.core.common.UiState
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
    private val createCollabUseCase: CreateCollabUseCase,
    private val s3Uploader: S3Uploader
) : ViewModel() {

    private val _userCollabsState = MutableStateFlow<UiState<List<CollabResponse>>>(UiState.Idle)
    val userCollabsState : StateFlow<UiState<List<CollabResponse>>> = _userCollabsState.asStateFlow()

    private val _joinedCollabsState = MutableStateFlow<UiState<List<CollabResponse>>>(UiState.Idle)
    val joinedCollabsState: StateFlow<UiState<List<CollabResponse>>> = _joinedCollabsState.asStateFlow()

    private val _selectedCollabState = MutableStateFlow<UiState<CollabResponse>>(UiState.Idle)
    val selectedCollabState = _selectedCollabState.asStateFlow()

    private val _createCollabState = MutableStateFlow<UiState<CollabResponse>>(UiState.Idle)
    val createCollabState = _createCollabState.asStateFlow()

    private val _updateProjectStatusState = MutableStateFlow<UiState<CollabResponse>>(UiState.Idle)
    val updateProjectStatusState = _updateProjectStatusState.asStateFlow()

    private val _updateCollabState = MutableStateFlow<UiState<CollabResponse>>(UiState.Idle)
    val updateCollabState: StateFlow<UiState<CollabResponse>> = _updateCollabState.asStateFlow()

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

    fun updateProjectStatus(collabId: String, status: ProjectStatus) {
        viewModelScope.launch {
            _updateProjectStatusState.value = UiState.Loading
            repository.updateProjectStatus(collabId, status).fold(
                onSuccess = { updated ->
                    _updateProjectStatusState.value = UiState.Success(updated)
                    _selectedCollabState.value = UiState.Success(updated)
                },
                onFailure = {
                    _updateProjectStatusState.value = UiState.Error(it.localizedMessage ?: "Failed to update project status")
                }
            )
        }
    }

    fun resetUpdateProjectStatusState() {
        _updateProjectStatusState.value = UiState.Idle
    }

    fun updateCollab(collabId: String, request: UpdateCollabRequest) {
        viewModelScope.launch {
            _updateCollabState.value = UiState.Loading
            repository.updateCollab(collabId, request).fold(
                onSuccess = { updated ->
                    _updateCollabState.value = UiState.Success(updated)
                    _selectedCollabState.value = UiState.Success(updated)
                },
                onFailure = {
                    _updateCollabState.value = UiState.Error(
                        it.localizedMessage ?: "Failed to update project"
                    )
                }
            )
        }
    }

    fun updateCollabWithLogo(
        collabId: String,
        title: String,
        description: String,
        projectUrl: String,
        logoUri: String?,
        roles: List<ProjectRoleUiModel>,
        originalCollab: CollabResponse
    ) {
        viewModelScope.launch {
            _updateCollabState.value = UiState.Loading
            var finalLogoUrl: String? = logoUri

            if (!logoUri.isNullOrBlank() &&
                !logoUri.startsWith("http://") &&
                !logoUri.startsWith("https://")
            ) {
                try {
                    finalLogoUrl = s3Uploader.UploadImageToS3(
                        uri = Uri.parse(logoUri),
                        folder = "collab_logos",
                        onProgress = {}
                    )
                    if (finalLogoUrl == null) {
                        _updateCollabState.value = UiState.Error("Logo upload failed. Please retry or remove logo.")
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("CollabViewModel", "Logo upload error: ${e.message}", e)
                    _updateCollabState.value = UiState.Error("Logo upload failed: ${e.localizedMessage}")
                    return@launch
                }
            }

            val projectRoles = roles.map {
                ProjectRole(
                    id = it.id,
                    roleName = it.roleName.trim(),
                    skills = it.skills,
                    peopleNeeded = it.peopleNeeded
                )
            }
            val totalNeeded = roles.sumOf { it.peopleNeeded }.coerceAtLeast(1)

            val reqList = roles.flatMap { role ->
                if (role.skills.isNotEmpty()) {
                    listOf(role.roleName) + role.skills
                } else {
                    listOf(role.roleName)
                }
            }.distinct().filter { it.isNotBlank() }

            val hasRolesChanged = projectRoles != originalCollab.roles

            val request = UpdateCollabRequest(
                title = title.trim().takeIf { it != originalCollab.title },
                description = description.trim().takeIf { it != originalCollab.description },
                requirements = reqList.takeIf { it != originalCollab.requirements },
                projectUrl = projectUrl.trim().let { url ->
                    if (url != (originalCollab.projectUrl ?: "")) url else null
                },
                logoUrl = if (finalLogoUrl != originalCollab.logoUrl) finalLogoUrl else null,
                roles = projectRoles.takeIf { hasRolesChanged },
                participantsNeeded = totalNeeded.takeIf { it != originalCollab.participantsNeeded }
            )

            repository.updateCollab(collabId, request).fold(
                onSuccess = { updated ->
                    _updateCollabState.value = UiState.Success(updated)
                    _selectedCollabState.value = UiState.Success(updated)
                },
                onFailure = {
                    _updateCollabState.value = UiState.Error(
                        it.localizedMessage ?: "Failed to update project"
                    )
                }
            )
        }
    }

    fun resetUpdateCollabState() {
        _updateCollabState.value = UiState.Idle
    }

    fun getCollabsByUserId(userId: String) {
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

    fun getJoinedCollabsByUserId(userId: String) {
        viewModelScope.launch {
            _joinedCollabsState.value = UiState.Loading
            repository.getJoinedCollabsByUserId(userId).fold(
                onSuccess = {
                    _joinedCollabsState.value = UiState.Success(it)
                },
                onFailure = {
                    _joinedCollabsState.value = UiState.Error(it.message ?: "Failed to fetch joined collaborations")
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
                        if (response == CollabRequestStatus.ACCEPTED) {
                            fetchCollabMembers(collabId)
                            fetchCollabById(collabId)
                        }
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

    private val _collabMembersState = MutableStateFlow<UiState<List<CollabMemberResponse>>>(UiState.Idle)
    val collabMembersState: StateFlow<UiState<List<CollabMemberResponse>>> = _collabMembersState.asStateFlow()

    private val _removeMemberState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val removeMemberState: StateFlow<UiState<Unit>> = _removeMemberState.asStateFlow()

    fun fetchCollabMembers(collabId: String) {
        viewModelScope.launch {
            _collabMembersState.value = UiState.Loading
            repository.getCollabMembers(collabId).fold(
                onSuccess = { members ->
                    _collabMembersState.value = UiState.Success(members)
                },
                onFailure = { error ->
                    _collabMembersState.value = UiState.Error(error.message ?: "Failed to fetch team members")
                }
            )
        }
    }

    fun removeCollabMember(collabId: String, memberUserId: String) {
        viewModelScope.launch {
            _removeMemberState.value = UiState.Loading
            repository.removeCollabMember(collabId, memberUserId).fold(
                onSuccess = {
                    _removeMemberState.value = UiState.Success(Unit)
                    fetchCollabMembers(collabId)
                    fetchCollabRequests(collabId)
                    fetchCollabById(collabId)
                },
                onFailure = { error ->
                    _removeMemberState.value = UiState.Error(error.message ?: "Failed to remove member")
                }
            )
        }
    }

    fun resetRemoveMemberState() {
        _removeMemberState.value = UiState.Idle
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
