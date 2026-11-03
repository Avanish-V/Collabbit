package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditEducation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.remote.response.College
import com.iota.campusX.Feature.UserProfile.data.remote.response.CollegeResponse
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import com.iota.campusX.Feature.UserProfile.domain.repository.UniversityRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.core.common.UiState
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar
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

        // Academic Period (Duration / Year) Validation
        val startStr = education.start.trim()
        val endStr = education.end.trim()

        if (startStr.isBlank()) {
            _uiState.value = UiState.Error("Please enter a start year")
            return
        }

        if (endStr.isBlank()) {
            _uiState.value = UiState.Error("Please enter an end year")
            return
        }

        val startYear = startStr.toIntOrNull()
        if (startYear == null || startStr.length != 4) {
            _uiState.value = UiState.Error("Start year must be a 4-digit year (e.g. 2021)")
            return
        }

        val endYear = endStr.toIntOrNull()
        if (endYear == null || endStr.length != 4) {
            _uiState.value = UiState.Error("End year must be a 4-digit year (e.g. 2025)")
            return
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        if (startYear < 1960 || startYear > currentYear + 1) {
            _uiState.value = UiState.Error("Please enter a realistic start year (1960 - ${currentYear + 1})")
            return
        }

        if (endYear < startYear) {
            _uiState.value = UiState.Error("End year cannot be earlier than start year")
            return
        }

        val duration = endYear - startYear
        if (duration > 10) {
            _uiState.value = UiState.Error("Education duration cannot exceed 10 years")
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
        val filtered = start.filter { it.isDigit() }.take(4)
        _editingEducation.value = _editingEducation.value?.copy(start = filtered)
    }

    fun setEnd(end: String) {
        val filtered = end.filter { it.isDigit() }.take(4)
        _editingEducation.value = _editingEducation.value?.copy(end = filtered)
    }

    fun clearSearchResults() {
        _universityData.value = UiState.Idle
    }
}
