package com.iota.campusX.Screens.Post

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedVisualContentViewModel : ViewModel(){

    private val _post : MutableStateFlow<GetPostDTO?> = MutableStateFlow(null)
    val post: StateFlow<GetPostDTO?> = _post


    fun setPost(post: GetPostDTO){
        _post.value = post
    }

}