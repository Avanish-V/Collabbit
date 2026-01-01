package com.iota.campusX.Feature.Post.domain.UseCases

import android.net.Uri
import com.iota.campusX.Feature.Post.data.model.ReplyRequest
import com.iota.campusX.Feature.Post.data.model.ReplyResponse
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.domain.repository.ReplyRepositoryInterface

class CreateReplyUseCase(private val repository: ReplyRepositoryInterface) {
    suspend operator fun invoke(
        replyRequest: ReplyRequest,
        uploadImage: Uri?
    ): Result<ReplyResponse> {
        return repository.createReply(replyRequest,uploadImage)
    }
}
