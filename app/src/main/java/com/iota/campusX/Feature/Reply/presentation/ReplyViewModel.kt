package com.iota.campusX.Feature.Reply.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Reply.data.remote.request.ReplyRequest
import com.iota.campusX.Feature.Reply.data.remote.response.ReplyResponse
import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    private var repliesJob: Job? = null
    private val childJobs = mutableMapOf<String, Job>()

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
                    // Room will automatically trigger update if we are observing
                    _replyText.value = ""
                    _pickedImage.value = null
                    UiState.Success(newReply)
                },
                onFailure = { UiState.Error(it.message ?: "Failed to create reply") }
            )
        }
    }

    fun fetchPostReplies(feedId: String) {
        repliesJob?.cancel()
        repliesJob = viewModelScope.launch {
            _postReplies.value = UiState.Loading
            
            // Observe Room for real-time updates (Optimistic updates will reflect here)
            replyRepository.observeReplies(feedId).collectLatest { replies ->
                _postReplies.value = UiState.Success(replies)
            }
        }

        // Refresh from network
        viewModelScope.launch {
            val result = replyRepository.getReplies(feedId)
            if (result.isFailure && _postReplies.value !is UiState.Success) {
                _postReplies.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch replies")
            }
        }
    }

    fun fetchChildReplies(parentId: String) {
        childJobs[parentId]?.cancel()
        childJobs[parentId] = viewModelScope.launch {
            _childLoadingStates.value = _childLoadingStates.value + (parentId to true)
            
            // Observe Room for child replies
            launch {
                replyRepository.observeChildReplies(parentId).collectLatest { children ->
                    _childReplies.value = _childReplies.value + (parentId to children)
                }
            }

            // Fetch from network
            val result = replyRepository.getChildReplies(parentId)
            result.onFailure {
                Log.e("ReplyVM", "Failed to fetch child replies: ${it.message}")
            }
            _childLoadingStates.value = _childLoadingStates.value - parentId
        }
    }

    fun toggleReplyLike(repliedById: String, replyId: String, currentLiked: Boolean) {
        viewModelScope.launch {
            // Note: Optimistic update logic moved to Repository (Room level)
            val result = replyRepository.likeReply(
                repliedById = repliedById,
                postId = "", 
                replyId = replyId,
                isLiked = !currentLiked
            )

            result.onFailure {
                Log.e("ReplyVM", "Like failed: ${it.message}")
            }
        }
    }

    fun deleteReply(replyId: String) {
        viewModelScope.launch {
            // Note: Optimistic update logic moved to Repository (Room level)
            val result = replyRepository.deleteReply("", replyId)
            result.onFailure {
                Log.e("ReplyVM", "Delete failed: ${it.message}")
            }
        }
    }

    fun clearPostReplies() {
        repliesJob?.cancel()
        childJobs.values.forEach { it.cancel() }
        childJobs.clear()
        _postReplies.value = UiState.Idle
        _childReplies.value = emptyMap()
    }
}
