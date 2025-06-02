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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(private val userProfileRepo: UserProfileRepo):ViewModel() {

    private val _userBaseProfile: MutableStateFlow<UserBaseProfileResultState> = MutableStateFlow(UserBaseProfileResultState())
    val userBaseProfile: StateFlow<UserBaseProfileResultState> = _userBaseProfile.asStateFlow()

    private val _profileById: MutableStateFlow<UserBaseProfileResultState> = MutableStateFlow(UserBaseProfileResultState())
    val profileById: StateFlow<UserBaseProfileResultState> = _profileById.asStateFlow()


    private val _universityData: MutableStateFlow<UniversityDataResultState> = MutableStateFlow(UniversityDataResultState())
    val universityData: StateFlow<UniversityDataResultState> = _universityData.asStateFlow()

    private val _connections:MutableStateFlow<ConnectionsResultState> = MutableStateFlow(ConnectionsResultState())
    val connections:StateFlow<ConnectionsResultState> = _connections.asStateFlow()

    private val _connectionCount:MutableStateFlow<Int> = MutableStateFlow(0)
    val connectionCount:StateFlow<Int> = _connectionCount.asStateFlow()


    fun getUserProfile(){

        if (userBaseProfile.value.baseProfileData.id.isNotEmpty()) return

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

    fun modifyGender(gender: Gender) = userProfileRepo.updateGender(gender)

    fun modifySocialAccount(social:String) = userProfileRepo.updateSocialAccounts(social)

    fun updateInterests(interests:List<String>) = userProfileRepo.updateInterests(interests)

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


    fun getConnections(userId: String){
        viewModelScope.launch {
            userProfileRepo.getConnections(userId)
                .collect{
                    when(it){
                        is ResultState.Loading->{
                            _connections.value = ConnectionsResultState(isLoading = true)

                        }
                        is ResultState.Success->{
                            _connections.value = ConnectionsResultState(connectionList = it.data)
                        }
                        is ResultState.Error->{
                            _connections.value = ConnectionsResultState(error = it.message)
                        }
                    }
                }
        }
    }

    fun getConnectionCount(userId: String){
        viewModelScope.launch {
            userProfileRepo.getConnectionsCount(userId)
                .collect{
                    when(it){
                        is ResultState.Loading->{}
                        is ResultState.Success->{
                            _connectionCount.value = it.data
                        }
                        is ResultState.Error->{}


                    }
                }
        }
    }



    fun updateName(name: String){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData.copy(userName = name))
    }
    fun updateAbout(about: String){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData.copy(userBio = about))
    }
    fun updateGender(gender: Gender){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData.copy(userGender = gender))
    }
    fun clearUniversityData(){
        _universityData.value = UniversityDataResultState()
    }
    fun updateCampus(campus: Campus){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData.copy(campus = campus))
    }
    fun updateProfileImage(imageUrl: String){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData.copy(userImage = imageUrl))
    }
    fun updateConnectionDeleted(connectionId:String){
        _connections.value = ConnectionsResultState(connectionList = _connections.value.connectionList.filter { it.user.id != connectionId })
    }
    fun updateModifiedInterests(userInterests:List<String>){
        _userBaseProfile.value = UserBaseProfileResultState(baseProfileData = _userBaseProfile.value.baseProfileData.copy(interests = userInterests))
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
