package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.repository.ReplyRepositoryInterface

class LikeReplyUseCase(private val repository: ReplyRepositoryInterface) {
    suspend operator fun invoke(
        replyId: String,
        postId: String,
        isLiked: Boolean,
        creatorId: String,
    ): Result<Unit> {
        return repository.likeReply(
            creatorId,
            replyId,
            postId,
            isLiked,
        )
    }
}
