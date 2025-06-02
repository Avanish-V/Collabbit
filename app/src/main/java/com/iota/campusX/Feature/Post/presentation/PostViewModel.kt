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

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _uploadingProgress: MutableStateFlow<UploadResponse> = MutableStateFlow(UploadResponse())
    val uploadingProgress: StateFlow<UploadResponse> = _uploadingProgress.asStateFlow()


    private val _postState: MutableStateFlow<PostResultState> = MutableStateFlow(PostResultState())
    val postState: StateFlow<PostResultState> = _postState.asStateFlow()


    private val _repliesState: MutableStateFlow<DataResultState> = MutableStateFlow(DataResultState())
    val repliesState: StateFlow<DataResultState> = _repliesState.asStateFlow()



    fun toggleLike(userId: String, postId: String, isLiked: Boolean) {

        viewModelScope.launch {

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

    fun likeReply(creatorId: String, postId: String, replyId: String, isLiked: Boolean) {
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

            postRepository.likeReply(creatorId, postId, replyId, isLiked)
        }
        // Update local state
    }

    fun createReply(replyId: String, postId: String, content: String, repliedAt: Long, creatorId: String,userType: String) = postRepository.createReply(replyId = replyId, postId = postId, content = content, repliedAt = repliedAt, creatorId = creatorId,userType = userType)

    fun createPost(createPostDTO: CreatePostDTO, postMode: Boolean, imageUri: Uri? = null, user: User, onCompletion: (String) -> Unit, onError: (String) -> Unit, ) {

        if (_isSubmitting.value) return // avoid duplicate calls

        viewModelScope.launch {

            _isSubmitting.value = true

            postRepository.createPost(createPostDTO, postMode, imageUri).collect {
                when (it) {
                    is ResultState.Loading -> {
                        _uploadingProgress.value = UploadResponse(status = "LOADING")
                    }
                    is ResultState.Success -> {

                        _uploadingProgress.value = it.data

                        if (it.data.status == "COMPLETED"){

                            val postMode: Pair<String, String> =
                                if (createPostDTO.type == "USER") Pair(
                                    user.userName,
                                    user.userImage
                                )
                                else Pair(
                                    "Anonymous",
                                    "https://res.cloudinary.com/dni4h8jjy/image/upload/v1746629954/wyuwxwa8qwx0hu0i6flk.png"
                                )

                            updatePost(
                                PostDTO(
                                    postId = createPostDTO.postId,
                                    postedAt = getTimeMillis(),
                                    creatorDetail = CreatorDetail(
                                        isCurrentUser = true,
                                        isVerified = false,
                                        isPremium = false,
                                        type = createPostDTO.type,
                                        profile = User(
                                            userName = postMode.first,
                                            id = user.id,
                                            userImage = postMode.second
                                        )
                                    ),
                                    reference = Reference(
                                        icon = createPostDTO.reference.icon,
                                        title = createPostDTO.reference.title

                                    ),
                                    postMode = null,
                                    postContent = PostContent(
                                        postType = createPostDTO.postContent.postType,
                                        postData = PostData(
                                            postText = createPostDTO.postContent.postData.postText,
                                            postImage = imageUri.toString()
                                        )
                                    ),
                                    postActions = PostActions(
                                        isLiked = false,
                                    )
                                )
                            )
                            onCompletion("COMPLETED")
                        }

                    }
                    is ResultState.Error -> {
                       onError(it.message)
                    }
                }
            }

        }
    }

    fun clearResponse(){
        _uploadingProgress.value = UploadResponse()
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

    fun fetchPostById(userId: String, campusId: String?) {

        viewModelScope.launch {
            postRepository.getPostsById(userId, campusId = campusId).collect {
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

    fun deletePost(postId: String,campusId: String?) = postRepository.deletePost(postId,campusId)

    fun deleteReply(postId: String,replyId:String,campusId: String?) = postRepository.deleteReply(postId,replyId,campusId)

    fun editReply(postId: String,replyId:String,content: String,campusId: String?) = postRepository.editReply(postId,replyId,content,campusId)

    fun editPost(postId: String,editedText: String,campusId: String?) = postRepository.editPost(postId,editedText,campusId)

    fun createPoll(createPostDTO: CreatePostDTO){

        viewModelScope.launch {
            postRepository.createPoll(createPostDTO){

            }
        }


    }

    fun cancelUpload() {
        MediaManager.get().cancelRequest(uploadingProgress.value.uploadId)
        _uploadingProgress.value = UploadResponse(status = "CANCELED", uploadId = "")
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

    fun refreshPosts(postMode: Boolean) {
        _postState.value.postData = emptyList()
        fetchPosts(postMode)
    }

    fun updateDeletePost(postId: String) {
        val updatedPosts = _postState.value.postData.toMutableList()
        updatedPosts.removeIf { it.postId == postId }
        _postState.value = _postState.value.copy(postData = updatedPosts)
    }

    fun updateDeleteReply(postId: String,replyId: String) {
        val updatedReplies = _repliesState.value.data.toMutableList()
        updatedReplies.removeIf { it.postId == postId && it.replyId == replyId }
        _repliesState.value = _repliesState.value.copy(data = updatedReplies)
    }

    fun updateEditReply(postId: String, replyId: String, content: String) {
        val updatedReplies = _repliesState.value.data.map { reply ->
            if (reply.postId == postId && reply.replyId == replyId) {
                reply.copy(content = content) // assuming `content` is a property of GetRepliesDTO
            } else {
                reply
            }
        }
        _repliesState.value = _repliesState.value.copy(data = updatedReplies)
    }

    fun updateEditPost(postId: String, editedText: String) {
        val updatedPosts = _postState.value.postData.map { post ->
            if (post.postId == postId) {
                post.copy(
                    postContent = post.postContent.copy(
                        postData = post.postContent.postData.copy(
                            postText = editedText
                        )
                    )
                )
            } else {
                post
            }
        }

        _postState.value = _postState.value.copy(postData = updatedPosts)
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