package com.iota.campusX.Feature.Post.presentation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.cloudinary.android.MediaManager
import com.iota.campusX.Feature.Post.data.visibilityMode
import com.iota.campusX.Feature.Post.domain.Models.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.Models.CreatorDetail
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.PostActions
import com.iota.campusX.Feature.Post.domain.Models.PostContent
import com.iota.campusX.Feature.Post.domain.Models.PostData
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePollUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePostUseCase
import com.iota.campusX.NetworkCapability.ConnectivityObserver
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.generateUID
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostCreationViewModel(
    private val createPostUseCase: CreatePostUseCase,
    private val createPollUseCase: CreatePollUseCase,
    private val feedViewModel: PostFeedViewModel,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private var haveSubmitted: Boolean = false

    val isConnected = connectivityObserver
        .isConnected
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )

    private val _uploadingProgress = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadingProgress: StateFlow<UploadState> = _uploadingProgress.asStateFlow()

    var createPollUiState by mutableStateOf<UiState<Unit>>(UiState.Idle)
        private set

    fun createPost(dto: CreatePostDTO, imageUri: Uri?, user: UserDetail, navHostController: NavHostController, postFeedViewModel: PostFeedViewModel) {

        if (isConnected.value) {
            UiState.Error("No internet connection")
            return
        }

        viewModelScope.launch {
            createPostUseCase(dto, imageUri).collect { state ->
                when(state){
                    is UploadState.Success -> {

                       _uploadingProgress.value = state

                        val visibilityMode = visibilityMode(
                            dto.visibilityMode,
                            user.userName,
                            user.userImage
                        )
                       postFeedViewModel.updatePostLocally(
                            getPostDTO = GetPostDTO(
                                postId = dto.postId,
                                visibilityMode = dto.visibilityMode,
                                createdAt = getTimeMillis(),
                                reference = dto.reference,
                                creatorDetail = CreatorDetail(
                                    profile = UserDetail(
                                        userName = visibilityMode.first,
                                        id = user.id,
                                        userImage = visibilityMode.second,
                                        userBio = user.userBio,
                                        designation = ""
                                    ),
                                    isCurrentUser = true
                                ),
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

        if (!isConnected.value) {
            UiState.Error("No internet connection")
            return
        }
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
