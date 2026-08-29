package com.iota.campusX.Feature.Reply.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Reply.data.remote.request.ReplyRequest
import com.iota.campusX.Feature.Reply.data.remote.response.ReplyResponse
import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReplyViewModel(private val replyRepository: ReplyRepository) : ViewModel() {

    private val _createReplyState = MutableStateFlow<UiState<ReplyResponse>>(UiState.Idle)
    val createReplyState: StateFlow<UiState<ReplyResponse>> = _createReplyState.asStateFlow()

    private val _postReplies = MutableStateFlow<UiState<List<ReplyResponse>>>(UiState.Idle)
    val postReplies: StateFlow<UiState<List<ReplyResponse>>> = _postReplies.asStateFlow()

    // Map to store child replies by parent ID
    private val _childReplies = MutableStateFlow<Map<String, List<ReplyResponse>>>(emptyMap())
    val childReplies: StateFlow<Map<String, List<ReplyResponse>>> = _childReplies.asStateFlow()

    // Loading states for child replies
    private val _childLoadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val childLoadingStates: StateFlow<Map<String, Boolean>> = _childLoadingStates.asStateFlow()

    private val _replyText = MutableStateFlow("")
    val replyText: StateFlow<String> = _replyText.asStateFlow()

    private val _pickedImage = MutableStateFlow<Uri?>(null)
    val pickedImage: StateFlow<Uri?> = _pickedImage.asStateFlow()

    fun onReplyTextChange(text: String) {
        _replyText.value = text
    }

    fun onImagePicked(uri: Uri?) {
        _pickedImage.value = uri
    }

    fun createReply(replyRequest: ReplyRequest, feedId: String) {
        viewModelScope.launch {
            _createReplyState.value = UiState.Loading
            val result = replyRepository.createReply(replyRequest, feedId, _pickedImage.value)
            _createReplyState.value = result.fold(
                onSuccess = { newReply ->
                    if (newReply.parentReplyId != null) {
                        fetchChildReplies(newReply.parentReplyId)
                    } else {
                        fetchPostReplies(feedId)
                    }
                    _replyText.value = ""
                    _pickedImage.value = null
                    UiState.Success(newReply)
                },
                onFailure = { UiState.Error(it.message ?: "Failed to create reply") }
            )
        }
    }

    fun fetchPostReplies(feedId: String) {
        viewModelScope.launch {
            _postReplies.value = UiState.Loading
            val result = replyRepository.getReplies(feedId)
            _postReplies.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to fetch replies") }
            )
        }
    }

    fun fetchChildReplies(parentId: String) {
        viewModelScope.launch {
            _childLoadingStates.value = _childLoadingStates.value + (parentId to true)
            val result = replyRepository.getChildReplies(parentId)
            result.onSuccess { children ->
                _childReplies.value = _childReplies.value + (parentId to children)
            }.onFailure {
                Log.e("ReplyVM", "Failed to fetch child replies: ${it.message}")
            }
            _childLoadingStates.value = _childLoadingStates.value - parentId
        }
    }

    fun toggleReplyLike(repliedById: String, replyId: String, currentLiked: Boolean) {
        viewModelScope.launch {
            val targetLiked = !currentLiked
            
            // Optimistic UI update
            updateLikeStateLocally(replyId, targetLiked)

            val result = replyRepository.likeReply(
                repliedById = repliedById,
                postId = "", // Not needed by backend if replyId is unique
                replyId = replyId,
                isLiked = targetLiked
            )

            result.onFailure {
                // Rollback on failure
                updateLikeStateLocally(replyId, currentLiked)
            }
        }
    }

    private fun updateLikeStateLocally(replyId: String, isLiked: Boolean) {
        val diff = if (isLiked) 1 else -1

        // Update top-level replies
        val currentTop = _postReplies.value
        if (currentTop is UiState.Success) {
            val updated = currentTop.data.map {
                if (it.id == replyId) {
                    it.copy(isLiked = isLiked, likesCount = maxOf(0, it.likesCount + diff))
                } else it
            }
            _postReplies.value = UiState.Success(updated)
        }

        // Update child replies
        val currentChildren = _childReplies.value
        val newChildren = currentChildren.mapValues { (_, list) ->
            list.map {
                if (it.id == replyId) {
                    it.copy(isLiked = isLiked, likesCount = maxOf(0, it.likesCount + diff))
                } else it
            }
        }
        _childReplies.value = newChildren
    }

    fun deleteReply(replyId: String) {
        viewModelScope.launch {
            // Optimistic update
            removeReplyLocally(replyId)

            val result = replyRepository.deleteReply("", replyId)
            result.onFailure {
                // Should probably re-fetch instead of complex rollback
                Log.e("ReplyVM", "Delete failed: ${it.message}")
            }
        }
    }

    private fun removeReplyLocally(replyId: String) {
        // Remove from top-level
        val currentTop = _postReplies.value
        if (currentTop is UiState.Success) {
            _postReplies.value = UiState.Success(currentTop.data.filter { it.id != replyId })
        }

        // Remove from children
        _childReplies.value = _childReplies.value.mapValues { (_, list) ->
            list.filter { it.id != replyId }
        }
    }

    fun clearPostReplies() {
        _postReplies.value = UiState.Idle
        _childReplies.value = emptyMap()
    }
}
