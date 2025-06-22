package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.PostRepository

class GetPostByIdUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(userId: String, campusId: String?,feedMode: FeedMode): Result<List<GetPostDTO>> {
        return repository.getPostsById(userId, campusId,feedMode)
    }
}