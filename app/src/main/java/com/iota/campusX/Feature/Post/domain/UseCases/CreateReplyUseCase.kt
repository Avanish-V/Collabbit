package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.PostRepository

class CreateReplyUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(
        replyId: String,
        postId: String,
        content: String,
        creatorId: String,
        visibilityMode: PostVisibilityMode
    ): Result<Unit> {
        return repository.createReply(
            replyId,
            postId,
            content,
            creatorId,
            visibilityMode
        )
    }
}
