package com.iota.campusX.Feature.UserProfile.domain

import android.net.Uri
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.Gender
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserProfileRepository (
    private val userProfileRepo: UserProfileRepo
){

    private val _currentUser = MutableStateFlow<BaseProfileDTO?>(null)
    val currentUser: StateFlow<BaseProfileDTO?> = _currentUser.asStateFlow()

    suspend fun loadCurrentUser() {
        val result = userProfileRepo.getBaseProfile() // returns Result<BaseProfileDTO>
        result.onSuccess { profile ->
            _currentUser.value = profile
        }.onFailure {
            _currentUser.value = null // or keep previous value
        }
    }

    fun updateNameLocally(name:String) {
        _currentUser.update { profile->
            profile?.copy(userName = name)
        }
    }

    fun updateAboutLocally(about:String) {
        _currentUser.update { profile->
            profile?.copy(userBio = about)
        }
    }

    fun updateGenderLocally(gender:Gender) {
        _currentUser.update { profile->
            profile?.copy(userGender = gender)
        }
    }

    fun updateInterestLocally(interests: List<String>){
        _currentUser.update { profile->
            profile?.copy(interests = interests)
        }
    }

    fun updateCampusLocally(campus: Campus){
        _currentUser.update { profile->
            profile?.copy(campus = campus)
        }
    }

    fun updateImageLocally(imageUri: Uri){
        _currentUser.update { profile->
            profile?.copy(userImage = imageUri.toString())
        }
    }

}