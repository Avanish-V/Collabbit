package com.iota.campusX.Feature.Opportunities.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Opportunities.data.model.CourseEnrollmentRequest
import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.Feature.Opportunities.domain.usecase.EnrollInCourseUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetCourseDetailUseCase
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Feature.UserProfile.utils.ProfileScoreCalculator
import com.iota.campusX.core.common.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class CourseUiEvent {
    object EnrollSuccess : CourseUiEvent()
    data class ProfileIncomplete(val score: Int) : CourseUiEvent()
    data class Error(val message: String) : CourseUiEvent()
}

class CourseDetailViewModel(
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val enrollInCourseUseCase: EnrollInCourseUseCase,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _courseState = MutableStateFlow<UiState<CourseResponse>>(UiState.Idle)
    val courseState = _courseState.asStateFlow()

    private val _enrollmentLoading = MutableStateFlow(false)
    val enrollmentLoading = _enrollmentLoading.asStateFlow()

    private val _event = MutableSharedFlow<CourseUiEvent>()
    val event = _event.asSharedFlow()

    fun fetchCourseDetail(id: String) {
        viewModelScope.launch {
            _courseState.value = UiState.Loading
            getCourseDetailUseCase(id).fold(
                onSuccess = { course ->
                    _courseState.value = UiState.Success(course)
                },
                onFailure = {
                    _courseState.value = UiState.Error(it.message ?: "An unknown error occurred")
                }
            )
        }
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _enrollmentLoading.value = true
            try {
                val profile = userProfileRepository.observeProfile().first()
                val completionScore = ProfileScoreCalculator.calculateScore(profile)

                if (completionScore < 80) {
                    _enrollmentLoading.value = false
                    _event.emit(CourseUiEvent.ProfileIncomplete(completionScore))
                    return@launch
                }

                val request = CourseEnrollmentRequest(
                    externalUserId = profile.uid,
                    name = profile.baseProfile.name,
                    email = profile.contact.email,
                    photo = profile.baseProfile.image ?: ""
                )
                enrollInCourseUseCase(courseId, request).fold(
                    onSuccess = {
                        _enrollmentLoading.value = false
                        _event.emit(CourseUiEvent.EnrollSuccess)
                    },
                    onFailure = {
                        _enrollmentLoading.value = false
                        _event.emit(CourseUiEvent.Error(it.message ?: "Enrollment failed"))
                    }
                )
            } catch (e: Exception) {
                _enrollmentLoading.value = false
                _event.emit(CourseUiEvent.Error(e.message ?: "An error occurred during enrollment"))
            }
        }
    }
}
