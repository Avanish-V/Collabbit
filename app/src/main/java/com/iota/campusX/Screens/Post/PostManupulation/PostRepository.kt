package com.iota.campusX.Screens.Post.PostManupulation

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.domain.UseCases.DeletePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.EditPostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetCampusPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostByIdUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.ToggleLikeUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PostRepository(
    private val getPostsUseCase: GetPostsUseCase,
    private val getCampusPostsUseCase: GetCampusPostsUseCase,
    private val postByIdUseCase: GetPostByIdUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val editPostUseCase: EditPostUseCase,
    private val likeUseCase: ToggleLikeUseCase,
    private val postRepository: PostRepository,
) {
    private val _globalPosts = MutableStateFlow<UiState<List<GetPostDTO>>>(UiState.Idle)
    val globalPosts: StateFlow<UiState<List<GetPostDTO>>> = _globalPosts.asStateFlow()

    private val _campusPosts = MutableStateFlow<UiState<List<GetPostDTO>>>(UiState.Idle)
    val campusPosts: StateFlow<UiState<List<GetPostDTO>>> = _campusPosts.asStateFlow()

    private val _postById = MutableStateFlow<UiState<List<GetPostDTO>>>(UiState.Idle)
    val postById: StateFlow<UiState<List<GetPostDTO>>> = _postById.asStateFlow()

    private val _singlePost = MutableStateFlow<UiState<GetPostDTO?>>(UiState.Idle)
    val singlePost: StateFlow<UiState<GetPostDTO?>> = _singlePost.asStateFlow()

    //---------FETCH DATA---------------------------------------------------------------------

    suspend fun fetchGlobalPosts() {
        if (_globalPosts.value is UiState.Success) return
        _globalPosts.value = UiState.Loading
        getPostsUseCase().collect { result ->
            _globalPosts.value = result.fold(
                onSuccess = {
                    UiState.Success(it.sortedByDescending { it.createdAt })
                },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )
        }
    }

    suspend fun fetchCampusPosts(campusId: String, feedMode: FeedMode) {
        if (_campusPosts.value is UiState.Success) return
        _campusPosts.value = UiState.Loading
        getCampusPostsUseCase(feedMode, campusId).collect { result ->
            _campusPosts.value = result.fold(
                onSuccess = { UiState.Success(it.sortedByDescending { it.createdAt }) },
                onFailure = { UiState.Error(it.message ?: "Something went wrong") }
            )
        }
    }

    suspend fun fetchUserPosts(userId: String) {
        _postById.value = UiState.Loading
        val result = postByIdUseCase(userId)
        _postById.value = result.fold(
            onSuccess = { UiState.Success(it.sortedByDescending { it.createdAt }) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }

    suspend fun fetchSinglePost(postId: String) {
        _singlePost.value = UiState.Loading
        val result = postRepository.fetchSinglePost(postId)
        _singlePost.value = result.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )
    }


    //----------MANIPULATE DATA--------------------------------------------------------------

    suspend fun editPost(postId: String, newText: String): Result<Unit> {

       val result =  editPostUseCase.invoke(postId = postId, newText, campusId = null, feedMode = FeedMode.CAMPUS)

       result.fold(
           onSuccess = {
               updateLocalPost(postId) { post ->
                   post.copy(
                       postContent = post.postContent.copy(
                           postData = post.postContent.postData.copy(postText = newText)
                       )
                   )
               }
           },
           onFailure = {

           }

       )
        return  result
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return runCatching {
            deletePostUseCase.invoke(
                postId,
                campusId = null,
                feedMode = FeedMode.CAMPUS
            )
            removeLocalPost(postId)
        }
    }

    suspend fun likePost(userId: String, postId: String, isLiked: Boolean): Result<Unit> {
        // 1. Update local state immediately (optimistic UI update)
        updateLocalLike(postId, isLiked)

        // 2. Trigger backend update (real source of truth)
        return runCatching {
            likeUseCase.invoke(userId, postId, isLiked)
        }

    }


    //---------UPDATE DATA LOCALLY-----------------------------------------------------------
    private fun updateLocalPost(postId: String, update: (GetPostDTO) -> GetPostDTO) {

        _globalPosts.update { state ->
            if (state is UiState.Success) {
                UiState.Success(state.data.mapIfMatch(postId, update))
            } else state
        }

        _campusPosts.update { state ->
            if (state is UiState.Success) {
                UiState.Success(state.data.mapIfMatch(postId, update))
            } else state
        }

        _postById.update { state ->
            if (state is UiState.Success) {
                UiState.Success(state.data.mapIfMatch(postId, update))
            } else state
        }
        _singlePost.update { state ->
            if (state is UiState.Success) {
                UiState.Success(if (state.data?.postId == postId) update(state.data) else state.data)
            } else state
        }

    }

    private fun removeLocalPost(postId: String) {
        _globalPosts.update { state ->
            if (state is UiState.Success) {
                UiState.Success(state.data.filterNot { it.postId == postId })
            } else state
        }

        _campusPosts.update { state ->
            if (state is UiState.Success) {
                UiState.Success(state.data.filterNot { it.postId == postId })
            } else state
        }

        _postById.update { state ->
            if (state is UiState.Success) {
                UiState.Success(state.data.filterNot { it.postId == postId })
            } else state
        }
        _singlePost.update { state ->
            if (state is UiState.Success) {
                UiState.Success(if (state.data?.postId == postId) null else state.data)
            } else state
        }

    }

    suspend fun addPostLocally(post: GetPostDTO) {

        val postMode = if (post.feedMode == FeedMode.GLOBAL) _globalPosts else _campusPosts

        postMode.update { state ->
            if (state is UiState.Success) {
                UiState.Success(state.data + post)
            } else state
        }
        _postById.update { state ->
            if (state is UiState.Success) {
                UiState.Success(state.data + post)
            } else state
        }

    }

    private fun updateLocalLike(postId: String, isLiked: Boolean) {


        _campusPosts.update {
            if (it is UiState.Success) {
                UiState.Success(
                    it.data.map { post ->
                        if (post.postId == postId) {
                            val updatedActions = post.postActions.copy(
                                isLiked = !isLiked,
                                likesCount = if (isLiked) {
                                    post.postActions.likesCount - 1
                                } else {
                                    post.postActions.likesCount + 1
                                }
                            )
                            post.copy(postActions = updatedActions)
                        } else post
                    }
                )
            } else it
        }

        _globalPosts.update {
            if (it is UiState.Success) {
                UiState.Success(
                    it.data.map { post ->
                        if (post.postId == postId) {
                            val updatedActions = post.postActions.copy(
                                isLiked = !isLiked,
                                likesCount = if (isLiked) {
                                    post.postActions.likesCount - 1
                                } else {
                                    post.postActions.likesCount + 1
                                }
                            )
                            post.copy(postActions = updatedActions)
                        } else post
                    }
                )
            } else it
        }

        _postById.update {
            if (it is UiState.Success) {
                UiState.Success(
                    it.data.map { post ->
                        if (post.postId == postId) {
                            val updatedActions = post.postActions.copy(
                                isLiked = !isLiked,
                                likesCount = if (isLiked) {
                                    post.postActions.likesCount - 1
                                } else {
                                    post.postActions.likesCount + 1
                                }
                            )
                            post.copy(postActions = updatedActions)
                        } else post
                    }
                )
            } else it
        }

        _singlePost.update {
            if (it is UiState.Success) {
                UiState.Success(
                    if (it.data?.postId == postId) {
                        val updatedActions = it.data.postActions.copy(
                            isLiked = !isLiked,
                            likesCount = if (isLiked) {
                                it.data.postActions.likesCount - 1
                            } else {
                                it.data.postActions.likesCount + 1
                            }
                        )
                        it.data.copy(postActions = updatedActions)
                    } else it.data
                )
            } else it
        }
    }


}

private inline fun List<GetPostDTO>.mapIfMatch(
    postId: String,
    update: (GetPostDTO) -> GetPostDTO
): List<GetPostDTO> =
    map { if (it.postId == postId) update(it) else it }
