package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.domain.PostRepository

class GetPostByIdUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(userId: String): Result<List<GetPostDTO>> {
        return repository.getPostsById(userId)
    }
}