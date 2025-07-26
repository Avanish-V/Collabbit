package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.PostRepository

class LikeReplyUseCase(private val repository: PostRepository) {
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
