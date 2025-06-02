package com.iota.campusX.Feature.UserProfile.domain

import android.net.Uri
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Feature.UserProfile.data.BasicProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Gender
import kotlinx.coroutines.flow.Flow

interface UserProfileRepo {

    suspend fun getBaseProfile():Flow<ResultState<BasicProfileDTO>>

    suspend fun getUserProfileById(userId:String):Flow<ResultState<BasicProfileDTO>>

    fun deleteAccount():Flow<ResultState<Boolean>>

    fun updateUserName(userName:String):Flow<ResultState<Boolean>>

    fun updateSocialAccounts(accounts:String):Flow<ResultState<Boolean>>

    fun updateAbout(about:String):Flow<ResultState<Boolean>>

    fun updateGender(gender: Gender):Flow<ResultState<Boolean>>

    fun updateInterests(interests:List<String>):Flow<ResultState<Boolean>>

    fun updateProfileImage(imageUri:Uri):Flow<ResultState<Boolean>>

    fun updateUniversity(title: String): Flow<ResultState<List<UniversityDTO>>>

    fun updateCampus(campus: Campus): Flow<ResultState<Boolean>>

    fun sendLinkUpRequest(requestUserId: String,currentState: Boolean? = null): Flow<ResultState<Boolean>>

    fun acceptLinkUpRequest(requestUserId: String): Flow<ResultState<Boolean>>

    fun rejectLinkUpRequest(requestUserId: String): Flow<ResultState<Boolean>>

    fun getConnectionsCount(userId: String):Flow<ResultState<Int>>

    fun getConnections(userId: String):Flow<ResultState<List<ConnectionsDTO>>>

}