package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.PostRepository

class VotePollUseCase(
    private val pollRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, optionId: String): Result<Unit> {
        return try {
            pollRepository.voteOnPoll(postId, optionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
