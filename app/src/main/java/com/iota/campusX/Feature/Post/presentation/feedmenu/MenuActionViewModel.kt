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
    
    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState

    fun loadMenu(menuController: MenuController, menuContext: MenuContext) {
        viewModelScope.launch {
            val options = repository.getMenuOptions(context = menuContext)
            menuController.show(menuContext, options)
        }
    }

    /**
     * Shows the menu by updating the controller and loading the menu options.
     * This follows DRY by centralizing the menu trigger logic.
     */
    fun showMenu(menuController: MenuController, context: MenuContext) {
        loadMenu(menuController, context)
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
                },
                onFailure = {
                    _deleteState.value = UiState.Error(it.message ?: "Failed to delete post")
                }
            )
        }
    }

    fun deleteReply(replyId: String){
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            val result = replyRepository.deleteReply(postId = "", replyId = replyId)
            result.fold(
                onSuccess = {
                    _deleteState.value = UiState.Success(Unit)
                },
                onFailure = {
                    _deleteState.value = UiState.Error(it.message ?: "Failed to delete reply")
                }
            )
        }
    }

    fun resetDeleteState() {
        _deleteState.value = UiState.Idle
    }
}