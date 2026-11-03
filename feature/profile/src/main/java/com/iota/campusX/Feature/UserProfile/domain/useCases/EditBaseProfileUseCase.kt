package com.iota.campusX.Feature.UserProfile.domain.useCases

import android.net.Uri
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import com.iota.campusX.Feature.UserProfile.data.remote.Request.BasicDetailsRequest
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

class EditBaseProfileUseCase(
    private val repository: UserProfileRepository,
    private val s3Uploader: S3Uploader
) {
    suspend operator fun invoke(
        basicDetailsRequest: BasicDetailsRequest,
        imageUri: Uri?
    ): Result<Unit> {

        if (imageUri != null) {
            val uploadedUrl = s3Uploader.UploadImageToS3(imageUri)
            if (uploadedUrl != null) {
                basicDetailsRequest.photo = uploadedUrl
            } else {
                return Result.failure(Exception("Failed to upload image"))
            }
        }

        if (basicDetailsRequest.name.isBlank()) {
            return Result.failure(Exception("Name cannot be empty"))
        }

        return repository.updateProfile(basicDetailsRequest)
    }
}
