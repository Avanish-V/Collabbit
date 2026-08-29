package com.iota.campusX.Feature.Collab.presentation

import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.CollabType
import com.iota.campusX.Feature.Collab.data.model.CreateCollabRequest
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateCollabViewModel: ViewModel() {
    private val _createCollabState = MutableStateFlow<UiState<CollabResponse>>(UiState.Idle)
    val createCollabState = _createCollabState.asStateFlow()

    private val _collabDraftState = MutableStateFlow<CreateCollabRequest>(
        CreateCollabRequest(
            title = "",
            description = "",
            collabType = CollabType.NONE,
            requirements = emptyList(),
            participantsNeeded = 1
        )
    )
    val collabDraftState = _collabDraftState.asStateFlow()

    fun setCollabType(type: CollabType){
        _collabDraftState.value = _collabDraftState.value.copy(collabType = type)
    }

    fun setCollabTitle(title: String){
        _collabDraftState.value = _collabDraftState.value.copy(title = title)
    }

    fun setCollabDescription(description: String){
        _collabDraftState.value = _collabDraftState.value.copy(description = description)
    }

    fun setCollabRequirements(requirements: List<String>){
        _collabDraftState.value = _collabDraftState.value.copy(requirements = requirements)
    }

    fun setCollabParticipantsNeeded(participantsNeeded: Int){
        _collabDraftState.value = _collabDraftState.value.copy(participantsNeeded = participantsNeeded)
    }

    fun setDeadline(deadline: Long?){
        _collabDraftState.value = _collabDraftState.value.copy(deadline = deadline)
    }

    fun setCollabUrl(url: String){
        _collabDraftState.value = _collabDraftState.value.copy(projectUrl = url)
    }

    fun resetDraft() {
        _collabDraftState.value = CreateCollabRequest(
            title = "",
            description = "",
            collabType = CollabType.NONE,
            requirements = emptyList(),
            participantsNeeded = 1
        )
    }
}