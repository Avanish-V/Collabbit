package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.Post.data.model.PostPayload
import com.iota.campusX.Feature.Post.presentation.UploadState
import kotlinx.coroutines.flow.Flow

class CreatePostUseCase(private val repository: PostRepositoryInterface) {
    suspend operator fun invoke(
        postType: PostPayload
    ): Flow<UploadState> {
        return repository.createPost(postType)
    }
}