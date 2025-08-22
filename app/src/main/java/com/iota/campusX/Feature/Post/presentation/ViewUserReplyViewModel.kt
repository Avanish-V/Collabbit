package com.iota.campusX.Feature.Post.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.UserReplyDTO
import com.iota.campusX.Feature.Post.domain.ReplyRepository
import com.iota.campusX.Feature.Post.domain.UseCases.CreateReplyUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetRepliesUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewUserReplyViewModel(
    private val getRepliesUseCase: GetRepliesUseCase,
    private val createReplyUseCase: CreateReplyUseCase,
    private val replyRepository: ReplyRepository
) : ViewModel() {


    private val _viewUserReplies = MutableStateFlow<UiState<List<UserReplyDTO>>>(UiState.Idle)
    val viewUserReplies: StateFlow<UiState<List<UserReplyDTO>>> = _viewUserReplies.asStateFlow()


    private val _likeReplyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val likeReplyState: StateFlow<UiState<Unit>> get() = _likeReplyState



    fun getUserReplies(userId: String) {
        if (_viewUserReplies.value is UiState.Success) return
        viewModelScope.launch {
            _viewUserReplies.value = UiState.Loading
            val result = replyRepository.fetchUserReplies(userId)
            _viewUserReplies.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to fetch replies") }
            )
        }
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
