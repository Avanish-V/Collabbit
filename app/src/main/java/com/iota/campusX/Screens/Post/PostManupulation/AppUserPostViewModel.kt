package com.iota.campusX.Screens.Post.PostManupulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppUserPostViewModel(
    private val repository: PostRepository
) : ViewModel() {

    val userPosts = repository.postById
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Idle)

    fun fetchUserPosts(userId: String) = viewModelScope.launch {
        if (userPosts.value is UiState.Success) return@launch
        repository.fetchUserPosts(userId)
    }
}
