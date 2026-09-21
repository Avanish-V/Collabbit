package com.iota.campusX.Feature.Post.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.UseCases.EditPostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetSinglePostByIdUseCase
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository
import com.iota.campusX.Feature.Post.presentation.feedmenu.ContentType
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditPostViewModel(
    private val editPostUseCase: EditPostUseCase,
    private val getSinglePostByIdUseCase: GetSinglePostByIdUseCase,
    private val postFeedViewModel: PostFeedViewModel,
    private val replyRepository: ReplyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _caption = MutableStateFlow("")
    val caption = _caption.asStateFlow()

    private val _post = MutableStateFlow<Post?>(null)
    val post = _post.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadContent(id: String, type: String) {
        viewModelScope.launch {
            _isLoading.value = true
            if (type == ContentType.REPLY.name) {
                val result = replyRepository.getReplyById(id)
                result.fold(
                    onSuccess = { reply ->
                        _caption.value = reply.content
                        _isLoading.value = false
                    },
                    onFailure = {
                        _isLoading.value = false
                        _uiState.value = UiState.Error(it.message ?: "Failed to load reply")
                    }
                )
            } else {
                val result = getSinglePostByIdUseCase(id)
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
    }

    fun onCaptionChanged(newCaption: String) {
        _caption.value = newCaption
    }

    fun updateContent(id: String, type: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            if (type == ContentType.REPLY.name) {
                val result = replyRepository.editReply(id, _caption.value)
                result.fold(
                    onSuccess = {
                        _uiState.value = UiState.Success(Unit)
                    },
                    onFailure = {
                        _uiState.value = UiState.Error(it.message ?: "Failed to update reply")
                    }
                )
            } else {
                postFeedViewModel.onPostEvent(PostAction.Edit(id, _caption.value))
                val result = editPostUseCase(id, _caption.value)
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
}
