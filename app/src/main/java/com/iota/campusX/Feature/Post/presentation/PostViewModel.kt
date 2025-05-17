package com.iota.campusX.Feature.Post.presentation


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.iota.campusX.Feature.Post.domain.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.CreatorDetail
import com.iota.campusX.Feature.Post.domain.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.PostActions
import com.iota.campusX.Feature.Post.domain.PostContent
import com.iota.campusX.Feature.Post.domain.PostDTO
import com.iota.campusX.Feature.Post.domain.PostData
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.domain.Reference
import com.iota.campusX.Feature.Post.domain.UploadResponse
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Utils.ResultState
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostViewModel(private val postRepository: PostRepository) : ViewModel() {

    private val _uploadingProgress: MutableStateFlow<UploadResponse> =
        MutableStateFlow(UploadResponse())
    val uploadingProgress: StateFlow<UploadResponse> = _uploadingProgress.asStateFlow()


    private val _postState: MutableStateFlow<PostResultState> = MutableStateFlow(PostResultState())
    val postState: StateFlow<PostResultState> = _postState.asStateFlow()

    private val _postsById: MutableStateFlow<PostResultState> = MutableStateFlow(PostResultState())
    val postsById: StateFlow<PostResultState> = _postsById.asStateFlow()

    private val _repliesState: MutableStateFlow<DataResultState> =
        MutableStateFlow(DataResultState())
    val repliesState: StateFlow<DataResultState> = _repliesState.asStateFlow()

    fun toggleLike(userId: String, postId: String, isLiked: Boolean) {
        viewModelScope.launch {
            // Update local state
            val updatedPosts = _postState.value.postData.map { post ->
                if (post.postId == postId) {
                    val updatedActions = post.postActions.copy(
                        isLiked = !isLiked,
                        likesCount = if (isLiked) post.postActions.likesCount - 1 else post.postActions.likesCount + 1
                    )
                    post.copy(postActions = updatedActions)
                } else {
                    post
                }
            }

            _postState.value = _postState.value.copy(postData = updatedPosts)

            postRepository.toggleLike(userId, postId, isLiked)
        }
    }

    fun likeReply(userId: String, postId: String, replyId: String, isLiked: Boolean) {
        viewModelScope.launch {
            val updatedReplies = _repliesState.value.data.map { reply ->
                if (reply.replyId == replyId) {
                    val updatedActions = reply.actions.copy(
                        isLiked = !isLiked,
                        likesCount = if (isLiked) reply.actions.likesCount - 1 else reply.actions.likesCount + 1
                    )
                    reply.copy(actions = updatedActions)
                } else {
                    reply
                }
            }
            _repliesState.value = _repliesState.value.copy(data = updatedReplies)

            postRepository.likeReply(userId, postId, replyId, isLiked)
        }
        // Update local state
    }

    fun createReply(
        replyId: String,
        postId: String,
        content: String,
        repliedAt: Long,
        createrId: String
    ) = postRepository.createReply(
        replyId = replyId,
        postId = postId,
        content = content,
        repliedAt = repliedAt,
        creatorId = createrId
    )

    fun createPost(createPostDTO: CreatePostDTO, postMode: Boolean, imageUri: Uri?) {
        viewModelScope.launch {
            postRepository.createPost(createPostDTO, postMode, imageUri).collect {
                when (it) {
                    is ResultState.Loading -> {

                    }

                    is ResultState.Success -> {
                        _uploadingProgress.value = it.data
                    }

                    is ResultState.Error -> {
                        _uploadingProgress.value = UploadResponse(status = it.message)
                    }
                }
            }
        }
    }

    fun cancelUpload() {
        MediaManager.get().cancelRequest(uploadingProgress.value.uploadId)
        _uploadingProgress.value = UploadResponse(
            status = "CANCELED",
            uploadId = ""
        )
    }


    fun getReplies(postId: String) {
        viewModelScope.launch {
            postRepository.getReplies(postId).collect {
                when (it) {
                    is ResultState.Loading -> {
                        _repliesState.value = DataResultState(isLoading = true)
                        delay(1000)
                    }

                    is ResultState.Success -> {
                        _repliesState.value = DataResultState(data = it.data)
                    }

                    is ResultState.Error -> {
                        _repliesState.value = DataResultState(error = "")
                    }
                }
            }
        }
    }

    fun updateReply(getRepliesDTO: GetRepliesDTO) {
        val updatedReplies = _repliesState.value.data.toMutableList()
        updatedReplies.add(getRepliesDTO)
        _repliesState.value = _repliesState.value.copy(data = updatedReplies)
    }

    fun updatePost(postDTO: PostDTO) {
        val updatedPosts = _postState.value.postData.toMutableList()
        updatedPosts.add(postDTO)
        _postState.value = _postState.value.copy(postData = updatedPosts)
    }

    fun fetchPosts(postMode: Boolean) {

        if (_postState.value.postData.isNotEmpty()) return

        viewModelScope.launch {
            postRepository.getPosts(postMode).collect {
                when (it) {
                    is ResultState.Loading -> {
                        _postState.value = PostResultState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _postState.value = PostResultState(postData = it.data)
                    }

                    is ResultState.Error -> {
                        _postState.value = PostResultState(error = it.message)
                    }
                }
            }
        }
    }

    fun fetchPostById(userId: String, campusId: String) {

        viewModelScope.launch {
            postRepository.getPostsById(userId, campusId = campusId).collect {
                when (it) {
                    is ResultState.Loading -> {
                        _postsById.value = PostResultState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _postsById.value = PostResultState(postData = it.data)
                    }

                    is ResultState.Error -> {
                        _postsById.value = PostResultState(error = it.message)
                    }
                }

            }

        }

    }

    fun refreshPosts(postMode: Boolean) {
        _postState.value.postData = emptyList()
        fetchPosts(postMode)
    }

}

data class PostResultState(
    val isLoading: Boolean = false,
    var postData: List<PostDTO> = emptyList(),
    val error: String = ""
)

data class DataResultState(
    val isLoading: Boolean = false,
    val data: List<GetRepliesDTO> = emptyList(),
    val error: String = ""
)