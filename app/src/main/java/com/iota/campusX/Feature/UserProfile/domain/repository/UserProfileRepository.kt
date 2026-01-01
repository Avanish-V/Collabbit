package com.iota.campusX.Feature.UserProfile.domain.repository

import android.net.Uri
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Campus
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Gender
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UniversityDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UpdateProfileDTO
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {

    suspend fun syncUserProfile() : Result<Unit>

     fun getUserProfile(): Flow<BaseProfileDTO?>

    suspend fun getBaseProfile(): Result<BaseProfileDTO>

    suspend fun getUserProfileById(userId:String): Result<BaseProfileDTO>

    suspend fun deleteAccount(): Result<Boolean>


    suspend fun updateInterests(interests:List<String>): Result<Boolean>

    suspend fun updateCampus(campus: Campus): Result<Boolean>

    suspend fun updateProfile(updateProfileDTO: UpdateProfileDTO): Result<Unit>




}