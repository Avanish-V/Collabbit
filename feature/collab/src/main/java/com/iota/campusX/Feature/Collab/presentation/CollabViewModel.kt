package com.iota.campusX.Feature.Collab.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Collab.data.model.*
import com.iota.campusX.Feature.Collab.domain.repository.CollabFeedbackRepository
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

data class ReplyingTarget(
    val feedbackId: String,
    val userName: String
)

data class RequestUiState(
    val isAcceptLoading: Boolean = false,
    val isRejectLoading: Boolean = false,
    val request: CollabConnectRequestResponse
)

class CollabViewModel(
    private val repository: CollabRepository,
    private val feedbackRepository: CollabFeedbackRepository,
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
            repository.getCollabsPaging(
                type = filter.type,
                query = filter.query
            )
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        _filterFlow.value = _filterFlow.value.copy(query = query)
    }

    fun onFilterTypeChanged(type: String) {
        _filterFlow.value = _filterFlow.value.copy(type = type)
    }

    fun fetchCollabs(type: String = "All", query: String = "") {
        _filterFlow.value = CollabFilter(type = type, query = query)
    }

    fun fetchUserCollabs(userId: String) {
        viewModelScope.launch {
            _userCollabsState.value = UiState.Loading
            repository.getCollabsByUserId(userId).fold(
                onSuccess = { _userCollabsState.value = UiState.Success(it) },
                onFailure = { _userCollabsState.value = UiState.Error(it.message ?: "Failed to fetch user collabs") }
            )
        }
    }

    fun getCollabsByUserId(userId: String) {
        fetchUserCollabs(userId)
    }

    fun getJoinedCollabsByUserId(userId: String) {
        fetchJoinedCollabs(userId)
    }

    fun fetchJoinedCollabs(userId: String) {
        viewModelScope.launch {
            _joinedCollabsState.value = UiState.Loading
            repository.getJoinedCollabsByUserId(userId).fold(
                onSuccess = { _joinedCollabsState.value = UiState.Success(it) },
                onFailure = { _joinedCollabsState.value = UiState.Error(it.message ?: "Failed to fetch joined collabs") }
            )
        }
    }

    fun fetchCollabById(collabId: String) {
        viewModelScope.launch {
            _selectedCollabState.value = UiState.Loading
            repository.getCollabById(collabId).fold(
                onSuccess = { _selectedCollabState.value = UiState.Success(it) },
                onFailure = { _selectedCollabState.value = UiState.Error(it.message ?: "Failed to fetch collab details") }
            )
        }
    }

    fun updateCollabStatus(collabId: String, status: ProjectStatus) {
        viewModelScope.launch {
            _updateProjectStatusState.value = UiState.Loading
            repository.updateProjectStatus(collabId, status).fold(
                onSuccess = {
                    _updateProjectStatusState.value = UiState.Success(it)
                    fetchCollabById(collabId)
                },
                onFailure = {
                    _updateProjectStatusState.value = UiState.Error(it.message ?: "Failed to update project status")
                }
            )
        }
    }

    fun updateProjectStatus(collabId: String, status: ProjectStatus) {
        updateCollabStatus(collabId, status)
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
                    description = it.description.trim()
                )
            }
            val totalNeeded = roles.size.coerceAtLeast(1)

            val reqList = roles.map { it.roleName.trim() }.distinct().filter { it.isNotBlank() }

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
                onSuccess = { updatedCollab ->
                    _updateCollabState.value = UiState.Success(updatedCollab)
                    _selectedCollabState.value = UiState.Success(updatedCollab)
                },
                onFailure = { error ->
                    _updateCollabState.value = UiState.Error(error.localizedMessage ?: "Failed to update project")
                }
            )
        }
    }

    fun resetUpdateCollabState() {
        _updateCollabState.value = UiState.Idle
    }

    fun resetUpdateProjectStatusState() {
        _updateProjectStatusState.value = UiState.Idle
    }

    private val _connectState = MutableStateFlow<UiState<CollabRequestStatus>>(UiState.Idle)
    val connectState = _connectState.asStateFlow()

    private val _hasRequested = MutableStateFlow<UiState<CollabRequestStatus>>(UiState.Idle)
    val hasRequested = _hasRequested.asStateFlow()

    private val _collabRequestsState = MutableStateFlow<UiState<List<RequestUiState>>>(UiState.Idle)
    val collabRequestsState = _collabRequestsState.asStateFlow()

    private val _collabMembersState = MutableStateFlow<UiState<List<CollabMemberResponse>>>(UiState.Idle)
    val collabMembersState = _collabMembersState.asStateFlow()

    private val _removeMemberState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val removeMemberState = _removeMemberState.asStateFlow()

    fun connectToCollab(collabId: String) {
        val selectedCollab = (_selectedCollabState.value as? UiState.Success)?.data
        if (selectedCollab != null && selectedCollab.status != ProjectStatus.LOOKING) {
            _connectState.value = UiState.Error("Project is not accepting applications (${selectedCollab.status.displayName})")
            return
        }
        viewModelScope.launch {
            _connectState.value = UiState.Loading
            repository.requestToCollab(collabId).fold(
                onSuccess = { status ->
                    _connectState.value = UiState.Success(status)
                    _hasRequested.value = UiState.Success(status)
                },
                onFailure = { error ->
                    _connectState.value = UiState.Error(error.message ?: "Failed to connect to collab")
                }
            )
        }
    }

    fun hasRequested(collabId: String) {
        viewModelScope.launch {
            _hasRequested.value = UiState.Loading
            repository.hasAlreadyApplied(collabId).fold(
                onSuccess = { status ->
                    _hasRequested.value = UiState.Success(status)
                },
                onFailure = { error ->
                    _hasRequested.value = UiState.Error(error.message ?: "Failed to check application status")
                }
            )
        }
    }

    fun fetchCollabRequests(collabId: String) {
        viewModelScope.launch {
            _collabRequestsState.value = UiState.Loading
            repository.getCollabRequests(collabId).fold(
                onSuccess = { requests ->
                    val uiList = requests.map { RequestUiState(request = it) }
                    _collabRequestsState.value = UiState.Success(uiList)
                },
                onFailure = { error ->
                    _collabRequestsState.value = UiState.Error(error.message ?: "Failed to fetch requests")
                }
            )
        }
    }

    fun updateCollabRequestStatus(requestId: String, collabId: String, status: CollabRequestStatus) {
        viewModelScope.launch {
            _collabRequestsState.value = UiState.Loading
            repository.updateCollabRequestStatus(requestId, status.name).fold(
                onSuccess = {
                    fetchCollabRequests(collabId)
                    fetchCollabMembers(collabId)
                },
                onFailure = { error ->
                    _collabRequestsState.value = UiState.Error(error.message ?: "Failed to update request status")
                }
            )
        }
    }

    fun resetConnectState() {
        _connectState.value = UiState.Idle
    }

    fun fetchCollabMembers(collabId: String) {
        viewModelScope.launch {
            _collabMembersState.value = UiState.Loading
            repository.getCollabMembers(collabId).fold(
                onSuccess = { members ->
                    _collabMembersState.value = UiState.Success(members)
                },
                onFailure = { error ->
                    _collabMembersState.value = UiState.Error(error.message ?: "Failed to fetch members")
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

    private val _feedbackState = MutableStateFlow<UiState<List<CollabFeedback>>>(UiState.Idle)
    val feedbackState: StateFlow<UiState<List<CollabFeedback>>> = _feedbackState.asStateFlow()

    private val _addFeedbackState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val addFeedbackState: StateFlow<UiState<Unit>> = _addFeedbackState.asStateFlow()

    private val _replyingTarget = MutableStateFlow<ReplyingTarget?>(null)
    val replyingTarget: StateFlow<ReplyingTarget?> = _replyingTarget.asStateFlow()

    fun setReplyingTarget(target: ReplyingTarget?) {
        _replyingTarget.value = target
    }

    fun fetchFeedback(collabId: String) {
        viewModelScope.launch {
            _feedbackState.value = UiState.Loading
            feedbackRepository.getCollabFeedback(collabId).fold(
                onSuccess = { _feedbackState.value = UiState.Success(it) },
                onFailure = { _feedbackState.value = UiState.Error(it.message ?: "Failed to load feedback") }
            )
        }
    }

    fun addFeedback(collabId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _addFeedbackState.value = UiState.Loading
            feedbackRepository.addCollabFeedback(collabId, content).fold(
                onSuccess = {
                    _addFeedbackState.value = UiState.Success(Unit)
                    _replyingTarget.value = null
                    fetchFeedback(collabId)
                },
                onFailure = {
                    _addFeedbackState.value = UiState.Error(it.message ?: "Failed to submit feedback")
                }
            )
        }
    }

    fun replyToFeedback(collabId: String, feedbackId: String, replyText: String) {
        if (replyText.isBlank()) return
        viewModelScope.launch {
            _addFeedbackState.value = UiState.Loading
            feedbackRepository.replyToCollabFeedback(collabId, feedbackId, replyText).fold(
                onSuccess = {
                    _addFeedbackState.value = UiState.Success(Unit)
                    _replyingTarget.value = null
                    fetchFeedback(collabId)
                },
                onFailure = { error ->
                    _addFeedbackState.value = UiState.Error(error.message ?: "Failed to reply to feedback")
                }
            )
        }
    }

    fun resetAddFeedbackState() {
        _addFeedbackState.value = UiState.Idle
    }
}
