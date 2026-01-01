package com.iota.campusX.Feature.UserProfile.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UniversityDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UpdateProfileDTO
import com.iota.campusX.Feature.UserProfile.data.repository.UserProfileRepositoryData
import com.iota.campusX.Feature.UserProfile.domain.repository.UniversityRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UpdateProfileViewModel(
    private val userProfileRepo: UserProfileRepository,
    private val userProfileRepository: UserProfileRepositoryData,
    private val universityRepository: UniversityRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state.asStateFlow()

    private val _events = Channel<Event>(Channel.Factory.BUFFERED)
    val events = _events.receiveAsFlow()


    private val searchQuery = MutableStateFlow("")

    private val _universityData = MutableStateFlow<UiState<List<UniversityDTO>>>(UiState.Idle)
    val universityData: StateFlow<UiState<List<UniversityDTO>>> = _universityData.asStateFlow()



    sealed class Event {
        data class ShowMessage(val message: String) : Event()
        object NavigateToBack : Event()
    }


    sealed class ProfileMutation {
        data class UpdateProfile(val updateProfileDTO: UpdateProfileDTO?) : ProfileMutation()
        data class Interests(val value: List<String>) : ProfileMutation()
        data class Campus(val value: com.iota.campusX.Feature.UserProfile.data.remote.dtos.Campus, val oldCampusId: String?) : ProfileMutation()
        data class ProfileImage(val uri: Uri) : ProfileMutation()
        object DeleteAccount : ProfileMutation()
    }

    fun modifyProfile(mutation: ProfileMutation) = viewModelScope.launch {
        // ✅ Pre-validation
        validate(mutation)?.let { validationError ->
            _state.value = UiState.Error(validationError)
            return@launch
        }

        _state.value = UiState.Loading

        when (mutation) {

            is ProfileMutation.UpdateProfile -> {

                if (mutation.updateProfileDTO == null) return@launch

                userProfileRepo.updateProfile(mutation.updateProfileDTO)
                    .fold(
                        onSuccess = {
                            mutation.updateProfileDTO.updateUserName?.let {
                                userProfileRepository.updateNameLocally(it)
                            }
                            mutation.updateProfileDTO.updateTagline?.let {
                                userProfileRepository.updateTaglineLocally(it)
                            }
                            mutation.updateProfileDTO.updateGender?.let {
                                userProfileRepository.updateGenderLocally(it)
                            }
                            mutation.updateProfileDTO.updateAbout?.let {
                                userProfileRepository.updateAboutLocally(it)
                            }
                            _state.value = UiState.Success(Unit)
                        },
                        onFailure = {
                            _state.value = UiState.Error(it.message.toString())
                        }
                    )

            }

            is ProfileMutation.Interests -> {
                userProfileRepo.updateInterests(mutation.value)
                userProfileRepository.updateInterestLocally(mutation.value)
            }

            is ProfileMutation.Campus -> {
                val result = userProfileRepo.updateCampus(mutation.value)
                result.fold(
                    onSuccess = {

                        userProfileRepository.updateCampusLocally(mutation.value)

                        if (mutation.oldCampusId == mutation.value.code){
                            mutation.value.code?.let { topic -> Firebase.messaging.subscribeToTopic(topic) }
                        }else{

                            mutation.oldCampusId?.let {
                                    topic -> Firebase.messaging.unsubscribeFromTopic(topic).addOnSuccessListener {}
                            }
                            mutation.value.code?.let {
                                    topic -> Firebase.messaging.subscribeToTopic(topic).addOnSuccessListener {}
                            }
                        }
                        _state.value = UiState.Success(Unit)
                    },
                    onFailure = {
                        _state.value = UiState.Error(parseError(it))
                    }
                )

            }

            is ProfileMutation.ProfileImage -> {
                //userProfileRepo.updateProfile(UpdateProfileDTO(updateProfileImage = mutation.uri))
                userProfileRepository.updateImageLocally(mutation.uri)
            }

            is ProfileMutation.DeleteAccount -> {
                userProfileRepo.deleteAccount()
            }

        }

    }

    fun onUniversityQueryChanged(query: String) {
        searchQuery.value = query
    }

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(1000) // 500ms debounce delay
                .filter { it.isNotBlank() && it.length > 3 }
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    universityRepository.updateUniversity(query)
                }
                .onStart { _universityData.value = UiState.Loading }
                .catch { e ->
                    _universityData.value = UiState.Error("Unexpected error: ${e.localizedMessage ?: "Unknown"}")
                }
                .collect { result ->
                    _universityData.value = result
                }
        }
    }

    fun resetUniversityData() {
        if (_universityData.value is UiState.Success){
            _universityData.value = (_universityData.value as UiState.Success<List<UniversityDTO>>).copy(data = emptyList())
            _universityData.value = UiState.Idle
        }else{
            _universityData.value = UiState.Idle
        }
    }

    // ✅ Centralized validation rules
    private fun validate(mutation: ProfileMutation): String? = when (mutation) {
        is ProfileMutation.Interests -> {

            if (mutation.value.isEmpty()) "Please select at least one interest" else null
        }
        is ProfileMutation.Campus -> {

            if (mutation.value.collegeName.isNullOrEmpty() || mutation.value.university == null) "Please enter college or university"
            else if (mutation.value.code.isNullOrEmpty()) "Please select a campus"
            else if (mutation.value.degree.isNullOrEmpty()) "Please fill a degree"
            else if (mutation.value.fieldOfStudy.isNullOrEmpty()) "Please select fieldOfStudy"
            else if (mutation.value?.isCurrent == true && mutation.value.courseStart.isNullOrEmpty()) "Please select a start date"
            else if (mutation.value?.isCurrent == false && mutation.value.courseStart.isNullOrEmpty()) "Please select a start date"
            else if (mutation.value?.isCurrent == false && mutation.value.courseEnd.isNullOrEmpty()) "Please select end date"
            else null

        }

        is ProfileMutation.ProfileImage -> if (mutation.uri.toString().isBlank()
        ) "Invalid profile image" else null

        is ProfileMutation.DeleteAccount -> null // no validation needed
        is ProfileMutation.UpdateProfile -> null
    }

    // ✅ Centralized error parsing
    private fun parseError(e: Throwable): String {
        return when (e) {
            is UnknownHostException -> "No internet connection"
            is TimeoutCancellationException -> "Request timed out"
            else -> e.message ?: "Something went wrong"
        }
    }
}