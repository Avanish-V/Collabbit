package com.iota.campusX.Feature.Reply

import android.net.Uri
import com.iota.campusX.Feature.Notification.data.CommentContent
import com.iota.campusX.Feature.Notification.data.CreateNotification
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Post.data.remote.visibilityMode
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.GetRepliesDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.ReplyRequest
import com.iota.campusX.Feature.Post.data.model.ReplyResponse
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
import kotlin.collections.emptyList

class ReplyRepository (
    private val getRepliesUseCase: GetRepliesUseCase,
    private val createReplyUseCase: CreateReplyUseCase,
    private val replyRepository: ReplyRepositoryInterface,
    private val notificationRepository: NotificationRepository
){

    private val _postRepliesState = MutableStateFlow<UiState<List<ReplyResponse>>>(UiState.Idle)
    val postRepliesState: StateFlow<UiState<List<ReplyResponse>>> = _postRepliesState.asStateFlow()

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

    suspend fun getPostReplies(postId: String) {

        _postRepliesState.value = UiState.Loading

        val result = getRepliesUseCase(postId, null, FeedMode.CAMPUS)

        _postRepliesState.value = result.fold(
            onSuccess = {
                if (postRepliesState.value is UiState.Success){
                   _postRepliesState.value = UiState.Idle
                }
                UiState.Success(it.sortedByDescending { it.createdAt })
            },
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

    suspend fun createReply(replyRequest: ReplyRequest,postDTO: GetPostDTO,uploadImage: Uri?){

        _createReplyState.value = UiState.Loading

        val result = createReplyUseCase.invoke(replyRequest,uploadImage)

        _createReplyState.value = result.fold(
            onSuccess = {
                val reply = buildReplyDTO(
                    replyId = it.replyId.toString(),
                    postId = it.postId.toString(),
                    content = it.text,
                    visibilityMode = it.visibility,
                    creatorDetail = CreatorDetail(
                        profile = UserBasicDetail(
                            name = it.author.authorName?:"",
                            id = it.author.authorId,
                            image = it.author.authorImage?:"",
                            tagline = it.author.authorTagline?:"",
                        ),
                        isCurrentUser = true,
                        isVerified = it.author.isVerified?:false
                    ),
                )
                val userReply = buildUserReplyDTO(reply,postDTO)

                createReplyLocally(it,userReply)

                notificationRepository.createNotification(
                    CreateNotification.CommentNotification(
                        notificationId = it.replyId.toString(),
                        type = NotificationType.COMMENT,
                        createdAt = it.createdAt,
                        postId = it.postId.toString(),
                        commentContent = listOf(
                            CommentContent(
                                visibilityMode = it.visibility,
                                repliedBy = it.author.authorId,
                                replyId = it.replyId.toString(),
                                repliedAt = it.createdAt
                            )
                        )
                    ),
                    creatorId = postDTO.creatorDetail.profile?.id ?: "",
                )

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
                        UiState.Success(currentState.data.filter { it.replyId.toString() != replyId })
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

    suspend fun editReply(replyId: String, postId: String, content: String?): Result<Unit>{

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

    private fun editReplyLocally(replyId: String, content: String?) {
        _postRepliesState.update { currentState ->
            if (currentState is UiState.Success) {
                UiState.Success(
                    currentState.data.map { reply ->
                        if (reply.replyId.toString() == replyId) {
                            reply.copy(text = content.toString())
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
                        if (reply.replyId.toString() == replyId) {
                            val updatedActions = reply.actions.copy(
                                isLiked = isLiked,
                                likesCount = if (isLiked) {
                                    reply.actions.likesCount + 1
                                } else {
                                    reply.actions.likesCount - 1
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

    private fun createReplyLocally(reply: ReplyResponse, userReply: UserReplyDTO) {

        _postRepliesState.update { currentState ->
            if (currentState is UiState.Success) {
                if (reply.parentId == null) {
                    // Top-level reply
                    UiState.Success(listOf(reply) + currentState.data)
                } else {
                    // Nested reply (shallow version)
                    val updatedData = currentState.data.map { parentReply ->
                        if (parentReply.replyId == reply.parentId) {
                            parentReply.copy(children = parentReply.children + reply)
                        } else {
                            parentReply
                        }
                    }
                    UiState.Success(updatedData)
                }
            } else {
                currentState
            }
        }



        _userRepliesState.update { currentState ->
            if (currentState is UiState.Success) {
                val merged = (listOf(userReply) + currentState.data)
                    .sortedByDescending { it.reply.repliedAt }
                UiState.Success(merged)
            } else {
                currentState
            }
        }
    }



    private fun buildReplyDTO(replyId: String, postId: String, content: String, visibilityMode: VisibilityMode, creatorDetail: CreatorDetail): GetRepliesDTO {

        val visibility = creatorDetail.profile?.let {
            visibilityMode(
               visibilityMode =  visibilityMode,
                userName = it.name,
                userImage = it.image
            )
        }

        return GetRepliesDTO(
            postId = postId,
            content = content,
            replyId = replyId,
            visibility = visibilityMode,
            creatorDetail = CreatorDetail(
                profile = UserBasicDetail(
                    name = visibility?.first ?: "",
                    id = creatorDetail.profile?.id ?: "",
                    image = visibility?.second ?: "",
                    tagline = creatorDetail.profile?.tagline ?: "",
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

    suspend fun clearPostReplies(){
        _postRepliesState.value = UiState.Success(emptyList())
        _postRepliesState.value = UiState.Idle
    }


}