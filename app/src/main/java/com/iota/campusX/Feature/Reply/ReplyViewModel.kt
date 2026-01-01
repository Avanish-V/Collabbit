package com.iota.campusX.Feature.Reply

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.GetRepliesDTO
import com.iota.campusX.Feature.Post.data.model.ReplyRequest
import com.iota.campusX.Feature.Post.data.model.ReplyResponse
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReplyViewModel (private val replyRepository: ReplyRepository): ViewModel() {

    val postReplies: StateFlow<UiState<List<ReplyResponse>>> = replyRepository.postRepliesState


    val createReplyState = replyRepository.createReplyState
        .map { it }
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = UiState.Idle)


    fun fetchPostReplies(postId: String) = viewModelScope.launch {
        replyRepository.getPostReplies(postId)
    }

    fun clearPostReplies() = viewModelScope.launch {
        replyRepository.clearPostReplies()
    }

    fun createReply(replyRequest: ReplyRequest,postDTO: GetPostDTO,uploadImage: Uri?) = viewModelScope.launch{
        replyRepository.createReply(replyRequest,postDTO,uploadImage)
    }


}
