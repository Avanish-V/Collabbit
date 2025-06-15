package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.PostRepository

class CreatePollUseCase(
    private val pollRepository: PostRepository
) {
    suspend operator fun invoke(createPostDTO: CreatePostDTO): Result<Unit> {
        return try {
            pollRepository.createPoll(createPostDTO)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
