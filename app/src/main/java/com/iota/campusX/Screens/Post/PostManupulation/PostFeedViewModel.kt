package com.iota.campusX.Screens.Post.PostManupulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostFeedViewModel(
    private val repository: PostRepository,

) : ViewModel() {

    val globalPosts = repository.globalPosts
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Idle)

    val campusPosts = repository.campusPosts
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Idle)

    val singlePost = repository.singlePost
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Idle)

    fun fetchGlobalPosts() = viewModelScope.launch { repository.fetchGlobalPosts() }
    fun fetchCampusPosts(campusId: String) = viewModelScope.launch { repository.fetchCampusPosts(campusId, feedMode = FeedMode.CAMPUS) }

    fun fetchSinglePost(postId: String) = viewModelScope.launch { repository.fetchSinglePost(postId) }

    fun deletePost(postId: String) = viewModelScope.launch { repository.deletePost(postId) }

    fun editPost(postId: String, text: String) = viewModelScope.launch { repository.editPost(postId, text) }
}
