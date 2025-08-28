package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.repository.ReplyRepositoryInterface

class GetRepliesUseCase(private val repository: ReplyRepositoryInterface) {
    suspend operator fun invoke(postId: String,campusId:String?,feedMode: FeedMode): Result<List<GetRepliesDTO>> {
        return repository.getReplies(postId,campusId,feedMode)
    }
}
