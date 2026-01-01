package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface

class EditPostUseCase(private val repository: PostRepositoryInterface) {
    suspend operator fun invoke(postId: String, text: String) =
        repository.editPost(postId, text)

}