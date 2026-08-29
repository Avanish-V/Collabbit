package com.iota.campusX.Feature.UserProfile.domain.repository

import com.iota.campusX.Feature.UserProfile.data.remote.Request.BasicDetailsRequest
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {

    suspend fun syncUserProfile() : Result<ProfileResponse>

    fun observeProfile(): Flow<ProfileResponse>
    suspend fun getUserProfileById(userId:String): Result<ProfileResponse>
    suspend fun deleteAccount(): Result<Boolean>

    suspend fun updateSkills(skills:List<SkillResponse>): Result<Boolean>
    suspend fun updateSummary(summary:String): Result<Boolean>
    suspend fun updateCampus(campus: Education): Result<Boolean>
    suspend fun updateProfile(basicDetailsRequest: BasicDetailsRequest): Result<Unit>

    suspend fun updateFcmToken(token: String): Result<Unit>
}
