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


    var deletePostState by mutableStateOf<UiState<Unit>>(UiState.Idle)

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

    fun deletePost(postId: String, isCampus: Boolean) {
        viewModelScope.launch {
            deletePostState = UiState.Loading
            val result = deletePostUseCase(postId, null) // pass campusId if needed
            deletePostState = result.fold(
                onSuccess = {
                    val state = _globalPosts
                    state.update {
                        if (it is UiState.Success) {
                            UiState.Success(it.data.filterNot { post -> post.postId == postId })
                        } else it
                    }
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.localizedMessage ?: "Delete failed") }
            )
        }
    }

    fun editPost(postId: String, newText: String, isCampus: Boolean) {
        viewModelScope.launch {
            editPostState = UiState.Loading
            val result = editPostUseCase(postId, newText, null)
            editPostState = result.fold(
                onSuccess = {
                    val state = if (isCampus) _campusPosts else _globalPosts
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
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.localizedMessage ?: "Edit failed") }
            )
        }
    }

    fun toggleLike(userId: String, postId: String, isLiked: Boolean, isCampus: Boolean) {
        viewModelScope.launch {
            val state = if (isCampus) _campusPosts else _globalPosts
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
            postRepository.toggleLike(userId, postId, isLiked)
        }
    }

    fun voteOnPoll(postId: String, optionId: String) {
        viewModelScope.launch {
            votePollState = UiState.Loading
            votePollUseCase(postId, optionId)
                .onSuccess { votePollState = UiState.Success(Unit) }
                .onFailure {
                    votePollState = UiState.Error(it.localizedMessage ?: "Poll vote failed")
                }
        }
    }

    fun refreshGlobalPosts() {

    }

    fun refreshCampusPosts(feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            isRefreshing.value = true
            fetchCampusPosts(feedMode, campusId)
            isRefreshing.value = false
        }
    }

    suspend fun updatePostLocally(getPostDTO: GetPostDTO, feedMode: FeedMode) {


        when (feedMode) {

            FeedMode.GLOBAL -> {


                val current = _globalPosts.value
                if (current is UiState.Success) {
                    val updatedList = listOf(getPostDTO) + current.data
                    _globalPosts.emit(UiState.Success(updatedList))
                } else {
                    _globalPosts.emit(UiState.Success(listOf(getPostDTO)))
                }
            }

            FeedMode.CAMPUS -> {
                val current = _campusPosts.value
                if (current is UiState.Success) {
                    val updatedList = listOf(getPostDTO) + current.data
                    _campusPosts.emit(UiState.Success(updatedList))
                } else {
                    _campusPosts.emit(UiState.Success(listOf(getPostDTO)))
                }
            }
        }
    }


}
