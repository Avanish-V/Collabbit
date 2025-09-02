package com.iota.campusX.Screens.Post.PostActions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostActionViewModel(
    private val actionHandler: PostActionHandler
) : ViewModel() {

    private val _result : MutableStateFlow<UiState<Unit>> = MutableStateFlow(UiState.Loading)
    val result : StateFlow<UiState<Unit>> = _result.asStateFlow()

    fun onAction(action: PostAction) {
        viewModelScope.launch {
           actionHandler.handle(action)
        }
    }

}
