package com.iota.campusX.Feature.Post.presentation.feedmenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.domain.UseCases.CreateReplyUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.DeletePostUseCase
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MenuActionViewModel(
    private val repository: PostMenuRepository,
    private val deletePostUseCase: DeletePostUseCase,
    private val replyRepository: ReplyRepository,
    private val postFeedViewModel: PostFeedViewModel
): ViewModel() {
    private val _menuOptions = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuOptions: StateFlow<List<MenuItem>> = _menuOptions

    private val _selectedPost = MutableStateFlow<MenuContext?>(null)
    val selectedPost: StateFlow<MenuContext?> = _selectedPost

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState

    fun loadMenu(menuContext: MenuContext) {
        _selectedPost.value = menuContext
        viewModelScope.launch {
            val options = repository.getMenuOptions(context = menuContext)
            _menuOptions.value = options
        }
    }

    fun onMenuActionEvent(menuEvent: MenuAction){
        when(menuEvent){
            is MenuAction.Delete -> {
                if (menuEvent.context.type == ContentType.POST){
                    deletePost(menuEvent.context.id)
                }
                if (menuEvent.context.type == ContentType.REPLY){
                    deleteReply(menuEvent.context.id)
                }
            }
            else -> {}
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postFeedViewModel.onPostEvent(PostAction.Delete(postId))
            _deleteState.value = UiState.Loading
            val result = deletePostUseCase.invoke(postId)
            result.fold(
                onSuccess = {
                    _deleteState.value = UiState.Success(Unit)
                    // Reset to idle after a delay or UI consumes it
                    _deleteState.value = UiState.Idle
                },
                onFailure = {
                    _deleteState.value = UiState.Error(it.message ?: "Failed to delete post")
                    _deleteState.value = UiState.Idle
                }
            )
        }
    }

    fun deleteReply(replyId: String){
        viewModelScope.launch {
            postFeedViewModel.onPostEvent(PostAction.Delete(replyId))
            _deleteState.value = UiState.Loading
            val result = replyRepository.deleteReply(postId = "", replyId = replyId)
            result.fold(
                onSuccess = {
                    _deleteState.value = UiState.Success(Unit)
                    // Reset to idle after a delay or UI consumes it
                    _deleteState.value = UiState.Idle
                },
                onFailure = {
                    _deleteState.value = UiState.Error(it.message ?: "Failed to delete post")
                    _deleteState.value = UiState.Idle
                }
            )
        }
    }
}