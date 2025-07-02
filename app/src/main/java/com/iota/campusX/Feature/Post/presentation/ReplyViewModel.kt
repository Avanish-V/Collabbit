package com.iota.campusX.Feature.Post.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.visibilityMode
import com.iota.campusX.Feature.Post.domain.Models.CreatorDetail
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.Models.PostActions
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.domain.UseCases.CreateReplyUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetRepliesUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReplyViewModel(
    private val getRepliesUseCase: GetRepliesUseCase,
    private val createReplyUseCase: CreateReplyUseCase,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _repliesState = MutableStateFlow<UiState<List<GetRepliesDTO>>>(UiState.Idle)
    val repliesState: StateFlow<UiState<List<GetRepliesDTO>>> = _repliesState.asStateFlow()

    private val _createReplyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createReplyState: StateFlow<UiState<Unit>> get() = _createReplyState

    private val _deleteReplyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteReplyState: StateFlow<UiState<Unit>> get() = _deleteReplyState

    private val _editReplyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val editReplyState: StateFlow<UiState<Unit>> get() = _editReplyState

    private val _likeReplyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val likeReplyState: StateFlow<UiState<Unit>> get() = _likeReplyState

    fun getReplies(postId: String,campusId:String?,feedMode: FeedMode) {
        viewModelScope.launch {
            _repliesState.value = UiState.Loading
            val result = getRepliesUseCase(postId,campusId,feedMode)
            _repliesState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to fetch replies") }
            )
        }
    }

    fun createReply(
        replyId: String,
        postId: String,
        content: String,
        creatorId: String,
        visibilityMode: PostVisibilityMode,
        mode: FeedMode,
        campusId: String?,
        user: UserDetail
    ) {
        viewModelScope.launch {
            _createReplyState.value = UiState.Loading
            val result = createReplyUseCase(replyId, postId, content, creatorId, visibilityMode,mode,campusId)
            _createReplyState.value = result.fold(
                onSuccess = {

                    val visibility = visibilityMode(
                        visibilityMode,
                        user
                    )

                    addNewReplyOnCreate(
                        GetRepliesDTO(
                            postId = postId,
                            content = content,
                            replyId = replyId,
                            visibilityMode = visibilityMode,
                            creatorDetail = CreatorDetail(
                                profile = UserDetail(
                                    userName = visibility.first,
                                    id = user.id,
                                    userImage = visibility.second,
                                    userBio = user.userBio,
                                    designation = ""
                                ),
                                isCurrentUser = true
                            ),
                            actions = PostActions(),
                            repliedAt = System.currentTimeMillis()
                        )
                    )
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Failed to create reply") }
            )
        }
    }

    fun likeReply(creatorId: String, postId: String, replyId: String, isLiked: Boolean) {
        viewModelScope.launch {

        }
    }

    fun deleteReply(postId: String, replyId: String, campusId: String?,feedMode: FeedMode) {
        viewModelScope.launch {
            _deleteReplyState.value = UiState.Loading
            val result = postRepository.deleteReply(postId, replyId, campusId,feedMode)
            _deleteReplyState.value = result.fold(
                onSuccess = {
                    removeReplyOnDelete(replyId)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Delete failed") }
            )
        }
    }

    fun editReply(postId: String, replyId: String, content: String, campusId: String?,feedMode: FeedMode) {
        viewModelScope.launch {
            _editReplyState.value = UiState.Loading
            val result = postRepository.editReply(postId, replyId, content, campusId,feedMode)
            _editReplyState.value = result.fold(
                onSuccess = {
                    UiState.Success(Unit).also {
                        updateReplyLocallyOnEdit(replyId, content)
                    }
                },
                onFailure = { UiState.Error(it.message ?: "Edit failed") }
            )
        }
    }

    fun updateReplyLocallyOnEdit(replyId: String, editedContent: String) {
        Log.d("REPLY_VIEW_MODEL", " ${repliesState.value}")
        val current = _repliesState.value as? UiState.Success ?: return
        val updatedReplies = current.data.map { reply ->
            if (reply.replyId == replyId) reply.copy(content = editedContent) else reply
        }
        _repliesState.value = UiState.Success(updatedReplies)
    }

    fun addNewReplyOnCreate(newReply: GetRepliesDTO) {
        val current = _repliesState.value
        _repliesState.value = when (current) {
            is UiState.Success -> UiState.Success(current.data + newReply)
            else -> UiState.Success(listOf(newReply))
        }
    }

    fun removeReplyOnDelete(replyId: String) {
        val current = _repliesState.value
        if (current is UiState.Success) {
            _repliesState.value = UiState.Success(current.data.filterNot { it.replyId == replyId })
        }
    }
}
