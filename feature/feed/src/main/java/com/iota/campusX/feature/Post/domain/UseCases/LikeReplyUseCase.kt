package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository

class LikeReplyUseCase(private val repository: ReplyRepository) {
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
