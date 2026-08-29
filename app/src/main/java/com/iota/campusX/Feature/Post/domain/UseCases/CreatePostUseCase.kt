package com.iota.campusX.Feature.Post.domain.UseCases

import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Post.domain.attachment.Attachment
import com.iota.campusX.Feature.Post.data.remote.request.CreatePostRequest
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.attachment.AttachmentProcessor
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.UserProfile.data.local.dao.UserProfileDao

class CreatePostUseCase(
    private val repository: PostRepositoryInterface,
    private val userProfileDao: UserProfileDao,
    private val firebaseAuth: FirebaseAuth,
    private val attachmentProcessor: AttachmentProcessor
) {
    suspend operator fun invoke(
        caption: String,
        attachment: Attachment?
    ): Result<Post> {

        if ((firebaseAuth.currentUser?.uid.isNullOrEmpty())) {
            return Result.failure(Exception("User not logged in"))
        }


        val attachmentDto = attachmentProcessor.process(
            attachment = attachment
        )

        val request = CreatePostRequest(
            caption = caption,
            attachment = attachmentDto
        )

        return repository.createPost(request)

    }
}