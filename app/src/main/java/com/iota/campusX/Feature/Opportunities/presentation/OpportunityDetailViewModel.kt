package com.iota.campusX.Feature.Opportunities.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityApplicationRequest
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityResponse
import com.iota.campusX.Feature.Opportunities.domain.usecase.ApplyForOpportunityUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetOpportunityDetailUseCase
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class OpportunityUiEvent {
    object ApplySuccess : OpportunityUiEvent()
    data class Error(val message: String) : OpportunityUiEvent()
}

class OpportunityDetailViewModel(
    private val getOpportunityDetailUseCase: GetOpportunityDetailUseCase,
    private val applyForOpportunityUseCase: ApplyForOpportunityUseCase,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _opportunityState = MutableStateFlow<UiState<OpportunityResponse>>(UiState.Idle)
    val opportunityState = _opportunityState.asStateFlow()

    private val _applicationLoading = MutableStateFlow(false)
    val applicationLoading = _applicationLoading.asStateFlow()

    private val _event = MutableSharedFlow<OpportunityUiEvent>()
    val event = _event.asSharedFlow()

    fun fetchOpportunityDetail(id: String) {
        viewModelScope.launch {
            _opportunityState.value = UiState.Loading
            getOpportunityDetailUseCase(id).fold(
                onSuccess = {
                    _opportunityState.value = UiState.Success(it)
                },
                onFailure = {
                    _opportunityState.value = UiState.Error(it.message ?: "An unknown error occurred")
                }
            )
        }
    }

    fun applyForOpportunity(opportunityId: String, resumeUrl: String? = null, coverLetter: String? = null) {
        viewModelScope.launch {
            _applicationLoading.value = true
            val profile = userProfileRepository.observeProfile().first()
            val request = OpportunityApplicationRequest(
                externalUserId = profile.uid,
                guestName = profile.baseProfile.name,
                guestEmail = profile.contact.email,
                guestResumeUrl = resumeUrl,
                guestCoverLetter = coverLetter
            )
            applyForOpportunityUseCase(opportunityId, request).fold(
                onSuccess = {
                    _applicationLoading.value = false
                    _event.emit(OpportunityUiEvent.ApplySuccess)
                },
                onFailure = {
                    _applicationLoading.value = false
                    _event.emit(OpportunityUiEvent.Error(it.message ?: "Application failed"))
                }
            )
        }
    }
}
