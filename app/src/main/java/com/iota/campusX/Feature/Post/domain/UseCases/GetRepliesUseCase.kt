package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.PostRepository

class GetRepliesUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(postId: String): Result<List<GetRepliesDTO>> {
        return repository.getReplies(postId)
    }
}
