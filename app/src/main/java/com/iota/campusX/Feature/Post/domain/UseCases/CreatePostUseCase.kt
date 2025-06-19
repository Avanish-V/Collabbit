package com.iota.campusX.Feature.Post.domain.UseCases

import android.net.Uri
import com.iota.campusX.Feature.Post.domain.Models.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.presentation.UploadState
import kotlinx.coroutines.flow.Flow

class CreatePostUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(
        dto: CreatePostDTO,
        imageUri: Uri?
    ): Flow<UploadState> {
        return repository.createPost(dto, imageUri)
    }
}