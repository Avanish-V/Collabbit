package com.iota.campusX.Feature.Post.presentation.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.UseCases.DeletePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.ToggleLikeUseCase
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PostFeedViewModel(
    private val getPostsUseCase: GetPostsUseCase,
    private val likeUseCase: ToggleLikeUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val getSinglePostByIdUseCase: com.iota.campusX.Feature.Post.domain.UseCases.GetSinglePostByIdUseCase,
    private val postRepositoryInterface: PostRepositoryInterface

) : ViewModel() {

    private val _userPosts = MutableStateFlow<PagingData<Post>>(PagingData.empty())
    val userPosts: StateFlow<PagingData<Post>> = _userPosts.asStateFlow()

    private val _allPosts = MutableStateFlow<PagingData<Post>>(PagingData.empty())
    val allPosts: StateFlow<PagingData<Post>> = _allPosts.asStateFlow()

    init {
        viewModelScope.launch {
            getPostsUseCase()
                .cachedIn(viewModelScope)
                .collectLatest {
                    _allPosts.value = it
                }
        }
    }

    fun getUserPost(userId: String) {
        _userPosts.value = PagingData.empty() // Clear old posts immediately
        viewModelScope.launch {
            postRepositoryInterface.getPostsById(userId)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _userPosts.value = pagingData
                    Log.d("POST-FEED", "User posts: ${pagingData.map { it.caption}}")
                }
        }
    }


    //---------------------------------FETCH THE DATA----------------------------------


    private val _singlePost = MutableStateFlow<UiState<Post>>(UiState.Idle)
    val singlePost: StateFlow<UiState<Post>> = _singlePost


    fun fetchSinglePost(postId: String) {
        viewModelScope.launch {
            _singlePost.value = UiState.Loading
            val result = getSinglePostByIdUseCase(postId)
            _singlePost.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load post") }
            )
        }
    }


    fun onPostEvent(event: PostAction){

        when(event){
            is PostAction.Like -> {
                updatePostOptimistically(event.contentId) { post ->
                    post.copy(
                        isLiked = event.isLiked,
                        likesCount = if (event.isLiked) post.likesCount + 1 else post.likesCount - 1
                    )
                }
                viewModelScope.launch {
                    val result = likeUseCase(event.contentId, event.isLiked)
                    if (result.isFailure) {
                        // Rollback
                        updatePostOptimistically(event.contentId) { post ->
                            post.copy(
                                isLiked = !event.isLiked,
                                likesCount = if (!event.isLiked) post.likesCount + 1 else post.likesCount - 1
                            )
                        }
                    }
                }
            }
            is PostAction.Delete -> {
                deletePostOptimistically(event.postId)
                viewModelScope.launch {
                    deletePostUseCase(event.postId)
                }
            }
            is PostAction.Edit -> {
                updatePostOptimistically(event.postId) { post ->
                    post.copy(caption = event.newCaption)
                }
            }
            is PostAction.OpenUserProfile -> {

            }
            else -> {}
        }

    }

    private fun updatePostOptimistically(postId: String, transform: (Post) -> Post) {
        _userPosts.value = _userPosts.value.map { if (it.postId == postId) transform(it) else it }
        _allPosts.value = _allPosts.value.map { if (it.postId == postId) transform(it) else it }
    }

    private fun deletePostOptimistically(postId: String) {
        _userPosts.value = _userPosts.value.filter { it.postId != postId }
        _allPosts.value = _allPosts.value.filter { it.postId != postId }
    }



}