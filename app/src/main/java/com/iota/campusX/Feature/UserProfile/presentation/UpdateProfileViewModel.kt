package com.iota.campusX.Feature.UserProfile.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Feature.UserProfile.domain.UserProfileInterface
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepository
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
import kotlinx.coroutines.tasks.await

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UpdateProfileViewModel(
    private val userProfileRepo: UserProfileInterface,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()


    private val searchQuery = MutableStateFlow("")

    private val _universityData = MutableStateFlow<UiState<List<UniversityDTO>>>(UiState.Idle)
    val universityData: StateFlow<UiState<List<UniversityDTO>>> = _universityData.asStateFlow()



    sealed class Event {
        data class ShowMessage(val message: String) : Event()
        object NavigateToBack : Event()
    }


    sealed class ProfileMutation {
        data class Name(val value: String) : ProfileMutation()
        data class About(val value: String) : ProfileMutation()
        data class Gender(val value: com.iota.campusX.Feature.UserProfile.data.Gender) :
            ProfileMutation()

        data class SocialAccount(val value: String) : ProfileMutation()
        data class Interests(val value: List<String>) : ProfileMutation()
        data class Campus(val value: com.iota.campusX.Feature.UserProfile.data.Campus,val oldCampusId: String?) : ProfileMutation()

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

        val result = runCatching {
            when (mutation) {
                is ProfileMutation.Name -> {
                    userProfileRepo.updateUserName(mutation.value)
                    userProfileRepository.updateNameLocally(mutation.value)
                }

                is ProfileMutation.About -> {
                    userProfileRepo.updateAbout(mutation.value)
                    userProfileRepository.updateAboutLocally(mutation.value)
                }

                is ProfileMutation.Gender -> {
                    userProfileRepo.updateGender(mutation.value)
                    userProfileRepository.updateGenderLocally(mutation.value)
                }

                is ProfileMutation.SocialAccount -> {
                    userProfileRepo.updateSocialAccounts(mutation.value)
                }

                is ProfileMutation.Interests -> {
                    userProfileRepo.updateInterests(mutation.value)
                    userProfileRepository.updateInterestLocally(mutation.value)
                }

                is ProfileMutation.Campus -> {
                    userProfileRepo.updateCampus(mutation.value)
                    userProfileRepository.updateCampusLocally(mutation.value)
                }

                is ProfileMutation.ProfileImage -> {
                    userProfileRepo.updateProfileImage(mutation.uri)
                    userProfileRepository.updateImageLocally(mutation.uri)
                }

                is ProfileMutation.DeleteAccount -> {
                    userProfileRepo.deleteAccount()
                }
            }
        }

        result.fold(
            onSuccess = {
                userProfileRepo.syncUserProfile()
                _state.value = UiState.Success(Unit)
                delay(1000)
                _state.value = UiState.Idle

                val message = when (mutation) {
                    is ProfileMutation.Name -> "Name updated successfully"
                    is ProfileMutation.About -> "About updated successfully"
                    is ProfileMutation.Gender -> "Gender updated successfully"
                    is ProfileMutation.SocialAccount -> "Social account updated successfully"
                    is ProfileMutation.Interests -> "Interests updated successfully"
                    is ProfileMutation.Campus -> "Campus updated successfully"
                    is ProfileMutation.ProfileImage -> "Profile image updated successfully"
                    is ProfileMutation.DeleteAccount -> "Account deleted"
                }

                _events.send(Event.ShowMessage(message))

                if (mutation is ProfileMutation.Campus) {
                    if (mutation.oldCampusId == mutation.value.campusCode){
                        mutation.value.campusCode?.let { topic -> Firebase.messaging.subscribeToTopic(topic) }
                    }else{

                        mutation.oldCampusId?.let {
                            topic -> Firebase.messaging.unsubscribeFromTopic(topic).addOnSuccessListener {}
                        }
                        mutation.value.campusCode?.let {
                            topic -> Firebase.messaging.subscribeToTopic(topic).addOnSuccessListener {}
                        }
                    }
                }
            },
            onFailure = { e ->
                _state.value = UiState.Error(parseError(e))
                delay(1000)
                _state.value = UiState.Idle
            }
        )
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
                    userProfileRepo.updateUniversity(query)
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
        is ProfileMutation.Name -> if (mutation.value.isBlank()) "Name cannot be empty" else null
        is ProfileMutation.About -> {
            if (mutation.value.length > 500){
                "About cannot be more than 500 characters"
            }else if (mutation.value.isBlank()){
                "About cannot be empty"
            }else{
                null
            }
        }
        is ProfileMutation.Gender -> if (mutation.value == null) "Please select a gender" else null
        is ProfileMutation.SocialAccount -> if (mutation.value.isBlank()) "Social account cannot be empty" else null
        is ProfileMutation.Interests -> {

            if (mutation.value.isEmpty()) "Please select at least one interest" else null
        }
        is ProfileMutation.Campus -> {

            if (mutation.value.collegeName.isNullOrEmpty() || mutation.value.university == null) "Please enter college or university"
            else if (mutation.value.campusCode.isNullOrEmpty()) "Please select a campus"
            else if (mutation.value.degree.isNullOrEmpty()) "Please fill a degree"
            else if (mutation.value.fieldOfStudy.isNullOrEmpty()) "Please select fieldOfStudy"
            else if (mutation.value.duration?.current == true && mutation.value.duration.start.isEmpty()) "Please select a start date"
            else if (mutation.value.duration?.current == false && mutation.value.duration.start.isEmpty()) "Please select a start date"
            else if (mutation.value.duration?.current == false && mutation.value.duration.end.isNullOrEmpty()) "Please select end date"
            else null

        }

        is ProfileMutation.ProfileImage -> if (mutation.uri.toString().isBlank()
        ) "Invalid profile image" else null

        is ProfileMutation.DeleteAccount -> null // no validation needed
    }

    // ✅ Centralized error parsing
    private fun parseError(e: Throwable): String {
        return when (e) {
            is java.net.UnknownHostException -> "No internet connection"
            is TimeoutCancellationException -> "Request timed out"
            else -> e.message ?: "Something went wrong"
        }
    }
}