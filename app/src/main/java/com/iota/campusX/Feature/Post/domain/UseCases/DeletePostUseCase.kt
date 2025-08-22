package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.domain.PostRepository

class DeletePostUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(postId: String, campusId: String?,feedMode: FeedMode) =
        repository.deletePost(postId, campusId,feedMode)
}

