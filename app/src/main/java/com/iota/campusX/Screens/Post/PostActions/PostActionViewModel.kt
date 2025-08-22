package com.iota.campusX.Screens.Post.PostActions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PostActionViewModel(
    private val actionHandler: PostActionHandler
) : ViewModel() {

    fun onAction(action: PostAction) {
        viewModelScope.launch {
            actionHandler.handle(action)
        }
    }

}
