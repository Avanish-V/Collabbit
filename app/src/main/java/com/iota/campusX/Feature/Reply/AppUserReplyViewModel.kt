package com.iota.campusX.Feature.Reply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppUserReplyViewModel(
    private val replyRepository: com.iota.campusX.Feature.Reply.ReplyRepository
) : ViewModel() {

    val userReplies = replyRepository.userRepliesState
        .map { it }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, UiState.Idle)

    fun getUserReplies(userId: String) {
        if (userReplies.value is UiState.Success) return
        viewModelScope.launch { replyRepository.getUserReplies(userId) }
    }

//    fun likeReply(
//        repliedById: String,
//        postId: String,
//        replyId: String,
//        isLiked: Boolean,
//        campusId: String?,
//        feedMode: FeedMode
//    ) {
//        viewModelScope.launch {
//
//            _viewUserReplies.update {
//                if (it is UiState.Success){
//                    UiState.Success(
//                        it.data.map {reply->
//
//                            if (reply.replyId == replyId){
//                                val update = reply.actions.copy(
//                                    isLiked = !isLiked,
//                                    likesCount = if (isLiked) reply.actions.likesCount - 1 else reply.actions.likesCount + 1
//                                )
//                                reply.copy(
//                                    actions = update
//                                )
//                            }
//                            else reply
//
//                        }
//                    )
//                }else{
//                    it
//                }
//            }
//
//            replyRepository.likeReply(repliedById, postId, replyId, isLiked, campusId, feedMode)
//
//        }
//    }

}