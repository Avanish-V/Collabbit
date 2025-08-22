package com.iota.campusX.Feature.Post.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Screens.Post.PostManupulation.PostRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViewUserPostViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    val viewUserPost = postRepository.postById
        .map { it }
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = UiState.Idle)


    var votePollState by mutableStateOf<UiState<Unit>>(UiState.Idle)
        private set


    fun fetchPostById(userId: String){
        viewModelScope.launch { postRepository.fetchUserPosts(userId) }
    }


//    fun voteOnPoll(postId: String, optionId: String, userId: String,campusId:String?,feedMode: FeedMode) {
//        Log.d("PostFeedViewModel", "voteOnPoll called with postId: $postId, optionId: $optionId, userId: $userId")
//        viewModelScope.launch {
//            votePollState = UiState.Loading
//            votePollUseCase(postId, optionId,campusId,feedMode)
//                .onSuccess {
//                    votePollState = UiState.Success(Unit)
//
//                    val updatedPosts = (_postById.value as? UiState.Success)?.data?.map { post ->
//                        if (post.postId == postId && post.postContent.postData.poll != null) {
//                            val updatedOptions = post.postContent.postData.poll.options?.map { option ->
//                                if (option.optionId == optionId) {
//                                    option.copy(
//                                        votes = option.votes + userId
//                                    )
//                                } else option
//                            }
//
//                            // Create updated PostData
//                            val updatedPostData = post.postContent.postData.copy(
//                                poll = post.postContent.postData.poll.copy(
//                                    options = updatedOptions,
//                                    hasVoted = true
//                                )
//                            )
//
//                            // Create updated PostContent
//                            val updatedPostContent = post.postContent.copy(
//                                postData = updatedPostData
//                            )
//
//                            // Return the updated post
//                            post.copy(postContent = updatedPostContent)
//                        } else post
//                    }
//
//                    updatedPosts?.let {
//                        _postById.value = UiState.Success(it)
//                    }
//                }
//                .onFailure {
//                    votePollState = UiState.Error(it.localizedMessage ?: "Poll vote failed")
//                }
//        }
//    }

}
