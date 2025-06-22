package com.iota.campusX.Screens.Profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.UserProfile.data.BasicProfileDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfilyTypeViewModel: ViewModel() {

    private val _userType = MutableStateFlow<UserType>(UserType.Idle)
    val userType: StateFlow<UserType> = _userType.asStateFlow()


    fun setUserType(type: UserType) {
        if (_userType.value != type && type != UserType.Idle) {
            _userType.value = type
        }
    }
}

sealed class UserType{
    object Idle:UserType()
    object User: UserType()
    object Owner: UserType()
}
