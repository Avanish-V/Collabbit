package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.domain.ReplyRepository

class LikeReplyUseCase(private val repository: ReplyRepository) {
    suspend operator fun invoke(
        replyId: String,
        postId: String,
        isLiked: Boolean,
        creatorId: String,
        campusId: String?,
        feedMode: FeedMode
    ): Result<Unit> {
        return repository.likeReply(
            creatorId,
            replyId,
            postId,
            isLiked,
            campusId,
            feedMode
        )
    }
}
