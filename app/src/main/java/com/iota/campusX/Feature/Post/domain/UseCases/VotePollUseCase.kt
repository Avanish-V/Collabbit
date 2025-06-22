package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.PostRepository

class VotePollUseCase(
    private val pollRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, optionId: String,campusId:String?,feedMode: FeedMode): Result<Unit> {
        return try {
            pollRepository.voteOnPoll(postId, optionId,campusId,feedMode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
