package com.iota.campusX.Feature.Post.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.domain.repository.PostRepository
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostFeedViewModel(
    private val repository: PostRepository,
) : ViewModel() {

    val globalPosts: StateFlow<PagingData<GetPostDTO>> = repository.globalPosts
    val campusPosts: StateFlow<PagingData<GetPostDTO>> = repository.campusPosts
    val userPosts: StateFlow<PagingData<GetPostDTO>> = repository.postById

    val singlePost  = repository.singlePost.map {
        it
    }.stateIn(viewModelScope, SharingStarted.Lazily, UiState.Idle)

    //---------------------------------FETCH THE DATA----------------------------------


    fun fetchGlobalPost(feedMode: FeedMode, campusId: String?) = viewModelScope.launch {
       repository.fetchGlobalPosts(viewModelScope,feedMode,campusId)
    }
    fun fetchCampusPost(campusId: String) = viewModelScope.launch { repository.fetchCampusPosts(viewModelScope,campusId) }
    fun fetchUserPost(userId: String) = viewModelScope.launch { repository.fetchUserPosts(viewModelScope,userId) }
    fun fetchSinglePost(postId: String) = viewModelScope.launch {
        repository.fetchSinglePost(postId)
    }

    fun clearSinglePost() = viewModelScope.launch {
        repository.clearSinglePost()
    }

}