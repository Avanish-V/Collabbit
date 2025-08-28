package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.Post.presentation.UploadState
import com.iota.campusX.Screens.Post.PostType
import kotlinx.coroutines.flow.Flow

class CreatePostUseCase(private val repository: PostRepositoryInterface) {
    suspend operator fun invoke(
        postType: PostType
    ): Flow<UploadState> {
        return repository.createPost(postType)
    }
}