package com.iota.campusX.Feature.UserProfile.domain

import android.net.Uri
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.LinkUpRequestDTO
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Feature.UserProfile.data.UserBasicProfileDTO
import kotlinx.coroutines.flow.Flow

interface UserProfileRepo {

    suspend fun getBaseProfile():Flow<ResultState<UserBasicProfileDTO>>

    suspend fun getUserProfileById(userId:String):Flow<ResultState<UserBasicProfileDTO>>

    fun deleteAccount():Flow<ResultState<Boolean>>

    fun updateUserName(userName:String):Flow<ResultState<Boolean>>

    fun updateSocialAccounts(accounts:String):Flow<ResultState<Boolean>>

    fun updateAbout(about:String):Flow<ResultState<Boolean>>

    fun updateGender(gender:String):Flow<ResultState<Boolean>>

    fun updateInterests(interests:List<String>):Flow<ResultState<Boolean>>

    fun updateProfileImage(imageUri:Uri):Flow<ResultState<Boolean>>

    fun updateUniversity(title: String): Flow<ResultState<List<UniversityDTO>>>

    fun updateCampus(campus: Campus): Flow<ResultState<Boolean>>

    fun sendLinkUpRequest(requestUserId: String,currentState: Boolean? = null): Flow<ResultState<Boolean>>

    fun acceptLinkUpRequest(requestUserId: String): Flow<ResultState<Boolean>>

    fun rejectLinkUpRequest(requestUserId: String): Flow<ResultState<Boolean>>



}