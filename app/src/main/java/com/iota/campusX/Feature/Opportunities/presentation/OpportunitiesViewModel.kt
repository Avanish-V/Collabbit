package com.iota.campusX.Feature.Opportunities.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
 import com.iota.campusX.Feature.Opportunities.data.model.CourseEnrollmentRequest
import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.Feature.Opportunities.data.model.ModuleResponse
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityApplicationRequest
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityResponse
import com.iota.campusX.Feature.Opportunities.domain.usecase.ApplyForOpportunityUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.EnrollInCourseUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetCourseDetailUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetCourseModulesUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetCoursesUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetOpportunitiesUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetOpportunityDetailUseCase
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OpportunitiesViewModel(
    private val getOpportunitiesUseCase: GetOpportunitiesUseCase,
    private val getOpportunityDetailUseCase: GetOpportunityDetailUseCase,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val getCourseModulesUseCase: GetCourseModulesUseCase,
    private val enrollInCourseUseCase: EnrollInCourseUseCase,
    private val applyForOpportunityUseCase: ApplyForOpportunityUseCase,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _opportunitiesState = MutableStateFlow<UiState<List<OpportunityResponse>>>(UiState.Idle)
    val opportunitiesState = _opportunitiesState.asStateFlow()

    private val _allOpportunities = MutableStateFlow<List<OpportunityResponse>>(emptyList())

    private val _coursesState = MutableStateFlow<UiState<List<CourseResponse>>>(UiState.Idle)
    val coursesState = _coursesState.asStateFlow()

    private val _selectedOpportunityState = MutableStateFlow<UiState<OpportunityResponse>>(UiState.Idle)
    val selectedOpportunityState = _selectedOpportunityState.asStateFlow()

    private val _selectedCourseState = MutableStateFlow<UiState<CourseResponse>>(UiState.Idle)
    val selectedCourseState = _selectedCourseState.asStateFlow()

    private val _courseModulesState = MutableStateFlow<UiState<List<ModuleResponse>>>(UiState.Idle)
    val courseModulesState = _courseModulesState.asStateFlow()

    private val _enrollmentState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val enrollmentState = _enrollmentState.asStateFlow()

    private val _applicationState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val applicationState = _applicationState.asStateFlow()

    var selectedTabIndex = 0
    private var hasInitialFetch = false

    fun fetchOpportunities(type: String, forceRefresh: Boolean = false) {
        // If we already have data and it's not a force refresh, just filter the existing data
        if (!forceRefresh && hasInitialFetch && _allOpportunities.value.isNotEmpty()) {
            filterOpportunitiesByType(type)
            return
        }

        viewModelScope.launch {
            if (_opportunitiesState.value !is UiState.Success || forceRefresh) {
                _opportunitiesState.value = UiState.Loading
            }
            getOpportunitiesUseCase().fold(
                onSuccess = { allOpportunities ->
                    _allOpportunities.value = allOpportunities
                    hasInitialFetch = true
                    filterOpportunitiesByType(type)
                },
                onFailure = {
                    _opportunitiesState.value = UiState.Error(it.message ?: "An unknown error occurred")
                    hasInitialFetch = false
                }
            )
        }
    }

    private fun filterOpportunitiesByType(type: String) {
        val filtered = _allOpportunities.value.filter { opportunity ->
            opportunity.type.equals(type, ignoreCase = true)
        }
        _opportunitiesState.value = UiState.Success(filtered)
    }

    fun fetchCourses(forceRefresh: Boolean = false) {
        if (!forceRefresh && _coursesState.value is UiState.Success) {
            return
        }

        viewModelScope.launch {
            if (_coursesState.value !is UiState.Success || forceRefresh) {
                _coursesState.value = UiState.Loading
            }
            getCoursesUseCase().fold(
                onSuccess = {
                    _coursesState.value = UiState.Success(it)
                },
                onFailure = {
                    _coursesState.value = UiState.Error(it.message ?: "An unknown error occurred")
                }
            )
        }
    }

    fun fetchOpportunityDetail(id: String) {
        // First check if we already have it in the list to avoid refetching
        val currentList = (_opportunitiesState.value as? UiState.Success)?.data
        val existingOpportunity = currentList?.find { it.id == id }

        if (existingOpportunity != null) {
            _selectedOpportunityState.value = UiState.Success(existingOpportunity)
            return
        }

        // If not found in the current list state, then fetch from server
        viewModelScope.launch {
            _selectedOpportunityState.value = UiState.Loading
            getOpportunityDetailUseCase(id).fold(
                onSuccess = {
                    _selectedOpportunityState.value = UiState.Success(it)
                },
                onFailure = {
                    _selectedOpportunityState.value = UiState.Error(it.message ?: "An unknown error occurred")
                }
            )
        }
    }

    fun fetchCourseDetail(id: String) {
        // First check if we already have it in the list
        val currentList = (_coursesState.value as? UiState.Success)?.data
        val existingCourse = currentList?.find { it.id == id }

        if (existingCourse != null) {
            _selectedCourseState.value = UiState.Success(existingCourse)
            fetchCourseModules(id)
            return
        }

        viewModelScope.launch {
            _selectedCourseState.value = UiState.Loading
            getCourseDetailUseCase(id).fold(
                onSuccess = {
                    _selectedCourseState.value = UiState.Success(it)
                    fetchCourseModules(id)
                },
                onFailure = {
                    _selectedCourseState.value = UiState.Error(it.message ?: "An unknown error occurred")
                }
            )
        }
    }

    fun fetchCourseModules(courseId: String) {
        viewModelScope.launch {
            _courseModulesState.value = UiState.Loading
            getCourseModulesUseCase(courseId).fold(
                onSuccess = {
                    _courseModulesState.value = UiState.Success(it)
                },
                onFailure = {
                    _courseModulesState.value = UiState.Error(it.message ?: "An unknown error occurred")
                }
            )
        }
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _enrollmentState.value = UiState.Loading
            val profile = userProfileRepository.observeProfile().first()
            val request = CourseEnrollmentRequest(
                externalUserId = profile.uid,
                name = profile.baseProfile.name,
                email = profile.contact.email
            )
            enrollInCourseUseCase(courseId, request).fold(
                onSuccess = {
                    _enrollmentState.value = UiState.Success(Unit)
                },
                onFailure = {
                    Log.e("OpportunitiesViewModel", "Enrollment failed: ${it.message}")
                    _enrollmentState.value = UiState.Error(it.message ?: "Enrollment failed")
                }
            )
        }
    }

    fun applyForOpportunity(opportunityId: String, resumeUrl: String? = null, coverLetter: String? = null) {
        viewModelScope.launch {
            _applicationState.value = UiState.Loading
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
                    _applicationState.value = UiState.Success(Unit)
                },
                onFailure = {
                    _applicationState.value = UiState.Error(it.message ?: "Application failed")
                }
            )
        }
    }

    fun resetEnrollmentState() {
        _enrollmentState.value = UiState.Idle
    }

    fun resetApplicationState() {
        _applicationState.value = UiState.Idle
    }
}
