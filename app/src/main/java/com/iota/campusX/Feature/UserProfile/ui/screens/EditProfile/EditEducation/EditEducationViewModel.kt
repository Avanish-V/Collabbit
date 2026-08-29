package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditEducation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.remote.response.College
import com.iota.campusX.Feature.UserProfile.data.remote.response.CollegeResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import com.iota.campusX.Feature.UserProfile.domain.repository.UniversityRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class EditEducationViewModel(
    private val profileRepository: UserProfileRepository,
    private val universityRepository: UniversityRepository
) : ViewModel() {

    val fieldsOfStudy = listOf(
        "Computer Science & Engineering",
        "Mechanical Engineering",
        "Civil Engineering",
        "Electrical Engineering",
        "Electronics & Communication",
        "Information Technology",
        "Business Administration",
        "Law",
        "Medicine",
        "Pharmacy",
        "Data Science",
        "Cybersecurity"
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _editingEducation = MutableStateFlow<Education?>(Education("", "", "", "", "", ""))
    val editingEducation: StateFlow<Education?> = _editingEducation.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _universityData = MutableStateFlow<UiState<CollegeResponse>>(UiState.Idle)
    val universityData: StateFlow<UiState<CollegeResponse>> = _universityData.asStateFlow()

    private val _selectedCollege = MutableStateFlow<College?>(null)
    val selectedCollege: StateFlow<College?> = _selectedCollege.asStateFlow()

    private var isSelectionFromDropdown = false

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500.milliseconds)
                .filter { query ->
                    val shouldSearch = query.isNotBlank() && query.length > 2 && !isSelectionFromDropdown
                    isSelectionFromDropdown = false // Reset for next change
                    shouldSearch
                }
                .distinctUntilChanged()
                .onEach { _universityData.value = UiState.Loading }
                .flatMapLatest { query ->
                    universityRepository.updateUniversity(query)
                        .catch { e ->
                            Log.e("UniversitySearch", "Inner flow error: $e")
                            emit(CollegeResponse(emptyList()))
                        }
                }
                .collect { result ->
                    Log.d("UniversitySearch", "searchQuery results: $result")
                    _universityData.value = UiState.Success(result)
                }
        }
    }

    fun educationUpdate() {
        val education = _editingEducation.value ?: return
        
        // Basic validation
        if (education.college.isBlank()) {
            _uiState.value = UiState.Error("Please select or enter your college")
            return
        }
        if (education.course.isBlank()) {
            _uiState.value = UiState.Error("Please enter your degree / course")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = profileRepository.updateCampus(education)
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success(Unit)
                },
                onFailure = {
                    _uiState.value = UiState.Error(it.message ?: "Failed to update education")
                }
            )
        }
    }

    fun onUniversityQueryChanged(query: String) {
        isSelectionFromDropdown = false
        _searchQuery.value = query
        _editingEducation.value = _editingEducation.value?.copy(college = query)
    }

    fun onCollegeSelect(college: College?) {
        isSelectionFromDropdown = true
        _selectedCollege.value = college
        _editingEducation.value = _editingEducation.value?.copy(college = college?.name ?: "")
        _searchQuery.value = college?.name ?: ""
        _universityData.value = UiState.Idle // Clear results after selection
    }

    fun setEducation(education: Education) {
        isSelectionFromDropdown = true
        _editingEducation.value = education
        _selectedCollege.value = College(name = education.college, domain = "")
        _searchQuery.value = education.college
    }

    fun setCgpa(cgpa: String) {
        _editingEducation.value = _editingEducation.value?.copy(cgpa = cgpa)
    }

    fun setSpecialization(specialization: String) {
        _editingEducation.value = _editingEducation.value?.copy(specialization = specialization)
    }

    fun setCourse(course: String) {
        _editingEducation.value = _editingEducation.value?.copy(course = course)
    }

    fun setStart(start: String) {
        _editingEducation.value = _editingEducation.value?.copy(start = start)
    }

    fun setEnd(end: String) {
        _editingEducation.value = _editingEducation.value?.copy(end = end)
    }

    fun clearSearchResults() {
        _universityData.value = UiState.Idle
    }
}
