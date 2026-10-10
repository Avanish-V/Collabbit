package com.iota.campusX.Feature.Collab.presentation

import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.CollabType
import com.iota.campusX.Feature.Collab.data.model.CreateCollabRequest
import com.iota.campusX.Feature.Collab.domain.usecase.CreateCollabUseCase
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ProjectRoleUiModel(
    val id: String = UUID.randomUUID().toString(),
    val roleName: String = "",
    val skills: List<String> = emptyList(),
    val peopleNeeded: Int = 1
)

data class CreateProjectUiState(
    val logoUri: String? = null,
    val title: String = "",
    val idea: String = "",
    val projectUrl: String = "",
    val roles: List<ProjectRoleUiModel> = emptyList(),
    val titleTouched: Boolean = false,
    val ideaTouched: Boolean = false,
    val urlTouched: Boolean = false,
    val isLoading: Boolean = false,
    val isUploadingLogo: Boolean = false,
    val logoUploadProgress: Float = 0f,
    val error: String? = null,
    val isCreated: Boolean = false,
    val createdCollab: CollabResponse? = null
) {
    val isTitleValid: Boolean
        get() = title.trim().isNotBlank() && title.length <= 60

    val isIdeaValid: Boolean
        get() = idea.trim().isNotBlank() && idea.length <= 500

    val isUrlValid: Boolean
        get() {
            if (projectUrl.isBlank()) return true
            val trimmed = projectUrl.trim()
            return Patterns.WEB_URL.matcher(trimmed).matches() ||
                    trimmed.startsWith("http://") ||
                    trimmed.startsWith("https://")
        }

    val isFormValid: Boolean
        get() = isTitleValid && isIdeaValid && isUrlValid && roles.isNotEmpty()
}

class CreateCollabViewModel(
    private val createCollabUseCase: CreateCollabUseCase,
    private val s3Uploader: S3Uploader
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState = _uiState.asStateFlow()

    fun updateLogoUri(uri: String?) {
        _uiState.update { it.copy(logoUri = uri) }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleTouched = true) }
    }

    fun updateIdea(idea: String) {
        _uiState.update { it.copy(idea = idea, ideaTouched = true) }
    }

    fun updateProjectUrl(url: String) {
        _uiState.update { it.copy(projectUrl = url, urlTouched = true) }
    }

    fun addRole(role: ProjectRoleUiModel) {
        _uiState.update { it.copy(roles = it.roles + role) }
    }

    fun updateRole(role: ProjectRoleUiModel) {
        _uiState.update { state ->
            val updated = state.roles.map { if (it.id == role.id) role else it }
            state.copy(roles = updated)
        }
    }

    fun removeRole(roleId: String) {
        _uiState.update { state ->
            state.copy(roles = state.roles.filterNot { it.id == roleId })
        }
    }

    fun createProject() {
        val currentState = _uiState.value
        if (!currentState.isFormValid || currentState.isLoading) return

        val reqList = currentState.roles.flatMap { role ->
            if (role.skills.isNotEmpty()) {
                listOf(role.roleName) + role.skills
            } else {
                listOf(role.roleName)
            }
        }.distinct().filter { it.isNotBlank() }

        val totalNeeded = currentState.roles.sumOf { it.peopleNeeded }.coerceAtLeast(1)

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            var uploadedLogoUrl: String? = null
            val rawLogo = currentState.logoUri

            if (!rawLogo.isNullOrBlank()) {
                if (rawLogo.startsWith("http://") || rawLogo.startsWith("https://")) {
                    uploadedLogoUrl = rawLogo
                } else {
                    _uiState.update { it.copy(isUploadingLogo = true, logoUploadProgress = 0f) }
                    try {
                        uploadedLogoUrl = s3Uploader.UploadImageToS3(
                            uri = Uri.parse(rawLogo),
                            folder = "collab_logos",
                            onProgress = { progress ->
                                _uiState.update { it.copy(logoUploadProgress = progress) }
                            }
                        )
                    } catch (e: Exception) {
                        Log.e("CreateCollabVM", "Error uploading logo: ${e.message}", e)
                    } finally {
                        _uiState.update { it.copy(isUploadingLogo = false) }
                    }

                    if (uploadedLogoUrl == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to upload project logo. Please try again or remove the logo."
                            )
                        }
                        return@launch
                    }
                }
            }

            val projectRoles = currentState.roles.map {
                com.iota.campusX.Feature.Collab.data.model.ProjectRole(
                    id = it.id,
                    roleName = it.roleName.trim(),
                    skills = it.skills,
                    peopleNeeded = it.peopleNeeded
                )
            }

            val request = CreateCollabRequest(
                title = currentState.title.trim(),
                description = currentState.idea.trim(),
                collabType = CollabType.COBUILDER,
                requirements = reqList,
                roles = projectRoles,
                participantsNeeded = totalNeeded,
                projectUrl = currentState.projectUrl.trim().ifBlank { null },
                logoUrl = uploadedLogoUrl
            )

            createCollabUseCase(request).fold(
                onSuccess = { response ->
                    val finalResponse = if (response.logoUrl == null && uploadedLogoUrl != null) {
                        response.copy(logoUrl = uploadedLogoUrl)
                    } else {
                        response
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCreated = true,
                            createdCollab = finalResponse,
                            error = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.localizedMessage ?: "Failed to create project"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetFlow() {
        _uiState.value = CreateProjectUiState()
    }
}
