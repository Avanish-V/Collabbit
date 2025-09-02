package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.Post.data.model.PostType

class CreatePollUseCase(
    private val pollRepository: PostRepositoryInterface
) {
    suspend operator fun invoke(postType: PostType): Result<Unit> {
        return try {
            pollRepository.createPoll(postType)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
