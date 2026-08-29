package com.iota.campusX.Feature.Post.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.UseCases.EditPostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetSinglePostByIdUseCase
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditPostViewModel(
    private val editPostUseCase: EditPostUseCase,
    private val getSinglePostByIdUseCase: GetSinglePostByIdUseCase,
    private val postFeedViewModel: PostFeedViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _caption = MutableStateFlow("")
    val caption = _caption.asStateFlow()

    private val _post = MutableStateFlow<Post?>(null)
    val post = _post.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadPost(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = getSinglePostByIdUseCase(postId)
            result.fold(
                onSuccess = { post ->
                    _post.value = post
                    _caption.value = post.caption
                    _isLoading.value = false
                },
                onFailure = {
                    _isLoading.value = false
                    _uiState.value = UiState.Error(it.message ?: "Failed to load post")
                }
            )
        }
    }

    fun onCaptionChanged(newCaption: String) {
        _caption.value = newCaption
    }

    fun updatePost(postId: String) {
        viewModelScope.launch {
            postFeedViewModel.onPostEvent(PostAction.Edit(postId, _caption.value))
            _uiState.value = UiState.Loading
            val result = editPostUseCase(postId, _caption.value)
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success(Unit)
                },
                onFailure = {
                    _uiState.value = UiState.Error(it.message ?: "Failed to update post")
                }
            )
        }
    }
}
