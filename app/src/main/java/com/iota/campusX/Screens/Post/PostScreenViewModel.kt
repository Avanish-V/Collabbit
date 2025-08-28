package com.iota.campusX.Screens.Post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PostScreenViewModel: ViewModel() {
    var _currentMode : MutableStateFlow<CreatePostMode> = MutableStateFlow(CreatePostMode.Text)
    val currentMode: StateFlow<CreatePostMode> = _currentMode.asStateFlow()


    fun chooseOption(option: CreatePostMode) {
        _currentMode.value = option
    }

}

sealed class CreatePostMode {
    object Text : CreatePostMode()
    object Media : CreatePostMode()
    object Poll : CreatePostMode()
}

enum class PostOptions { TEXT, IMAGE, VIDEO, FILE, POLL }