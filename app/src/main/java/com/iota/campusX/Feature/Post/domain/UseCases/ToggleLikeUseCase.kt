package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.PostRepository

class ToggleLikeUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(
        userId: String,
        postId: String,
        isLiked: Boolean,
    ): Result<Unit> {
        return repository.toggleLike(
            userId,
            postId,
            isLiked,
        )
    }
}
