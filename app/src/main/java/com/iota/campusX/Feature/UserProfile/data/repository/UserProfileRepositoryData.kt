package com.iota.campusX.Feature.UserProfile.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.UserProfile.data.local.database.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Campus
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Gender
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.SetCampus
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import toDomain

class UserProfileRepositoryData (
    private val userProfileRepo: UserProfileRepository,
    private val userProfileDao: UserProfileDao
){

    private val _currentUser = MutableStateFlow<UiState<BaseProfileDTO>>(UiState.Idle)
    val currentUser: StateFlow<UiState<BaseProfileDTO>> = _currentUser.asStateFlow()


    suspend fun loadCurrentUser() {
        _currentUser.value = UiState.Loading
        userProfileDao.getProfile(FirebaseAuth.getInstance().currentUser!!.uid).collect{ it->
           if (it != null){
               _currentUser.value = UiState.Success(it.toDomain())
           }else{
               _currentUser.value = UiState.Error("User not found")
           }
        }
    }


    suspend fun updateNameLocally(name: String) {
        userProfileDao.updateName(
            FirebaseAuth.getInstance().currentUser!!.uid,
            name
        )
    }

    suspend fun updateTaglineLocally(tagline: String) {
        userProfileDao.updateTagline(
            FirebaseAuth.getInstance().currentUser!!.uid,
            tagline
        )

    }

    fun updateAboutLocally(about:String) {
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(about = about))
        }
    }

    fun updateGenderLocally(gender: Gender) {
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(gender = gender))
        }
    }

    fun updateInterestLocally(interests: List<String>){
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(interests = interests))
        }
    }

    suspend fun updateCampusLocally(campus: Campus){
       userProfileDao.updateCampus(campus, FirebaseAuth.getInstance().currentUser!!.uid)
    }

    fun updateImageLocally(imageUri: Uri){
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(image = imageUri.toString()))
        }
    }

}