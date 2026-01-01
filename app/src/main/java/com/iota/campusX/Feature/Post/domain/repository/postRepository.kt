package com.iota.campusX.Feature.Post.domain.repository

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.insertHeaderItem
import androidx.paging.map
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.domain.UseCases.DeletePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.EditPostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetCampusPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostByIdUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.ToggleLikeUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.VotePollUseCase
import com.iota.campusX.Feature.Post.data.model.Vote
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.fold

class PostRepository(
    private val getPostsUseCase: GetPostsUseCase,
    private val getCampusPostsUseCase: GetCampusPostsUseCase,
    private val postByIdUseCase: GetPostByIdUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val editPostUseCase: EditPostUseCase,
    private val likeUseCase: ToggleLikeUseCase,
    private val votePollUseCase: VotePollUseCase,
    private val postRepository: PostRepositoryInterface,
) {
    private val _globalPosts = MutableStateFlow<PagingData<GetPostDTO>>(PagingData.empty())
    val globalPosts: StateFlow<PagingData<GetPostDTO>> = _globalPosts

    private val _campusPosts = MutableStateFlow<PagingData<GetPostDTO>>(PagingData.empty())
    val campusPosts: StateFlow<PagingData<GetPostDTO>> = _campusPosts.asStateFlow()

    private val _postById = MutableStateFlow<PagingData<GetPostDTO>>(PagingData.empty())
    val postById: StateFlow<PagingData<GetPostDTO>> = _postById.asStateFlow()

    private val _singlePost = MutableStateFlow<UiState<GetPostDTO?>>(UiState.Idle)
    val singlePost: StateFlow<UiState<GetPostDTO?>> = _singlePost.asStateFlow()

    private val _viewUserPost = MutableStateFlow<PagingData<GetPostDTO>>(PagingData.empty())
    val viewUserPost: StateFlow<PagingData<GetPostDTO>> = _viewUserPost.asStateFlow()


    private var currentPage = 0
    private var lastPage = false


    //---------FETCH DATA---------------------------------------------------------------------

    suspend fun fetchGlobalPosts(scope: CoroutineScope,feedMode: FeedMode,campusId: String? = null,size: Int = 10) {

        postRepository.getPosts(feedMode,campusId)
            .cachedIn(scope) // ✅ use caller scope (usually ViewModelScope)
            .collectLatest { pagingData ->
                if (feedMode == FeedMode.OPEN){
                    _globalPosts.value = pagingData
                }
                if (feedMode == FeedMode.CAMPUS){
                    _campusPosts.value = pagingData
                }
            }
    }
    suspend fun fetchCampusPosts(scope: CoroutineScope,campusId: String?) {
//        getCampusPostsUseCase(campusId = campusId,feedMode = FeedMode.CAMPUS)
//            .cachedIn(scope) // ✅ use caller scope (usually ViewModelScope)
//            .collectLatest { pagingData ->
//                _campusPosts.value = pagingData
//            }
    }
    suspend fun fetchUserPosts(scope: CoroutineScope,userId: String) {
        postByIdUseCase(userId = userId)
            .cachedIn(scope) // ✅ use caller scope (usually ViewModelScope)
            .collectLatest { pagingData ->
                _postById.value = pagingData
            }
    }
    suspend fun fetchSinglePost(postId: String) {

        _singlePost.value = UiState.Loading

        val result = postRepository.fetchSinglePost(postId)

        _singlePost.value = result.fold(
            onSuccess = {
                UiState.Success(null)
                UiState.Success(it)
            },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )


    }

    suspend fun clearSinglePost(){
        _singlePost.value = UiState.Success(null)
        _singlePost.value = UiState.Idle
    }
    fun getViewUserPost(userId: String,scope: CoroutineScope){
        scope.launch {
            postRepository.getPostsById(userId = userId)
                .cachedIn(scope) // ✅ use caller scope (usually ViewModelScope)
                .collectLatest { pagingData ->
                    _viewUserPost.value = pagingData
                }
        }

    }


    //----------MANIPULATE DATA--------------------------------------------------------------

    suspend fun editPost(postId: String, newText: String): Result<Unit> {

       val result =  editPostUseCase.invoke(postId = postId, newText)

       result.fold(
           onSuccess = {
               updateLocalPost(postId) { post ->
                   post.copy(
                       postContent = post.postContent.copy(
                           postText = newText
                       )
                   )
               }
           },
           onFailure = {
               return Result.failure(it)
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
    suspend fun votePoll(postId: String, optionId: String, feedMode: FeedMode): Result<Unit> {
        return runCatching {
            val result = votePollUseCase.invoke(postId, optionId)
            result.fold(
                onSuccess = {
                    updateVoteLocally(postId, optionId,feedMode)
                },
                onFailure = {
                    return Result.failure(it)
                }
            )
        }
    }

    //---------UPDATE DATA LOCALLY-----------------------------------------------------------

    private fun updateLocalPost(postId: String, update: (GetPostDTO) -> GetPostDTO) {
        // 🔹 Update global posts
        _globalPosts.update { pagingData ->
            pagingData.map { post ->
                if (post.postId == postId) update(post) else post
            }
        }

        // 🔹 Update campus posts
        _campusPosts.update { pagingData ->
            pagingData.map { post ->
                if (post.postId == postId) update(post) else post
            }
        }

        // 🔹 Update user posts
        _postById.update { pagingData ->
            pagingData.map { post ->
                if (post.postId == postId) update(post) else post
            }
        }

        // 🔹 Update single post
        _singlePost.update { state ->
            if (state is UiState.Success) {
                UiState.Success(
                    if (state.data?.postId == postId) update(state.data) else state.data
                )
            } else state
        }
    }
    private fun removeLocalPost(postId: String) {
        _campusPosts.update { pagingData ->
            pagingData.filter { it.postId != postId }
        }

        _globalPosts.update { pagingData ->
            pagingData.filter { it.postId != postId }
        }

        _postById.update { pagingData ->
            pagingData.filter { it.postId != postId }
        }

        _singlePost.update { state ->
            if (state is UiState.Success) {
                UiState.Success(if (state.data?.postId == postId) null else state.data)
            } else state
        }
    }
    private fun updateLocalLike(postId: String, isLiked: Boolean) {
        _globalPosts.updateLike(postId, isLiked)
        _campusPosts.updateLike(postId, isLiked)
        _postById.updateLike(postId, isLiked)
        _viewUserPost.updateLike(postId, isLiked)
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
    suspend fun addPostLocally(post: GetPostDTO) {

        val postMode = if (post.feedMode == FeedMode.OPEN) _globalPosts else _campusPosts

        postMode.update { pagingData ->
            pagingData.insertHeaderItem(item = post) // Add new post at top
        }

        _postById.update { pagingData ->
            pagingData.insertHeaderItem(item = post)
        }
    }
    suspend fun updateVoteLocally(postId: String, optionId: String,feedMode: FeedMode){

        val posts =  if (feedMode == FeedMode.OPEN) _globalPosts else _campusPosts

        posts.update { pagingData ->
           pagingData.map { post->
               if (post.postId == postId){
                   val updatedActions = post.postContent.poll?.copy(
                       votes = post.postContent.poll.votes + Vote(userId = "Random", optionId = optionId),
                       hasVoted = true,
                       selectedOptionId = optionId

                   )
                   post.copy(postContent = post.postContent.copy(poll = updatedActions))
               }else{
                   post
               }
           }
        }

    }

    suspend fun updateFollowLocally(userId: String, isFollowing: Boolean){
        _globalPosts.update { pagingData ->
            pagingData.map { post->
                if (post.creatorDetail.profile?.id == userId){
                    post.copy(creatorDetail = post.creatorDetail.copy(isFollow = isFollowing))
                }else{
                    post
                }
            }

        }
        _campusPosts.update { pagingData ->
            pagingData.map { post->
                if (post.creatorDetail.profile?.id == userId){
                    post.copy(creatorDetail = post.creatorDetail.copy(isFollow = isFollowing))
                }else{
                    post
                }
            }
        }
        _viewUserPost.update { pagingData ->
            pagingData.map { post->
                if (post.creatorDetail.profile?.id == userId){
                    post.copy(creatorDetail = post.creatorDetail.copy(isFollow = isFollowing))
                }else{
                    post
                }
            }
        }

        _singlePost.update {
            if (it is UiState.Success){
                UiState.Success(it.data?.copy(creatorDetail = it.data.creatorDetail.copy(isFollow = isFollowing)))
            }else{
                it
            }
        }

    }

}

private inline fun List<GetPostDTO>.mapIfMatch(
    postId: String,
    update: (GetPostDTO) -> GetPostDTO
): List<GetPostDTO> =
    map { if (it.postId == postId) update(it) else it }


private fun MutableStateFlow<PagingData<GetPostDTO>>.updateLike(postId: String, isLiked: Boolean) {
    this.update { pagingData ->
        pagingData.map { post ->
            if (post.postId == postId) {
                val updatedActions = post.postActions.copy(
                    isLiked = !isLiked,
                    likesCount = if (isLiked) post.postActions.likesCount - 1 else post.postActions.likesCount + 1
                )
                post.copy(postActions = updatedActions)
            } else post
        }
    }
}
