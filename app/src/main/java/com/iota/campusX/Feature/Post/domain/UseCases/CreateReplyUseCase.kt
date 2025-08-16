package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.domain.ReplyRepository

class CreateReplyUseCase(private val repository: ReplyRepository) {
    suspend operator fun invoke(
        replyId: String,
        postId: String,
        content: String,
        postCreatorId: String,
        visibilityMode: PostVisibilityMode,
        mode: FeedMode,
        campusId: String?
    ): Result<Unit> {
        return repository.createReply(
            replyId = replyId,
            postId =  postId,
            content = content,
            postCreatorId = postCreatorId,
            visibilityMode = visibilityMode,
            mode = mode,
            campusId
        )
    }
}
