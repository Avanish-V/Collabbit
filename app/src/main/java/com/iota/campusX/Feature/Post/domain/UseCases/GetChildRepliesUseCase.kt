package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Reply.data.remote.response.ReplyResponse
import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository

class GetChildRepliesUseCase(private val repository: ReplyRepository) {
    suspend operator fun invoke(replyId: String): Result<List<ReplyResponse>> {
        return repository.getChildReplies(replyId)
    }
}
