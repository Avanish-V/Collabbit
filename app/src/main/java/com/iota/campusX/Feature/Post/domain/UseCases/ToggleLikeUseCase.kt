package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface

class ToggleLikeUseCase(private val repository: PostRepositoryInterface) {
    suspend operator fun invoke(
        postId: String,
        isLiked: Boolean,
    ): Result<Unit> {
        return repository.toggleLike(
            postId,
            isLiked,
        )
    }
}
