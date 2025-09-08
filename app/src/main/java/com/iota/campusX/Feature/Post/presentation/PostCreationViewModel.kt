package com.iota.campusX.Feature.Post.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.MediaType
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.PostType
import com.iota.campusX.Feature.Post.data.model.Type
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePollUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePostUseCase
import com.iota.campusX.Feature.Post.domain.repository.PostRepository
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepository
import com.iota.campusX.Feature.Post.data.model.Poll
import com.iota.campusX.Utils.FirestoreIdGenerator
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class PostCreationViewModel(
    private val createPostUseCase: CreatePostUseCase,
    private val createPollUseCase: CreatePollUseCase,
    private val postRepository: PostRepository,
    private val userProfileRepository: UserProfileRepository,
    private val mediaManager: MediaManager
) : ViewModel() {

    val profile: StateFlow<UiState<BaseProfileDTO>> = userProfileRepository.currentUser

    val currentUser = when(profile.value){
        is UiState.Success<*> -> {
            (profile.value as UiState.Success<BaseProfileDTO>).data
        }
        else -> null
    }
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun createMediaPost(
        creatorId: String,
        campusId: String,
        feedMode: FeedMode,
        visibility: VisibilityMode,
        imageUri: Uri?,
        postText: String,
        type: Type,
        mediaType: MediaType
    ): PostType.MediaPost {
        return PostType.MediaPost(
            image = imageUri,
            postText = postText,
            postId = FirestoreIdGenerator.generate(),
            creatorId = creatorId,
            createdAt = FieldValue.serverTimestamp(),
            feedMode = feedMode,
            campusId = campusId,
            visibilityMode = visibility,
            type = type,
            mediaType = mediaType
        )
    }

    fun createPoll(
        creatorId: String,
        campusId: String,
        feedMode: FeedMode,
        visibility: VisibilityMode,
        type: Type,
        poll: Poll
    ): PostType {
        return PostType.PollPost(
            postId = FirestoreIdGenerator.generate(),
            creatorId = creatorId,
            createdAt = FieldValue.serverTimestamp(),
            feedMode = feedMode,
            campusId = campusId,
            visibilityMode = visibility,
            type = type,
            poll = poll
        )
    }

    fun uploadPost(
        postType: PostType,
    ){

        if (_uploadState.value is UploadState.Loading || _uploadState.value is UploadState.Progress) return

        viewModelScope.launch {

            _uploadState.value = UploadState.Loading

           when(postType){

                is PostType.MediaPost -> {

                   val result =  createPostUseCase.invoke(postType)

                   result.collect {
                       _uploadState.value = it
                       when(it){
                           is UploadState.Success -> {
                               val millis: Long = System.currentTimeMillis()
                               val timestamp = Timestamp(Date(millis))
                               postRepository.addPostLocally(
                                   GetPostDTO(
                                       postId = postType.postId,
                                       createdAt = timestamp,
                                       creatorDetail = CreatorDetail(
                                           profile = UserBasicDetail(
                                               userName = currentUser?.userName ?: "",
                                               id = currentUser?.id ?: "",
                                               userImage = currentUser?.userImage ?: "",
                                               userBio = currentUser?.userBio ?: "",
                                           ),
                                           isCurrentUser = true,
                                           isVerified = currentUser?.metaData?.verified ?: false
                                       ),
                                       feedMode = postType.feedMode,
                                       visibilityMode = postType.visibilityMode,
                                       campusId = postType.campusId,
                                       postContent = PostContent(
                                           postText = postType.postText,
                                           postImage = postType.image?.toString()
                                       ),
                                       postActions = PostActions(),
                                       type = postType.type,
                                       mediaType = postType.mediaType
                                   )
                               )
                               resetState()
                           }
                           is UploadState.Error -> {
                               resetState()
                           }
                           else -> {}
                       }


                   }
                }
                is PostType.PollPost -> {

                    val result = createPollUseCase.invoke(postType)

                    result.fold(
                        onSuccess = {
                            val millis: Long = System.currentTimeMillis()
                            val timestamp = Timestamp(Date(millis))
                            postRepository.addPostLocally(
                                GetPostDTO(
                                    postId = postType.postId,
                                    createdAt = timestamp,
                                    creatorDetail = CreatorDetail(
                                        profile = UserBasicDetail(
                                            userName = currentUser?.userName ?: "",
                                            id = currentUser?.id ?: "",
                                            userImage = currentUser?.userImage ?: "",
                                            userBio = currentUser?.userBio ?: "",
                                        ),
                                        isCurrentUser = true,
                                        isVerified = currentUser?.metaData?.verified ?: false
                                    ),
                                    feedMode = postType.feedMode,
                                    visibilityMode = postType.visibilityMode,
                                    campusId = postType.campusId,
                                    postContent = PostContent(
                                       poll = postType.poll
                                    ),
                                    postActions = PostActions(),
                                    type = postType.type,
                                )
                            )
                            _uploadState.value = UploadState.Success("Poll created successfully")
                            resetState()

                        },
                        onFailure = {
                            _uploadState.value = UploadState.Error(it.message ?: "Something went wrong")
                            resetState()
                        }
                    )
                }
            }
        }
    }

   suspend fun resetState(){
        delay(2000)
        _uploadState.value = UploadState.Idle
   }

    fun cancelUpload() {
        val uploadId = when (val state = _uploadState.value) {
            is UploadState.Progress -> state.requestId
            else -> ""
        }

        if (uploadId.isNotEmpty()) {
            mediaManager.cancelRequest(uploadId)
        }
        _uploadState.value = UploadState.Error("Upload Canceled")
    }

}



sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Started(val requestId: String) : UploadState()
    data class Progress(val progress: Int, val requestId: String) : UploadState()
    data class MediaUploaded(val url: String) : UploadState()
    data class MediaUploadError(val message: String) : UploadState()
    data class Success(val postId: String) : UploadState()
    data class Error(val message: String) : UploadState()
}


