package com.iota.campusX.Feature.UserProfile.domain.repository

import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UniversityDTO
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.Flow

interface UniversityRepository {
    fun updateUniversity(title: String): Flow<UiState<List<UniversityDTO>>>
}
