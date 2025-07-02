package com.iota.campusX.Screens.Profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileTypeViewModel: ViewModel() {

    private val _userType = MutableStateFlow<UserType>(UserType.Idle)
    val userType: StateFlow<UserType> = _userType.asStateFlow()


    var profileId = mutableStateOf("")


    fun setUserType(type: UserType) {
        if (_userType.value != type && type != UserType.Idle) {
            _userType.value = type
        }
    }

    fun getProfileIdByPost(
        userIdByFeed: String,
        loggedInUserId: String,
        currentDestination : String
    ){

    }
}

sealed class UserType{
    object Idle:UserType()
    object User: UserType()
    object Owner: UserType()
}
