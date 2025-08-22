package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.domain.ReplyRepository

class CreateReplyUseCase(private val repository: ReplyRepository) {
    suspend operator fun invoke(
        replyId: String,
        postId: String,
        content: String,
        postCreatorId: String,
        visibilityMode: VisibilityMode,
    ): Result<Unit> {
        return repository.createReply(
            replyId = replyId,
            postId =  postId,
            content = content,
            postCreatorId = postCreatorId,
            visibilityMode = visibilityMode,
        )
    }
}
