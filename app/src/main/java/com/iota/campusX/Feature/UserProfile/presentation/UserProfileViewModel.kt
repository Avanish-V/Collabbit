package com.iota.campusX.Feature.UserProfile.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Feature.UserProfile.data.BasicProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Gender
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepo
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart


@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UserProfileViewModel(private val userProfileRepo: UserProfileRepo):ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val _userBaseProfile = MutableStateFlow<UiState<BasicProfileDTO>>(UiState.Idle)
    val userBaseProfile: StateFlow<UiState<BasicProfileDTO>> = _userBaseProfile.asStateFlow()

    private val _profileById = MutableStateFlow<UiState<BasicProfileDTO>>(UiState.Idle)
    val profileById: StateFlow<UiState<BasicProfileDTO>> = _profileById.asStateFlow()

    private val _universityData = MutableStateFlow<UiState<List<UniversityDTO>>>(UiState.Idle)
    val universityData: StateFlow<UiState<List<UniversityDTO>>> = _universityData.asStateFlow()

    private val _hasConnection = MutableStateFlow<UiState<Boolean?>>(UiState.Idle)
    val hasConnection: StateFlow<UiState<Boolean?>> = _hasConnection.asStateFlow()

    private val _connections = MutableStateFlow<UiState<List<ConnectionsDTO>>>(UiState.Idle)
    val connections: StateFlow<UiState<List<ConnectionsDTO>>> = _connections.asStateFlow()

    private val _connectionCount = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val connectionCount: StateFlow<UiState<Int>> = _connectionCount.asStateFlow()

    private val _modifyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val modifyState: StateFlow<UiState<Unit>> = _modifyState.asStateFlow()


    fun getUserProfile() {

        if (userBaseProfile.value is UiState.Success && (userBaseProfile.value as UiState.Success).data.id.isNotEmpty()) return

        viewModelScope.launch {

            _userBaseProfile.value = UiState.Loading

            val result = userProfileRepo.getBaseProfile()

            _userBaseProfile.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )

        }

    }

    fun getUserById(userId: String) {
        viewModelScope.launch {

            _profileById.value = UiState.Loading

            val result = userProfileRepo.getUserProfileById(userId)

            _profileById.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )

        }
    }

    fun getConnections(userId: String) {
        viewModelScope.launch {
            _connections.value = UiState.Loading
            val result = userProfileRepo.getConnections(userId)
            _connections.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )
        }
    }

    fun getConnectionCount(userId: String) {
        viewModelScope.launch {

            _connectionCount.value = UiState.Loading

            val result = userProfileRepo.getConnectionsCount(userId)

            _connectionCount.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )
        }
    }

    // One-liner modify/update functions (no UI state needed)
    fun modifyName(userName: String) = viewModelScope.launch {
        _modifyState.value = UiState.Loading
        _modifyState.value = userProfileRepo.updateUserName(userName).fold(
            onSuccess = {
                UiState.Success(Unit).also {
                    updateNameLocally(userName)
                }
            },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
        resetModifyState()

    }

    fun modifyAbout(about: String) = viewModelScope.launch {
        _modifyState.value = UiState.Loading
        _modifyState.value = userProfileRepo.updateAbout(about).fold(
            onSuccess = { UiState.Success(Unit).also { updateAboutLocally(about) } },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
        resetModifyState()
    }

    fun modifyGender(gender: Gender) = viewModelScope.launch {
        _modifyState.value = UiState.Loading

        _modifyState.value = userProfileRepo.updateGender(gender).fold(
            onSuccess = { UiState.Success(Unit).also { updateGenderLocally(gender) } },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }

    fun modifySocialAccount(social: String) = viewModelScope.launch {
        _modifyState.value = UiState.Loading
        _modifyState.value = userProfileRepo.updateSocialAccounts(social).fold(
            onSuccess = { UiState.Success(Unit) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }

    fun updateInterests(interests: List<String>) = viewModelScope.launch {

        _modifyState.value = UiState.Loading

        _modifyState.value = userProfileRepo.updateInterests(interests).fold(
            onSuccess = { UiState.Success(Unit).also({ updateInterestLocally(interests) }) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
        resetModifyState()

    }

    fun modifyCampus(campus: Campus) = viewModelScope.launch {

        _modifyState.value = UiState.Loading

        _modifyState.value = userProfileRepo.updateCampus(campus).fold(
            onSuccess = { UiState.Success(Unit).also{ updateCampusLocally(campus) } },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )

        resetModifyState()

    }

    fun modifyProfileImage(imageUri: Uri) = viewModelScope.launch {
        _modifyState.value = UiState.Loading
        _modifyState.value = userProfileRepo.updateProfileImage(imageUri).fold(
            onSuccess = { UiState.Success(Unit) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )

    }

    fun sendLinkUpRequest(requestUserId: String, currentState: Boolean?) = viewModelScope.launch {
        _modifyState.value = UiState.Loading
        _modifyState.value = userProfileRepo.sendLinkUpRequest(requestUserId, currentState).fold(
            onSuccess = { UiState.Success(Unit) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
        resetModifyState()
    }

    fun hasConnection(userId: String) = viewModelScope.launch {
        _hasConnection.value = UiState.Loading
        _hasConnection.value = userProfileRepo.hasConnection(userId).fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }

    fun acceptLinkUpRequest(requestUserId: String) = viewModelScope.launch {
        _modifyState.value = UiState.Loading
        _modifyState.value = userProfileRepo.acceptLinkUpRequest(requestUserId).fold(
            onSuccess = { UiState.Success(Unit) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }

    fun rejectLinkUpRequest(requestUserId: String) = viewModelScope.launch {
        _modifyState.value = UiState.Loading
        _modifyState.value = userProfileRepo.rejectLinkUpRequest(requestUserId).fold(
            onSuccess = { UiState.Success(Unit) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }

    suspend fun deleteUserProfile() = userProfileRepo.deleteAccount()

    suspend fun resetModifyState(){
        delay(500)
        _modifyState.value = UiState.Idle
    }

    fun onUniversityQueryChanged(query: String) {
        searchQuery.value = query
    }

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(500) // 500ms debounce delay
                .filter { it.isNotBlank() }
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


    fun updateNameLocally(name:String) {
        _userBaseProfile.value = UiState.Success((userBaseProfile.value as UiState.Success).data.copy(userName = name))
    }

    fun updateAboutLocally(about:String) {
        _userBaseProfile.value = UiState.Success((userBaseProfile.value as UiState.Success).data.copy(userBio = about))
    }

    fun updateGenderLocally(gender:Gender) {
        _userBaseProfile.value = UiState.Success((userBaseProfile.value as UiState.Success).data.copy(userGender = gender))
    }

    fun updateInterestLocally(interests: List<String>){
        _userBaseProfile.value = UiState.Success((userBaseProfile.value as UiState.Success).data.copy(interests = interests))
    }

    fun updateCampusLocally(campus: Campus){
        _userBaseProfile.value = UiState.Success((userBaseProfile.value as UiState.Success).data.copy(campus = campus))
    }

    fun resetUniversityData() {
        _universityData.value = UiState.Idle
    }
}


data class UserBaseProfileResultState(
    val isLoading:Boolean = false,
    val baseProfileData: BasicProfileDTO = BasicProfileDTO(),
    val error:String = ""
)

data class UniversityDataResultState(
    val isLoading:Boolean = false,
    val universityList: List<UniversityDTO> = emptyList(),
    val error:String = ""
)

data class ConnectionsResultState(
    val isLoading:Boolean = false,
    val connectionList:List<ConnectionsDTO> = emptyList(),
    val error:String = ""
)
