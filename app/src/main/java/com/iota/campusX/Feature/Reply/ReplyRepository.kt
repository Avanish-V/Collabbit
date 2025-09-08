package com.iota.campusX.Feature.Reply

import com.iota.campusX.Feature.Post.data.remote.visibilityMode
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.GetRepliesDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.model.UserReplyDTO
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.domain.repository.ReplyRepositoryInterface
import com.iota.campusX.Feature.Post.domain.UseCases.CreateReplyUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetRepliesUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReplyRepository (
    private val getRepliesUseCase: GetRepliesUseCase,
    private val createReplyUseCase: CreateReplyUseCase,
    private val replyRepository: ReplyRepositoryInterface
){

    private val _postRepliesState = MutableStateFlow<UiState<List<GetRepliesDTO>>>(UiState.Idle)
    val postRepliesState: StateFlow<UiState<List<GetRepliesDTO>>> = _postRepliesState.asStateFlow()

    private val _userRepliesState = MutableStateFlow<UiState<List<UserReplyDTO>>>(UiState.Idle)
    val userRepliesState: StateFlow<UiState<List<UserReplyDTO>>> = _userRepliesState.asStateFlow()

    private val _createReplyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createReplyState: StateFlow<UiState<Unit>> = _createReplyState.asStateFlow()


    //---------FETCH DATA---------------------------------------------------------------------

    suspend fun getUserReplies(userId: String){
        _userRepliesState.value = UiState.Loading
        val result = replyRepository.fetchUserReplies(userId)
        _userRepliesState.value = result.fold(
            onSuccess = { UiState.Success(it.sortedByDescending { it.reply.repliedAt }) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )

    }

    suspend fun getPostReplies(postId: String){
        _postRepliesState.value = UiState.Loading
        val result = getRepliesUseCase(postId,null, FeedMode.CAMPUS)
        _postRepliesState.value = result.fold(
            onSuccess = { UiState.Success(it.sortedByDescending { it.repliedAt }) },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )

    }



    //---------MANIPULATE DATA---------------------------------------------------------------------

    suspend fun likeReply(userId: String, replyId: String, isLiked: Boolean, postId: String): Result<Unit> {

        updateReplyLikeState(replyId, isLiked)

        return runCatching {
            replyRepository.likeReply(repliedById = userId, replyId = replyId, isLiked =  isLiked, postId =  postId)

        }

    }

    suspend fun createReply(replyId: String, postId: String, content: String, postCreatorId: String, visibilityMode: VisibilityMode,creatorDetail: CreatorDetail,postDTO: GetPostDTO){

        _createReplyState.value = UiState.Loading

        val result = createReplyUseCase.invoke(
            replyId = replyId,
            postId = postId,
            content = content,
            postCreatorId = postCreatorId,
            visibilityMode = visibilityMode
        )
        _createReplyState.value = result.fold(
            onSuccess = {
                val reply = buildReplyDTO(
                    replyId = replyId,
                    postId = postId,
                    content = content,
                    visibilityMode = visibilityMode,
                    creatorDetail = creatorDetail,
                )
                val userReply = buildUserReplyDTO(reply,postDTO)

                createReplyLocally(reply,userReply)

                UiState.Success(Unit)
            },
            onFailure = { UiState.Error(it.message ?: "Something went wrong") }
        )

    }

    suspend fun deleteReply(replyId: String, postId: String): Result<Unit>{

        val result = replyRepository.deleteReply(postId, replyId)

        result.fold(
            onSuccess = {
                _postRepliesState.update { currentState ->
                    if (currentState is UiState.Success) {
                        UiState.Success(currentState.data.filter { it.replyId != replyId })
                    } else {
                        currentState
                    }
                }
                _userRepliesState.update { currentState ->
                    if (currentState is UiState.Success) {
                        UiState.Success(currentState.data.filter { it.reply.replyId != replyId })
                    } else {
                        currentState
                    }
                }
            },
            onFailure = {
                UiState.Error(it.message ?: "Something went wrong")
            }
        )

        return result
    }

    suspend fun editReply(replyId: String, postId: String, content: String): Result<Unit>{

       val result = replyRepository.editReply(postId, replyId, content)

       val data =  result.fold(
            onSuccess = {
                editReplyLocally(replyId, content)
                UiState.Success(Unit)

             },
            onFailure = {
                UiState.Error(it.message ?: "Something went wrong")
            }
        )
        return result
    }

    //---------UPDATE DATA LOCALLY---------------------------------------------------------------------

    private fun editReplyLocally(replyId: String, content: String) {
        _postRepliesState.update { currentState ->
            if (currentState is UiState.Success) {
                UiState.Success(
                    currentState.data.map { reply ->
                        if (reply.replyId == replyId) {
                            reply.copy(content = content)
                        } else {
                            reply
                        }
                    }
                )
            } else {
                currentState
            }
        }
    }

    private fun updateReplyLikeState(replyId: String, isLiked: Boolean) {
        _postRepliesState.update { currentState ->
            if (currentState is UiState.Success) {
                UiState.Success(
                    currentState.data.map { reply ->
                        if (reply.replyId == replyId) {
                            val updatedActions = reply.actions.copy(
                                isLiked = !isLiked,
                                likesCount = if (isLiked) {
                                    reply.actions.likesCount - 1
                                } else {
                                    reply.actions.likesCount + 1
                                }
                            )
                            reply.copy(actions = updatedActions)
                        } else {
                            reply
                        }
                    }
                )
            } else {
                currentState
            }
        }
        _userRepliesState.update { currentState ->
            if (currentState is UiState.Success) {
                UiState.Success(
                    currentState.data.map { reply ->
                        if (reply.reply.replyId == replyId) {
                            val updatedActions = reply.reply.actions.copy(
                                isLiked = !isLiked,
                                likesCount = if (isLiked) {
                                    reply.reply.actions.likesCount - 1
                                } else {
                                    reply.reply.actions.likesCount + 1
                                }
                            )
                            reply.copy(reply = reply.reply.copy(actions = updatedActions))
                        } else {
                            reply
                        }
                    }
                )
            } else {
                currentState
            }
        }
    }

    private fun createReplyLocally(reply: GetRepliesDTO,userReply: UserReplyDTO) {
        _postRepliesState.update { currentState ->
            if (currentState is UiState.Success) {
                UiState.Success(currentState.data + reply)
            } else {
                currentState
            }
        }
        _userRepliesState.update { currentState ->
            if (currentState is UiState.Success) {
                UiState.Success(currentState.data + userReply)
            } else {
                currentState
            }
        }
    }

    private fun buildReplyDTO(replyId: String, postId: String, content: String, visibilityMode: VisibilityMode, creatorDetail: CreatorDetail): GetRepliesDTO {

        val visibility = creatorDetail.profile?.let {
            visibilityMode(
               visibilityMode =  visibilityMode,
                userName = it.userName,
                userImage = it.userImage
            )
        }

        return GetRepliesDTO(
            postId = postId,
            content = content,
            replyId = replyId,
            visibility = visibilityMode,
            creatorDetail = CreatorDetail(
                profile = UserBasicDetail(
                    userName = visibility?.first ?: "",
                    id = creatorDetail.profile?.id ?: "",
                    userImage = visibility?.second ?: "",
                    userBio = creatorDetail.profile?.userBio ?: "",
                ),
                isCurrentUser = true,
                isVerified = creatorDetail.isVerified
            ),
            actions = PostActions(),
            repliedAt = System.currentTimeMillis()
        )
    }

    private fun buildUserReplyDTO(reply: GetRepliesDTO,postDTO: GetPostDTO): UserReplyDTO {
        return UserReplyDTO(
            reply = reply,
            post = postDTO
        )
    }


}