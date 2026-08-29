package com.iota.campusX.Feature.Post.domain.UseCases

import android.net.Uri
import com.iota.campusX.Feature.Reply.data.remote.request.ReplyRequest
import com.iota.campusX.Feature.Reply.data.remote.response.ReplyResponse
import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository

class CreateReplyUseCase(private val repository: ReplyRepository) {
    suspend operator fun invoke(
        replyRequest: ReplyRequest,
        feedId: String,
        uploadImage: Uri?
    ): Result<ReplyResponse> {
        return repository.createReply(replyRequest, feedId,uploadImage)
    }
}
