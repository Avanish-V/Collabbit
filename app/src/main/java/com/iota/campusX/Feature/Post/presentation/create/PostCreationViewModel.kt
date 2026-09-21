package com.iota.campusX.Feature.Post.presentation.create

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.provider.OpenableColumns
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.attachment.Attachment
import com.iota.campusX.Feature.Post.domain.attachment.DocumentAttachment
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachment
import com.iota.campusX.Feature.Post.domain.attachment.VideoAttachment
import com.iota.campusX.Feature.Post.domain.attachment.TeamFormationAttachment
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePostUseCase
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UniversityRepository

import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


data class DraftPost(
    val caption: String = "",
    val attachment: Attachment? = null,
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PostCreationViewModel(
    private val createPostUseCase: CreatePostUseCase,
    private val universityRepository: UniversityRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow<UiState<Post>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _draft = MutableStateFlow(DraftPost())
    val draft = _draft.asStateFlow()

    fun onCaptionChanged(caption: String) {

        _draft.update {
            it.copy(caption = caption)
        }

    }

    fun onImagesSelected(context: Context, uris: List<Uri>) {
        val widths = mutableListOf<Int>()
        val heights = mutableListOf<Int>()
        val aspectRatios = uris.map { uri ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            try {
                context.contentResolver.openInputStream(uri)?.use { 
                    BitmapFactory.decodeStream(it, null, options)
                }

                var width = options.outWidth
                var height = options.outHeight

                context.contentResolver.openInputStream(uri)?.use { exifStream ->
                    val exifInterface = ExifInterface(exifStream)
                    val orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || 
                        orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        val temp = width
                        width = height
                        height = temp
                    }
                }

                if (width > 0 && height > 0) {
                    widths.add(width)
                    heights.add(height)
                    width.toFloat() / height.toFloat()
                } else {
                    widths.add(0)
                    heights.add(0)
                    1f
                }
            } catch (e: Exception) {
                widths.add(0)
                heights.add(0)
                1f
            }
        }

        _draft.update {
            it.copy(attachment = ImageAttachment(uris.map { it.toString() }, widths, heights, aspectRatios))
        }
    }

    fun onVideoSelected(context: Context, uri: Uri) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            
            if (duration > 15500) { // Slight buffer for rounding
                _uiState.value = UiState.Error("Video duration must be less than 15 seconds")
                return
            }

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
            val aspectRatio = if (height > 0) width.toFloat() / height.toFloat() else 1f

            _draft.update {
                it.copy(attachment = VideoAttachment(uri.toString(), width, height, aspectRatio))
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error("Failed to process video")
        } finally {
            retriever.release()
        }
    }

    fun onDocumentSelected(context: Context, uri: Uri) {
        try {
            var fileName = "Document.pdf"
            var fileSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex) ?: "Document.pdf"
                    fileSize = cursor.getLong(sizeIndex)
                }
            }

            var pageCount = 0
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                    val renderer = android.graphics.pdf.PdfRenderer(fd)
                    pageCount = renderer.pageCount
                    renderer.close()
                }
            } catch (e: Exception) {
                Log.e("POST_CREATION", "Error getting page count: ${e.message}")
            }

            _draft.update {
                it.copy(attachment = DocumentAttachment(uri.toString(), fileName, fileSize, pageCount = pageCount))
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error("Failed to process document")
        }
    }

    fun onAttachmentSelected(
        attachment: Attachment
    ) {

        _draft.update {
            it.copy(attachment = attachment)
        }

    }

    fun onTeamTypeChanged(type: String) {
        _draft.update {
            val current = it.attachment as? TeamFormationAttachment ?: TeamFormationAttachment("", emptyList())
            it.copy(attachment = current.copy(teamType = type))
        }
    }

    fun onRequiredSkillsChanged(skills: List<String>) {
        _draft.update {
            val current = it.attachment as? TeamFormationAttachment ?: TeamFormationAttachment("", emptyList())
            it.copy(attachment = current.copy(requiredSkills = skills))
        }
    }

    fun removeAttachment() {

        _draft.update {
            it.copy(attachment = null)
        }

    }

    fun publishPost() {

        viewModelScope.launch {

            _uiState.value = UiState.Loading

            val draft = _draft.value

            val result = createPostUseCase(
                caption = draft.caption,
                attachment = draft.attachment
            )

            result
                .fold(
                    onSuccess = {
                        _uiState.value = UiState.Success(it)
                    },
                    onFailure = {
                        _uiState.value = UiState.Error(it.message.toString())
                    }
                )


        }
    }

     val skillQuery = MutableStateFlow("")

    private val _skillsQueryState = MutableStateFlow<UiState<List<SkillResponse>>>(UiState.Idle)
    val skillsQueryState: StateFlow<UiState<List<SkillResponse>>> = _skillsQueryState.asStateFlow()

    private val _skill: MutableStateFlow<List<SkillResponse>> = MutableStateFlow(emptyList())
    val skills: StateFlow<List<SkillResponse>> = _skill.asStateFlow()

    fun addSkill(skill: SkillResponse){
        _skill.value += skill
    }
    fun removeSkill(skill: SkillResponse){
        _skill.value -= skill
    }

    init {
        viewModelScope.launch {
            skillQuery
                .debounce(600.milliseconds)
                .filter { it.isNotBlank() && it.length > 2 && skillsQueryState.value is UiState.Loading || skillsQueryState.value is UiState.Idle }
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    universityRepository.searchKeySkills(query)
                }
                .onStart { _skillsQueryState.value = UiState.Loading }
                .catch { e ->
                    Log.e("SkillSearch", "searchQuery flow error: $e")
                    _skillsQueryState.value = UiState.Error("Unexpected error: ${e.localizedMessage ?: "Unknown"}")
                }
                .collect { result ->
                    Log.d("SkillSearch", "searchQuery results: $result")
                    _skillsQueryState.value = UiState.Success(result)
                }
        }
    }

    fun onSkillQueryChanged(query: String) {
        skillQuery.value = query
    }


}





sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Started(val requestId: String) : UploadState()
    data class Progress(val progress: Int, val requestId: String) : UploadState()
    data class MediaUploaded(val url: String) : UploadState()
    data class MediaUploadError(val message: String) : UploadState()
    data class Success(val postResponse: Post) : UploadState()
    data class Error(val message: String) : UploadState()
}


fun shortenContent(content: String, maxLength: Int = 80): String {
    val clean = content.replace("\n", " ") // remove line breaks
    return if (clean.length > maxLength) {
        clean.take(maxLength) + "…"
    } else clean
}
