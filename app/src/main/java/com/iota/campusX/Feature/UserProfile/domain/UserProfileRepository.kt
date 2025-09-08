package com.iota.campusX.Feature.UserProfile.domain

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.UserProfile.OfflineSupport.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.Gender
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import toDomain

class UserProfileRepository (
    private val userProfileRepo: UserProfileInterface,
    private val userProfileDao: UserProfileDao
){

    private val _currentUser = MutableStateFlow<UiState<BaseProfileDTO>>(UiState.Idle)
    val currentUser: StateFlow<UiState<BaseProfileDTO>> = _currentUser.asStateFlow()


    suspend fun loadCurrentUser() {
        _currentUser.value = UiState.Loading
        userProfileDao.getProfile(FirebaseAuth.getInstance().currentUser!!.uid).collect{it->
           if (it != null){
               _currentUser.value = UiState.Success(it.toDomain())
           }else{
               _currentUser.value = UiState.Error("User not found")
           }
        }
    }


    fun updateNameLocally(name: String) {
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(userName = name))
        }
    }

    fun updateAboutLocally(about:String) {
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(userBio = about))
        }
    }

    fun updateGenderLocally(gender:Gender) {
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(userGender = gender))
        }
    }

    fun updateInterestLocally(interests: List<String>){
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(interests = interests))
        }
    }

    fun updateCampusLocally(campus: Campus){
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(campus = campus))
        }
    }

    fun updateImageLocally(imageUri: Uri){
        (_currentUser.value as? UiState.Success)?.data?.let { user ->
            _currentUser.value = UiState.Success(user.copy(userImage = imageUri.toString()))
        }
    }

}