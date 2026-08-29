package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditBaseProfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.remote.Request.BasicDetailsRequest
import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.Feature.UserProfile.domain.Model.Gender
import com.iota.campusX.Feature.UserProfile.domain.useCases.EditBaseProfileUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditBaseProfileViewModel(
    private val editBaseProfileUseCase: EditBaseProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _tagline = MutableStateFlow("")
    val tagline: StateFlow<String> = _tagline.asStateFlow()

    private val _gender = MutableStateFlow(Gender.UNSPECIFIED)
    val gender: StateFlow<Gender> = _gender.asStateFlow()

    private val _pickedImage = MutableStateFlow<Uri?>(null)
    val pickedImage: StateFlow<Uri?> = _pickedImage.asStateFlow()

    private var initialProfile: BaseProfile? = null

    fun setInitialData(profile: BaseProfile) {
        if (initialProfile == null) {
            initialProfile = profile
            _name.value = profile.name
            _tagline.value = profile.tagline
            _gender.value = profile.gender ?: Gender.UNSPECIFIED
        }
    }

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onTaglineChange(newTagline: String) {
        _tagline.value = newTagline
    }

    fun onGenderChange(newGender: Gender) {
        _gender.value = newGender
    }

    fun onImagePicked(uri: Uri?) {
        _pickedImage.value = uri
    }

    fun updateProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {

                val request = BasicDetailsRequest(
                    name = _name.value,
                    photo = initialProfile?.image ?:"",
                    tagline = _tagline.value,
                    gender = _gender.value
                )

                val result = editBaseProfileUseCase(request,pickedImage.value)
                result.fold(
                    onSuccess = {
                        _uiState.value = UiState.Success(Unit)
                    },
                    onFailure = {
                        _uiState.value = UiState.Error(it.message ?: "Failed to update profile")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
