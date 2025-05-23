package com.iota.campusX.Feature.UserProfile.presentation

import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Feature.UserProfile.data.UserBasicProfileDTO
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepo
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(private val userProfileRepo: UserProfileRepo):ViewModel() {

    private val _hasMessage: MutableStateFlow<String> = MutableStateFlow("")
    val hasMessage: StateFlow<String> = _hasMessage.asStateFlow()

    private val _userBaseProfile: MutableStateFlow<UserBaseProfileResultState> = MutableStateFlow(UserBaseProfileResultState())
    val userBaseProfile: StateFlow<UserBaseProfileResultState> = _userBaseProfile.asStateFlow()

    private val _profileById: MutableStateFlow<UserBaseProfileResultState> = MutableStateFlow(UserBaseProfileResultState())
    val profileById: StateFlow<UserBaseProfileResultState> = _profileById.asStateFlow()


    private val _universityData: MutableStateFlow<UniversityDataResultState> = MutableStateFlow(UniversityDataResultState())
    val universityData: StateFlow<UniversityDataResultState> = _universityData.asStateFlow()


    fun getUserProfile(){

        if (userBaseProfile.value.baseProfileData != null) return

        viewModelScope.launch {
            userProfileRepo.getBaseProfile().collect{
                when(it){
                    is ResultState.Loading->{
                        _userBaseProfile.value = UserBaseProfileResultState(isLoading = true)
                    }
                    is ResultState.Success->{
                        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = it.data)
                    }
                    is ResultState.Error->{
                        _userBaseProfile.value = UserBaseProfileResultState(error = it.message)
                    }
                }
            }
        }
    }

    fun deleteUserProfile() = userProfileRepo.deleteAccount()

    fun hasMessage(userId:String){
        viewModelScope.launch {
            userProfileRepo.fetchChatRoomId(userId).collect{
                when(it){
                    is ResultState.Loading->{
                    }
                    is ResultState.Success->{
                        _hasMessage.value = it.data
                    }
                    is ResultState.Error->{

                    }
                }
            }
        }
    }

    fun getUserById(user:String){
        viewModelScope.launch {
            userProfileRepo.getUserProfileById(user).collect{
                when(it){
                    is ResultState.Loading->{
                        _profileById.value = UserBaseProfileResultState(isLoading = true)
                    }
                    is ResultState.Success->{

                        _profileById.value = UserBaseProfileResultState( baseProfileData= it.data)
                    }
                    is ResultState.Error->{
                        _profileById.value = UserBaseProfileResultState(error = it.message)
                    }
                }
            }
        }
    }

    fun modifyName(userName:String) = userProfileRepo.updateUserName(userName)

    fun modifyAbout(about:String) = userProfileRepo.updateAbout(about)

    fun modifyGender(gender:String) = userProfileRepo.updateGender(gender)

    fun modifySocialAccount(social:String) = userProfileRepo.updateSocialAccounts(social)

    fun modifyInterests(interests:List<String>) = userProfileRepo.updateInterests(interests)

    fun modifyCampus(campus: Campus) = userProfileRepo.updateCampus(campus)

    fun modifyProfileImage(imageUri: Uri) = userProfileRepo.updateProfileImage(imageUri)

    fun fetchUniversityData(title:String){
        viewModelScope.launch {
            userProfileRepo.updateUniversity(title).collect{
                when(it){
                    is ResultState.Loading->{
                        _universityData.value = UniversityDataResultState(isLoading = true)
                    }
                    is ResultState.Success->{
                        _universityData.value = UniversityDataResultState(universityList = it.data)
                    }
                    is ResultState.Error->{
                        _universityData.value = UniversityDataResultState(error = it.message)
                    }
                }
            }
        }
    }

    fun sendLinkUpRequest(requestUserId:String,currentState: Boolean? = null) = userProfileRepo.sendLinkUpRequest(requestUserId,currentState)

    fun acceptLinkUpRequest(requestUserId:String) = userProfileRepo.acceptLinkUpRequest(requestUserId)

    fun rejectLinkUpRequest(requestUserId:String) = userProfileRepo.rejectLinkUpRequest(requestUserId)

    fun updateName(name: String){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData?.copy(userName = name))
    }
    fun updateAbout(about: String){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData?.copy(userBio = about))
    }
    fun updateGender(gender: String){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData?.copy(userGender = gender))
    }
    fun clearUniversityData(){
        _universityData.value = UniversityDataResultState()
    }
    fun updateCampus(campus: Campus){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData?.copy(campus = campus))
    }
    fun updateProfileImage(imageUrl: String){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData?.copy(userImage = imageUrl))
    }


}


data class UserBaseProfileResultState(
    val isLoading:Boolean = false,
    val baseProfileData: UserBasicProfileDTO? = null,
    val error:String = ""
)

data class UniversityDataResultState(
    val isLoading:Boolean = false,
    val universityList: List<UniversityDTO> = emptyList(),
    val error:String = ""
)
