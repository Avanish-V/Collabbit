package com.iota.campusX.Feature.Post.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.domain.UseCases.DeletePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.EditPostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetCampusPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostByIdUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.VotePollUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostFeedViewModel(
    private val getPostsUseCase: GetPostsUseCase,
    private val getCampusPostsUseCase: GetCampusPostsUseCase,
    private val postByIdUseCase: GetPostByIdUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val editPostUseCase: EditPostUseCase,
    private val votePollUseCase: VotePollUseCase,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _globalPosts = MutableStateFlow<UiState<List<GetPostDTO>>>(UiState.Idle)
    val globalPosts: StateFlow<UiState<List<GetPostDTO>>> = _globalPosts.asStateFlow()

    private val _campusPosts = MutableStateFlow<UiState<List<GetPostDTO>>>(UiState.Idle)
    val campusPosts: StateFlow<UiState<List<GetPostDTO>>> = _campusPosts.asStateFlow()

    private val _postById = MutableStateFlow<UiState<List<GetPostDTO>>>(UiState.Idle)
    val postById: StateFlow<UiState<List<GetPostDTO>>> = _postById.asStateFlow()


    private val _deletePostState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deletePostState: StateFlow<UiState<Unit>> get() = _deletePostState

    var editPostState by mutableStateOf<UiState<Unit>>(UiState.Idle)

    var votePollState by mutableStateOf<UiState<Unit>>(UiState.Idle)
        private set

    var isRefreshing = mutableStateOf(false)
        private set


    fun fetchGlobalPosts() {
        if (_globalPosts.value is UiState.Success && (_globalPosts.value as UiState.Success).data.isNotEmpty()) return
        viewModelScope.launch {
            _globalPosts.value = UiState.Loading
            getPostsUseCase().collect { result ->
                _globalPosts.value = result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Something went wrong") }
                )
            }

        }
    }

    fun fetchCampusPosts(feedMode: FeedMode, campusId: String?) {
        if (_campusPosts.value is UiState.Success && (_campusPosts.value as UiState.Success).data.isNotEmpty()) return
        viewModelScope.launch {
            _campusPosts.value = UiState.Loading
            getCampusPostsUseCase(feedMode, campusId).collect { result ->
                _campusPosts.value = result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Something went wrong") }
                )
            }
        }
    }

    fun fetchPostById(userId: String, campusId: String?) {

        viewModelScope.launch {
            _postById.value = UiState.Loading
            val result = postByIdUseCase(userId, campusId)
            _postById.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )

        }

    }

    fun deletePost(postId: String, isCampus: String?,feedMode: FeedMode) {
        viewModelScope.launch {
            _deletePostState.value = UiState.Loading
            val result = deletePostUseCase(postId,isCampus,feedMode) // pass campusId if needed
            _deletePostState.value = result.fold(
                onSuccess = {
                    val state = if (feedMode == FeedMode.CAMPUS) _campusPosts else _globalPosts
                    state.update {
                        if (it is UiState.Success) {
                            UiState.Success(it.data.filterNot { post -> post.postId == postId })
                        } else it
                    }
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.localizedMessage ?: "Delete failed") }
            )
            if (_deletePostState.value is UiState.Success) {
                kotlinx.coroutines.delay(500)
                _deletePostState.value = UiState.Idle
            }
        }

    }

    fun editPost(postId: String, newText: String,campusId: String?, feedMode: FeedMode) {
        viewModelScope.launch {
            editPostState = UiState.Loading
            val result = editPostUseCase(postId, newText, campusId,feedMode)
            editPostState = result.fold(
                onSuccess = {
                    UiState.Success(Unit).also {
                        val state = if (feedMode == FeedMode.CAMPUS) _campusPosts else _globalPosts
                        state.update {
                            if (it is UiState.Success) {
                                UiState.Success(
                                    it.data.map { post ->
                                        if (post.postId == postId) post.copy(
                                            postContent = post.postContent.copy(
                                                postData = post.postContent.postData.copy(postText = newText)
                                            )
                                        ) else post
                                    }
                                )
                            } else it
                        }
                    }
                },
                onFailure = { UiState.Error(it.localizedMessage ?: "Edit failed") }
            )

            clearState()
        }
    }

    fun toggleLike(userId: String, postId: String, isLiked: Boolean, campusId: String?, feedMode: FeedMode) {
        viewModelScope.launch {
            val state = if (feedMode == FeedMode.CAMPUS) _campusPosts else _globalPosts
            state.update {
                if (it is UiState.Success) {
                    UiState.Success(
                        it.data.map { post ->
                            if (post.postId == postId) {
                                val updated = post.postActions.copy(
                                    isLiked = !isLiked,
                                    likesCount = if (isLiked) post.postActions.likesCount - 1 else post.postActions.likesCount + 1
                                )
                                post.copy(postActions = updated)
                            } else post
                        }
                    )
                } else it
            }
            postRepository.toggleLike(userId, postId, isLiked,campusId,feedMode)
        }
    }

    fun voteOnPoll(postId: String, optionId: String, userId: String) {
        viewModelScope.launch {
            votePollState = UiState.Loading
            votePollUseCase(postId, optionId)
                .onSuccess {
                    votePollState = UiState.Success(Unit)

                    val updatedPosts = (_globalPosts.value as? UiState.Success)?.data?.map { post ->
                        if (post.postId == postId && post.postContent.postData.poll != null) {
                            val updatedOptions = post.postContent.postData.poll.options?.map { option ->
                                if (option.optionId == optionId) {
                                    option.copy(
                                        votes = option.votes + userId
                                    )
                                } else option
                            }

                            // Create updated PostData
                            val updatedPostData = post.postContent.postData.copy(
                                poll = post.postContent.postData.poll.copy(
                                    options = updatedOptions,
                                    hasVoted = true
                                )
                            )

                            // Create updated PostContent
                            val updatedPostContent = post.postContent.copy(
                                postData = updatedPostData
                            )

                            // Return the updated post
                            post.copy(postContent = updatedPostContent)
                        } else post
                    }

                    updatedPosts?.let {
                        _globalPosts.value = UiState.Success(it)
                    }
                }
                .onFailure {
                    votePollState = UiState.Error(it.localizedMessage ?: "Poll vote failed")
                }
        }
    }

    fun refreshCampusPosts(feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            isRefreshing.value = true
            fetchCampusPosts(feedMode, campusId)
            isRefreshing.value = false
        }
    }

    suspend fun updatePostLocally(getPostDTO: GetPostDTO) {

        val currentState = when (getPostDTO.feedMode) {
            FeedMode.GLOBAL -> _globalPosts
            FeedMode.CAMPUS -> _campusPosts
        }

        val current = currentState.value
        val updatedList = if (current is UiState.Success) {
            // Remove any existing post with same ID
            val filtered = current.data.filter { it.postId != getPostDTO.postId }
            listOf(getPostDTO) + filtered

        } else {
            listOf(getPostDTO)
        }

        currentState.emit(UiState.Success(updatedList))
    }

    suspend fun clearState(){
         delay(1000)
        _deletePostState.value  = UiState.Idle
        editPostState = UiState.Idle
        votePollState = UiState.Idle
    }

}
