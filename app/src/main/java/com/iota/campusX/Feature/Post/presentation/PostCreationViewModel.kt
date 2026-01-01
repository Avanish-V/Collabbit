package com.iota.campusX.Feature.Post.presentation

import SendPushNotification
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.google.firebase.firestore.FieldValue
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.MediaType
import com.iota.campusX.Feature.Post.data.model.Poll
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.PostPayload
import com.iota.campusX.Feature.Post.data.model.Type
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePostUseCase
import com.iota.campusX.Feature.Post.domain.models.PostResponse
import com.iota.campusX.Feature.Post.domain.repository.PostRepository
import com.iota.campusX.Utils.FirestoreIdGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostCreationViewModel(
    private val createPostUseCase: CreatePostUseCase,
    private val postRepository: PostRepository,
    private val mediaManager: MediaManager,
    private val sendPushNotification: SendPushNotification,
) : ViewModel() {


    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun createTextPost(
        creatorId: String,
        campusId: String,
        feedMode: FeedMode,
        visibility: VisibilityMode,
        postText: String,
        type: Type,
    ): PostPayload.TextPost {
        return PostPayload.TextPost(
            postText = postText,
            postId = FirestoreIdGenerator.generate(),
            creatorId = creatorId,
            createdAt = FieldValue.serverTimestamp(),
            feedMode = feedMode,
            campusId = campusId,
            visibilityMode = visibility,
            type = type,
        )
    }

    fun createMediaPost(
        creatorId: String,
        campusId: String,
        feedMode: FeedMode,
        visibility: VisibilityMode,
        imageUri: List<Uri>?,
        postText: String,
        type: Type,
        mediaType: MediaType
    ): PostPayload.MediaPost {
        return PostPayload.MediaPost(
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
    ): PostPayload {
        return PostPayload.PollPost(
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
        postType: PostPayload,
    ){

        if (_uploadState.value is UploadState.Loading || _uploadState.value is UploadState.Progress) return

        viewModelScope.launch {

            _uploadState.value = UploadState.Loading

            val result =  createPostUseCase.invoke(postType)

            result.collect {

                _uploadState.value = it

                when(it){

                    is UploadState.Success -> {

                        val responseData = it.postResponse

                        postRepository.addPostLocally(
                            GetPostDTO(
                                postId = postType.postId,
                                createdAt = responseData.createdAt,
                                creatorDetail = CreatorDetail(
                                    profile = UserBasicDetail(
                                        name = responseData.authorDetails?.authorName ?: "",
                                        id = responseData.authorDetails?.authorId ?: "",
                                        image = responseData.authorDetails?.authorImage ?: "",
                                        tagline = responseData.authorDetails?.authorTagline ?: "",
                                    ),
                                    isCurrentUser = true,
                                    isVerified = responseData.authorDetails?.isVerified ?: false
                                ),
                                feedMode = responseData.feedMode,
                                visibilityMode = responseData.visibility,
                                campusId = postType.campusId,
                                postContent = PostContent(
                                    postText = responseData.text,
                                    postImage = responseData.mediaPost,
                                ),
                                postActions = PostActions(),
                                type = responseData.postType,
                            )
                        )

                        if (postType.feedMode == FeedMode.CAMPUS){
                            sendPushNotification.sendNotificationToSubscriber(
                                topic = postType.campusId,
                                title = "📢 New Campus Post",
                                body = shortenContent(responseData.text?:"",30)
                            )
                        }

                        resetState()
                    }
                    is UploadState.Error -> {
                        resetState()
                    }
                    else -> {}
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
    data class Success(val postResponse: PostResponse) : UploadState()
    data class Error(val message: String) : UploadState()
}


fun shortenContent(content: String, maxLength: Int = 80): String {
    val clean = content.replace("\n", " ") // remove line breaks
    return if (clean.length > maxLength) {
        clean.take(maxLength) + "…"
    } else clean
}
