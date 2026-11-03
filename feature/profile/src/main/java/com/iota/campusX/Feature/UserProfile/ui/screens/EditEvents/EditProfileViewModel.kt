package com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EditProfileViewModel(): ViewModel() {

    private val _editAction = MutableStateFlow<EditProfileActions?>(null)
    val editAction : StateFlow<EditProfileActions?> = _editAction

    fun onEditProfileEvent(actions: EditProfileActions){
        _editAction.value = actions
    }

}
