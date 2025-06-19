package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.PostRepository

class ToggleLikeUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(
        userId: String,
        postId: String,
        isLiked: Boolean,
        campusId: String?,
        feedMode: FeedMode
    ): Result<Unit> {
        return repository.toggleLike(
            userId,
            postId,
            isLiked,
            campusId,
            feedMode
        )
    }
}
