package com.iota.campusX.Screens.Post

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PostScreenViewModel: ViewModel() {

    private val _post = MutableStateFlow<PostOptions>(PostOptions.IMAGE_WITH_TEXT)
    val post: StateFlow<PostOptions> = _post.asStateFlow()


    fun chooseOption(option: PostOptions) {
        _post.value = option
    }

}

enum class PostOptions{ IMAGE_WITH_TEXT,POLL }