package com.iota.campusX.Feature.Post.presentation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.cloudinary.android.MediaManager
import com.iota.campusX.Feature.Post.data.model.CreatePostDTO
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.PostData
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePollUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePostUseCase
import com.iota.campusX.Screens.Post.PostManupulation.PostFeedViewModel
import com.iota.campusX.Screens.Post.PostManupulation.PostRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class PostCreationViewModel(
    private val createPostUseCase: CreatePostUseCase,
    private val createPollUseCase: CreatePollUseCase,
    private val postRepository: PostRepository,
) : ViewModel() {


//    val isConnected = connectivityObserver
//        .isConnected
//        .stateIn(
//            viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000L),
//            initialValue = false
//        )

    private val _uploadingProgress = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadingProgress: StateFlow<UploadState> = _uploadingProgress.asStateFlow()

    var createPollUiState by mutableStateOf<UiState<Unit>>(UiState.Idle)
        private set

    fun createPost(dto: CreatePostDTO, imageUri: Uri?, user: CreatorDetail, navHostController: NavHostController, postFeedViewModel: PostFeedViewModel) {

        if (_uploadingProgress.value !is UploadState.Idle) return

        viewModelScope.launch {
            createPostUseCase(dto, imageUri).collect { state ->
                when(state){
                    is UploadState.Success -> {

                       _uploadingProgress.value = state

                        val millis: Long = System.currentTimeMillis()
                        val timestamp = com.google.firebase.Timestamp(Date(millis))

                        postRepository.addPostLocally(
                            post = GetPostDTO(
                                postId = dto.postId,
                                visibilityMode = dto.visibilityMode,
                                createdAt = timestamp,
                                reference = dto.reference,
                                creatorDetail = user,
                                postContent = PostContent(
                                    postType = dto.postContent.postType,
                                    postData = PostData(
                                        postText = dto.postContent.postData.postText,
                                        postImage = imageUri.toString(),
                                        poll = dto.postContent.postData.poll
                                    )
                                ),
                                campusId = dto.campusId,
                                postActions = PostActions(),
                                feedMode = dto.feedMode
                            ),
                        )

                        navHostController.popBackStack()

                        clearUpload()

                    }
                    else -> {_uploadingProgress.value = state}
                }
            }
        }
    }
    fun createPoll(dto: CreatePostDTO) {

        viewModelScope.launch {
            createPollUiState = UiState.Loading
            val result = createPollUseCase(dto)
            result.onSuccess {
                createPollUiState = UiState.Success(Unit)
            }.onFailure {
                createPollUiState = UiState.Error(it.localizedMessage ?: "Unknown error")
            }
            resetState()
        }
    }
    fun clearUpload() {
        _uploadingProgress.value = UploadState.Idle
    }

    fun cancelUpload() {
        val uploadId = when (val state = _uploadingProgress.value) {
            is UploadState.Started -> state.uploadId
            is UploadState.Progress -> state.uploadId
            is UploadState.Success -> state.uploadId
            else -> ""
        }

        if (uploadId.isNotEmpty()) {
            MediaManager.get().cancelRequest(uploadId)
        }

        _uploadingProgress.value = UploadState.Error("Upload canceled")
    }

    suspend fun resetState(){
        delay(500)
        createPollUiState = UiState.Idle
    }
}

sealed class UploadState {
    object Idle : UploadState()                 // Represents no upload started yet
    object Loading : UploadState()              // Represents preparation phase
    data class Started(val uploadId: String) : UploadState()
    data class Progress(val progress: Int, val uploadId: String) : UploadState()
    data class Success(val uploadId: String) : UploadState()
    data class Error(val message: String) : UploadState()
    data class ImageUploadFailed(val message: String) : UploadState()
}
