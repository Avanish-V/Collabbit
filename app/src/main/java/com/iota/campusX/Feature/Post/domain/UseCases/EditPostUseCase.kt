package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.domain.PostRepository

class EditPostUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(postId: String, text: String, campusId: String?,feedMode: FeedMode) =
        repository.editPost(postId, text, campusId,feedMode)

}