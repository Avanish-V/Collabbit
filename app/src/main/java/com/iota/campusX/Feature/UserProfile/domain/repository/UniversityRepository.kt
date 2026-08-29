package com.iota.campusX.Feature.UserProfile.domain.repository

import com.iota.campusX.Feature.UserProfile.data.remote.response.CollegeResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.Flow

interface UniversityRepository {
    fun updateUniversity(title: String): Flow<CollegeResponse>

    fun searchKeySkills(query: String): Flow<List<SkillResponse>>
}
