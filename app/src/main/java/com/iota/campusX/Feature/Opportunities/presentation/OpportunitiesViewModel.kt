package com.iota.campusX.Feature.Opportunities.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityResponse
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetCoursesUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetOpportunitiesUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OpportunitiesViewModel(
    private val getOpportunitiesUseCase: GetOpportunitiesUseCase,
    private val getCoursesUseCase: GetCoursesUseCase
) : ViewModel() {

    private val _opportunitiesState = MutableStateFlow<UiState<List<OpportunityResponse>>>(UiState.Idle)
    val opportunitiesState = _opportunitiesState.asStateFlow()

    private val _allOpportunities = MutableStateFlow<List<OpportunityResponse>>(emptyList())

    private val _coursesState = MutableStateFlow<UiState<List<CourseResponse>>>(UiState.Idle)
    val coursesState = _coursesState.asStateFlow()

    var selectedTabIndex = 0
    private var hasInitialFetch = false

    fun fetchOpportunities(type: String, forceRefresh: Boolean = false) {
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
}
