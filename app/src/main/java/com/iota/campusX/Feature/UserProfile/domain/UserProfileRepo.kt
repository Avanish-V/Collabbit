package com.iota.campusX.Feature.UserProfile.domain

import android.net.Uri
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Gender
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.Flow

interface UserProfileRepo {

    suspend fun getBaseProfile(): Result<BaseProfileDTO>

    suspend fun getUserProfileById(userId:String): Result<BaseProfileDTO>

    suspend fun deleteAccount(): Result<Boolean>

    suspend fun updateUserName(userName:String): Result<Boolean>

    suspend fun updateSocialAccounts(accounts:String): Result<Boolean>

    suspend fun updateAbout(about:String): Result<Boolean>

    suspend fun updateGender(gender: Gender): Result<Boolean>

    suspend fun updateInterests(interests:List<String>): Result<Boolean>

    suspend fun updateProfileImage(imageUri:Uri): Result<Boolean>

    suspend fun updateCampus(campus: Campus): Result<Boolean>

    suspend fun sendLinkUpRequest(requestUserId: String,currentState: Boolean?): Result<Boolean>

    suspend fun acceptLinkUpRequest(requestUserId: String): Result<Boolean>

    suspend fun rejectLinkUpRequest(requestUserId: String): Result<Boolean>

    suspend fun getConnectionsCount(userId: String): Result<Int>

    suspend fun getConnections(userId: String): Result<List<ConnectionsDTO>>

    suspend fun hasConnection(userId: String): Result<Boolean?>

    fun updateUniversity(title: String): Flow<UiState<List<UniversityDTO>>>

}